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
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.Nack;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

public class AcknowledgementProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck == null)
			throw new SandeshaException("Sequence acknowledgement part is null");

		AbstractContext context = rmMsgCtx.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(rmMsgCtx.getMessageContext()
						.getSystemContext());
		RetransmitterBeanMgr retransmitterMgr = storageManager
				.getRetransmitterBeanMgr();
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		Iterator ackRangeIterator = sequenceAck.getAcknowledgementRanges()
				.iterator();

		Iterator nackIterator = sequenceAck.getNackList().iterator();
		String outSequenceId = sequenceAck.getIdentifier().getIdentifier();
		if (outSequenceId == null || "".equals(outSequenceId))
			throw new SandeshaException("OutSequenceId is null");

		SequencePropertyBean tempSequenceBean = seqPropMgr.retrieve(
				outSequenceId, Constants.SequenceProperties.TEMP_SEQUENCE_ID);

		if (tempSequenceBean == null || tempSequenceBean.getValue() == null)
			throw new SandeshaException("TempSequenceId is not set correctly");

		String tempSequenceId = (String) tempSequenceBean.getValue();

		//Following happens in the SandeshaGlobal handler
		rmMsgCtx.getMessageContext()
				.setProperty(Constants.ACK_PROCSSED, "true");

		//Removing relatesTo - Some WSRM endpoints tend to set relatesTo value
		// for ack messages.
		//Because of this dispatching may go wrong.
		//So we set relatesTo value to null for ackMessages. (this happens in
		// the SandeshaGlobal handler)
		rmMsgCtx.setRelatesTo(null);

		RetransmitterBean input = new RetransmitterBean();
		input.setTempSequenceId(tempSequenceId);
		Collection retransmitterEntriesOfSequence = retransmitterMgr
				.find(input);

		//TODO - make following more efficient
		while (ackRangeIterator.hasNext()) {
			AcknowledgementRange ackRange = (AcknowledgementRange) ackRangeIterator
					.next();
			long lower = ackRange.getLowerValue();
			long upper = ackRange.getUpperValue();

			for (long messageNo = lower; messageNo <= upper; messageNo++) {
				RetransmitterBean retransmitterBean = getRetransmitterEntry(
						retransmitterEntriesOfSequence, messageNo);
				if (retransmitterBean != null)
					retransmitterMgr.delete(retransmitterBean.getMessageId());
			}
		}

		//TODO - make following more efficient
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
				tempSequenceId, Constants.SequenceProperties.LAST_OUT_MESSAGE);
		if (lastOutMsgBean != null) {
			Long lastOutMsgNoLng = (Long) lastOutMsgBean.getValue();
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
						tempSequenceId);
			}

			//stopping the progress of the message further.
			rmMsgCtx.getMessageContext().setPausedTrue(
					new QName(Constants.IN_HANDLER_NAME));

			//return;
			//}
		}

		int i = 1;

	}

	private RetransmitterBean getRetransmitterEntry(Collection collection,
			long msgNo) {
		Iterator it = collection.iterator();
		while (it.hasNext()) {
			RetransmitterBean bean = (RetransmitterBean) it.next();
			if (bean.getMessageNumber() == msgNo)
				return bean;
		}

		return null;
	}

	public void addTerminateSequenceMessage(RMMsgContext incomingAckRMMsg,
			String outSequenceId, String tempSequenceId)
			throws SandeshaException {
		RMMsgContext terminateRMMessage = RMMsgCreator
				.createTerminateSequenceMessage(incomingAckRMMsg, outSequenceId);

		//detting addressing headers.
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(incomingAckRMMsg.getMessageContext()
						.getSystemContext());
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		//SequencePropertyBean replyToBean =
		// seqPropMgr.retrieve(tempSequenceId,Constants.SequenceProperties.REPLY_TO_EPR);
		SequencePropertyBean toBean = seqPropMgr.retrieve(tempSequenceId,
				Constants.SequenceProperties.TO_EPR);

		EndpointReference toEPR = (EndpointReference) toBean.getValue();
		if (toEPR == null)
			throw new SandeshaException("To EPR has an invalid value");

		terminateRMMessage.setTo(new EndpointReference(toEPR.getAddress()));
		terminateRMMessage.setFrom(new EndpointReference(
				Constants.WSA.NS_URI_ANONYMOUS));
		terminateRMMessage.setFaultTo(new EndpointReference(
				Constants.WSA.NS_URI_ANONYMOUS));
		//terminateRMMessage.setFrom(new EndpointReference
		// (replyToEPR.getAddress()));
		terminateRMMessage
				.setWSAAction(Constants.WSRM.Actions.TERMINATE_SEQUENCE);
		terminateRMMessage.setSOAPAction("\""
				+ Constants.WSRM.Actions.TERMINATE_SEQUENCE + "\"");

		try {
			terminateRMMessage.addSOAPEnvelope();
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		String key = SandeshaUtil.storeMessageContext(terminateRMMessage
				.getMessageContext());
		RetransmitterBean terminateBean = new RetransmitterBean();
		terminateBean.setKey(key);

		//Set a retransmitter lastSentTime so that terminate will be send with
		// some delay.
		//Otherwise this get send before return of the current request (ack).
		//TODO verify that the time given is correct
		terminateBean.setLastSentTime(System.currentTimeMillis()
				+ Constants.TERMINATE_DELAY);

		terminateBean.setMessageId(terminateRMMessage.getMessageId());
		terminateBean.setSend(true);
		terminateBean.setReSend(false);

		RetransmitterBeanMgr retramsmitterMgr = storageManager
				.getRetransmitterBeanMgr();
		retramsmitterMgr.insert(terminateBean);

	}

}