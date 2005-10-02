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
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.LastMessage;
import org.apache.sandesha2.wsrm.Nack;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

public class AcknowledgementProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {
		System.out.println("WITHIN ACKNOWLEDGEMENT PROCESSOR");

		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck == null)
			throw new SandeshaException("Sequence acknowledgement part is null");

		AbstractContext context = rmMsgCtx.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		Iterator ackRangeIterator = sequenceAck.getAcknowledgementRanges()
				.iterator();
		Iterator nackIterator = sequenceAck.getNackList().iterator();
		String outSequenceId = sequenceAck.getIdentifier().getIdentifier();
		if (outSequenceId == null || "".equals(outSequenceId))
			throw new SandeshaException("OutSequenceId is null");

		RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
				.getInstance(context).getRetransmitterBeanMgr();
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(context).getSequencePropretyBeanMgr();

		//getting IncomingSequenceId for the outSequenceId
		SequencePropertyBean incomingSequenceBean = seqPropMgr.retrieve(
				outSequenceId,
				Constants.SequenceProperties.INCOMING_SEQUENCE_ID);
		if (incomingSequenceBean == null
				|| incomingSequenceBean.getValue() == null)
			throw new SandeshaException(
					"Incoming Sequence id is not set correctly");

		String incomingSequenceId = (String) incomingSequenceBean.getValue();

		//getting TempSequenceId for the IncomingSequenceId
		SequencePropertyBean tempSequenceBean = seqPropMgr.retrieve(
				incomingSequenceId,
				Constants.SequenceProperties.TEMP_SEQUENCE_ID);
		if (tempSequenceBean == null || tempSequenceBean.getValue() == null)
			throw new SandeshaException(
					"Incoming Sequence id is not set correctly");

		String tempSequenceId = (String) tempSequenceBean.getValue();

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

		//If all messages up to last message have been acknowledged.
		//Add terminate Sequence message.
		SequencePropertyBean lastOutMsgBean = seqPropMgr.retrieve(
				incomingSequenceId,
				Constants.SequenceProperties.LAST_OUT_MESSAGE);
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
				addTerminateSequenceMessage(rmMsgCtx, outSequenceId,incomingSequenceId);
			}
		}

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
			String outSequenceId,String incomingSequenceId) throws SandeshaException {
		RMMsgContext terminateRMMessage = RMMsgCreator
				.createTerminateSequenceMessage(incomingAckRMMsg, outSequenceId);

		//detting addressing headers.
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory.getInstance(
				incomingAckRMMsg.getContext()).getSequencePropretyBeanMgr();
		SequencePropertyBean replyToBean = seqPropMgr.retrieve(incomingSequenceId,Constants.SequenceProperties.REPLY_TO_EPR);
		if (replyToBean==null)
			throw new SandeshaException ("ReplyTo property is not set");
		
		EndpointReference replyToEPR = (EndpointReference) replyToBean.getValue();
		if (replyToEPR==null)
			throw new SandeshaException ("ReplyTo EPR has an invalid value");
		 
		terminateRMMessage.setTo(new EndpointReference (replyToEPR.getAddress()));
		terminateRMMessage.setFrom(incomingAckRMMsg.getTo());
		terminateRMMessage
				.setWSAAction(Constants.WSRM.Actions.TERMINATE_SEQUENCE);

		try {
			terminateRMMessage.addSOAPEnvelope();
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		String key = SandeshaUtil.storeMessageContext(terminateRMMessage
				.getMessageContext());
		RetransmitterBean terminateBean = new RetransmitterBean();
		terminateBean.setKey(key);
		terminateBean.setLastSentTime(0);
		terminateBean.setMessageId(terminateRMMessage.getMessageId());
		terminateBean.setSend(true);

		RetransmitterBeanMgr retramsmitterMgr = AbstractBeanMgrFactory.getInstance(
				incomingAckRMMsg.getContext()).getRetransmitterBeanMgr();
		retramsmitterMgr.insert(terminateBean);
		
		
		try {
			System.out.println("SERIALIZING TERMINATE MSG");
			SOAPEnvelope envel = terminateRMMessage.getSOAPEnvelope();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			envel.serialize(writer);
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}