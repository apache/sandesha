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

package org.apache.sandesha2.msgprocessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.transport.Sandesha2TransportOutDesc;
import org.apache.sandesha2.transport.Sandesha2TransportSender;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.Nack;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Responsible for processing an incoming acknowledgement message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class AcknowledgementProcessor implements MsgProcessor {

	Log log = LogFactory.getLog(getClass());
	
	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck == null) {
			String message = "Sequence acknowledgement part is null";
			log.debug(message);
			throw new SandeshaException(message);
		}
		
		AbstractContext context = rmMsgCtx.getContext();
		if (context == null)
		{
			String message = "Context is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(rmMsgCtx.getMessageContext()
						.getConfigurationContext());
		SenderBeanMgr retransmitterMgr = storageManager
				.getRetransmitterBeanMgr();
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();



		Iterator ackRangeIterator = sequenceAck.getAcknowledgementRanges()
				.iterator();

		Iterator nackIterator = sequenceAck.getNackList().iterator();
		String outSequenceId = sequenceAck.getIdentifier().getIdentifier();
		if (outSequenceId == null || "".equals(outSequenceId)) {
			String message = "OutSequenceId is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		//updating the last activated time of the sequence.
//		Transaction lastUpdatedTimeTransaction = storageManager.getTransaction();
//		SequenceManager.updateLastActivatedTime(outSequenceId,rmMsgCtx.getMessageContext().getConfigurationContext());
//		lastUpdatedTimeTransaction.commit();
		
		//Starting transaction
		Transaction ackTransaction = storageManager.getTransaction();

		SequencePropertyBean internalSequenceBean = seqPropMgr.retrieve(
				outSequenceId, Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);

		if (internalSequenceBean == null || internalSequenceBean.getValue() == null) {
			String message = "TempSequenceId is not set correctly";
			log.debug(message);
			throw new SandeshaException(message);
		}

		String internalSequenceId = (String) internalSequenceBean.getValue();

		//Following happens in the SandeshaGlobal handler
		rmMsgCtx.getMessageContext()
				.setProperty(Sandesha2Constants.ACK_PROCSSED, "true");

		//Removing relatesTo - Some WSRM endpoints tend to set relatesTo value for ack messages.
		//Because of this dispatching may go wrong. So we set relatesTo value to null for ackMessages. 
		//(this happens in the SandeshaGlobal handler). Do this only if this is a standalone ACK.
		if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.ACK)
			rmMsgCtx.setRelatesTo(null);

		SenderBean input = new SenderBean();
		input.setSend(true);
		input.setReSend(true);
		Collection retransmitterEntriesOfSequence = retransmitterMgr
				.find(input);

		ArrayList ackedMessagesList = new ArrayList ();
		while (ackRangeIterator.hasNext()) {
			AcknowledgementRange ackRange = (AcknowledgementRange) ackRangeIterator
					.next();
			long lower = ackRange.getLowerValue();
			long upper = ackRange.getUpperValue();

			for (long messageNo = lower; messageNo <= upper; messageNo++) {
				SenderBean retransmitterBean = getRetransmitterEntry(
						retransmitterEntriesOfSequence, messageNo);
				if (retransmitterBean != null)
					retransmitterMgr.delete(retransmitterBean.getMessageID());
				
				ackedMessagesList.add(new Long (messageNo));
			}
		}

		while (nackIterator.hasNext()) {
			Nack nack = (Nack) nackIterator.next();
			long msgNo = nack.getNackNumber();

			//TODO - Process Nack
		}
		
		//setting acked message date.
		//TODO add details specific to each message.
		long noOfMsgsAcked = getNoOfMessagesAcked(sequenceAck.getAcknowledgementRanges().iterator());
		SequencePropertyBean noOfMsgsAckedBean = seqPropMgr.retrieve(outSequenceId,Sandesha2Constants.SequenceProperties.NO_OF_OUTGOING_MSGS_ACKED);
		boolean added = false;
		
		if (noOfMsgsAckedBean==null) {
			added = true;
			noOfMsgsAckedBean = new SequencePropertyBean ();
			noOfMsgsAckedBean.setSequenceID(outSequenceId);
			noOfMsgsAckedBean.setName(Sandesha2Constants.SequenceProperties.NO_OF_OUTGOING_MSGS_ACKED);
		}
		
		noOfMsgsAckedBean.setValue(Long.toString(noOfMsgsAcked));
		
		if (added) 
			seqPropMgr.insert(noOfMsgsAckedBean);
		else
			seqPropMgr.update(noOfMsgsAckedBean);
		
		
		//setting the completed_messages list. This gives all the messages of the sequence that were acked.
		SequencePropertyBean allCompletedMsgsBean = seqPropMgr.retrieve(internalSequenceId,Sandesha2Constants.SequenceProperties.COMPLETED_MESSAGES);
		if (allCompletedMsgsBean==null) {
			allCompletedMsgsBean = new SequencePropertyBean ();
			allCompletedMsgsBean.setSequenceID(internalSequenceId);
			allCompletedMsgsBean.setName(Sandesha2Constants.SequenceProperties.COMPLETED_MESSAGES);
			
			seqPropMgr.insert(allCompletedMsgsBean);
		}
				
		String str = ackedMessagesList.toString();
		allCompletedMsgsBean.setValue(str);
		seqPropMgr.update(allCompletedMsgsBean);
		
		//If all messages up to last message have been acknowledged. Add terminate Sequence message.
		SequencePropertyBean lastOutMsgBean = seqPropMgr.retrieve(
				internalSequenceId, Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE);
		if (lastOutMsgBean != null) {
			Long lastOutMsgNoLng = new Long (lastOutMsgBean.getValue());
			if (lastOutMsgNoLng == null) {
				String message = "Invalid object set for the Last Out Message";
				log.debug(message);
				throw new SandeshaException(message);
			}
			
			long lastOutMessageNo = lastOutMsgNoLng.longValue();
			if (lastOutMessageNo <= 0) {
				String message = "Invalid value set for the last out message";
				log.debug(message);
				throw new SandeshaException(message);
			}


			//commiting transaction
			ackTransaction.commit();

			boolean complete = SandeshaUtil.verifySequenceCompletion(
					sequenceAck.getAcknowledgementRanges().iterator(),
					lastOutMessageNo);
			
			if (complete) {
				//Transaction terminateTransaction = storageManager.getTransaction();
				addTerminateSequenceMessage(rmMsgCtx, outSequenceId,
						internalSequenceId);
				//terminateTransaction.commit();
			}
		}
	
		//stopping the progress of the message further.
		rmMsgCtx.pause();	
		

	}

	private SenderBean getRetransmitterEntry(Collection collection,
			long msgNo) {
		Iterator it = collection.iterator();
		while (it.hasNext()) {
			SenderBean bean = (SenderBean) it.next();
			if (bean.getMessageNumber() == msgNo)
				return bean;
		}

		return null;
	}

	public void addTerminateSequenceMessage(RMMsgContext incomingAckRMMsg,
			String outSequenceId, String internalSequenceId)
			throws SandeshaException {

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(incomingAckRMMsg.getMessageContext()
						.getConfigurationContext());

		Transaction addTerminateSeqTransaction = storageManager.getTransaction();
		
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean terminated = seqPropMgr.retrieve(outSequenceId,
				Sandesha2Constants.SequenceProperties.TERMINATE_ADDED);

		if (terminated != null && terminated.getValue() != null
				&& "true".equals(terminated.getValue())) {
			String message = "Terminate was added previously.";
			log.info(message);
			return;
		}

		RMMsgContext terminateRMMessage = RMMsgCreator
				.createTerminateSequenceMessage(incomingAckRMMsg, outSequenceId);

		terminateRMMessage.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");
		
		SequencePropertyBean toBean = seqPropMgr.retrieve(internalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR);

		EndpointReference toEPR = new EndpointReference ( toBean.getValue());
		if (toEPR == null) {
			String message = "To EPR has an invalid value";
			throw new SandeshaException(message);
		}

		terminateRMMessage.setTo(new EndpointReference(toEPR.getAddress()));
		terminateRMMessage.setFrom(new EndpointReference(
				Sandesha2Constants.WSA.NS_URI_ANONYMOUS));
		terminateRMMessage.setFaultTo(new EndpointReference(
				Sandesha2Constants.WSA.NS_URI_ANONYMOUS));
		terminateRMMessage
				.setWSAAction(Sandesha2Constants.WSRM.Actions.ACTION_TERMINATE_SEQUENCE);
		terminateRMMessage
				.setSOAPAction(Sandesha2Constants.WSRM.Actions.SOAP_ACTION_TERMINATE_SEQUENCE);

		SequencePropertyBean transportToBean = seqPropMgr.retrieve(internalSequenceId,Sandesha2Constants.SequenceProperties.TRANSPORT_TO);
		if (transportToBean!=null) {
			terminateRMMessage.setProperty(MessageContextConstants.TRANSPORT_URL,transportToBean.getValue());
		}
		
		try {
			terminateRMMessage.addSOAPEnvelope();
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		String key = SandeshaUtil.getUUID();
		
		SenderBean terminateBean = new SenderBean();
		terminateBean.setMessageContextRefKey(key);

		
		//storageManager.storeMessageContext(key,terminateRMMessage.getMessageContext());

		
		//Set a retransmitter lastSentTime so that terminate will be send with
		// some delay.
		//Otherwise this get send before return of the current request (ack).
		//TODO: refine the terminate delay.
		terminateBean.setTimeToSend(System.currentTimeMillis()
				+ Sandesha2Constants.TERMINATE_DELAY);

		terminateBean.setMessageID(terminateRMMessage.getMessageId());
		
		//this will be set to true at the sender.
		terminateBean.setSend(true);
		
		terminateRMMessage.getMessageContext().setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,
				Sandesha2Constants.VALUE_FALSE);
		
		terminateBean.setReSend(false);

		SenderBeanMgr retramsmitterMgr = storageManager
				.getRetransmitterBeanMgr();

		retramsmitterMgr.insert(terminateBean);
		
		SequencePropertyBean terminateAdded = new SequencePropertyBean();
		terminateAdded.setName(Sandesha2Constants.SequenceProperties.TERMINATE_ADDED);
		terminateAdded.setSequenceID(outSequenceId);
		terminateAdded.setValue("true");

		seqPropMgr.insert(terminateAdded);
		
		//This should be dumped to the storage by the sender
		TransportOutDescription transportOut = terminateRMMessage.getMessageContext().getTransportOut();
		
		terminateRMMessage.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_OUT_DESC,transportOut);
		
		terminateRMMessage.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY,key);
		
		terminateRMMessage.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,Sandesha2Constants.VALUE_TRUE);
		
		terminateRMMessage.getMessageContext().setTransportOut(new Sandesha2TransportOutDesc ());
		
		addTerminateSeqTransaction.commit();
		
	    AxisEngine engine = new AxisEngine (incomingAckRMMsg.getMessageContext().getConfigurationContext());
	    try {
			engine.send(terminateRMMessage.getMessageContext());
		} catch (AxisFault e) {
			throw new SandeshaException (e.getMessage());
		}
	    
	}
	
	private static long getNoOfMessagesAcked (Iterator ackRangeIterator) {
		long noOfMsgs = 0;
		while (ackRangeIterator.hasNext()) {
			AcknowledgementRange acknowledgementRange = (AcknowledgementRange) ackRangeIterator.next();
			long lower = acknowledgementRange.getLowerValue();
			long upper = acknowledgementRange.getUpperValue();
			
			for (long i=lower;i<=upper;i++) {
				noOfMsgs++;
			}
		}
		
		return noOfMsgs;
	}
}