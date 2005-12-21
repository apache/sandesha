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

import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.Nack;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Responsible for processing an incoming acknowledgement message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class AcknowledgementProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		
		
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck == null)
			throw new SandeshaException("Sequence acknowledgement part is null");
		
		AbstractContext context = rmMsgCtx.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(rmMsgCtx.getMessageContext()
						.getConfigurationContext());
		
		Transaction ackTransaction = storageManager.getTransaction();
		
		SenderBeanMgr retransmitterMgr = storageManager
				.getRetransmitterBeanMgr();
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		Iterator ackRangeIterator = sequenceAck.getAcknowledgementRanges()
				.iterator();

		Iterator nackIterator = sequenceAck.getNackList().iterator();
		String outSequenceId = sequenceAck.getIdentifier().getIdentifier();
		if (outSequenceId == null || "".equals(outSequenceId))
			throw new SandeshaException("OutSequenceId is null");

		SequencePropertyBean internalSequenceBean = seqPropMgr.retrieve(
				outSequenceId, Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);

		if (internalSequenceBean == null || internalSequenceBean.getValue() == null)
			throw new SandeshaException("TempSequenceId is not set correctly");

		String internalSequenceId = (String) internalSequenceBean.getValue();

		//Following happens in the SandeshaGlobal handler
		rmMsgCtx.getMessageContext()
				.setProperty(Sandesha2Constants.ACK_PROCSSED, "true");

		//Removing relatesTo - Some WSRM endpoints tend to set relatesTo value
		// for ack messages.
		//Because of this dispatching may go wrong.
		//So we set relatesTo value to null for ackMessages. (this happens in
		// the SandeshaGlobal handler)
		//Do this only if this is a standalone ACK.
		if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.ACK)
			rmMsgCtx.setRelatesTo(null);

		SenderBean input = new SenderBean();
		input.setInternalSequenceID(internalSequenceId);
		input.setSend(true);
		input.setReSend(true);
		Collection retransmitterEntriesOfSequence = retransmitterMgr
				.find(input);

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
			}
		}

		while (nackIterator.hasNext()) {
			Nack nack = (Nack) nackIterator.next();
			long msgNo = nack.getNackNumber();

			//TODO - Process Nack
		}

		//following get called in the SandesaInHandler
		//if (justSendTerminateIfNeeded) {
		//If all messages up to last message have been acknowledged.
		//Add terminate Sequence message.
		SequencePropertyBean lastOutMsgBean = seqPropMgr.retrieve(
				internalSequenceId, Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE);
		if (lastOutMsgBean != null) {
			Long lastOutMsgNoLng = new Long (lastOutMsgBean.getValue());
			if (lastOutMsgNoLng == null)
				throw new SandeshaException(
						"Invalid object set for the Last Out Message");

			long lastOutMessageNo = lastOutMsgNoLng.longValue();
			if (lastOutMessageNo <= 0)
				throw new SandeshaException(
						"Invalid value set for the last out message");

			boolean complete = SandeshaUtil.verifySequenceCompletion(
					sequenceAck.getAcknowledgementRanges().iterator(),
					lastOutMessageNo);

			if (complete) {
				addTerminateSequenceMessage(rmMsgCtx, outSequenceId,
						internalSequenceId);
			}


		}
		
		//stopping the progress of the message further.
		//rmMsgCtx.getMessageContext().pause();
		rmMsgCtx.getMessageContext().setPausedTrue(new QName (Sandesha2Constants.IN_HANDLER_NAME));
		
		ackTransaction.commit();
		
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

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean terminated = seqPropMgr.retrieve(outSequenceId,
				Sandesha2Constants.SequenceProperties.TERMINATE_ADDED);

		if (terminated != null && terminated.getValue() != null
				&& "true".equals(terminated.getValue())) {
			System.out.println("TERMINATE WAS ADDED PREVIOUSLY....");
			return;
		}

		RMMsgContext terminateRMMessage = RMMsgCreator
				.createTerminateSequenceMessage(incomingAckRMMsg, outSequenceId);

		terminateRMMessage.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");
		
		SequencePropertyBean toBean = seqPropMgr.retrieve(internalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR);

		EndpointReference toEPR = new EndpointReference ( toBean.getValue());
		if (toEPR == null)
			throw new SandeshaException("To EPR has an invalid value");

		terminateRMMessage.setTo(new EndpointReference(toEPR.getAddress()));
		terminateRMMessage.setFrom(new EndpointReference(
				Sandesha2Constants.WSA.NS_URI_ANONYMOUS));
		terminateRMMessage.setFaultTo(new EndpointReference(
				Sandesha2Constants.WSA.NS_URI_ANONYMOUS));
		terminateRMMessage
				.setWSAAction(Sandesha2Constants.WSRM.Actions.ACTION_TERMINATE_SEQUENCE);
		terminateRMMessage
				.setSOAPAction(Sandesha2Constants.WSRM.Actions.SOAP_ACTION_TERMINATE_SEQUENCE);

		try {
			terminateRMMessage.addSOAPEnvelope();
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		String key = SandeshaUtil.storeMessageContext(terminateRMMessage
				.getMessageContext());
		SenderBean terminateBean = new SenderBean();
		terminateBean.setMessageContextRefKey(key);

		//Set a retransmitter lastSentTime so that terminate will be send with
		// some delay.
		//Otherwise this get send before return of the current request (ack).
		terminateBean.setTimeToSend(System.currentTimeMillis()
				+ Sandesha2Constants.TERMINATE_DELAY);

		terminateBean.setMessageID(terminateRMMessage.getMessageId());
		terminateBean.setSend(true);
		terminateBean.setReSend(false);

		SenderBeanMgr retramsmitterMgr = storageManager
				.getRetransmitterBeanMgr();

		SequencePropertyBean terminateAdded = new SequencePropertyBean();
		terminateAdded.setName(Sandesha2Constants.SequenceProperties.TERMINATE_ADDED);
		terminateAdded.setSequenceID(outSequenceId);
		terminateAdded.setValue("true");

		seqPropMgr.insert(terminateAdded);

		retramsmitterMgr.insert(terminateBean);

	}

}