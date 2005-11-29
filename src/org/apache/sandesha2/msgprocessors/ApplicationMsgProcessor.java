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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.InvokerBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.LastMessage;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.wsdl.WSDLConstants;

/**
 * Responsible for processing an incoming Application message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class ApplicationMsgProcessor implements MsgProcessor {

	private boolean letInvoke = false;

	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		//Processing for ack if any
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck != null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor();
			ackProcessor.processMessage(rmMsgCtx);
		}

		//Processing the application message.
		MessageContext msgCtx = rmMsgCtx.getMessageContext();
		if (msgCtx == null)
			throw new SandeshaException("Message context is null");

		if (rmMsgCtx.getProperty(Constants.APPLICATION_PROCESSING_DONE) != null
				&& rmMsgCtx.getProperty(Constants.APPLICATION_PROCESSING_DONE)
						.equals("true")) {
			return;
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(rmMsgCtx.getMessageContext()
						.getSystemContext());
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		//setting acked msg no range
		Sequence sequence = (Sequence) rmMsgCtx
				.getMessagePart(Constants.MessageParts.SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		ConfigurationContext configCtx = rmMsgCtx.getMessageContext()
				.getSystemContext();
		if (configCtx == null)
			throw new SandeshaException("Configuration Context is null");

		SequencePropertyBean msgsBean = seqPropMgr.retrieve(sequenceId,
				Constants.SequenceProperties.RECEIVED_MESSAGES);

		long msgNo = sequence.getMessageNumber().getMessageNumber();
		if (msgNo == 0)
			throw new SandeshaException("Wrong message number");

		String messagesStr = (String) msgsBean.getValue();

		if (msgNoPresentInList(messagesStr, msgNo)
				&& (Constants.QOS.InvocationType.DEFAULT_INVOCATION_TYPE == Constants.QOS.InvocationType.EXACTLY_ONCE)) {
			//this is a duplicate message and the invocation type is
			// EXACTLY_ONCE.

			msgCtx.setPausedTrue(new QName(Constants.IN_HANDLER_NAME));

		}

		if (messagesStr != "" && messagesStr != null)
			messagesStr = messagesStr + "," + Long.toString(msgNo);
		else
			messagesStr = Long.toString(msgNo);

		msgsBean.setValue(messagesStr);
		seqPropMgr.update(msgsBean);

		sendAckIfNeeded(rmMsgCtx, messagesStr);

		//	Pause the messages bean if not the right message to invoke.
		NextMsgBeanMgr mgr = storageManager.getNextMsgBeanMgr();
		NextMsgBean bean = mgr.retrieve(sequenceId);

		if (bean == null)
			throw new SandeshaException("Error- The sequence does not exist");

		InvokerBeanMgr storageMapMgr = storageManager.getStorageMapBeanMgr();

		long nextMsgno = bean.getNextMsgNoToProcess();

		if (msgCtx.isServerSide()) {
			boolean inOrderInvocation = PropertyManager.getInstance().isInOrderInvocation();
			if (inOrderInvocation) {
				//pause the message
				msgCtx.setPausedTrue(new QName(Constants.IN_HANDLER_NAME));

				SequencePropertyBean incomingSequenceListBean = (SequencePropertyBean) seqPropMgr
						.retrieve(
								Constants.SequenceProperties.ALL_SEQUENCES,
								Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);

				if (incomingSequenceListBean == null) {
					ArrayList incomingSequenceList = new ArrayList();
					incomingSequenceListBean = new SequencePropertyBean();
					incomingSequenceListBean
							.setSequenceId(Constants.SequenceProperties.ALL_SEQUENCES);
					incomingSequenceListBean
							.setName(Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);
					incomingSequenceListBean.setValue(incomingSequenceList);

					seqPropMgr.insert(incomingSequenceListBean);
				}

				ArrayList incomingSequenceList = (ArrayList) incomingSequenceListBean
						.getValue();

				//Adding current sequence to the incoming sequence List.
				if (!incomingSequenceList.contains(sequenceId)) {
					incomingSequenceList.add(sequenceId);
				}

				//saving the message.
				try {
					String key = SandeshaUtil.storeMessageContext(rmMsgCtx
							.getMessageContext());
					storageMapMgr.insert(new InvokerBean(key, msgNo,
							sequenceId));

					//This will avoid performing application processing more
					// than
					// once.
					rmMsgCtx.setProperty(Constants.APPLICATION_PROCESSING_DONE,
							"true");

				} catch (Exception ex) {
					throw new SandeshaException(ex.getMessage());
				}

				//Starting the invoker if stopped.
				SandeshaUtil.startInvokerIfStopped(msgCtx.getSystemContext());

			}
		}

		try {
			MessageContext requestMessage = rmMsgCtx.getMessageContext()
					.getOperationContext().getMessageContext(
							WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			String requestMessageId = requestMessage.getMessageID();
			SequencePropertyBean checkResponseBean = seqPropMgr.retrieve(
					requestMessageId,
					Constants.SequenceProperties.CHECK_RESPONSE);
			if (checkResponseBean != null) {
				checkResponseBean.setValue(msgCtx);
				seqPropMgr.update(checkResponseBean);
			}

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}
	}

	//TODO convert following from INT to LONG
	private boolean msgNoPresentInList(String list, long no) {
		String[] msgStrs = list.split(",");

		int l = msgStrs.length;

		for (int i = 0; i < l; i++) {
			if (msgStrs[i].equals(Long.toString(no)))
				return true;
		}

		return false;
	}

	public void sendAckIfNeeded(RMMsgContext rmMsgCtx, String messagesStr)
			throws SandeshaException {

		MessageContext msgCtx = rmMsgCtx.getMessageContext();

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(msgCtx.getEnvelope()));
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(msgCtx.getSystemContext());
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		Sequence sequence = (Sequence) rmMsgCtx
				.getMessagePart(Constants.MessageParts.SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		ConfigurationContext configCtx = rmMsgCtx.getMessageContext()
				.getSystemContext();
		if (configCtx == null)
			throw new SandeshaException("Configuration Context is null");

		AckRequested ackRequested = (AckRequested) rmMsgCtx
				.getMessagePart(Constants.MessageParts.ACK_REQUEST);
		LastMessage lastMessage = (LastMessage) sequence.getLastMessage();

		//Setting the ack depending on AcksTo.
		SequencePropertyBean acksToBean = seqPropMgr.retrieve(sequenceId,
				Constants.SequenceProperties.ACKS_TO_EPR);

		EndpointReference acksTo = (EndpointReference) acksToBean.getValue();
		String acksToStr = acksTo.getAddress();

		if (acksToStr == null || messagesStr == null)
			throw new SandeshaException(
					"Seqeunce properties are not set correctly");

		if (Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo.getAddress())) {
			// send ack in the sync case, only if the last message or the
			// ackRequested tag is present.
			boolean ackRequired = false;
			if (ackRequested != null || lastMessage != null)
				ackRequired = true;

			if (!ackRequired) {
				return;
			}
		}
		AxisOperation ackOperation = null;

		try {
			ackOperation = AxisOperationFactory
					.getOperetionDescription(AxisOperationFactory.MEP_URI_IN_ONLY);
		} catch (AxisFault e) {
			throw new SandeshaException("Could not create the Operation");
		}

		AxisOperation rmMsgOperation = rmMsgCtx.getMessageContext()
				.getAxisOperation();
		if (rmMsgOperation != null) {
			ArrayList outFlow = rmMsgOperation.getPhasesOutFlow();
			if (outFlow != null) {
				ackOperation.setPhasesOutFlow(outFlow);
				ackOperation.setPhasesOutFaultFlow(outFlow);
			}
		}

		MessageContext ackMsgCtx = SandeshaUtil.createNewRelatedMessageContext(
				rmMsgCtx, ackOperation);
		RMMsgContext ackRMMsgCtx = MsgInitializer.initializeMessage(ackMsgCtx);

		ackMsgCtx.setMessageID(SandeshaUtil.getUUID());

		//Setting new envelope
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		try {
			ackMsgCtx.setEnvelope(envelope);
		} catch (AxisFault e3) {
			throw new SandeshaException(e3.getMessage());
		}

		ackMsgCtx.setTo(acksTo);
		ackMsgCtx.setReplyTo(msgCtx.getTo());
		RMMsgCreator.addAckMessage(ackRMMsgCtx, sequenceId);

		if (Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo.getAddress())) {

			AxisEngine engine = new AxisEngine(ackRMMsgCtx.getMessageContext()
					.getSystemContext());

			//setting CONTEXT_WRITTEN since acksto is anonymous
			if (rmMsgCtx.getMessageContext().getOperationContext() == null) {
				//operation context will be null when doing in a GLOBAL
				// handler.
				try {
					AxisOperation op = AxisOperationFactory
							.getAxisOperation(AxisOperationFactory.MEP_CONSTANT_IN_OUT);
					OperationContext opCtx = new OperationContext(op);
					rmMsgCtx.getMessageContext().setAxisOperation(op);
					rmMsgCtx.getMessageContext().setOperationContext(opCtx);
				} catch (AxisFault e2) {
					throw new SandeshaException(e2.getMessage());
				}
			}

			rmMsgCtx.getMessageContext().getOperationContext().setProperty(
					org.apache.axis2.Constants.RESPONSE_WRITTEN, "true");

			rmMsgCtx.getMessageContext().setProperty(Constants.ACK_WRITTEN,
					"true");
			try {
				engine.send(ackRMMsgCtx.getMessageContext());
			} catch (AxisFault e1) {
				throw new SandeshaException(e1.getMessage());
			}
		} else {

			SenderBeanMgr retransmitterBeanMgr = storageManager
					.getRetransmitterBeanMgr();

			String key = SandeshaUtil.storeMessageContext(ackMsgCtx);
			SenderBean ackBean = new SenderBean();
			ackBean.setKey(key);
			ackBean.setMessageId(ackMsgCtx.getMessageID());
			ackBean.setReSend(false);
			ackBean.setSend(true);
			ackBean.setMessagetype(Constants.MessageTypes.ACK);

			//the internalSequenceId value of the retransmitter Table for the
			// messages related to an incoming
			//sequence is the actual sequence ID
			ackBean.setInternalSequenceId(sequenceId);

			RMPolicyBean policyBean = (RMPolicyBean) rmMsgCtx
					.getProperty(Constants.WSP.RM_POLICY_BEAN);
			long ackInterval = PropertyManager.getInstance().getAcknowledgementInterval();
			if (policyBean != null) {
				ackInterval = policyBean.getAcknowledgementInaterval();
			}

			//Ack will be sent as stand alone, only after the retransmitter
			// interval.
			long timeToSend = System.currentTimeMillis() + ackInterval;
			ackBean.setTimeToSend(timeToSend);

			//removing old acks.
			SenderBean findBean = new SenderBean();
			findBean.setMessagetype(Constants.MessageTypes.ACK);
			findBean.setInternalSequenceId(sequenceId);
			Collection coll = retransmitterBeanMgr.find(findBean);
			Iterator it = coll.iterator();
			while (it.hasNext()) {
				SenderBean retransmitterBean = (SenderBean) it
						.next();
				retransmitterBeanMgr.delete(retransmitterBean.getMessageId());
			}

			//inserting the new ack.
			retransmitterBeanMgr.insert(ackBean);

			SandeshaUtil.startSenderIfStopped(configCtx);
		}

	}
}