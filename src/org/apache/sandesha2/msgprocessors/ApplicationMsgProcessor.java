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

package org.apache.sandesha2.msgprocessors;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.handlers.SandeshaOutHandler;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.MsgValidator;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.workers.InOrderInvoker;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.wsdl.WSDLConstants;
import org.ietf.jgss.MessageProp;

/**
 * @author Chamikara
 * @author Sanka
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

		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(rmMsgCtx.getContext())
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

			//throw new SandeshaException(
			//		"Duplicate message - Invocation type is EXACTLY_ONCE");

			//TODO is this enough
			msgCtx.setPausedTrue(new QName(Constants.IN_HANDLER_NAME));

		}

		if (messagesStr != "" && messagesStr != null)
			messagesStr = messagesStr + "," + Long.toString(msgNo);
		else
			messagesStr = Long.toString(msgNo);

		msgsBean.setValue(messagesStr);
		seqPropMgr.update(msgsBean);

		sendAckIfNeeded(rmMsgCtx,messagesStr);

		//		} else {
		//			//TODO Add async Ack
		//		}

		//		Pause the messages bean if not the right message to invoke.
		NextMsgBeanMgr mgr = AbstractBeanMgrFactory.getInstance(configCtx)
				.getNextMsgBeanMgr();
		NextMsgBean bean = mgr.retrieve(sequenceId);

		if (bean == null)
			throw new SandeshaException("Error- The sequence does not exist");

		StorageMapBeanMgr storageMapMgr = AbstractBeanMgrFactory.getInstance(
				configCtx).getStorageMapBeanMgr();

		long nextMsgno = bean.getNextMsgNoToProcess();

		//FIXME - fix delivery assurances for the client side

		if (msgCtx.isServerSide()) {
			if (Constants.QOS.DeliveryAssurance.DEFAULT_DELIVERY_ASSURANCE == Constants.QOS.DeliveryAssurance.IN_ORDER) {
				//pause the message
				msgCtx.setPausedTrue(new QName(Constants.IN_HANDLER_NAME));

				//Adding an entry in the SequencesToInvoke List TODO - add this
				// to
				// a module init kind of place.
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

				//This must be a List :D
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
					storageMapMgr.insert(new StorageMapBean(key, msgNo,
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

		//client side - SET CheckResponse to true.
		//FIXME this will not work. Even in client side inServerSide () is true
		// for the messages.
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

		//SET THe RESPONSE


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
	
	public void sendAckIfNeeded (RMMsgContext rmMsgCtx,String messagesStr) throws SandeshaException {
		
		MessageContext msgCtx = rmMsgCtx.getMessageContext();
		
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
		.getInstance(rmMsgCtx.getContext())
		.getSequencePropretyBeanMgr();
		
		
		Sequence sequence = (Sequence) rmMsgCtx
		.getMessagePart(Constants.MessageParts.SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		ConfigurationContext configCtx = rmMsgCtx.getMessageContext()
			.getSystemContext();
		if (configCtx == null)
			throw new SandeshaException("Configuration Context is null");

		//Setting the ack depending on AcksTo.
		//TODO: Stop sending askc for every message.
		SequencePropertyBean acksToBean = seqPropMgr.retrieve(sequenceId,
				Constants.SequenceProperties.ACKS_TO_EPR);

		EndpointReference acksTo = (EndpointReference) acksToBean.getValue();
		String acksToStr = acksTo.getAddress();

		if (acksToStr == null || messagesStr == null)
			throw new SandeshaException(
					"Seqeunce properties are not set correctly");

		//if (acksToStr.equals(Constants.WSA.NS_URI_ANONYMOUS)) {

		AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
		AxisServiceGroup serviceGroup = new AxisServiceGroup (axisConfig);
		AxisService service = new AxisService (new QName ("RMClientService")); // This is a dummy service.
		ServiceGroupContext serviceGroupContext = new ServiceGroupContext (configCtx,serviceGroup);
		ServiceContext serviceContext = new ServiceContext (service,serviceGroupContext);
		

		RMMsgContext ackRMMsgCtx = SandeshaUtil.deepCopy(rmMsgCtx);
		MessageContext ackMsgCtx = ackRMMsgCtx.getMessageContext();
		
		ackMsgCtx.setAxisServiceGroup(serviceGroup);
		ackMsgCtx.setServiceGroupContext(serviceGroupContext);
		ackMsgCtx.setAxisService(service);
		ackMsgCtx.setServiceContext(serviceContext);
		
		try {
			AxisOperation ackOperation = AxisOperationFactory.getOperetionDescription(AxisOperationFactory.MEP_URI_IN_ONLY);

			AxisOperation rmMsgOperation = rmMsgCtx.getMessageContext().getAxisOperation();
			if (rmMsgOperation!=null) {
				ArrayList outFlow = rmMsgOperation.getPhasesOutFlow();
				if (outFlow!=null) {
					ackOperation.setPhasesOutFlow(outFlow);
					ackOperation.setPhasesOutFaultFlow(outFlow);
				}
			}
			
			OperationContext ackOpContext = new OperationContext (ackOperation);
			ackMsgCtx.setAxisOperation(ackOperation);
			ackMsgCtx.setOperationContext(ackOpContext);
			ackOpContext.addMessageContext(ackMsgCtx);
			ackMsgCtx.setOperationContext(ackOpContext);
			
		} catch (AxisFault e) {
			throw new SandeshaException (e.getMessage());
		}

		//Set new envelope
		SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).getDefaultEnvelope();
		try {
			ackMsgCtx.setEnvelope(envelope);
		} catch (AxisFault e3) {
			throw new SandeshaException(e3.getMessage());
		}

		//FIXME set acksTo instead of ReplyTo
		ackMsgCtx.setTo(acksTo);
		ackMsgCtx.setReplyTo(msgCtx.getTo());
		RMMsgCreator.addAckMessage(ackRMMsgCtx, sequenceId);

		if (Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo.getAddress())) {
			AxisEngine engine = new AxisEngine(ackRMMsgCtx.getMessageContext()
					.getSystemContext());


			//set CONTEXT_WRITTEN since acksto is anonymous
			rmMsgCtx.getMessageContext().getOperationContext().setProperty(
					org.apache.axis2.Constants.RESPONSE_WRITTEN, "true");
			rmMsgCtx.getMessageContext().setProperty(Constants.ACK_WRITTEN, "true");
			try {
				engine.send(ackRMMsgCtx.getMessageContext());
			} catch (AxisFault e1) {
				throw new SandeshaException(e1.getMessage());
			}
		} else {
			RetransmitterBeanMgr retransmitterBeanMgr = AbstractBeanMgrFactory.getInstance(configCtx).getRetransmitterBeanMgr();
			
			String key = SandeshaUtil.storeMessageContext(ackMsgCtx);
			RetransmitterBean ackBean = new RetransmitterBean ();
			ackBean.setKey(key);
			ackBean.setMessageId(ackMsgCtx.getMessageID());
			ackBean.setReSend(false);
			ackBean.setSend(true);
			
			retransmitterBeanMgr.insert(ackBean);
			
			SandeshaUtil.startSenderIfStopped(configCtx);
		}
		
	}
}