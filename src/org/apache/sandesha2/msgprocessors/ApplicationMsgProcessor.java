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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.InvokerBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.transport.Sandesha2TransportOutDesc;
import org.apache.sandesha2.transport.Sandesha2TransportSender;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaPropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.LastMessage;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Responsible for processing an incoming Application message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class ApplicationMsgProcessor implements MsgProcessor {

	private boolean letInvoke = false;

	private Log log = LogFactory.getLog(getClass());
	
	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		//Processing for ack if any
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck != null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor();
			ackProcessor.processMessage(rmMsgCtx);
		}

		//Processing the application message.
		MessageContext msgCtx = rmMsgCtx.getMessageContext();
		if (msgCtx == null) {
			String message = "Message context is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		if (rmMsgCtx
				.getProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE) != null
				&& rmMsgCtx.getProperty(
						Sandesha2Constants.APPLICATION_PROCESSING_DONE).equals(
						"true")) {
			return;
		}

		//RM will not rend sync responses. If sync acks are there this will be
		// made true again later.
		if (rmMsgCtx.getMessageContext().getOperationContext() != null) {
			rmMsgCtx.getMessageContext().getOperationContext().setProperty(
					Constants.RESPONSE_WRITTEN, Constants.VALUE_FALSE);
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(rmMsgCtx.getMessageContext()
						.getConfigurationContext());



		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		//setting acked msg no range
		Sequence sequence = (Sequence) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		ConfigurationContext configCtx = rmMsgCtx.getMessageContext()
				.getConfigurationContext();
		if (configCtx == null) {
			String message = "Configuration Context is null";
			log.debug(message);
			throw new SandeshaException(message);
		}


		Transaction lastUpdatedTimeTransaction = storageManager.getTransaction();
		//updating the last activated time of the sequence.
		SequenceManager.updateLastActivatedTime(sequenceId,configCtx);
		lastUpdatedTimeTransaction.commit();
		
		Transaction updataMsgStringTransaction = storageManager
				.getTransaction();
		
		SequencePropertyBean msgsBean = seqPropMgr.retrieve(sequenceId,
				Sandesha2Constants.SequenceProperties.COMPLETED_MESSAGES);

		long msgNo = sequence.getMessageNumber().getMessageNumber();
		if (msgNo == 0) {
			String message = "Wrong message number";
			log.debug(message);
			throw new SandeshaException(message);
		}
		
		String messagesStr = (String) msgsBean.getValue();

		if (msgNoPresentInList(messagesStr, msgNo)
				&& (Sandesha2Constants.QOS.InvocationType.DEFAULT_INVOCATION_TYPE == Sandesha2Constants.QOS.InvocationType.EXACTLY_ONCE)) {
			//this is a duplicate message and the invocation type is
			// EXACTLY_ONCE.

			rmMsgCtx.pause();

		}

		if (messagesStr != "" && messagesStr != null)
			messagesStr = messagesStr + "," + Long.toString(msgNo);
		else
			messagesStr = Long.toString(msgNo);

		msgsBean.setValue(messagesStr);
		seqPropMgr.update(msgsBean);

		updataMsgStringTransaction.commit();



		Transaction invokeTransaction = storageManager.getTransaction();

		//	Pause the messages bean if not the right message to invoke.
		NextMsgBeanMgr mgr = storageManager.getNextMsgBeanMgr();
		NextMsgBean bean = mgr.retrieve(sequenceId);

		if (bean == null)
			throw new SandeshaException("Error- The sequence does not exist");

		InvokerBeanMgr storageMapMgr = storageManager.getStorageMapBeanMgr();

	//	long nextMsgno = bean.getNextMsgNoToProcess();

//		boolean inOrderInvocation = PropertyManager.getInstance()
//				.isInOrderInvocation();
//		
		//TODO currently this is an module-level property. Make this service specific.
//		SandeshaPropertyBean propertyBean = (SandeshaPropertyBean) msgCtx.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN).getValue();
//		boolean inOrderInvocation = propertyBean.isInOrder();
		boolean inOrderInvocation = PropertyManager.getInstance().isInOrderInvocation();
		
		
		if (inOrderInvocation) {
			
			//pause the message
			rmMsgCtx.pause();
//			rmMsgCtx.getMessageContext().setPausedTrue(
//					new QName(Sandesha2Constants.IN_HANDLER_NAME));
			
			SequencePropertyBean incomingSequenceListBean = (SequencePropertyBean) seqPropMgr
					.retrieve(
							Sandesha2Constants.SequenceProperties.ALL_SEQUENCES,
							Sandesha2Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);

			if (incomingSequenceListBean == null) {
				ArrayList incomingSequenceList = new ArrayList();
				incomingSequenceListBean = new SequencePropertyBean();
				incomingSequenceListBean
						.setSequenceID(Sandesha2Constants.SequenceProperties.ALL_SEQUENCES);
				incomingSequenceListBean
						.setName(Sandesha2Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);
				incomingSequenceListBean.setValue(incomingSequenceList
						.toString());

				seqPropMgr.insert(incomingSequenceListBean);
			}

			ArrayList incomingSequenceList = SandeshaUtil
					.getArrayListFromString(incomingSequenceListBean.getValue());

			//Adding current sequence to the incoming sequence List.
			if (!incomingSequenceList.contains(sequenceId)) {
				incomingSequenceList.add(sequenceId);

				//saving the property.
				incomingSequenceListBean.setValue(incomingSequenceList
						.toString());
				seqPropMgr.insert(incomingSequenceListBean);
			}

			//saving the message.
			try {
				String key = SandeshaUtil.getUUID();
				storageManager.storeMessageContext(key,rmMsgCtx
						.getMessageContext());
				storageMapMgr.insert(new InvokerBean(key, msgNo, sequenceId));

				//This will avoid performing application processing more
				// than
				// once.
				rmMsgCtx.setProperty(
						Sandesha2Constants.APPLICATION_PROCESSING_DONE, "true");

			} catch (Exception ex) {
				throw new SandeshaException(ex.getMessage());
			}

			//Starting the invoker if stopped.
			SandeshaUtil
					.startInvokerForTheSequence(msgCtx.getConfigurationContext(),sequenceId);

		}

		invokeTransaction.commit();

		//Sending acknowledgements
		sendAckIfNeeded(rmMsgCtx, messagesStr);

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
				.getSandeshaStorageManager(msgCtx.getConfigurationContext());
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		Sequence sequence = (Sequence) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		ConfigurationContext configCtx = rmMsgCtx.getMessageContext()
				.getConfigurationContext();
		if (configCtx == null)
			throw new SandeshaException("Configuration Context is null");

		AckRequested ackRequested = (AckRequested) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST);
		LastMessage lastMessage = (LastMessage) sequence.getLastMessage();

		//Setting the ack depending on AcksTo.
		SequencePropertyBean acksToBean = seqPropMgr.retrieve(sequenceId,
				Sandesha2Constants.SequenceProperties.ACKS_TO_EPR);

		EndpointReference acksTo = new EndpointReference(acksToBean.getValue());
		String acksToStr = acksTo.getAddress();

		if (acksToStr == null || messagesStr == null)
			throw new SandeshaException(
					"Seqeunce properties are not set correctly");

		if (Sandesha2Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo.getAddress())) {
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
			ackOperation = AxisOperationFactory.getOperationDescription(AxisOperationFactory.MEP_URI_IN_ONLY);
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

		ackMsgCtx.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,
				"true");

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

		if (Sandesha2Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo.getAddress())) {

			AxisEngine engine = new AxisEngine(ackRMMsgCtx.getMessageContext()
					.getConfigurationContext());

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
					org.apache.axis2.Constants.RESPONSE_WRITTEN,
					Constants.VALUE_TRUE);

			rmMsgCtx.getMessageContext().setProperty(
					Sandesha2Constants.ACK_WRITTEN, "true");
			try {
				engine.send(ackRMMsgCtx.getMessageContext());
			} catch (AxisFault e1) {
				throw new SandeshaException(e1.getMessage());
			}
		} else {

			Transaction asyncAckTransaction = storageManager.getTransaction();

			SenderBeanMgr retransmitterBeanMgr = storageManager
					.getRetransmitterBeanMgr();

			String key = SandeshaUtil.getUUID();
			
			//dumping to the storage will be done be Sandesha2 Transport Sender
			//storageManager.storeMessageContext(key,ackMsgCtx);
			
			SenderBean ackBean = new SenderBean();
			ackBean.setMessageContextRefKey(key);
			ackBean.setMessageID(ackMsgCtx.getMessageID());
			ackBean.setReSend(false);
			
			//this will be set to true in the sender.
			ackBean.setSend(true);
			
			ackMsgCtx.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,
					Sandesha2Constants.VALUE_FALSE);
			
			ackBean.setMessageType(Sandesha2Constants.MessageTypes.ACK);
			
			//the internalSequenceId value of the retransmitter Table for the
			// messages related to an incoming
			//sequence is the actual sequence ID

//			RMPolicyBean policyBean = (RMPolicyBean) rmMsgCtx
//					.getProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN);
		
//			long ackInterval = PropertyManager.getInstance()
//					.getAcknowledgementInterval();
			
			Parameter param = msgCtx.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
			
			SandeshaPropertyBean propertyBean = null;
			if (param!=null) {
				propertyBean = (SandeshaPropertyBean)  param.getValue();
			}else {
				propertyBean = PropertyManager.getInstance().getPropertyBean();
			}
			
			
			long ackInterval = propertyBean.getAcknowledgementInaterval();
			
			//			if (policyBean != null) {
//				ackInterval = policyBean.getAcknowledgementInaterval();
//			}
			
			//Ack will be sent as stand alone, only after the retransmitter
			// interval.
			long timeToSend = System.currentTimeMillis() + ackInterval;

			//removing old acks.
			SenderBean findBean = new SenderBean();
			findBean.setMessageType(Sandesha2Constants.MessageTypes.ACK);
			
			//this will be set to true in the sandesha2TransportSender.
			findBean.setSend(true);
			findBean.setReSend(false);
			Collection coll = retransmitterBeanMgr.find(findBean);
			Iterator it = coll.iterator();

			if (it.hasNext()) {
				SenderBean oldAckBean = (SenderBean) it.next();
				timeToSend = oldAckBean.getTimeToSend();		//If there is an old ack. This ack will be sent in the old timeToSend.
				retransmitterBeanMgr.delete(oldAckBean.getMessageID());
			}
			
			ackBean.setTimeToSend(timeToSend);

			storageManager.storeMessageContext(key,ackMsgCtx);
			
			//inserting the new ack.
			retransmitterBeanMgr.insert(ackBean);

			asyncAckTransaction.commit();

			//passing the message through sandesha2sender

			ackMsgCtx.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_OUT_DESC,ackMsgCtx.getTransportOut());
			ackMsgCtx.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,Sandesha2Constants.VALUE_TRUE);
			
			ackMsgCtx.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY,key);
			
			ackMsgCtx.setTransportOut(new Sandesha2TransportOutDesc ());
			
			AxisEngine engine = new AxisEngine (configCtx);
			try {
				engine.send(ackMsgCtx);
			} catch (AxisFault e) {
				throw new SandeshaException (e.getMessage());
			}
			
			SandeshaUtil.startSenderForTheSequence(configCtx,sequenceId);
		}

	}
}