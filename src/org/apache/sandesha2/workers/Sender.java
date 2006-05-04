/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *  
 */
package org.apache.sandesha2.workers;

import java.util.ArrayList;

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
import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.util.AcknowledgementManager;
import org.apache.sandesha2.util.MessageRetransmissionAdjuster;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.TerminateManager;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * This is responsible for sending and re-sending messages of Sandesha2. This
 * represent a thread that keep running all the time. This keep looking at the
 * Sender table to find out any entries that should be sent.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class Sender extends Thread {

	private boolean runSender = false;
	private boolean stopSenderAfterWork = false;
	private ArrayList workingSequences = new ArrayList();
	private ConfigurationContext context = null;
	private Log log = LogFactory.getLog(getClass());
	private ThreadPool threadPool = new ThreadPool ();

	public synchronized void stopSenderForTheSequence(String sequenceID) {
		workingSequences.remove(sequenceID);
		if (workingSequences.size() == 0) {
			 runSender = false;
		}
	}
	
	public synchronized void stopSending () {
		runSender = false;
	}

	public synchronized boolean isSenderStarted() {
		return runSender;
	}

	public void run() {

		StorageManager storageManager = null;

		try {
			storageManager = SandeshaUtil.getSandeshaStorageManager(context);
		} catch (SandeshaException e2) {
			// TODO Auto-generated catch block
			log.debug("ERROR: Could not start sender");
			e2.printStackTrace();
			return;
		}

		while (runSender) {

			try {
				Thread.sleep(Sandesha2Constants.SENDER_SLEEP_TIME);
			} catch (InterruptedException e1) {
				// e1.printStackTrace();
				log.debug("Sender was interupted...");
				log.debug(e1.getMessage());
				log.debug("End printing Interrupt...");
			}
			
			try {
				if (context == null) {
					String message = "Can't continue the Sender. Context is null";
					log.debug(message);
					throw new SandeshaException(message);
				}

				Transaction pickMessagesToSendTransaction = storageManager.getTransaction();

				SenderBeanMgr mgr = storageManager.getRetransmitterBeanMgr();
				SenderBean senderBean = mgr.getNextMsgToSend();
				if (senderBean==null) {
					pickMessagesToSendTransaction.commit();
					continue;
			    }
				

				MessageRetransmissionAdjuster retransmitterAdjuster = new MessageRetransmissionAdjuster();
				boolean continueSending = retransmitterAdjuster.adjustRetransmittion(senderBean, context);
				if (!continueSending)
					continue;
				
				pickMessagesToSendTransaction.commit();
				
				String key = (String) senderBean.getMessageContextRefKey();
				MessageContext msgCtx = storageManager.retrieveMessageContext(key, context);

				if (msgCtx == null) {
					String message = "Message context is not present in the storage";
				}

				// sender will not send the message if following property is
				// set and not true.
				// But it will set if it is not set (null)

				// This is used to make sure that the mesage get passed the Sandesha2TransportSender.

				String qualifiedForSending = (String) msgCtx.getProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING);
				if (qualifiedForSending != null && !qualifiedForSending.equals(Sandesha2Constants.VALUE_TRUE)) {
					continue;
				}

				if (msgCtx == null) {
					log.debug("ERROR: Sender has an Unavailable Message entry");
					break;
				}

				RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);

				//operation is the lowest level Sandesha2 should be attached
				ArrayList msgsNotToSend = SandeshaUtil.getPropertyBean(msgCtx.getAxisOperation()).getMsgTypesToDrop();

				if (msgsNotToSend != null && msgsNotToSend.contains(new Integer(rmMsgCtx.getMessageType()))) {
					continue;
				}
				
				updateMessage(msgCtx);

				Transaction preSendTransaction = storageManager.getTransaction();

				int messageType = rmMsgCtx.getMessageType();
				if (messageType == Sandesha2Constants.MessageTypes.APPLICATION) {
					Sequence sequence = (Sequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
					String sequenceID = sequence.getIdentifier().getIdentifier();
	
				}
				
				//checking weather this message can carry piggybacked acks
				if (isAckPiggybackableMsgType(messageType) && !isAckAlreadyPiggybacked(rmMsgCtx)) {
					// piggybacking if an ack if available for the same sequence.
					//TODO do piggybacking based on wsa:To
					AcknowledgementManager.piggybackAcksIfPresent(rmMsgCtx);
				}
				
				preSendTransaction.commit();
				
				//sending the message
				TransportOutDescription transportOutDescription = msgCtx.getTransportOut();
				TransportSender transportSender = transportOutDescription.getSender();
					
				boolean successfullySent = false;
				if (transportSender != null) {
					try {
						
						//TODO change this to cater for security.
						transportSender.invoke(msgCtx);
						successfullySent = true;
					} catch (AxisFault e) {
						// TODO Auto-generated catch block
					    log.debug("Could not send message");
						log.debug(e.getStackTrace().toString());
					}
				}

				Transaction postSendTransaction = storageManager.getTransaction();

				// update or delete only if the object is still present.
				SenderBean bean1 = mgr.retrieve(senderBean.getMessageID());
				if (bean1 != null) {
					if (senderBean.isReSend()) {
						bean1.setSentCount(senderBean.getSentCount());
						bean1.setTimeToSend(senderBean.getTimeToSend());
						mgr.update(bean1);
					} else
						mgr.delete(bean1.getMessageID());
				}

				postSendTransaction.commit(); // commiting the current transaction

				if (successfullySent) {
					if (!msgCtx.isServerSide())
						checkForSyncResponses(msgCtx);
				}

				Transaction terminateCleaningTransaction = storageManager.getTransaction();
				if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.TERMINATE_SEQ) {
					// terminate sending side.
					TerminateSequence terminateSequence = (TerminateSequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
					String sequenceID = terminateSequence.getIdentifier().getIdentifier();
					ConfigurationContext configContext = msgCtx.getConfigurationContext();
					
					String internalSequenceID = SandeshaUtil.getSequenceProperty(sequenceID,Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID,configContext);
					TerminateManager.terminateSendingSide(configContext,internalSequenceID, msgCtx.isServerSide());
				}

				terminateCleaningTransaction.commit();
				
			} catch (AxisFault e) {
				String message = "An Exception was throws in sending";
				log.debug(message,e);
				
				// TODO : when this is the client side throw the exception to
				// the client when necessary.

			}
		}
	}

	public synchronized void runSenderForTheSequence(
			ConfigurationContext context, String sequenceID) {

		if (sequenceID != null && !workingSequences.contains(sequenceID))
			workingSequences.add(sequenceID);

		if (!isSenderStarted()) {
			this.context = context;
			runSender = true; // so that isSenderStarted()=true.
			super.start();
		}
	}

	private void updateMessage(MessageContext msgCtx1) throws SandeshaException {
		//do updates if required.
	}

	private void checkForSyncResponses(MessageContext msgCtx)
			throws SandeshaException {

		try {

			boolean responsePresent = (msgCtx
					.getProperty(MessageContext.TRANSPORT_IN) != null);
			if (!responsePresent)
				return;
			
			// create the responseMessageContext

			MessageContext responseMessageContext = new MessageContext();
			responseMessageContext.setServerSide(false);
			responseMessageContext.setConfigurationContext(msgCtx
					.getConfigurationContext());
			responseMessageContext.setTransportIn(msgCtx.getTransportIn());
			responseMessageContext.setTransportOut(msgCtx.getTransportOut());

			responseMessageContext.setProperty(MessageContext.TRANSPORT_IN,
					msgCtx.getProperty(MessageContext.TRANSPORT_IN));
			responseMessageContext.setServiceContext(msgCtx.getServiceContext());
			responseMessageContext.setServiceGroupContext(msgCtx.getServiceGroupContext());
			
			//copying required properties from op. context to the response msg ctx.
			OperationContext requestMsgOpCtx = msgCtx.getOperationContext();
			if (requestMsgOpCtx!=null) {
			     if (responseMessageContext.getProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE)==null) {
			    	 responseMessageContext.setProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE,
			    			 requestMsgOpCtx.getProperty(HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE));
			     }
			     
			     if (responseMessageContext.getProperty(HTTPConstants.CHAR_SET_ENCODING)==null) {
			    	 responseMessageContext.setProperty(HTTPConstants.CHAR_SET_ENCODING,
			    			 requestMsgOpCtx.getProperty(HTTPConstants.CHAR_SET_ENCODING));
			     }
			}

			// If request is REST we assume the responseMessageContext is REST,
			// so set the variable

		    responseMessageContext.setDoingREST(msgCtx.isDoingREST());

			SOAPEnvelope resenvelope = null;
			try {
				resenvelope = TransportUtils.createSOAPMessage(msgCtx, 
						msgCtx.getEnvelope().getNamespace().getName());
				
			} catch (AxisFault e) {
				// TODO Auto-generated catch block
				log.debug("Valid SOAP envelope not found");
				log.debug(e.getStackTrace().toString());
			}

			if (resenvelope != null) {
				responseMessageContext.setEnvelope(resenvelope);
				AxisEngine engine = new AxisEngine(msgCtx
						.getConfigurationContext());
				
				if (isFaultEnvelope(resenvelope)) {
					engine.receiveFault(responseMessageContext);
				}else {
					engine.receive(responseMessageContext);
				}
			} 
			
		} catch (Exception e) {
			String message = "No valid Sync response...";
			log.info(message);
			throw new SandeshaException(message, e);
		}
	}
	
	private boolean isAckPiggybackableMsgType(int messageType) {
		boolean piggybackable = true;
		
		if (messageType==Sandesha2Constants.MessageTypes.ACK) 
			piggybackable = false;
		
		return piggybackable;	
	}
	
	private boolean isAckAlreadyPiggybacked (RMMsgContext rmMessageContext) {
		if (rmMessageContext.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT)!=null)
			return true;
		
		return false;
	}
	
	private boolean isFaultEnvelope (SOAPEnvelope envelope) throws SandeshaException {		
		SOAPFault fault = envelope.getBody().getFault();
		if (fault!=null)
			return true;
		else
			return false;
	}
	
}