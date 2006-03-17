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
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaPropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;

/**
 * Responsible for processing an incoming Application message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */
public class AckRequestedProcessor implements MsgProcessor {

	private Log log = LogFactory.getLog(getClass());
	
	public void processInMessage(RMMsgContext rmMsgCtx) throws SandeshaException {
		
		
		AckRequested ackRequested = (AckRequested) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST);
		if (ackRequested==null) {
			throw new SandeshaException ("Message identified as of type ackRequested does not have an AckRequeted element");
		}
		
		MessageContext msgContext = rmMsgCtx.getMessageContext();
		
		String sequenceID = ackRequested.getIdentifier().getIdentifier();
		
		ConfigurationContext configurationContext = rmMsgCtx.getMessageContext().getConfigurationContext();
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
		
		ackMsgCtx.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");
		
		RMMsgContext ackRMMsgCtx = MsgInitializer.initializeMessage(ackMsgCtx);
		ackRMMsgCtx.setRMNamespaceValue(rmMsgCtx.getRMNamespaceValue());
		
		ackMsgCtx.setMessageID(SandeshaUtil.getUUID());

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(msgContext.getEnvelope()));
		
		//Setting new envelope
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		try {
			ackMsgCtx.setEnvelope(envelope);
		} catch (AxisFault e3) {
			throw new SandeshaException(e3.getMessage());
		}

		ackMsgCtx.setTo(acksTo);
		ackMsgCtx.setReplyTo(msgContext.getTo());
		RMMsgCreator.addAckMessage(ackRMMsgCtx, sequenceID);

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
			
			Parameter param = msgContext.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
			
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
			
			AxisEngine engine = new AxisEngine (configurationContext);
			try {
				engine.send(ackMsgCtx);
			} catch (AxisFault e) {
				throw new SandeshaException (e.getMessage());
			}
			
			SandeshaUtil.startSenderForTheSequence(configurationContext,sequenceID);
			
			msgContext.pause(); 
		}
	}
	
	public void processOutMessage(RMMsgContext rmMsgCtx) throws SandeshaException {
		
	}
	
}
