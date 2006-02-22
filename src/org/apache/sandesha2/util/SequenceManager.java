/*
 * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2.util;

import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.OperationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.CreateSequence;

/**
 * This is used to set up a new sequence, both at the sending side and the receiving side.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SequenceManager {

	private static Log log = LogFactory.getLog(SequenceManager.class);
	
	public static String setupNewSequence(RMMsgContext createSequenceMsg)
			throws AxisFault {

		String sequenceId = SandeshaUtil.getUUID();

		EndpointReference to = createSequenceMsg.getTo();
		if (to == null) {
			String message = "To is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		EndpointReference replyTo = createSequenceMsg.getReplyTo();

		CreateSequence createSequence = (CreateSequence) createSequenceMsg
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSequence == null) {
			String message = "Create Sequence Part is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		EndpointReference acksTo = createSequence.getAcksTo().getAddress()
				.getEpr();

		if (acksTo == null) {
			String message = "AcksTo is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		StorageManager storageManager = null;
		ConfigurationContext configurationContext = createSequenceMsg.getMessageContext()
									.getConfigurationContext();
		try {
			storageManager = SandeshaUtil
					.getSandeshaStorageManager(configurationContext);
		} catch (SandeshaException e) {
			e.printStackTrace();
		}

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean receivedMsgBean = new SequencePropertyBean(
				sequenceId, Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES, "");
		
		//If no replyTo value. Send responses as sync.
		SequencePropertyBean toBean = null;
		if (replyTo!=null) {
			toBean = new SequencePropertyBean(sequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR, replyTo.getAddress());
		}else {
			toBean = new SequencePropertyBean(sequenceId,
					Sandesha2Constants.SequenceProperties.TO_EPR, Sandesha2Constants.WSA.NS_URI_ANONYMOUS);
		}
		
		SequencePropertyBean replyToBean = new SequencePropertyBean(sequenceId,
				Sandesha2Constants.SequenceProperties.REPLY_TO_EPR, to.getAddress());
		SequencePropertyBean acksToBean = new SequencePropertyBean(sequenceId,
				Sandesha2Constants.SequenceProperties.ACKS_TO_EPR, acksTo.getAddress());

		seqPropMgr.insert(receivedMsgBean);
		seqPropMgr.insert(replyToBean);
		seqPropMgr.insert(acksToBean);
		
		if (toBean!=null)
			seqPropMgr.insert(toBean);

		NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();
		nextMsgMgr.insert(new NextMsgBean(sequenceId, 1)); // 1 will be the next
		
		// message to invoke. This will apply for only in-order invocations.

		
		SandeshaUtil.startSenderForTheSequence(configurationContext,sequenceId);
		
		//Adding another entry to the ListnerManager to wait till the terminate sequence message.
		String transport = createSequenceMsg.getMessageContext().getTransportIn().getName().getLocalPart();
		
		
		//Only the client side should call below.
//		try {
//			//An bind method is thrown when this is done in the server side. TODO find a better method to do this.
//			ListenerManager.makeSureStarted(transport,configurationContext);
//		} catch (AxisFault ex) {
//			log.info("Counght exception when starting listner. Possible server side start.");
//		}
		
		
		
		return sequenceId;
	}

	public void removeSequence(String sequence) {

	}

	public static void setupNewClientSequence(
			MessageContext firstAplicationMsgCtx, String internalSequenceId)
			throws SandeshaException {
		
		ConfigurationContext configurationContext = firstAplicationMsgCtx
										.getConfigurationContext();

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(configurationContext);

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		EndpointReference toEPR = firstAplicationMsgCtx.getTo();
		String acksTo = (String) firstAplicationMsgCtx
				.getProperty(Sandesha2ClientAPI.AcksTo);

		if (toEPR == null) {
			String message = "WS-Addressing To is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		SequencePropertyBean toBean = new SequencePropertyBean(internalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR, toEPR.getAddress());
		SequencePropertyBean replyToBean = null;
		SequencePropertyBean acksToBean = null;
		
		if (firstAplicationMsgCtx.isServerSide()) {
			//setting replyTo value, if this is the server side.
			OperationContext opContext = firstAplicationMsgCtx.getOperationContext();
			try {
				MessageContext requestMessage = opContext.getMessageContext(OperationContextFactory.MESSAGE_LABEL_IN_VALUE);
				if (requestMessage==null) {
					String message = "Cannot find the request message from the operation context";
					log.error(message);
					throw new SandeshaException (message);
				}
				
				EndpointReference replyToEPR = requestMessage.getTo();    //'replyTo' of the response msg is the 'to' value of the req msg.
				if (replyToEPR!=null) {
					replyToBean = new SequencePropertyBean (internalSequenceId,Sandesha2Constants.SequenceProperties.REPLY_TO_EPR,replyToEPR.getAddress());
					acksToBean = new SequencePropertyBean (internalSequenceId,Sandesha2Constants.SequenceProperties.ACKS_TO_EPR,replyToEPR.getAddress());		
				} else {
					String message = "To EPR is not present in the request message. Need this information to set acksTo & replyTo value of reply messages";
					log.error(message);
					throw new SandeshaException (message);
				}
			} catch (AxisFault e) {
				String message = "Cannot get request message from the operation context";
				log.error(message);
				log.error(e.getStackTrace());
				throw new SandeshaException (message);
			}
		}
		//Default value for acksTo is anonymous  (this happens only for the client side)
		if (acksToBean==null) {
			acksTo = Sandesha2Constants.WSA.NS_URI_ANONYMOUS;

			EndpointReference acksToEPR = new EndpointReference(acksTo);
		    acksToBean = new SequencePropertyBean(
				internalSequenceId, Sandesha2Constants.SequenceProperties.ACKS_TO_EPR,
				acksToEPR.getAddress());
		}
		
		seqPropMgr.insert(toBean);
		if (acksToBean!=null)
			seqPropMgr.insert(acksToBean);
		if (replyToBean!=null)
			seqPropMgr.insert(replyToBean);
		
		//saving transportTo value;
		String transportTo = (String) firstAplicationMsgCtx.getProperty(MessageContextConstants.TRANSPORT_URL);
		if (transportTo!=null) {
			SequencePropertyBean transportToBean = new SequencePropertyBean ();
			transportToBean.setSequenceID(internalSequenceId);
			transportToBean.setName(Sandesha2Constants.SequenceProperties.TRANSPORT_TO);
			transportToBean.setValue(transportTo);
			
			seqPropMgr.insert(transportToBean);
		}


		SandeshaUtil.startSenderForTheSequence(configurationContext,internalSequenceId);
		
		updateClientSideListnerIfNeeded (firstAplicationMsgCtx);
		
	}
	
	private static void updateClientSideListnerIfNeeded (MessageContext messageContext) throws SandeshaException {
		if (messageContext.isServerSide())
			return;   //listners are updated only for the client side.
		
		String transportInProtocol = messageContext.getOptions().getTransportInProtocol();
		
		String acksTo = (String) messageContext.getProperty(Sandesha2ClientAPI.AcksTo);
		String mep = messageContext.getAxisOperation().getMessageExchangePattern();
		
		boolean startListnerForAsyncAcks = false;
		boolean startListnerForAsyncControlMsgs = false;   //For async createSerRes & terminateSeq.
		
		if (acksTo!=null && !Sandesha2Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo)) {
			//starting listner for async acks.
			startListnerForAsyncAcks = true;
		}
		
		if (mep!=null && !AxisOperation.MEP_URI_OUT_ONLY.equals(mep)) {
			//starting listner for the async createSeqResponse & terminateSer messages.
			startListnerForAsyncControlMsgs = true;
		}
		
		try {
			if ((startListnerForAsyncAcks || startListnerForAsyncControlMsgs) && transportInProtocol==null)
				throw new SandeshaException ("Cant start the listner since the TransportInProtocol is null");

//			if (startListnerForAsyncAcks)
//				ListenerManager.makeSureStarted(messageContext.getOptions().getTransportInProtocol(),messageContext.getConfigurationContext());
//		
//			if (startListnerForAsyncControlMsgs)
//				ListenerManager.makeSureStarted(messageContext.getOptions().getTransportInProtocol(),messageContext.getConfigurationContext());
							
			
		} catch (AxisFault e) {
			String message = "Cant start the listner for incoming messages";
			log.error(e.getStackTrace());
			System.out.println(e.getStackTrace());
			throw new SandeshaException (message);
		}
	
	}
	
	/**
	 * Takes the internalSeqID as the param. Not the sequenceID.
	 * @param internalSequenceID
	 * @param configContext
	 * @throws SandeshaException
	 */
	public static void updateLastActivatedTime (String sequenceID, ConfigurationContext configContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		//Transaction lastActivatedTransaction = storageManager.getTransaction();
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
//		SequencePropertyBean internalSequenceFindBean = new SequencePropertyBean (sequenceID,Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID,null);
//		SequencePropertyBean internalSequenceBean = sequencePropertyBeanMgr.findUnique(internalSequenceFindBean);
//		if (internalSequenceBean==null) {
//			String message = "InternalSequenceBean is not set";
//			log.error(message);
//			throw new SandeshaException (message);
//		}
//		
//		String internalSequenceID = internalSequenceBean.getValue();
		SequencePropertyBean lastActivatedBean = sequencePropertyBeanMgr.retrieve(sequenceID, Sandesha2Constants.SequenceProperties.LAST_ACTIVATED_TIME);
		
		boolean added = false;
		
		if (lastActivatedBean==null) {
			added = true;
			lastActivatedBean = new SequencePropertyBean ();
			lastActivatedBean.setSequenceID(sequenceID);
			lastActivatedBean.setName(Sandesha2Constants.SequenceProperties.LAST_ACTIVATED_TIME);
		}
		
		long currentTime = System.currentTimeMillis();
		lastActivatedBean.setValue(Long.toString(currentTime));
		
		if (added)
			sequencePropertyBeanMgr.insert(lastActivatedBean);
		else
			sequencePropertyBeanMgr.update(lastActivatedBean);
		
	//	lastActivatedTransaction.commit();
	}
	
	
	public static long getLastActivatedTime (String sequenceID, ConfigurationContext configContext) throws SandeshaException {
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean lastActivatedBean = seqPropBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.LAST_ACTIVATED_TIME);
		
		long lastActivatedTime = -1;
		
		if (lastActivatedBean!=null) {
			lastActivatedTime = Long.parseLong(lastActivatedBean.getValue());
		}
		
		return lastActivatedTime;
	}
		
	public static boolean hasSequenceTimedOut (String sequenceID, RMMsgContext rmMsgCtx) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(rmMsgCtx.getMessageContext().getConfigurationContext());
		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		RMPolicyBean policyBean = (RMPolicyBean) rmMsgCtx
			.getProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN);
		if (policyBean == null) {
			//loading default policies.
			//policyBean = PropertyManager.getInstance().getRMPolicyBean();
			Parameter parameter =  rmMsgCtx.getMessageContext().getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
			SandeshaPropertyBean propertyBean = (SandeshaPropertyBean) parameter.getValue();
			policyBean = propertyBean.getPolicyBean();
		}
		
		if (policyBean.getInactiveTimeoutInterval()<=0)
			return false;

		boolean sequenceTimedOut = false;
		
		//SequencePropertyBean lastActivatedBean = seqPropBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.LAST_ACTIVATED_TIME);
		//if (lastActivatedBean!=null) {
		long lastActivatedTime = getLastActivatedTime(sequenceID,rmMsgCtx.getMessageContext().getConfigurationContext());
		long timeNow = System.currentTimeMillis();
		if (lastActivatedTime>0 && (lastActivatedTime+policyBean.getInactiveTimeoutInterval()<timeNow))
			sequenceTimedOut = true;
		//}
		
		return sequenceTimedOut;
	}
	
	public static long getOutGoingSequenceAckedMessageCount (String internalSequenceID,ConfigurationContext configurationContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		Transaction transaction = storageManager.getTransaction();
		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean findSeqIDBean = new SequencePropertyBean ();
		findSeqIDBean.setValue(internalSequenceID);
		findSeqIDBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		Collection seqIDBeans = seqPropBeanMgr.find(findSeqIDBean);
		
		if (seqIDBeans.size()==0) {
			String message = "A sequence with give data has not been created";
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		if (seqIDBeans.size()>1) {
			String message = "Sequence data is not unique. Cant generate report";
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		SequencePropertyBean seqIDBean = (SequencePropertyBean) seqIDBeans.iterator().next();
		String sequenceID = seqIDBean.getSequenceID();

		SequencePropertyBean ackedMsgBean = seqPropBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.NO_OF_OUTGOING_MSGS_ACKED);
		if (ackedMsgBean==null)
			return 0; //No acknowledgement has been received yet.
		
		long noOfMessagesAcked = Long.parseLong(ackedMsgBean.getValue());
		transaction.commit();
		
		return noOfMessagesAcked;
	}
	
	public static boolean isOutGoingSequenceCompleted (String internalSequenceID,ConfigurationContext configurationContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		Transaction transaction = storageManager.getTransaction();
		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean findSeqIDBean = new SequencePropertyBean ();
		findSeqIDBean.setValue(internalSequenceID);
		findSeqIDBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		Collection seqIDBeans = seqPropBeanMgr.find(findSeqIDBean);
		
		if (seqIDBeans.size()==0) {
			String message = "A sequence with give data has not been created";
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		if (seqIDBeans.size()>1) {
			String message = "Sequence data is not unique. Cant generate report";
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		SequencePropertyBean seqIDBean = (SequencePropertyBean) seqIDBeans.iterator().next();
		String sequenceID = seqIDBean.getSequenceID();
		
		SequencePropertyBean terminateAddedBean = seqPropBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.TERMINATE_ADDED);
		if (terminateAddedBean==null)
			return false;
		
		if ("true".equals(terminateAddedBean.getValue()))
			return true;

		transaction.commit();
		return false;
	}
	
//	public static long getIncomingSequenceAckedMessageCount (String sequenceID, ConfigurationContext configurationContext) throws SandeshaException {
//		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
//		Transaction transaction = storageManager.getTransaction();
//		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
//		
//		SequencePropertyBean receivedMsgsBean = seqPropBeanMgr.retrieve(sequenceID, Sandesha2Constants.SequenceProperties.COMPLETED_MESSAGES);
//		
//		//we should be able to assume that all the received messages has been acked.
//		String receivedMsgsStr = receivedMsgsBean.getValue();
//
//		StringTokenizer tokenizer = new StringTokenizer (receivedMsgsStr,",");
//		
//		long count = 0;
//		while (tokenizer.hasMoreTokens()) {
//			String temp = tokenizer.nextToken();
//			count++;
//		}
//
//		transaction.commit();
//		return count;
//	}
	
	public static boolean isIncomingSequenceCompleted (String sequenceID, ConfigurationContext configurationContext) throws SandeshaException {
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		Transaction transaction = storageManager.getTransaction();
		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean terminateReceivedBean = seqPropBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.TERMINATE_RECEIVED);
		boolean complete = false;
		
		if (terminateReceivedBean!=null && "true".equals(terminateReceivedBean.getValue()))
			complete = true;
		
		transaction.commit();
		return complete;
	}
	
	
}