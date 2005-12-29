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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2ClientAPI;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
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

	public static String setupNewSequence(RMMsgContext createSequenceMsg)
			throws AxisFault {

		String sequenceId = SandeshaUtil.getUUID();
		AbstractContext context = createSequenceMsg.getContext();

		EndpointReference to = createSequenceMsg.getTo();
		if (to == null)
			throw new AxisFault("To is null");

		EndpointReference replyTo = createSequenceMsg.getReplyTo();
//		if (replyTo == null)
//			throw new AxisFault("ReplyTo is null");

		CreateSequence createSequence = (CreateSequence) createSequenceMsg
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSequence == null)
			throw new AxisFault("Create Sequence Part is null");

		EndpointReference acksTo = createSequence.getAcksTo().getAddress()
				.getEpr();

		if (acksTo == null)
			throw new AxisFault("AcksTo is null");

		StorageManager storageManager = null;

		try {
			storageManager = SandeshaUtil
					.getSandeshaStorageManager(createSequenceMsg
							.getMessageContext().getConfigurationContext());
		} catch (SandeshaException e) {
			e.printStackTrace();
		}

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean receivedMsgBean = new SequencePropertyBean(
				sequenceId, Sandesha2Constants.SequenceProperties.RECEIVED_MESSAGES, "");
		
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
		// message to invoke
		//this will apply for only in-order invocations.

		updateLastActivatedTime(sequenceId,createSequenceMsg.getMessageContext().getConfigurationContext());
		
		return sequenceId;
	}

	public void removeSequence(String sequence) {

	}

	public static void setupNewClientSequence(
			MessageContext firstAplicationMsgCtx, String internalSequenceId)
			throws SandeshaException {

		AbstractContext context = firstAplicationMsgCtx.getConfigurationContext();
 
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(firstAplicationMsgCtx
						.getConfigurationContext());

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		EndpointReference toEPR = firstAplicationMsgCtx.getTo();
		EndpointReference replyToEPR = firstAplicationMsgCtx.getReplyTo();
		String acksTo = (String) firstAplicationMsgCtx
				.getProperty(Sandesha2ClientAPI.AcksTo);

		if (toEPR == null)
			throw new SandeshaException("WS-Addressing To is null");

		SequencePropertyBean toBean = new SequencePropertyBean(internalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR, toEPR.getAddress());

		//Default value for acksTo is anonymous
		if (acksTo == null)
			acksTo = Sandesha2Constants.WSA.NS_URI_ANONYMOUS;

		EndpointReference acksToEPR = new EndpointReference(acksTo);
		SequencePropertyBean acksToBean = new SequencePropertyBean(
				internalSequenceId, Sandesha2Constants.SequenceProperties.ACKS_TO_EPR,
				acksToEPR.getAddress());
		seqPropMgr.insert(toBean);
		seqPropMgr.insert(acksToBean);
		
		//saving transportTo value;
		String transportTo = (String) firstAplicationMsgCtx.getProperty(MessageContextConstants.TRANSPORT_URL);
		if (transportTo!=null) {
			SequencePropertyBean transportToBean = new SequencePropertyBean ();
			transportToBean.setSequenceID(internalSequenceId);
			transportToBean.setName(Sandesha2Constants.SequenceProperties.TRANSPORT_TO);
			transportToBean.setValue(transportTo);
			
			seqPropMgr.insert(transportToBean);
		}

	}
	
	public static void updateLastActivatedTime (String sequenceID, ConfigurationContext configContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		Transaction lastActivatedTransaction = storageManager.getTransaction();
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
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
		
		lastActivatedTransaction.commit();
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
			policyBean = PropertyManager.getInstance().getRMPolicyBean();
		}
		
		if (policyBean.getInactiveTimeoutInterval()<=0)
			return false;

		boolean sequenceTimedOut = false;
		
		SequencePropertyBean lastActivatedBean = seqPropBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.LAST_ACTIVATED_TIME);
		if (lastActivatedBean!=null) {
			long lastActivatedTime = Long.parseLong(lastActivatedBean.getValue());
			long timeNow = System.currentTimeMillis();
			if (lastActivatedTime+policyBean.getInactiveTimeoutInterval()<timeNow)
				sequenceTimedOut = true;
		}
		
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
		
		if (seqIDBeans.size()==0)
			throw new SandeshaException ("A sequence with give data has not been created");
		
		if (seqIDBeans.size()>1) 
			throw new SandeshaException ("Sequence data is not unique. Cant generate report");
		
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
		
		if (seqIDBeans.size()==0)
			throw new SandeshaException ("A sequence with give data has not been created");
		
		if (seqIDBeans.size()>1) 
			throw new SandeshaException ("Sequence data is not unique. Cant generate report");
		
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
	
	public static long getIncomingSequenceAckedMessageCount (String sequenceID, ConfigurationContext configurationContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		Transaction transaction = storageManager.getTransaction();
		SequencePropertyBeanMgr seqPropBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean receivedMsgsBean = seqPropBeanMgr.retrieve(sequenceID, Sandesha2Constants.SequenceProperties.RECEIVED_MESSAGES);
		
		//we should be able to assume that all the received messages has been acked.
		String receivedMsgsStr = receivedMsgsBean.getValue();

		StringTokenizer tokenizer = new StringTokenizer (receivedMsgsStr,",");
		
		long count = 0;
		while (tokenizer.hasMoreTokens()) {
			String temp = tokenizer.nextToken();
			count++;
		}


		transaction.commit();
		return count;
	}
	
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