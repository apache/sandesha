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
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.SpecSpecificConstants;
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
import org.apache.sandesha2.util.FaultManager;
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
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;

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
		
		//TODO process embedded ack requests
		

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

		//RM will not send sync responses. If sync acks are there this will be
		// made true again later.
		if (rmMsgCtx.getMessageContext().getOperationContext() != null) {
			rmMsgCtx.getMessageContext().getOperationContext().setProperty(
					Constants.RESPONSE_WRITTEN, Constants.VALUE_FALSE);
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(rmMsgCtx.getMessageContext()
						.getConfigurationContext());



		FaultManager faultManager = new FaultManager();
		RMMsgContext faultMessageContext = faultManager.checkForLastMsgNumberExceeded(rmMsgCtx);
		if (faultMessageContext != null) {
			ConfigurationContext configurationContext = msgCtx.getConfigurationContext();
			AxisEngine engine = new AxisEngine(configurationContext);
			
			try {
				engine.sendFault(faultMessageContext.getMessageContext());
			} catch (AxisFault e) {
				throw new SandeshaException ("Could not send the fault message",e);
			}
			
			return;
		}
		
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

		faultMessageContext = faultManager.checkForUnknownSequence(rmMsgCtx,sequenceId);
		if (faultMessageContext != null) {
			ConfigurationContext configurationContext = msgCtx.getConfigurationContext();
			AxisEngine engine = new AxisEngine(configurationContext);
			
			try {
				engine.send(faultMessageContext.getMessageContext());
			} catch (AxisFault e) {
				throw new SandeshaException ("Could not send the fault message",e);
			}
			
			return;
		}
		
		
		//setting mustUnderstand to false.
		sequence.setMustUnderstand(false);
		rmMsgCtx.addSOAPEnvelope();
		
		
		//throwing a fault if the sequence is closed.
		faultMessageContext = faultManager. checkForSequenceClosed(rmMsgCtx,sequenceId);
		if (faultMessageContext != null) {
			ConfigurationContext configurationContext = msgCtx.getConfigurationContext();
			AxisEngine engine = new AxisEngine(configurationContext);
			
			try {
				engine.sendFault(faultMessageContext.getMessageContext());
			} catch (AxisFault e) {
				throw new SandeshaException ("Could not send the fault message",e);
			}
			
			return;
		}

			
		
		Transaction lastUpdatedTimeTransaction = storageManager.getTransaction();
		
		//updating the last activated time of the sequence.
		SequenceManager.updateLastActivatedTime(sequenceId,configCtx);
		lastUpdatedTimeTransaction.commit();
		
		Transaction updataMsgStringTransaction = storageManager
				.getTransaction();
		
		SequencePropertyBean msgsBean = seqPropMgr.retrieve(sequenceId,
				Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);

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
		
		if (ackRequested!=null) {
			//setting mustundestand=false for the ackRequested header block.
			ackRequested.setMustUnderstand(false);
			rmMsgCtx.addSOAPEnvelope();
		}
		
		LastMessage lastMessage = (LastMessage) sequence.getLastMessage();

		if (lastMessage!=null) {
			long messageNumber = sequence.getMessageNumber().getMessageNumber();
			SequencePropertyBean lastMessageBean = new SequencePropertyBean ();
			lastMessageBean.setSequenceID(sequenceId);
			lastMessageBean.setName(Sandesha2Constants.SequenceProperties.LAST_MESSAGE);
			lastMessageBean.setValue(new Long(messageNumber).toString());
			
			seqPropMgr.insert(lastMessageBean);
		}
		
	 	RMMsgContext ackRMMessage = AcknowledgementManager.generateAckMessage(rmMsgCtx,sequenceId);
		
	 	AxisEngine engine = new AxisEngine (configCtx);
	 	
	 	try {
			engine.send(ackRMMessage.getMessageContext());
		} catch (AxisFault e) {
			String message = "Exception thrown while trying to send the ack message";
			throw new SandeshaException (message,e);
		}
	}
}