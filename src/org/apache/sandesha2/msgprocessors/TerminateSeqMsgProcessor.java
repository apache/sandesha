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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.OperationContextFactory;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.SpecSpecificConstants;
import org.apache.sandesha2.TerminateManager;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.transport.Sandesha2TransportOutDesc;
import org.apache.sandesha2.util.FaultManager;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * Responsible for processing an incoming Terminate Sequence message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class TerminateSeqMsgProcessor implements MsgProcessor {

	private Log log = LogFactory.getLog(getClass());
	
	public void processInMessage(RMMsgContext terminateSeqRMMsg)
			throws SandeshaException {

		MessageContext terminateSeqMsg = terminateSeqRMMsg.getMessageContext();
		//Processing for ack if any
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) terminateSeqRMMsg
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck != null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor();
			ackProcessor.processInMessage(terminateSeqRMMsg);
		}
		
		//Processing the terminate message
		//TODO Add terminate sequence message logic.
		TerminateSequence terminateSequence = (TerminateSequence) terminateSeqRMMsg.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
		if (terminateSequence==null) {
			String message = "Terminate Sequence part is not available";
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		String sequenceId = terminateSequence.getIdentifier().getIdentifier();
		if (sequenceId==null || "".equals(sequenceId)) {
			String message = "Invalid sequence id";
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		FaultManager faultManager = new FaultManager();
		RMMsgContext faultMessageContext = faultManager.checkForUnknownSequence(terminateSeqRMMsg,sequenceId);
		if (faultMessageContext != null) {
			ConfigurationContext configurationContext = terminateSeqMsg.getConfigurationContext();
			AxisEngine engine = new AxisEngine(configurationContext);
			
			try {
				engine.sendFault(faultMessageContext.getMessageContext());
			} catch (AxisFault e) {
				throw new SandeshaException ("Could not send the fault message",e);
			}
			
			return;
		}
		
		ConfigurationContext context = terminateSeqMsg.getConfigurationContext();
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(context);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();

		Transaction terminateReceivedTransaction = storageManager.getTransaction();
		SequencePropertyBean terminateReceivedBean = new SequencePropertyBean ();
		terminateReceivedBean.setSequenceID(sequenceId);
		terminateReceivedBean.setName(Sandesha2Constants.SequenceProperties.TERMINATE_RECEIVED);
		terminateReceivedBean.setValue("true");
		
		sequencePropertyBeanMgr.insert(terminateReceivedBean);
		
		
		//add the terminate sequence response if required.
		if (SpecSpecificConstants.isTerminateSequenceResponseRequired (terminateSeqRMMsg.getRMSpecVersion()))
			addTerminateSequenceResponse (terminateSeqRMMsg);
		
		
		
		setUpHighestMsgNumbers(context,storageManager,sequenceId,terminateSeqRMMsg);
		
		
		terminateReceivedTransaction.commit();
		
		Transaction terminateTransaction = storageManager.getTransaction();
		TerminateManager.cleanReceivingSideOnTerminateMessage(context,sequenceId);
		
		
		SequencePropertyBean terminatedBean = new SequencePropertyBean (
				     sequenceId,Sandesha2Constants.SequenceProperties.SEQUENCE_TERMINATED,Sandesha2Constants.VALUE_TRUE);
		
		sequencePropertyBeanMgr.insert(terminatedBean);
		
		
		terminateTransaction.commit(); 
		
		SandeshaUtil.stopSenderForTheSequence(sequenceId);
		
		//removing an entry from the listener
		String transport = terminateSeqMsg.getTransportIn().getName().getLocalPart();
	
		Transaction lastUpdatedTransaction = storageManager.getTransaction();
		SequenceManager.updateLastActivatedTime(sequenceId,context);
		
		
		
		lastUpdatedTransaction.commit();
		
		
		
		terminateSeqRMMsg.pause();
	}
	

	private void setUpHighestMsgNumbers (ConfigurationContext configCtx, StorageManager storageManager, String sequenceID, RMMsgContext terminateRMMsg) throws SandeshaException {
		
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		String highestImMsgNumberStr = SandeshaUtil.getSequenceProperty(sequenceID,Sandesha2Constants.SequenceProperties.HIGHEST_IN_MSG_NUMBER,configCtx);
		String highestImMsgKey = SandeshaUtil.getSequenceProperty(sequenceID,Sandesha2Constants.SequenceProperties.HIGHEST_IN_MSG_KEY,configCtx);
		
		long highestInMsgNo = 0;
		if (highestImMsgNumberStr!=null) {
			if (highestImMsgKey==null)
				throw new SandeshaException ("Key of the highest message number has not been stored");
			
			highestInMsgNo = Long.parseLong(highestImMsgNumberStr);
		}
		
		//following will be valid only for the server side, since the obtained int. seq ID is only valid there.
		String responseSideInternalSequenceID = SandeshaUtil.getInternalSequenceID(sequenceID);
		
		long highestOutMsgNo = 0;
		try {
			boolean addResponseSideTerminate = false;
			if (highestInMsgNo==0) {
				addResponseSideTerminate=false;
			} else {
				
				//setting the last in message property
				SequencePropertyBean lastInMsgBean = new SequencePropertyBean (
						sequenceID,Sandesha2Constants.SequenceProperties.LAST_IN_MESSAGE_NO,highestImMsgNumberStr);
				seqPropMgr.insert(lastInMsgBean);
				
				MessageContext highestInMsg = storageManager.retrieveMessageContext(highestImMsgKey,configCtx);
				MessageContext highestOutMessage = highestInMsg.getOperationContext().getMessageContext(OperationContextFactory.MESSAGE_LABEL_OUT_VALUE);
				
				if (highestOutMessage!=null) {
					RMMsgContext highestOutRMMsg = MsgInitializer.initializeMessage(highestOutMessage);
					Sequence seqPartOfOutMsg = (Sequence) highestOutRMMsg.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
				
					if (seqPartOfOutMsg!=null) {
						
						//response message of the last in message can be considered as the last out message.
						highestOutMsgNo = seqPartOfOutMsg.getMessageNumber().getMessageNumber();
						SequencePropertyBean highestOutMsgBean = new SequencePropertyBean (
								responseSideInternalSequenceID,
								Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE_NO,
								new Long(highestOutMsgNo).toString() );
					
						seqPropMgr.insert(highestOutMsgBean);
						addResponseSideTerminate = true;
					}
				}
			}
			
			// If all the out message have been acked, add the outgoing terminate seq msg.
			String outgoingSqunceID = SandeshaUtil.getSequenceProperty(responseSideInternalSequenceID,Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID,configCtx);
			if (addResponseSideTerminate && highestOutMsgNo>0 
					&& responseSideInternalSequenceID!=null && outgoingSqunceID!=null ) {
				boolean allAcked = SandeshaUtil.isAllMsgsAckedUpto (highestOutMsgNo, responseSideInternalSequenceID, configCtx); 
				
				if (allAcked) 
					TerminateManager.addTerminateSequenceMessage(terminateRMMsg, outgoingSqunceID,responseSideInternalSequenceID);
			}
		} catch (AxisFault e) {
			throw new SandeshaException (e);
		}
		
	}
	
	private void addTerminateSequenceResponse (RMMsgContext terminateSeqRMMsg) throws SandeshaException {
		
		MessageContext terminateSeqMsg = terminateSeqRMMsg.getMessageContext();
		
		MessageContext outMessage = null;
		outMessage = Utils.createOutMessageContext(terminateSeqMsg);
		
		RMMsgContext terminateSeqResponseRMMsg = RMMsgCreator
				.createTerminateSeqResponseMsg(terminateSeqRMMsg, outMessage);
		
		terminateSeqResponseRMMsg.setFlow(MessageContext.OUT_FLOW);
		terminateSeqResponseRMMsg.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");

		outMessage.setResponseWritten(true);
		
		AxisEngine engine = new AxisEngine (terminateSeqMsg.getConfigurationContext());
		
		try {
			engine.send(outMessage);
		} catch (AxisFault e) {
			String message = "Could not send the terminate sequence response";
			throw new SandeshaException (message,e);
		}
	}
	
	public void processOutMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		MessageContext msgContext = rmMsgCtx.getMessageContext();
		ConfigurationContext configurationContext = msgContext.getConfigurationContext();
		Options options = msgContext.getOptions();
		
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		String toAddress = rmMsgCtx.getTo().getAddress();
		String sequenceKey = (String) options.getProperty(Sandesha2ClientAPI.SEQUENCE_KEY);
        String internalSeqenceID = SandeshaUtil.getInternalSequenceID(toAddress,sequenceKey);
        
        String outSequenceID = SandeshaUtil.getSequenceProperty(internalSeqenceID,Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID,configurationContext);
        if (outSequenceID==null)
        	throw new SandeshaException ("SequenceID was not found. Cannot send the terminate message");
        
		Transaction addTerminateSeqTransaction = storageManager.getTransaction();
		
		String terminated = SandeshaUtil.getSequenceProperty(outSequenceID,
				Sandesha2Constants.SequenceProperties.TERMINATE_ADDED,configurationContext);

		if (terminated != null
				&& "true".equals(terminated)) {
			String message = "Terminate was added previously.";
			log.info(message);
			return;
		}

//		RMMsgContext terminateRMMessage = RMMsgCreator
//				.createTerminateSequenceMessage(incomingAckRMMsg, outSequenceId,internalSequenceId);
		
		TerminateSequence terminateSequencePart = (TerminateSequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
		terminateSequencePart.getIdentifier().setIndentifer(outSequenceID);
		
		rmMsgCtx.setFlow(MessageContext.OUT_FLOW);
		msgContext.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");
		
//		String toAddress =  SandeshaUtil.getSequenceProperty(internalSeqenceID,Sandesha2Constants.SequenceProperties.TO_EPR,configurationContext);

//		EndpointReference toEPR = new EndpointReference ( toBean.getValue());
//		if (toEPR == null) {
//			String message = "To EPR has an invalid value";
//			throw new SandeshaException(message);
//		}

		rmMsgCtx.setTo(new EndpointReference(toAddress));
		
		
//		terminateRMMessage.setFrom(new EndpointReference(
//				Sandesha2Constants.WSA.NS_URI_ANONYMOUS));
//		terminateRMMessage.setFaultTo(new EndpointReference(
//				Sandesha2Constants.WSA.NS_URI_ANONYMOUS));
		
		String rmVersion = SandeshaUtil.getRMVersion(internalSeqenceID,configurationContext);
		if (rmVersion==null)
			throw new SandeshaException ("Cant find the rmVersion of the given message");
		
		rmMsgCtx.setWSAAction(SpecSpecificConstants.getTerminateSequenceAction(rmVersion));
		rmMsgCtx.setSOAPAction(SpecSpecificConstants.getTerminateSequenceSOAPAction(rmVersion));

		//SequencePropertyBean transportToBean = seqPropMgr.retrieve(internalSequenceId,Sandesha2Constants.SequenceProperties.TRANSPORT_TO);
		String transportTo = SandeshaUtil.getSequenceProperty(internalSeqenceID,Sandesha2Constants.SequenceProperties.TRANSPORT_TO,configurationContext);
		if (transportTo!=null) {
			rmMsgCtx.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		}
		
		try {
			rmMsgCtx.addSOAPEnvelope();
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		String key = SandeshaUtil.getUUID();
		
		SenderBean terminateBean = new SenderBean();
		terminateBean.setMessageContextRefKey(key);

		
		storageManager.storeMessageContext(key,msgContext);

		
		//Set a retransmitter lastSentTime so that terminate will be send with
		// some delay.
		//Otherwise this get send before return of the current request (ack).
		//TODO: refine the terminate delay.
		terminateBean.setTimeToSend(System.currentTimeMillis()
				+ Sandesha2Constants.TERMINATE_DELAY);

		terminateBean.setMessageID(msgContext.getMessageID());
		
		//this will be set to true at the sender.
		terminateBean.setSend(true);
		
		msgContext.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,
				Sandesha2Constants.VALUE_FALSE);
		
		terminateBean.setReSend(false);

		SenderBeanMgr retramsmitterMgr = storageManager
				.getRetransmitterBeanMgr();

		retramsmitterMgr.insert(terminateBean);
		
		SequencePropertyBean terminateAdded = new SequencePropertyBean();
		terminateAdded.setName(Sandesha2Constants.SequenceProperties.TERMINATE_ADDED);
		terminateAdded.setSequenceID(outSequenceID);
		terminateAdded.setValue("true");

		
		seqPropMgr.insert(terminateAdded);
		
		//This should be dumped to the storage by the sender
		TransportOutDescription transportOut = msgContext.getTransportOut();
		rmMsgCtx.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_OUT_DESC,transportOut);
		rmMsgCtx.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY,key);
		rmMsgCtx.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,Sandesha2Constants.VALUE_TRUE);
		rmMsgCtx.getMessageContext().setTransportOut(new Sandesha2TransportOutDesc ());
		addTerminateSeqTransaction.commit();
		
	    AxisEngine engine = new AxisEngine (configurationContext);
	    try {
			engine.send(msgContext);
		} catch (AxisFault e) {
			throw new SandeshaException (e.getMessage());
		}
	}
	
}