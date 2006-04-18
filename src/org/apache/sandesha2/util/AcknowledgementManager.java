/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.sandesha2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.Sandesha2Constants.MessageParts;
import org.apache.sandesha2.Sandesha2Constants.MessageTypes;
import org.apache.sandesha2.Sandesha2Constants.SequenceProperties;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.transport.Sandesha2TransportOutDesc;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Contains logic for managing acknowledgements.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class AcknowledgementManager {

	private static Log log = LogFactory.getLog(AcknowledgementManager.class);
	
	/**
	 * Piggybacks any available acks of the same sequence to the given
	 * application message.
	 * 
	 * @param applicationRMMsgContext
	 * @throws SandeshaException
	 */
	public static void piggybackAcksIfPresent(
			RMMsgContext rmMessageContext) throws SandeshaException {
		
		ConfigurationContext configurationContext = rmMessageContext.getConfigurationContext();
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);

		SenderBeanMgr retransmitterBeanMgr = storageManager.getRetransmitterBeanMgr();
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();

		SenderBean findBean = new SenderBean();

		String sequnceID = SandeshaUtil.getSequenceIDFromRMMessage (rmMessageContext);

		findBean.setMessageType(Sandesha2Constants.MessageTypes.ACK);
		findBean.setSend(true);
		findBean.setReSend(false);
		
		String carrietTo = rmMessageContext.getTo().getAddress();
		
		Collection collection = retransmitterBeanMgr.find(findBean);
		
		Iterator it = collection.iterator();

		
		piggybackLoop:
		while (it.hasNext()) {
			SenderBean ackBean = (SenderBean) it.next();

			long timeNow = System.currentTimeMillis();
			if (ackBean.getTimeToSend() > timeNow) { 
				//Piggybacking will happen only if the end of ack interval (timeToSend) is not reached.

				MessageContext ackMsgContext = storageManager
				.retrieveMessageContext(ackBean.getMessageContextRefKey(),configurationContext);
				
				//wsa:To has to match for piggybacking.
				String to = ackMsgContext.getTo().getAddress();
				if (!carrietTo.equals(to)) {
					continue piggybackLoop;
				}
				
				String ackSequenceID = ackBean.getSequenceID();
				
				//sequenceID has to match for piggybacking
				if (!ackSequenceID.equals(sequnceID)) {
					continue piggybackLoop;
				}
				
				//deleting the ack entry.
				retransmitterBeanMgr.delete(ackBean.getMessageID());

				//Adding the ack to the application message
				RMMsgContext ackRMMsgContext = MsgInitializer.initializeMessage(ackMsgContext);
				if (ackRMMsgContext.getMessageType() != Sandesha2Constants.MessageTypes.ACK) {
					String message = "Invalid ack message entry";
					log.debug(message);
					throw new SandeshaException(message);
				}

				SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) ackRMMsgContext
						.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
				rmMessageContext.setMessagePart(
						Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT,
						sequenceAcknowledgement);

				rmMessageContext.addSOAPEnvelope();
				break piggybackLoop;
			}
		}
	}
	
	/**this is used to get the acked messages of a sequence. If this is an outgoing message the sequenceIdentifier should
	 * be the internal sequenceID.
	 * 
	 * @param sequenceIdentifier
	 * @param outGoingMessage
	 * @return
	 */
	public static ArrayList getClientCompletedMessagesList (String sequenceID,SequencePropertyBeanMgr seqPropMgr) throws SandeshaException {
	
		//first trying to get it from the internal sequence id.
		SequencePropertyBean internalSequenceBean = seqPropMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		String internalSequenceID = null;
		if (internalSequenceBean!=null)
			internalSequenceID = internalSequenceBean.getValue();
		
		SequencePropertyBean completedMessagesBean = null;
		if (internalSequenceID!=null)
			completedMessagesBean = seqPropMgr.retrieve(internalSequenceID,Sandesha2Constants.SequenceProperties.CLIENT_COMPLETED_MESSAGES);
		
		if (completedMessagesBean==null)
			completedMessagesBean = seqPropMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.CLIENT_COMPLETED_MESSAGES);
		
		ArrayList completedMsgList = null;
		if (completedMessagesBean!=null) {
			completedMsgList = SandeshaUtil.getArrayListFromString(completedMessagesBean.getValue());
		} else {
			String message = "Completed messages bean is null, for the sequence " + sequenceID;
			throw new SandeshaException (message);
		}
		
		return completedMsgList;
	}
	
	public static ArrayList getServerCompletedMessagesList (String sequenceID,SequencePropertyBeanMgr seqPropMgr) throws SandeshaException {
				
		SequencePropertyBean completedMessagesBean = null;

		completedMessagesBean = seqPropMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);
		
		ArrayList completedMsgList = null;
		if (completedMessagesBean!=null) {
			completedMsgList = SandeshaUtil.getArrayListFromMsgsString (completedMessagesBean.getValue());
		} else {
			String message = "Completed messages bean is null, for the sequence " + sequenceID;
			throw new SandeshaException (message);
		}
		
		return completedMsgList;
	}
	
	public static RMMsgContext generateAckMessage (RMMsgContext referenceRMMessage, String sequenceID)throws SandeshaException {
		
		MessageContext referenceMsg = referenceRMMessage.getMessageContext();
		
		ConfigurationContext configurationContext = referenceRMMessage.getMessageContext().getConfigurationContext();
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		//Setting the ack depending on AcksTo.
		SequencePropertyBean acksToBean = seqPropMgr.retrieve(sequenceID,
				Sandesha2Constants.SequenceProperties.ACKS_TO_EPR);

		EndpointReference acksTo = new EndpointReference(acksToBean.getValue());
		String acksToStr = acksTo.getAddress();

		if (acksToStr == null)
			throw new SandeshaException(
					"acksToStr Seqeunce property is not set correctly");
		
		AxisOperation ackOperation = null;

		try {
			ackOperation = AxisOperationFactory.getOperationDescription(AxisOperationFactory.MEP_URI_IN_ONLY);
		} catch (AxisFault e) {
			throw new SandeshaException("Could not create the Operation");
		}

		AxisOperation rmMsgOperation = referenceRMMessage.getMessageContext()
				.getAxisOperation();
		if (rmMsgOperation != null) {
			ArrayList outFlow = rmMsgOperation.getPhasesOutFlow();
			if (outFlow != null) {
				ackOperation.setPhasesOutFlow(outFlow);
				ackOperation.setPhasesOutFaultFlow(outFlow);
			}
		}

		MessageContext ackMsgCtx = SandeshaUtil.createNewRelatedMessageContext(
				referenceRMMessage, ackOperation);
		ackMsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
				referenceMsg.getProperty(AddressingConstants.WS_ADDRESSING_VERSION));  //TODO do this in the RMMsgCreator
		
		
		ackMsgCtx.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");
		
		RMMsgContext ackRMMsgCtx = MsgInitializer.initializeMessage(ackMsgCtx);
		ackRMMsgCtx.setRMNamespaceValue(referenceRMMessage.getRMNamespaceValue());
		
		ackMsgCtx.setMessageID(SandeshaUtil.getUUID());

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(referenceMsg.getEnvelope()));
		
		//Setting new envelope
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		try {
			ackMsgCtx.setEnvelope(envelope);
		} catch (AxisFault e3) {
			throw new SandeshaException(e3.getMessage());
		}

		ackMsgCtx.setTo(acksTo);
		
		//adding the SequenceAcknowledgement part.
		RMMsgCreator.addAckMessage(ackRMMsgCtx, sequenceID);
		
		ackMsgCtx.setProperty(MessageContext.TRANSPORT_IN,null);

		String addressingNamespaceURI = SandeshaUtil.getSequenceProperty(sequenceID,Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE,configurationContext);
		String anonymousURI = SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespaceURI);
		
		if (anonymousURI.equals(acksTo.getAddress())) {

//			AxisEngine engine = new AxisEngine(ackRMMsgCtx.getMessageContext()
//					.getConfigurationContext());

			//setting CONTEXT_WRITTEN since acksto is anonymous
			if (referenceRMMessage.getMessageContext().getOperationContext() == null) {
				//operation context will be null when doing in a GLOBAL
				// handler.
				try {
					AxisOperation op = AxisOperationFactory
							.getAxisOperation(AxisOperationFactory.MEP_CONSTANT_IN_OUT);
					OperationContext opCtx = new OperationContext(op);
					referenceRMMessage.getMessageContext().setAxisOperation(op);
					referenceRMMessage.getMessageContext().setOperationContext(opCtx);
				} catch (AxisFault e2) {
					throw new SandeshaException(e2.getMessage());
				}
			}

			referenceRMMessage.getMessageContext().getOperationContext().setProperty(
					org.apache.axis2.Constants.RESPONSE_WRITTEN,
					Constants.VALUE_TRUE);

			referenceRMMessage.getMessageContext().setProperty(
					Sandesha2Constants.ACK_WRITTEN, "true");
			
			ackRMMsgCtx.getMessageContext().setServerSide(true);
			return ackRMMsgCtx;
			
		} else {

			Transaction asyncAckTransaction = storageManager.getTransaction();

			SenderBeanMgr retransmitterBeanMgr = storageManager
					.getRetransmitterBeanMgr();

			String key = SandeshaUtil.getUUID();
			
			SenderBean ackBean = new SenderBean();
			ackBean.setMessageContextRefKey(key);
			ackBean.setMessageID(ackMsgCtx.getMessageID());
			ackBean.setReSend(false);
			ackBean.setSequenceID(sequenceID);
			
			//this will be set to true in the sender.
			ackBean.setSend(true);
			
			ackMsgCtx.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,
					Sandesha2Constants.VALUE_FALSE);
			
			ackBean.setMessageType(Sandesha2Constants.MessageTypes.ACK);
			long ackInterval = SandeshaUtil.getPropretyBean(referenceMsg).getAcknowledgementInaterval();
			
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
			RMMsgContext ackRMMessageCtx = MsgInitializer.initializeMessage(ackMsgCtx);
			
			SandeshaUtil.startSenderForTheSequence(configurationContext,sequenceID);	
			referenceMsg.pause(); 
			return ackRMMessageCtx;
		}	
	}
	
	public static boolean verifySequenceCompletion(Iterator ackRangesIterator,
			long lastMessageNo) {
		HashMap startMap = new HashMap();

		while (ackRangesIterator.hasNext()) {
			AcknowledgementRange temp = (AcknowledgementRange) ackRangesIterator
					.next();
			startMap.put(new Long(temp.getLowerValue()), temp);
		}

		long start = 1;
		boolean loop = true;
		while (loop) {
			AcknowledgementRange temp = (AcknowledgementRange) startMap
					.get(new Long(start));
			if (temp == null) {
				loop = false;
				continue;
			}

			if (temp.getUpperValue() >= lastMessageNo)
				return true;

			start = temp.getUpperValue() + 1;
		}

		return false;
	}
}