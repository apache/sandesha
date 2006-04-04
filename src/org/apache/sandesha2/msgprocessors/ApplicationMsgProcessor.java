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
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.OperationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.SpecSpecificConstants;
import org.apache.sandesha2.client.RMFaultCallback;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
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
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.LastMessage;
import org.apache.sandesha2.wsrm.MessageNumber;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.wsdl.WSDLConstants;

/**
 * Responsible for processing an incoming Application message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class ApplicationMsgProcessor implements MsgProcessor {

	private boolean letInvoke = false;

	private Log log = LogFactory.getLog(getClass());
	
	public void processInMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		//Processing for ack if any
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck != null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor();
			ackProcessor.processInMessage(rmMsgCtx);
		}
		
		//TODO process embedded ack requests
		AckRequested ackRequested = (AckRequested) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST);
		if (ackRequested!=null) {
			ackRequested.setMustUnderstand(false);
			rmMsgCtx.addSOAPEnvelope();
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
		
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
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
		
		String key = SandeshaUtil.getUUID();  //key to store the message.
		
		//updating the Highest_In_Msg_No property which gives the highest message number retrieved from this sequence.
		String highetsInMsgNoStr = SandeshaUtil.getSequenceProperty(sequenceId,Sandesha2Constants.SequenceProperties.HIGHEST_IN_MSG_NUMBER,configCtx);
		String highetsInMsgKey = SandeshaUtil.getSequenceProperty(sequenceId,Sandesha2Constants.SequenceProperties.HIGHEST_IN_MSG_KEY,configCtx);
		
		long highestInMsgNo=0;
		if (highetsInMsgNoStr!=null) {
			highestInMsgNo = Long.parseLong(highetsInMsgNoStr);
		}
		
		if (msgNo>highestInMsgNo) {
			highestInMsgNo = msgNo;
			
			String str = new Long(msgNo).toString();
			SequencePropertyBean highestMsgNoBean = new SequencePropertyBean (sequenceId,Sandesha2Constants.SequenceProperties.HIGHEST_IN_MSG_NUMBER,str);
			SequencePropertyBean highestMsgKeyBean = new SequencePropertyBean (sequenceId,Sandesha2Constants.SequenceProperties.HIGHEST_IN_MSG_KEY,key);
			
			if (highetsInMsgNoStr!=null) {
				seqPropMgr.update(highestMsgNoBean);
				seqPropMgr.update(highestMsgKeyBean);
			}else{
				seqPropMgr.insert(highestMsgNoBean);
				seqPropMgr.insert(highestMsgKeyBean);
			}
		}
		
		String messagesStr = "";
		if (msgsBean!=null) 
			messagesStr = (String) msgsBean.getValue();
		else {
			msgsBean = new SequencePropertyBean ();
			msgsBean.setSequenceID(sequenceId);
			msgsBean.setName(Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);
			msgsBean.setValue(messagesStr);
		}
			

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

//		if (lastMessage!=null) {
//			long messageNumber = sequence.getMessageNumber().getMessageNumber();
//			SequencePropertyBean lastMessageBean = new SequencePropertyBean ();
//			lastMessageBean.setSequenceID(sequenceId);
//			lastMessageBean.setName(Sandesha2Constants.SequenceProperties.LAST_MESSAGE);
//			lastMessageBean.setValue(new Long(messageNumber).toString());
//			
//			seqPropMgr.insert(lastMessageBean);
//		}
		
	 	RMMsgContext ackRMMessage = AcknowledgementManager.generateAckMessage(rmMsgCtx,sequenceId);
		
	 	AxisEngine engine = new AxisEngine (configCtx);
	 	
	 	try {
			engine.send(ackRMMessage.getMessageContext());
		} catch (AxisFault e) {
			String message = "Exception thrown while trying to send the ack message";
			throw new SandeshaException (message,e);
		}
	}
	
	public void processOutMessage(RMMsgContext rmMsgCtx) throws SandeshaException {
				
		
		MessageContext msgContext = rmMsgCtx.getMessageContext();
		ConfigurationContext configContext = msgContext .getConfigurationContext();
		
		//setting the Fault callback		
		RMFaultCallback faultCallback = (RMFaultCallback) msgContext.getOptions().getProperty(Sandesha2ClientAPI.RM_FAULT_CALLBACK);
		if (faultCallback!=null) {
			OperationContext operationContext = msgContext.getOperationContext();
			if (operationContext!=null) {
				operationContext.setProperty(Sandesha2ClientAPI.RM_FAULT_CALLBACK,faultCallback);
			}
		}
		
		//retrieving the the storage manager
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		Parameter policyParam = msgContext.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
		if (policyParam == null) {
			SandeshaPropertyBean propertyBean = PropertyManager.getInstance().getPropertyBean();
			Parameter parameter = new Parameter();
			parameter.setName(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
			parameter.setValue(propertyBean);

			// TODO this should be addede to the AxisMessage
			try {
				if (msgContext.getAxisOperation() != null)
					msgContext.getAxisOperation().addParameter(parameter);
				else if (msgContext.getAxisService() != null)
					msgContext.getAxisService().addParameter(parameter);
			} catch (AxisFault e) {
				throw new SandeshaException (e);
			}
		}

		Transaction outHandlerTransaction = storageManager.getTransaction();
		boolean serverSide = msgContext.isServerSide();  

		// setting message Id if null
		if (msgContext.getMessageID() == null) 
			msgContext.setMessageID(SandeshaUtil.getUUID());

		// find internal sequence id
		String internalSequenceId = null;

		String storageKey = SandeshaUtil.getUUID();  //the key which will be used to store this message.
		
		/* Internal sequence id is the one used to refer to the sequence (since
		actual sequence id is not available when first msg arrives)
		server side - a derivation of the sequenceId of the incoming sequence
		client side - a derivation of wsaTo & SeequenceKey */

		boolean lastMessage = false;
		if (serverSide) {
			// getting the request message and rmMessage.
			MessageContext reqMsgCtx;
			try {
				reqMsgCtx = msgContext.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			} catch (AxisFault e) {
				throw new SandeshaException (e);
			}

			RMMsgContext requestRMMsgCtx = MsgInitializer.initializeMessage(reqMsgCtx);

			Sequence reqSequence = (Sequence) requestRMMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			if (reqSequence == null) {
				String message = "Sequence part is null";
				log.debug(message);
				throw new SandeshaException(message);
			}

			String incomingSeqId = reqSequence.getIdentifier().getIdentifier();
			if (incomingSeqId == null || incomingSeqId == "") {
				String message = "Invalid seqence Id";
				log.debug(message);
				throw new SandeshaException(message);
			}

			long requestMsgNo = reqSequence.getMessageNumber().getMessageNumber();
			
			internalSequenceId = SandeshaUtil.getInternalSequenceID(incomingSeqId);
			
			//deciding weather the last message.
			String requestLastMsgNoStr = SandeshaUtil.getSequenceProperty(incomingSeqId,Sandesha2Constants.SequenceProperties.LAST_IN_MESSAGE_NO,configContext);
			if (requestLastMsgNoStr!=null) {
				long requestLastMsgNo = Long.parseLong(requestLastMsgNoStr);
				if (requestLastMsgNo==requestMsgNo)
					lastMessage = true;
			}

		} else {
			// set the internal sequence id for the client side.
			EndpointReference toEPR = msgContext.getTo();
			if (toEPR == null || toEPR.getAddress() == null || "".equals(toEPR.getAddress())) {
				String message = "TO End Point Reference is not set correctly. This is a must for the sandesha client side.";
				log.debug(message);
				throw new SandeshaException(message);
			}

			String to = toEPR.getAddress();
			String sequenceKey = (String) msgContext.getProperty(Sandesha2ClientAPI.SEQUENCE_KEY);
			internalSequenceId = SandeshaUtil.getInternalSequenceID(to,sequenceKey);
			
			String lastAppMessage = (String) msgContext.getProperty(Sandesha2ClientAPI.LAST_MESSAGE);
			if (lastAppMessage!=null && "true".equals(lastAppMessage))
				lastMessage = true;
		}

		/* checking weather the user has given the messageNumber (most of the cases this will not be the case where
		   the system will generate the message numbers */

		//User should set it as a long object.
		Long messageNumberLng = (Long) msgContext.getProperty(Sandesha2ClientAPI.MESSAGE_NUMBER);
		
		long givenMessageNumber = -1;
		if (messageNumberLng!=null) {
			givenMessageNumber = messageNumberLng.longValue();
			if (givenMessageNumber<=0) {
				throw new SandeshaException ("The givem message number value is invalid (has to be larger than zero)");
			}
		}
		
		//the message number that was last used. 
		long systemMessageNumber = getPreviousMsgNo(configContext, internalSequenceId);
		
		//The number given by the user has to be larger than the last stored number.
		if (givenMessageNumber>0 && givenMessageNumber<=systemMessageNumber) {
			String message = "The given message number is not larger than value of the last sent message.";
			throw new SandeshaException (message);
		}
		
		//Finding the correct message number.
		long messageNumber = -1;
		if (givenMessageNumber>0)          // if given message number is valid use it. (this is larger than the last stored due to the last check)
			messageNumber = givenMessageNumber;
		else if (systemMessageNumber>0) {	//if system message number is valid use it.
			messageNumber = systemMessageNumber+1;
		} else {         //This is the fist message (systemMessageNumber = -1)
			messageNumber = 1;
		}
		
		//A dummy message is a one which will not be processed as a actual application message.
		//The RM handlers will simply let these go.
		String dummyMessageString = (String) msgContext.getOptions().getProperty(Sandesha2ClientAPI.DUMMY_MESSAGE);
		boolean dummyMessage = false;
		if (dummyMessageString!=null && Sandesha2ClientAPI.VALUE_TRUE.equals(dummyMessageString))
			dummyMessage = true;
		
		//saving the used message number
		if (!dummyMessage)
			setNextMsgNo(configContext,internalSequenceId,messageNumber);
		
		
		//set this as the response highest message.
		SequencePropertyBean responseHighestMsgBean = new SequencePropertyBean (
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.HIGHEST_OUT_MSG_NUMBER,
				new Long (messageNumber).toString()
		);
		seqPropMgr.insert(responseHighestMsgBean);
		
		if (lastMessage) {
	
			SequencePropertyBean responseHighestMsgKeyBean = new SequencePropertyBean (
					internalSequenceId,
					Sandesha2Constants.SequenceProperties.HIGHEST_OUT_MSG_KEY,
					storageKey
			);	
			
			SequencePropertyBean responseLastMsgKeyBean = new SequencePropertyBean (
					internalSequenceId,
					Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE_NO,
					new Long (messageNumber).toString()
			);	
			
			seqPropMgr.insert(responseHighestMsgKeyBean);
			seqPropMgr.insert(responseLastMsgKeyBean);
		}
		
		boolean sendCreateSequence = false;

		SequencePropertyBean outSeqBean = seqPropMgr.retrieve(
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);

		// setting async ack endpoint for the server side. (if present)
		if (serverSide) {
			String incomingSequenceID = SandeshaUtil
					.getServerSideIncomingSeqIdFromInternalSeqId(internalSequenceId);
			SequencePropertyBean incomingToBean = seqPropMgr.retrieve(
					incomingSequenceID,
					Sandesha2Constants.SequenceProperties.TO_EPR);
			if (incomingToBean != null) {
				String incomingTo = incomingToBean.getValue();
				msgContext.setProperty(Sandesha2ClientAPI.AcksTo, incomingTo);
			}
		}

		
		//FINDING THE SPEC VERSION
		String specVersion = null;
		if (msgContext.isServerSide()) {
			//in the server side, get the RM version from the request sequence.
			MessageContext requestMessageContext;
			try {
				requestMessageContext = msgContext.getOperationContext().getMessageContext(AxisOperationFactory.MESSAGE_LABEL_IN_VALUE);
			} catch (AxisFault e) {
				throw new SandeshaException (e);
			}
			
			if (requestMessageContext==null) 
				throw new SandeshaException ("Request message context is null, cant find out the request side sequenceID");
			
			RMMsgContext requestRMMsgCtx = MsgInitializer.initializeMessage(requestMessageContext);
			Sequence sequence = (Sequence) requestRMMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			
			String requestSequenceID = sequence.getIdentifier().getIdentifier();
			SequencePropertyBean specVersionBean = seqPropMgr.retrieve(requestSequenceID,Sandesha2Constants.SequenceProperties.RM_SPEC_VERSION);
			if (specVersionBean==null) 
				throw new SandeshaException ("SpecVersion sequence property bean is not available for the incoming sequence. Cant find the RM version for outgoing side");
			
			specVersion = specVersionBean.getValue();
		} else {
			//in the client side, user will set the RM version.
			specVersion = (String) msgContext.getProperty(Sandesha2ClientAPI.RM_SPEC_VERSION);
		}
		
		if (specVersion==null) 
			specVersion = SpecSpecificConstants.getDefaultSpecVersion();   //TODO change the default to WSRX.
		
		if (messageNumber == 1) {
			if (outSeqBean == null) { // out sequence will be set for the server side, in the case of an offer.
				sendCreateSequence = true;   // message number being one and not having an out sequence, implies that a create sequence has to be send.
			}

			// if fist message - setup the sending side sequence - both for the server and the client sides
			SequenceManager.setupNewClientSequence(msgContext, internalSequenceId,specVersion);
		}

		ServiceContext serviceContext = msgContext.getServiceContext();
		OperationContext operationContext = msgContext.getOperationContext();
		
		//SENDING THE CREATE SEQUENCE.
		if (sendCreateSequence) { 
			SequencePropertyBean responseCreateSeqAdded = seqPropMgr
					.retrieve(internalSequenceId,Sandesha2Constants.SequenceProperties.OUT_CREATE_SEQUENCE_SENT);

			String addressingNamespaceURI = SandeshaUtil.getSequenceProperty(internalSequenceId,Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE,configContext);
			String anonymousURI = SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespaceURI);
			
			if (responseCreateSeqAdded == null) {
				responseCreateSeqAdded = new SequencePropertyBean(
						internalSequenceId,Sandesha2Constants.SequenceProperties.OUT_CREATE_SEQUENCE_SENT,"true");
				seqPropMgr.insert(responseCreateSeqAdded);

				String acksTo = null;
				if (serviceContext != null)
					acksTo = (String) msgContext.getProperty(Sandesha2ClientAPI.AcksTo);

				if (msgContext.isServerSide()) {
					// we do not set acksTo value to anonymous when the create
					// sequence is send from the server.
					MessageContext requestMessage;
					try {
						requestMessage = operationContext.getMessageContext(OperationContextFactory.MESSAGE_LABEL_IN_VALUE);
					} catch (AxisFault e) {
						throw new SandeshaException (e);
					}
					
					if (requestMessage == null) {
						String message = "Request message is not present";
						log.debug(message);
						throw new SandeshaException(message);
					}
					acksTo = requestMessage.getTo().getAddress();

				} else {
					if (acksTo == null)
						acksTo = anonymousURI;
				}

				if (!anonymousURI.equals(acksTo) && !serverSide) {
					String transportIn = (String) configContext   //TODO verify
							.getProperty(MessageContext.TRANSPORT_IN);
					if (transportIn == null)
						transportIn = org.apache.axis2.Constants.TRANSPORT_HTTP;
				} else if (acksTo == null && serverSide) {
					String incomingSequencId = SandeshaUtil.getServerSideIncomingSeqIdFromInternalSeqId(internalSequenceId);
					SequencePropertyBean bean = seqPropMgr.retrieve(
							incomingSequencId,Sandesha2Constants.SequenceProperties.REPLY_TO_EPR);
					if (bean != null) {
						EndpointReference acksToEPR = new EndpointReference(bean.getValue());
						if (acksToEPR != null)
							acksTo = (String) acksToEPR.getAddress();
					}
				} else if (anonymousURI.equals(acksTo)) {
					// set transport in.
					Object trIn = msgContext.getProperty(MessageContext.TRANSPORT_IN);
					if (trIn == null) {
						//TODO
					}
				}
				addCreateSequenceMessage(rmMsgCtx, internalSequenceId, acksTo);
			}
		}

		SOAPEnvelope env = rmMsgCtx.getSOAPEnvelope();
		if (env == null) {
			SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
					SandeshaUtil.getSOAPVersion(env)).getDefaultEnvelope();
			rmMsgCtx.setSOAPEnvelop(envelope);
		}

		SOAPBody soapBody = rmMsgCtx.getSOAPEnvelope().getBody();
		if (soapBody == null) {
			String message = "Invalid SOAP message. Body is not present";
			log.debug(message);
			throw new SandeshaException(message);
		}

		String messageId1 = SandeshaUtil.getUUID();
		if (rmMsgCtx.getMessageId() == null) {
			rmMsgCtx.setMessageId(messageId1);
		}


		if (serverSide) {
			// let the request end with 202 if a ack has not been
			// written in the incoming thread.
			
			MessageContext reqMsgCtx = null;
			try {
				reqMsgCtx = msgContext.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			} catch (AxisFault e) {
				throw new SandeshaException (e);
			}

			if (reqMsgCtx.getProperty(Sandesha2Constants.ACK_WRITTEN) == null
					|| !"true".equals(reqMsgCtx.getProperty(Sandesha2Constants.ACK_WRITTEN)))
				reqMsgCtx.getOperationContext().setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN, "false");
		}
		
		EndpointReference toEPR = msgContext.getTo();
		if (toEPR == null) {
			String message = "To EPR is not found";
			log.debug(message);
			throw new SandeshaException(message);
		}
		
		//setting default actions.
		String to = toEPR.getAddress();
		String operationName = msgContext.getOperationContext().getAxisOperation().getName().getLocalPart();
		if (msgContext.getWSAAction() == null) {
			msgContext.setWSAAction(to + "/" + operationName);
		}
		if (msgContext.getSoapAction() == null) {
			msgContext.setSoapAction("\"" + to + "/" + operationName + "\"");
		}
		
		// processing the response if not an dummy.
		if (!dummyMessage)
			processResponseMessage(rmMsgCtx, internalSequenceId, messageNumber,storageKey);
		
		msgContext.pause();  // the execution will be stopped.
		outHandlerTransaction.commit();		

	}
	
	private void addCreateSequenceMessage(RMMsgContext applicationRMMsg,
			String internalSequenceId, String acksTo) throws SandeshaException {

		MessageContext applicationMsg = applicationRMMsg.getMessageContext();
		ConfigurationContext configCtx = applicationMsg.getConfigurationContext();
		
		//generating a new create sequeuce message.
		RMMsgContext createSeqRMMessage = RMMsgCreator.createCreateSeqMsg(applicationRMMsg, internalSequenceId, acksTo);
		
		createSeqRMMessage.setFlow(MessageContext.OUT_FLOW);
		CreateSequence createSequencePart = (CreateSequence) createSeqRMMessage.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);

		//retrieving the storage manager.
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();
		SenderBeanMgr retransmitterMgr = storageManager.getRetransmitterBeanMgr();
		
		SequenceOffer offer = createSequencePart.getSequenceOffer();
		if (offer != null) {
			String offeredSequenceId = offer.getIdentifer().getIdentifier();

			SequencePropertyBean offeredSequenceBean = new SequencePropertyBean();
			offeredSequenceBean.setName(Sandesha2Constants.SequenceProperties.OFFERED_SEQUENCE);
			offeredSequenceBean.setSequenceID(internalSequenceId);
			offeredSequenceBean.setValue(offeredSequenceId);

			seqPropMgr.insert(offeredSequenceBean);
		}

		MessageContext createSeqMsg = createSeqRMMessage.getMessageContext();
		createSeqMsg.setRelatesTo(null); // create seq msg does not relateTo anything

		CreateSeqBean createSeqBean = new CreateSeqBean(internalSequenceId,createSeqMsg.getMessageID(), null);
		createSeqMgr.insert(createSeqBean);

		String addressingNamespaceURI = SandeshaUtil.getSequenceProperty(internalSequenceId,Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE,configCtx);
		String anonymousURI = SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespaceURI);
		
		if (createSeqMsg.getReplyTo() == null)
			createSeqMsg.setReplyTo(new EndpointReference(anonymousURI));

		String key = SandeshaUtil.getUUID();   //the key used to store the create sequence message.

		SenderBean createSeqEntry = new SenderBean();
		createSeqEntry.setMessageContextRefKey(key);
		createSeqEntry.setTimeToSend(System.currentTimeMillis());
		createSeqEntry.setMessageID(createSeqRMMessage.getMessageId());

		// this will be set to true in the sender
		createSeqEntry.setSend(true);

		createSeqMsg.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,Sandesha2Constants.VALUE_FALSE);
		createSeqEntry.setMessageType(Sandesha2Constants.MessageTypes.CREATE_SEQ);
		retransmitterMgr.insert(createSeqEntry);
		
		storageManager.storeMessageContext(key,createSeqMsg);   //storing the message.
		

		// message will be stored in the Sandesha2TransportSender		
		createSeqMsg.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY, key);
		
		TransportOutDescription transportOut = createSeqMsg.getTransportOut();
		
		createSeqMsg.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_OUT_DESC,transportOut);
		createSeqMsg.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,Sandesha2Constants.VALUE_TRUE);
		createSeqMsg.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY, key);
		
		Sandesha2TransportOutDesc sandesha2TransportOutDesc = new Sandesha2TransportOutDesc ();
		createSeqMsg.setTransportOut(sandesha2TransportOutDesc);

		// sending the message once through Sandesha2TransportSender.
		AxisEngine engine = new AxisEngine(createSeqMsg.getConfigurationContext());
		 try {
			 log.info ("Sending create seq msg...");
			 engine.send(createSeqMsg);
		 } catch (AxisFault e) {
			 throw new SandeshaException (e.getMessage());
		 }
	}

	private void processResponseMessage(RMMsgContext rmMsg,
			String internalSequenceId, long messageNumber, String storageKey) throws SandeshaException {

		MessageContext msg = rmMsg.getMessageContext();
		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion(rmMsg.getSOAPEnvelope()));
		ConfigurationContext configurationContext = rmMsg.getMessageContext().getConfigurationContext();

		//retrieving storage manager
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(msg.getConfigurationContext());
		SequencePropertyBeanMgr sequencePropertyMgr = storageManager.getSequencePropretyBeanMgr();
		SenderBeanMgr retransmitterMgr = storageManager.getRetransmitterBeanMgr();

		SequencePropertyBean toBean = sequencePropertyMgr.retrieve(
				internalSequenceId,Sandesha2Constants.SequenceProperties.TO_EPR);
		SequencePropertyBean replyToBean = sequencePropertyMgr.retrieve(
				internalSequenceId,Sandesha2Constants.SequenceProperties.REPLY_TO_EPR);

		// again - looks weird in the client side - but consistent
		SequencePropertyBean outSequenceBean = sequencePropertyMgr.retrieve(
				internalSequenceId,Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);

		if (toBean == null) {
			String message = "To is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		EndpointReference toEPR = new EndpointReference(toBean.getValue());
		
		EndpointReference replyToEPR = null;
		if (replyToBean != null) {
			replyToEPR = new EndpointReference(replyToBean.getValue());
		}

		if (toEPR == null || toEPR.getAddress() == null || toEPR.getAddress() == "") {
			String message = "To Property has an invalid value";
			log.debug(message);
			throw new SandeshaException(message);
		}

		String newToStr = null;
		if (msg.isServerSide()) {
			try {
				MessageContext requestMsg = msg.getOperationContext().getMessageContext(
								OperationContextFactory.MESSAGE_LABEL_IN_VALUE);
				if (requestMsg != null) {
					newToStr = requestMsg.getReplyTo().getAddress();
				}
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}
		}

		if (newToStr != null)
			rmMsg.setTo(new EndpointReference(newToStr));
		else
			rmMsg.setTo(toEPR);

		if (replyToEPR != null)
			rmMsg.setReplyTo(replyToEPR);

		String rmVersion = SandeshaUtil.getRMVersion(internalSequenceId,configurationContext);
		if (rmVersion==null)
			throw new SandeshaException ("Cant find the rmVersion of the given message");
		
		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);
		
		Sequence sequence = new Sequence(factory,rmNamespaceValue);
		MessageNumber msgNumber = new MessageNumber(factory,rmNamespaceValue);
		msgNumber.setMessageNumber(messageNumber);
		sequence.setMessageNumber(msgNumber);

		boolean lastMessage = false;
		// setting last message
		if (msg.isServerSide()) {
			MessageContext requestMsg = null;

			try {
				requestMsg = msg.getOperationContext().getMessageContext(OperationContextFactory.MESSAGE_LABEL_IN_VALUE);
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}

			RMMsgContext reqRMMsgCtx = MsgInitializer.initializeMessage(requestMsg);
			Sequence requestSequence = (Sequence) reqRMMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			if (requestSequence == null) {
				String message = "Request Sequence is null";
				log.debug(message);
				throw new SandeshaException(message);
			}

			if (requestSequence.getLastMessage() != null) {
				lastMessage = true;
				sequence.setLastMessage(new LastMessage(factory,rmNamespaceValue));

//				// saving the last message no.
//				SequencePropertyBean lastOutMsgBean = new SequencePropertyBean(
//						internalSequenceId,
//						Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE,
//						new Long(messageNumber).toString());
//				sequencePropertyMgr.insert(lastOutMsgBean);
			}

		} else {
			// client side

			OperationContext operationContext = msg.getOperationContext();
			if (operationContext != null) {
				Object obj = msg.getProperty(Sandesha2ClientAPI.LAST_MESSAGE);
				if (obj != null && "true".equals(obj)) {
					lastMessage = true;
					
					SequencePropertyBean specVersionBean = sequencePropertyMgr.retrieve(internalSequenceId,Sandesha2Constants.SequenceProperties.RM_SPEC_VERSION);
					if (specVersionBean==null)
						throw new SandeshaException ("Spec version bean is not set");
					
					String specVersion = specVersionBean.getValue();
					if (SpecSpecificConstants.isLastMessageIndicatorRequired(specVersion))
						sequence.setLastMessage(new LastMessage(factory,rmNamespaceValue));
				}
			}
		}

		AckRequested ackRequested = null;

		boolean addAckRequested = false;
		//if (!lastMessage)
//		addAckRequested = true;   //TODO decide the policy to add the ackRequested tag

		// setting the Sequnece id.
		// Set send = true/false depending on the availability of the out
		// sequence id.
		String identifierStr = null;
		if (outSequenceBean == null || outSequenceBean.getValue() == null) {
			identifierStr = Sandesha2Constants.TEMP_SEQUENCE_ID;

		} else {
			identifierStr = (String) outSequenceBean.getValue();
		}

		Identifier id1 = new Identifier(factory,rmNamespaceValue);
		id1.setIndentifer(identifierStr);
		sequence.setIdentifier(id1);
		rmMsg.setMessagePart(Sandesha2Constants.MessageParts.SEQUENCE,sequence);

		if (addAckRequested) {
			ackRequested = new AckRequested(factory,rmNamespaceValue);
			Identifier id2 = new Identifier(factory,rmNamespaceValue);
			id2.setIndentifer(identifierStr);
			ackRequested.setIdentifier(id2);
			rmMsg.setMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST,
					ackRequested);
		}

		try {
			rmMsg.addSOAPEnvelope();
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		//Retransmitter bean entry for the application message
		SenderBean appMsgEntry = new SenderBean();
		
		appMsgEntry.setMessageContextRefKey(storageKey);

		appMsgEntry.setTimeToSend(System.currentTimeMillis());
		appMsgEntry.setMessageID(rmMsg.getMessageId());
		appMsgEntry.setMessageNumber(messageNumber);
		appMsgEntry.setMessageType(Sandesha2Constants.MessageTypes.APPLICATION);
		if (outSequenceBean == null || outSequenceBean.getValue() == null) {
			appMsgEntry.setSend(false);
		} else {
			appMsgEntry.setSend(true);
			// Send will be set to true at the sender.
			msg.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,
					Sandesha2Constants.VALUE_TRUE);
		}

		appMsgEntry.setInternalSequenceID(internalSequenceId);
		storageManager.storeMessageContext(storageKey,msg);
		retransmitterMgr.insert(appMsgEntry);
		msg.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,Sandesha2Constants.VALUE_FALSE);

		// changing the sender. This will set send to true.
		TransportSender sender = msg.getTransportOut().getSender();

		if (sender != null) {
			Sandesha2TransportOutDesc sandesha2TransportOutDesc = new Sandesha2TransportOutDesc ();
			msg.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY, storageKey);
			msg.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_OUT_DESC,
					msg.getTransportOut());
			msg.setTransportOut(sandesha2TransportOutDesc);
			
		}

		//increasing the current handler index, so that the message will not be going throught the SandeshaOutHandler again.
		msg.setCurrentHandlerIndex(msg.getCurrentHandlerIndex()+1);
		
		//sending the message through, other handlers and the Sandesha2TransportSender so that it get dumped to the storage.
		AxisEngine engine = new AxisEngine (msg.getConfigurationContext());
		try {
			engine.resumeSend(msg);
		} catch (AxisFault e) {
			throw new SandeshaException (e);
		}
	}	
	
	private long getPreviousMsgNo(ConfigurationContext context,
			String internalSequenceId) throws SandeshaException {

		//retrieving the storage managers
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(context);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean nextMsgNoBean = seqPropMgr.retrieve(
				internalSequenceId,Sandesha2Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);
		
		long nextMsgNo = -1;
		if (nextMsgNoBean != null) {
			Long nextMsgNoLng = new Long(nextMsgNoBean.getValue());
			nextMsgNo = nextMsgNoLng.longValue();
		} 
		
		return nextMsgNo;
	}
	
	private void setNextMsgNo(ConfigurationContext context,
			String internalSequenceId, long msgNo) throws SandeshaException {

		if (msgNo<=0) {
			String message = "Message number '" + msgNo + "' is invalid. Has to be larger than zero.";
			throw new SandeshaException (message);
		}
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(context);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean nextMsgNoBean = seqPropMgr.retrieve(
				internalSequenceId,Sandesha2Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);

		boolean update = true;
		if (nextMsgNoBean == null) {
			update = false;
			nextMsgNoBean = new SequencePropertyBean();
			nextMsgNoBean.setSequenceID(internalSequenceId);
			nextMsgNoBean.setName(Sandesha2Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);
		}

		nextMsgNoBean.setValue(new Long(msgNo).toString());
		if (update)
			seqPropMgr.update(nextMsgNoBean);
		else
			seqPropMgr.insert(nextMsgNoBean);

	}	
}