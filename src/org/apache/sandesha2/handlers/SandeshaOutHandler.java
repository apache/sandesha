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

package org.apache.sandesha2.handlers;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.OperationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.transport.Sandesha2TransportSender;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.RMPolicyManager;
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
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.wsdl.WSDLConstants;

import sun.security.action.GetPropertyAction;

/**
 * This is invoked in the outFlow of an RM endpoint
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaOutHandler extends AbstractHandler {

	protected Log log = LogFactory.getLog(SandeshaOutHandler.class.getName());

	public void invoke(MessageContext msgCtx) throws AxisFault {

		ConfigurationContext context = msgCtx.getConfigurationContext();
		if (context == null) {
			String message = "ConfigurationContext is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		AxisService axisService = msgCtx.getAxisService();
		if (axisService == null) {
			String message = "AxisService is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		// getting rm message
		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);

		String DONE = (String) msgCtx
				.getProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE);
		if (null != DONE && "true".equals(DONE))
			return;

		msgCtx.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,
				"true");

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(context);

		ServiceContext serviceContext = msgCtx.getServiceContext();
		OperationContext operationContext = msgCtx.getOperationContext();

		// continue only if an possible application message
		if (!(rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.UNKNOWN)) {
			return;
		}

		// Adding the policy bean
		// RMPolicyBean policyBean = RMPolicyManager.getPolicyBean(rmMsgCtx);
		// rmMsgCtx.setProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN,
		// policyBean);
		Parameter policyParam = msgCtx
				.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
		if (policyParam == null) {
			SandeshaPropertyBean propertyBean = PropertyManager.getInstance()
					.getPropertyBean();
			Parameter parameter = new ParameterImpl();
			parameter.setName(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
			parameter.setValue(propertyBean);

			// TODO this should be addede to the AxisMessage
			if (msgCtx.getAxisOperation() != null)
				msgCtx.getAxisOperation().addParameter(parameter);
			else if (msgCtx.getAxisService() != null)
				msgCtx.getAxisService().addParameter(parameter);
		}

		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		Transaction transaction = storageManager.getTransaction();

		boolean serverSide = msgCtx.isServerSide();

		// setting message Id if null
		if (msgCtx.getMessageID() == null) {
			msgCtx.setMessageID(SandeshaUtil.getUUID());
		}
		// initial work
		// find internal sequence id
		String internalSequenceId = null;

		// Temp sequence id is the one used to refer to the sequence (since
		// actual sequence id is not available when first msg arrives)
		// server side - sequenceId if the incoming sequence
		// client side - wsaTo + SeequenceKey

		if (serverSide) {
			// getting the request message and rmMessage.
			MessageContext reqMsgCtx = msgCtx.getOperationContext()
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

			RMMsgContext requestRMMsgCtx = MsgInitializer
					.initializeMessage(reqMsgCtx);

			Sequence reqSequence = (Sequence) requestRMMsgCtx
					.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
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

			internalSequenceId = SandeshaUtil
					.getServerSideInternalSeqIdFromIncomingSeqId(incomingSeqId);

		} else {
			// set the internal sequence id for the client side.
			EndpointReference toEPR = msgCtx.getTo();
			if (toEPR == null || toEPR.getAddress() == null
					|| "".equals(toEPR.getAddress())) {
				String message = "TO End Point Reference is not set correctly. This is a must for the sandesha client side.";
				log.debug(message);
				throw new AxisFault(message);
			}

			String to = toEPR.getAddress();
			String sequenceKey = (String) msgCtx
					.getProperty(Sandesha2ClientAPI.SEQUENCE_KEY);

			internalSequenceId = SandeshaUtil.getInternalSequenceID(to,
					sequenceKey);

		}

		// Strating the sender.
		// SandeshaUtil.startSenderForTheSequence(context,internalSequenceId);

		// check if the first message

		long messageNumber = getNextMsgNo(context, internalSequenceId);

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
				msgCtx.setProperty(Sandesha2ClientAPI.AcksTo, incomingTo);
			}
		}

		if (messageNumber == 1) {
			if (outSeqBean == null) {
				sendCreateSequence = true;
			}

			// if fist message - setup the sending side sequence - both for the
			// server and the client sides
			// if (!serverSide && sendCreateSequence) {
			SequenceManager.setupNewClientSequence(msgCtx, internalSequenceId);
		}

		// if first message - add create sequence
		if (sendCreateSequence) {

			SequencePropertyBean responseCreateSeqAdded = seqPropMgr
					.retrieve(
							internalSequenceId,
							Sandesha2Constants.SequenceProperties.OUT_CREATE_SEQUENCE_SENT);

			if (responseCreateSeqAdded == null) {
				responseCreateSeqAdded = new SequencePropertyBean(
						internalSequenceId,
						Sandesha2Constants.SequenceProperties.OUT_CREATE_SEQUENCE_SENT,
						"true");
				seqPropMgr.insert(responseCreateSeqAdded);

				String acksTo = null;
				if (serviceContext != null) {
					acksTo = (String) msgCtx
							.getProperty(Sandesha2ClientAPI.AcksTo);
				}

				if (msgCtx.isServerSide()) {
					// we do not set acksTo value to anonymous when the create
					// sequence is send from the server.

					MessageContext requestMessage = operationContext
							.getMessageContext(OperationContextFactory.MESSAGE_LABEL_IN_VALUE);
					if (requestMessage == null) {
						String message = "Request message is not present";
						log.debug(message);
						throw new SandeshaException(message);
					}

					acksTo = requestMessage.getTo().getAddress();

				} else {
					if (acksTo == null)
						acksTo = Sandesha2Constants.WSA.NS_URI_ANONYMOUS;
				}

				// If acksTo is not anonymous. Start the listner TODO: verify
				if (!Sandesha2Constants.WSA.NS_URI_ANONYMOUS.equals(acksTo)
						&& !serverSide) {
					String transportIn = (String) context
							.getProperty(MessageContext.TRANSPORT_IN);
					if (transportIn == null)
						transportIn = org.apache.axis2.Constants.TRANSPORT_HTTP;
				} else if (acksTo == null && serverSide) {
					String incomingSequencId = SandeshaUtil
							.getServerSideIncomingSeqIdFromInternalSeqId(internalSequenceId);
					SequencePropertyBean bean = seqPropMgr.retrieve(
							incomingSequencId,
							Sandesha2Constants.SequenceProperties.REPLY_TO_EPR);
					if (bean != null) {
						EndpointReference acksToEPR = new EndpointReference(
								bean.getValue());
						if (acksToEPR != null)
							acksTo = (String) acksToEPR.getAddress();
					}
				} else if (Sandesha2Constants.WSA.NS_URI_ANONYMOUS
						.equals(acksTo)) {
					// set transport in.
					Object trIn = msgCtx
							.getProperty(MessageContext.TRANSPORT_IN);
					if (trIn == null) {

					}
				}

				transaction.commit();

				addCreateSequenceMessage(rmMsgCtx, internalSequenceId, acksTo);
			}
		}

		// do response processing
		Transaction responseProcessTransaction = storageManager
				.getTransaction();

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

			// processing the response
			processResponseMessage(rmMsgCtx, internalSequenceId, messageNumber);

			MessageContext reqMsgCtx = msgCtx.getOperationContext()
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			RMMsgContext requestRMMsgCtx = MsgInitializer
					.initializeMessage(reqMsgCtx);

			// let the request end with 202 if a ack has not been
			// written in the incoming thread.
			if (reqMsgCtx.getProperty(Sandesha2Constants.ACK_WRITTEN) == null
					|| !"true".equals(reqMsgCtx
							.getProperty(Sandesha2Constants.ACK_WRITTEN)))
				reqMsgCtx.getOperationContext().setProperty(
						org.apache.axis2.Constants.RESPONSE_WRITTEN, "false");
		} else {
			EndpointReference toEPR = msgCtx.getTo();

			if (toEPR == null) {
				String message = "To EPR is not found";
				log.debug(message);
				throw new SandeshaException(message);
			}

			String to = toEPR.getAddress();
			String operationName = msgCtx.getOperationContext()
					.getAxisOperation().getName().getLocalPart();

			if (msgCtx.getWSAAction() == null) {
				msgCtx.setWSAAction(to + "/" + operationName);
			}

			if (msgCtx.getSoapAction() == null) {
				msgCtx.setSoapAction("\"" + to + "/" + operationName + "\"");
			}

			// processing the response
			processResponseMessage(rmMsgCtx, internalSequenceId, messageNumber);

		}

		// cannot pause since the message have to be saved at the sender
		// pausing the message
		msgCtx.pause();

		responseProcessTransaction.commit();
	}

	public void addCreateSequenceMessage(RMMsgContext applicationRMMsg,
			String internalSequenceId, String acksTo) throws SandeshaException {

		MessageContext applicationMsg = applicationRMMsg.getMessageContext();
		if (applicationMsg == null) {
			String message = "Message context is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		RMMsgContext createSeqRMMessage = RMMsgCreator.createCreateSeqMsg(
				applicationRMMsg, internalSequenceId, acksTo);
		CreateSequence createSequencePart = (CreateSequence) createSeqRMMessage
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSequencePart == null) {
			String message = "Create Sequence part is null for a CreateSequence message";
			log.debug(message);
			throw new SandeshaException(message);
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(applicationMsg
						.getConfigurationContext());

		Transaction createSeqTransaction = storageManager.getTransaction();
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequenceOffer offer = createSequencePart.getSequenceOffer();
		if (offer != null) {
			// Offer processing
			String offeredSequenceId = offer.getIdentifer().getIdentifier();
			SequencePropertyBean msgsBean = new SequencePropertyBean();
			msgsBean.setSequenceID(offeredSequenceId);
			msgsBean
					.setName(Sandesha2Constants.SequenceProperties.COMPLETED_MESSAGES);
			msgsBean.setValue("");

			SequencePropertyBean offeredSequenceBean = new SequencePropertyBean();
			offeredSequenceBean
					.setName(Sandesha2Constants.SequenceProperties.OFFERED_SEQUENCE);
			offeredSequenceBean.setSequenceID(internalSequenceId);
			offeredSequenceBean.setValue(offeredSequenceId);

			seqPropMgr.insert(msgsBean);
			seqPropMgr.insert(offeredSequenceBean);
		}

		MessageContext createSeqMsg = createSeqRMMessage.getMessageContext();
		createSeqMsg.setRelatesTo(null); // create seq msg does not relateTo
		// anything
		AbstractContext context = applicationRMMsg.getContext();
		if (context == null) {
			String message = "Context is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();

		CreateSeqBean createSeqBean = new CreateSeqBean(internalSequenceId,
				createSeqMsg.getMessageID(), null);
		createSeqMgr.insert(createSeqBean);

		if (createSeqMsg.getReplyTo() == null)
			createSeqMsg.setReplyTo(new EndpointReference(
					Sandesha2Constants.WSA.NS_URI_ANONYMOUS));

		SenderBeanMgr retransmitterMgr = storageManager
				.getRetransmitterBeanMgr();

		String key = SandeshaUtil.getUUID();
		storageManager.storeMessageContext(key, createSeqMsg);

		SenderBean createSeqEntry = new SenderBean();
		createSeqEntry.setMessageContextRefKey(key);
		createSeqEntry.setTimeToSend(System.currentTimeMillis());
		createSeqEntry.setMessageID(createSeqRMMessage.getMessageId());

		// this will be set to true in the sender
		createSeqEntry.setSend(true);
		createSeqEntry
				.setMessageType(Sandesha2Constants.MessageTypes.CREATE_SEQ);
		retransmitterMgr.insert(createSeqEntry);
		createSeqTransaction.commit();

		// sending the message once through our sender.
		AxisEngine engine = new AxisEngine(createSeqMsg
				.getConfigurationContext());
		Sandesha2TransportSender sender = new Sandesha2TransportSender();

		// message will be stored in the Sandesha2TransportSender
		createSeqMsg.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY, key);
		// sender.setMessageStoreKey(key);

		TransportSender oldSender = createSeqMsg.getTransportOut().getSender();
		createSeqMsg.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_SENDER,
				oldSender);
		createSeqMsg.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,
				Sandesha2Constants.VALUE_TRUE);
		createSeqMsg.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY, key);

		// setting the sandesha2 sender as the sender.
		// createSeqMsg.getTransportOut().setSender(sender);

		// try {
		// log.info ("Sending create seq msg...");
		// engine.send(createSeqMsg);
		// } catch (AxisFault e) {
		// throw new SandeshaException (e.getMessage());
		// }

	}

	private void processResponseMessage(RMMsgContext rmMsg,
			String internalSequenceId, long messageNumber)
			throws SandeshaException {

		MessageContext msg = rmMsg.getMessageContext();

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(rmMsg.getSOAPEnvelope()));

		if (rmMsg == null) {
			String message = "Message or reques message is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		AbstractContext context = rmMsg.getContext();
		if (context == null) {
			String message = "Context is null";
			log.debug(message);
			throw new SandeshaException(message);
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(msg.getConfigurationContext());
		SequencePropertyBeanMgr sequencePropertyMgr = storageManager
				.getSequencePropretyBeanMgr();

		SenderBeanMgr retransmitterMgr = storageManager
				.getRetransmitterBeanMgr();

		SequencePropertyBean toBean = sequencePropertyMgr.retrieve(
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR);
		SequencePropertyBean replyToBean = sequencePropertyMgr.retrieve(
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.REPLY_TO_EPR);

		// again - looks weird in the client side - but consistent
		SequencePropertyBean outSequenceBean = sequencePropertyMgr.retrieve(
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);

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

		if (toEPR == null || toEPR.getAddress() == null
				|| toEPR.getAddress() == "") {
			String message = "To Property has an invalid value";
			log.debug(message);
			throw new SandeshaException(message);
		}

		String newToStr = null;

		if (msg.isServerSide()) {
			try {
				MessageContext requestMsg = msg.getOperationContext()
						.getMessageContext(
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

		Sequence sequence = new Sequence(factory);

		MessageNumber msgNumber = new MessageNumber(factory);
		msgNumber.setMessageNumber(messageNumber);
		sequence.setMessageNumber(msgNumber);

		boolean lastMessage = false;
		// setting last message
		if (msg.isServerSide()) {
			// server side
			String incomingSeqId = internalSequenceId;
			MessageContext requestMsg = null;

			try {
				requestMsg = msg.getOperationContext().getMessageContext(
						WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}

			RMMsgContext reqRMMsgCtx = MsgInitializer
					.initializeMessage(requestMsg);
			Sequence requestSequence = (Sequence) reqRMMsgCtx
					.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			if (requestSequence == null) {
				String message = "Request Sequence is null";
				log.debug(message);
				throw new SandeshaException(message);
			}

			if (requestSequence.getLastMessage() != null) {
				lastMessage = true;
				sequence.setLastMessage(new LastMessage(factory));

				// saving the last message no.
				SequencePropertyBean lastOutMsgBean = new SequencePropertyBean(
						internalSequenceId,
						Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE,
						new Long(messageNumber).toString());
				sequencePropertyMgr.insert(lastOutMsgBean);
			}

		} else {
			// client side

			OperationContext operationContext = msg.getOperationContext();
			if (operationContext != null) {
				Object obj = msg.getProperty(Sandesha2ClientAPI.LAST_MESSAGE);
				if (obj != null && "true".equals(obj)) {
					lastMessage = true;
					sequence.setLastMessage(new LastMessage(factory));
					// saving the last message no.
					SequencePropertyBean lastOutMsgBean = new SequencePropertyBean(
							internalSequenceId,
							Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE,
							new Long(messageNumber).toString());
					sequencePropertyMgr.insert(lastOutMsgBean);
				}
			}
		}

		AckRequested ackRequested = null;

		boolean addAckRequested = false;
		if (!lastMessage)
			addAckRequested = true;

		// setting the Sequnece id.
		// Set send = true/false depending on the availability of the out
		// sequence id.
		String identifierStr = null;
		if (outSequenceBean == null || outSequenceBean.getValue() == null) {
			identifierStr = Sandesha2Constants.TEMP_SEQUENCE_ID;

		} else {
			identifierStr = (String) outSequenceBean.getValue();
		}

		Identifier id1 = new Identifier(factory);
		id1.setIndentifer(identifierStr);
		sequence.setIdentifier(id1);
		rmMsg
				.setMessagePart(Sandesha2Constants.MessageParts.SEQUENCE,
						sequence);

		if (addAckRequested) {
			ackRequested = new AckRequested(factory);
			Identifier id2 = new Identifier(factory);
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

		// Retransmitter bean entry for the application message
		SenderBean appMsgEntry = new SenderBean();
		// String key = storageManager
		// .storeMessageContext(rmMsg.getMessageContext());
		String storageKey = SandeshaUtil.getUUID();
		storageManager.storeMessageContext(storageKey, msg);

		appMsgEntry.setMessageContextRefKey(storageKey);

		appMsgEntry.setTimeToSend(System.currentTimeMillis());
		appMsgEntry.setMessageID(rmMsg.getMessageId());
		appMsgEntry.setMessageNumber(messageNumber);
		appMsgEntry.setMessageType(Sandesha2Constants.MessageTypes.APPLICATION);
		if (outSequenceBean == null || outSequenceBean.getValue() == null) {
			appMsgEntry.setSend(false);
		} else {
			appMsgEntry.setSend(false);
			// Send will be set to true at the sender.
			msg.setProperty(Sandesha2Constants.SET_SEND_TO_TRUE,
					Sandesha2Constants.VALUE_TRUE);
		}

		appMsgEntry.setInternalSequenceID(internalSequenceId);
		retransmitterMgr.insert(appMsgEntry);

		msg.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,
				Sandesha2Constants.VALUE_FALSE);

		// changing the sender. This will set send to true.
		TransportSender sender = msg.getTransportOut().getSender();

		if (sender != null) {
			Sandesha2TransportSender sandesha2Sender = new Sandesha2TransportSender();

			msg.setProperty(Sandesha2Constants.MESSAGE_STORE_KEY, storageKey);
			// sandesha2Sender.setMessageStoreKey(storageKey);

			// msg.getTransportOut().setSender(sandesha2Sender);
			msg.setProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_SENDER,
					sender);
		}

	}

	private long getNextMsgNo(ConfigurationContext context,
			String internalSequenceId) throws SandeshaException {

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(context);

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();
		SequencePropertyBean nextMsgNoBean = seqPropMgr.retrieve(
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);
		long nextMsgNo = 1;
		boolean update = false;
		if (nextMsgNoBean != null) {
			update = true;
			Long nextMsgNoLng = new Long(nextMsgNoBean.getValue());
			nextMsgNo = nextMsgNoLng.longValue();
		} else {
			nextMsgNoBean = new SequencePropertyBean();
			nextMsgNoBean.setSequenceID(internalSequenceId);
			nextMsgNoBean
					.setName(Sandesha2Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);
		}

		nextMsgNoBean.setValue(new Long(nextMsgNo + 1).toString());
		if (update)
			seqPropMgr.update(nextMsgNoBean);
		else
			seqPropMgr.insert(nextMsgNoBean);

		return nextMsgNo;
	}

	public QName getName() {
		return new QName(Sandesha2Constants.OUT_HANDLER_NAME);
	}
}