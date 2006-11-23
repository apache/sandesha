package org.apache.sandesha2.workers;

import java.util.ArrayList;
import java.util.MissingResourceException;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.util.AcknowledgementManager;
import org.apache.sandesha2.util.MessageRetransmissionAdjuster;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.TerminateManager;
import org.apache.sandesha2.wsrm.TerminateSequence;

public class SenderWorker extends SandeshaWorker implements Runnable {

	private ConfigurationContext configurationContext = null;
	private String messageId = null;
	private static final Log log = LogFactory.getLog(SenderWorker.class);
	private TransportOutDescription transportOut = null;
	
	public SenderWorker (ConfigurationContext configurationContext, String messageId) {
		this.configurationContext = configurationContext;
		this.messageId = messageId;
	}
	
	public void setTransportOut (TransportOutDescription transportOut) {
		this.transportOut = transportOut;
	}
	
	public void run () {
		
		Transaction transaction = null;
		
		try {
			StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext, configurationContext.getAxisConfiguration());
			SenderBeanMgr senderBeanMgr = storageManager.getSenderBeanMgr();
			
			transaction = storageManager.getTransaction();

			SenderBean senderBean = senderBeanMgr.retrieve(messageId);
			if (senderBean==null) {
				//the work is not present. May be invalid now. So should return.
				if (log.isDebugEnabled())
					log.debug(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.workNotPresent,workId));
				return;
			}
			String key = senderBean.getMessageContextRefKey();
			MessageContext msgCtx = storageManager.retrieveMessageContext(key, configurationContext);
			msgCtx.setProperty(Sandesha2Constants.WITHIN_TRANSACTION, Sandesha2Constants.VALUE_TRUE);

			RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);

			boolean continueSending = MessageRetransmissionAdjuster.adjustRetransmittion(rmMsgCtx, senderBean, configurationContext,
					storageManager);
			if (!continueSending) {
				return;
			}

			// sender will not send the message if following property is
			// set and not true.
			// But it will set if it is not set (null)

			// This is used to make sure that the mesage get passed the
			// Sandesha2TransportSender.

			String qualifiedForSending = (String) msgCtx.getProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING);
			if (qualifiedForSending != null && !qualifiedForSending.equals(Sandesha2Constants.VALUE_TRUE)) {
				return;
			}

			if (msgCtx == null) {
				if (log.isDebugEnabled())
					log.debug(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.sendHasUnavailableMsgEntry));
				return;			
			}

			// operation is the lowest level Sandesha2 should be attached
			ArrayList msgsNotToSend = SandeshaUtil.getPropertyBean(msgCtx.getAxisOperation()).getMsgTypesToDrop();

			if (msgsNotToSend != null && msgsNotToSend.contains(new Integer(rmMsgCtx.getMessageType()))) {
				return;	
			}

			updateMessage(msgCtx);

			int messageType = senderBean.getMessageType();
			
//			if (messageType == Sandesha2Constants.MessageTypes.APPLICATION) {
//				Sequence sequence = (Sequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
//				String sequenceID = sequence.getIdentifier().getIdentifier();
//			}

//			if (AcknowledgementManager.ackRequired (rmMsgCtx)) {
//				RMMsgCreator.addAckMessage(rmMsgCtx);
			
			//} else 
				
			if (isAckPiggybackableMsgType(messageType)) { // checking weather this message can carry piggybacked acks
				// piggybacking if an ack if available for the same
				// sequence.
				// TODO do piggybacking based on wsa:To
					
				AcknowledgementManager.piggybackAcksIfPresent(rmMsgCtx, storageManager);
			}

			// sending the message
			
			//if a different TransportOutDesc hs already been set, it will be used instead
			//of the one from te MessageContext.
			
			if (transportOut!=null)
				msgCtx.setTransportOut(transportOut);
			

			boolean successfullySent = false;

			// have to commit the transaction before sending. This may
			// get changed when WS-AT is available.
			transaction.commit();
			msgCtx.setProperty(Sandesha2Constants.WITHIN_TRANSACTION,
					Sandesha2Constants.VALUE_FALSE);

			try {

				// had to fully build the SOAP envelope to support
				// retransmissions.
				// Otherwise a 'parserAlreadyAccessed' exception could
				// get thrown in retransmissions.
				// But this has a performance reduction.
				msgCtx.getEnvelope().build();

				ArrayList retransmittablePhases = (ArrayList) msgCtx.getProperty(Sandesha2Constants.RETRANSMITTABLE_PHASES);
				if (retransmittablePhases!=null) {
					msgCtx.setExecutionChain(retransmittablePhases);
				} else {
					ArrayList emptyExecutionChain = new ArrayList ();
					msgCtx.setExecutionChain(emptyExecutionChain);
				}
				
				msgCtx.setCurrentHandlerIndex(0);
				msgCtx.setCurrentPhaseIndex(0);
				msgCtx.setPaused(false);
			
				AxisEngine engine = new AxisEngine (msgCtx.getConfigurationContext());
				engine.resumeSend(msgCtx);
				
				successfullySent = true;
			} catch (Exception e) {
				String message = SandeshaMessageHelper.getMessage(
						SandeshaMessageKeys.sendMsgError, e.toString());
				log.error(message, e);
			} finally {
				transaction = storageManager.getTransaction();
				msgCtx.setProperty(Sandesha2Constants.WITHIN_TRANSACTION,
						Sandesha2Constants.VALUE_TRUE);
			}

			// update or delete only if the object is still present.
			SenderBean bean1 = senderBeanMgr
					.retrieve(senderBean.getMessageID());
			if (bean1 != null) {
				if (senderBean.isReSend()) {
					bean1.setSentCount(senderBean.getSentCount());
					bean1.setTimeToSend(senderBean.getTimeToSend());
					senderBeanMgr.update(bean1);
				} else {
					senderBeanMgr.delete(bean1.getMessageID());

					// removing the message from the storage.
					String messageStoredKey = bean1.getMessageContextRefKey();
					storageManager.removeMessageContext(messageStoredKey);
				}
			}

			if (successfullySent) {
				if (!msgCtx.isServerSide())
					checkForSyncResponses(msgCtx);
			}

			if ((rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.TERMINATE_SEQ)
					&&
					 (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(rmMsgCtx.getRMNamespaceValue()))) {
				
				//terminate message sent using the SandeshaClient. Since the terminate message will simply get the
				//InFlow of the reference message get called which could be zero sized (OutOnly operations).
				
				// terminate sending side if this is the WSRM 1.0 spec. 
				// If the WSRM versoion is 1.1 termination will happen in the terminate sequence response message.
				
				TerminateSequence terminateSequence = (TerminateSequence) rmMsgCtx
						.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
				String sequenceID = terminateSequence.getIdentifier().getIdentifier();
				ConfigurationContext configContext = msgCtx.getConfigurationContext();

				String internalSequenceID = SandeshaUtil.getSequenceProperty(sequenceID,
						Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID, storageManager);
				String sequencePropertyKey = internalSequenceID; //property key of the sending side is the internal sequence Id.
				TerminateManager.terminateSendingSide(configContext, sequencePropertyKey ,internalSequenceID, msgCtx.isServerSide(),
						storageManager);
			}

			msgCtx.setProperty(Sandesha2Constants.WITHIN_TRANSACTION, Sandesha2Constants.VALUE_FALSE);
		} catch (SandeshaStorageException e) { 
			String message = "Sender got an Storage exception. Transaction will be rollbacked";
			log.error(message,e);

			if (transaction!=null && transaction.isActive())
				transaction.rollback();
		} catch (Exception e) {
			String message = "Sender got an  exception";
			log.error(message,e);
		} finally {
			if (transaction!=null && transaction.isActive())
				transaction.commit();
			
			if (lock!=null && workId!=null) {
				lock.removeWork(workId);
			}
		}
	}
	
	private void updateMessage(MessageContext msgCtx1) throws SandeshaException {
		// do updates if required.
	}
	
	private boolean isAckPiggybackableMsgType(int messageType) {
		if (log.isDebugEnabled())
			log.debug("Enter: Sender::isAckPiggybackableMsgType, " + messageType);
		boolean piggybackable = true;

		if (messageType == Sandesha2Constants.MessageTypes.ACK)
			piggybackable = false;

		if (log.isDebugEnabled())
			log.debug("Exit: Sender::isAckPiggybackableMsgType, " + piggybackable);
		return piggybackable;
	}
	
	private void checkForSyncResponses(MessageContext msgCtx) throws SandeshaException {
		if (log.isDebugEnabled())
			log.debug("Enter: Sender::checkForSyncResponses, " + msgCtx.getEnvelope().getHeader());

		try {

			boolean responsePresent = (msgCtx.getProperty(MessageContext.TRANSPORT_IN) != null);
			if (!responsePresent)
				return;

			// create the responseMessageContext

			MessageContext responseMessageContext = new MessageContext();
			
			//setting the message as serverSide will let it go through the MessageReceiver (may be callback MR).
			responseMessageContext.setServerSide(true);
			
			responseMessageContext.setConfigurationContext(msgCtx.getConfigurationContext());
			responseMessageContext.setTransportIn(msgCtx.getTransportIn());
			responseMessageContext.setTransportOut(msgCtx.getTransportOut());

			responseMessageContext.setProperty(MessageContext.TRANSPORT_IN, msgCtx
					.getProperty(MessageContext.TRANSPORT_IN));
			responseMessageContext.setServiceContext(msgCtx.getServiceContext());
			responseMessageContext.setServiceGroupContext(msgCtx.getServiceGroupContext());

			// copying required properties from op. context to the response msg
			// ctx.
			OperationContext requestMsgOpCtx = msgCtx.getOperationContext();
			if (requestMsgOpCtx != null) {
				if (responseMessageContext.getProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE) == null) {
					responseMessageContext.setProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE, requestMsgOpCtx
							.getProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE));
				}

				if (responseMessageContext.getProperty(HTTPConstants.CHAR_SET_ENCODING) == null) {
					responseMessageContext.setProperty(HTTPConstants.CHAR_SET_ENCODING, requestMsgOpCtx
							.getProperty(HTTPConstants.CHAR_SET_ENCODING));
				}
			}

			// If request is REST we assume the responseMessageContext is REST,
			// so set the variable

			responseMessageContext.setDoingREST(msgCtx.isDoingREST());

			SOAPEnvelope resenvelope = null;
			try {
				resenvelope = TransportUtils.createSOAPMessage(msgCtx, msgCtx.getEnvelope().getNamespace().getNamespaceURI());
			} catch (AxisFault e) {
				//Cannot find a valid SOAP envelope.
				if (log.isDebugEnabled()) {
					log.debug(SandeshaMessageHelper
							.getMessage(SandeshaMessageKeys.soapEnvNotSet));
					log.debug(e);
				}
				
				return;
			}

			// if the request msg ctx is withina a transaction, processing if
			// the response should also happen
			// withing the same transaction
			responseMessageContext.setProperty(Sandesha2Constants.WITHIN_TRANSACTION, msgCtx
					.getProperty(Sandesha2Constants.WITHIN_TRANSACTION));

			if (resenvelope != null) {
				responseMessageContext.setEnvelope(resenvelope);
				AxisEngine engine = new AxisEngine(msgCtx.getConfigurationContext());

				if (isFaultEnvelope(resenvelope)) {
					engine.receiveFault(responseMessageContext);
				} else {
					engine.receive(responseMessageContext);
				}
			}

		} catch (Exception e) {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.noValidSyncResponse);
			log.debug(message, e);
			throw new SandeshaException(message, e);
		}
		if (log.isDebugEnabled())
			log.debug("Exit: Sender::checkForSyncResponses");
	}
	
	private boolean isFaultEnvelope(SOAPEnvelope envelope) throws SandeshaException {
		if (log.isDebugEnabled())
			log.debug("Enter: Sender::isFaultEnvelope, " + envelope.getBody().getFault());
		SOAPFault fault = envelope.getBody().getFault();
		if (fault != null) {
			if (log.isDebugEnabled())
				log.debug("Exit: Sender::isFaultEnvelope, TRUE");
			return true;
		}

		if (log.isDebugEnabled())
			log.debug("Exit: Sender::isFaultEnvelope, FALSE");
		return false;
	}

}
