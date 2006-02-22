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

package org.apache.sandesha2.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.Address;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * Used to create new RM messages.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class RMMsgCreator {

	private static Log log = LogFactory.getLog(RMMsgCreator.class);

	
	private static void initializeCreation1(MessageContext relatedMessage,
			MessageContext newMessage) {
	}

	private static void finalizeCreation1(MessageContext relatedMessage,
			MessageContext newMessage) throws SandeshaException {

	}
	
	
	
	
	private static void initializeCreation(MessageContext relatedMessage,
			MessageContext newMessage) throws SandeshaException {
		// Seting RMPolicyBean
		// if
		// (rmMsgCtx.getProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN)==null)
		// rmMsgCtx.setProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN,
		// PropertyManager.getInstance().getRMPolicyBean());

		Parameter policyParam = relatedMessage
				.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
		// if (propertyParam!=null)
		// newMessage.setProperty(propertyParam.getName(),propertyParam.getValue());

		if (policyParam != null) {

			try {
				// TODO this should be added to the AxisMessage
				if (newMessage.getAxisOperation() != null)
					newMessage.getAxisOperation().addParameter(policyParam);
				else if (newMessage.getAxisService() != null) {
					newMessage.getAxisService().addParameter(policyParam);

				}
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}
		}
	}

	private static void finalizeCreation(MessageContext relatedMessage,
			MessageContext newMessage) throws SandeshaException {

		newMessage.setServerSide(relatedMessage.isServerSide());

		// adding all parameters from old message to the new one.
		try {
			// axisOperation parameters
			AxisOperation oldAxisOperation = relatedMessage.getAxisOperation();
			if (oldAxisOperation != null) {
				ArrayList axisOpParams = oldAxisOperation.getParameters();
				if (axisOpParams != null) {
					AxisOperation newAxisOperation = newMessage
							.getAxisOperation();
					Iterator iter = axisOpParams.iterator();
					while (iter.hasNext()) {
						Parameter nextParam = (Parameter) iter.next();
						Parameter newParam = new ParameterImpl();

						newParam.setName(nextParam.getName());
						newParam.setValue(nextParam.getValue());

						newAxisOperation.addParameter(newParam);
					}
				}
			}

		} catch (AxisFault e) {
			log
					.error("Could not copy parameters when creating the new RM Message");
			throw new SandeshaException(e.getMessage());
		}

		// TODO optimize by cloning the Map rather than copying one by one.

		// operationContext properties
		OperationContext oldOpContext = relatedMessage.getOperationContext();
		if (oldOpContext != null) {
			Map oldOpContextProperties = oldOpContext.getProperties();
			if (oldOpContextProperties != null) {
				OperationContext newOpContext = newMessage
						.getOperationContext();
				Iterator keyIter = oldOpContextProperties.keySet().iterator();
				while (keyIter.hasNext()) {
					String key = (String) keyIter.next();
					newOpContext.setProperty(key, oldOpContextProperties
							.get(key));
					// newAxisOperation.addParameter(new ParameterImpl
					// (nextParam.getName(),(String) nextParam.getValue()));
				}
			}
		}

		// MessageContext properties
		if (relatedMessage != null && newMessage != null) {
			Map oldMsgContextProperties = relatedMessage.getProperties();
			if (oldMsgContextProperties != null) {
				Iterator keyIter = oldMsgContextProperties.keySet().iterator();
				while (keyIter.hasNext()) {
					String key = (String) keyIter.next();
					newMessage.setProperty(key, oldMsgContextProperties
							.get(key));
					// newAxisOperation.addParameter(new ParameterImpl
					// (nextParam.getName(),(String) nextParam.getValue()));
				}
			}
		}

		// setting an options with properties copied from the old one.
		Options relatesMessageOptions = relatedMessage.getOptions();
		if (relatesMessageOptions != null) {
			Options newMessageOptions = newMessage.getOptions();
			if (newMessageOptions == null) {
				newMessageOptions = new Options();
				newMessage.setOptions(newMessageOptions);
			}
			

			Map relatedMessageProperties = relatesMessageOptions
					.getProperties();
			Iterator keys = relatedMessageProperties.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				newMessageOptions.setProperty(key, relatedMessageProperties
						.get(key));
			}

			Options relatedMessageParentOptions = relatesMessageOptions
					.getParent();
			if (relatedMessageParentOptions != null) {
				Map relatedMessageParentProperties = relatedMessageParentOptions.getProperties();
				keys = relatedMessageParentProperties.keySet().iterator();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					newMessageOptions.setProperty(key,
							relatedMessageParentProperties.get(key));
				}
			}
		}
	}

	/**
	 * Create a new CreateSeqnence message.
	 * 
	 * @param applicationRMMsg
	 * @param internalSequenceId
	 * @param acksTo
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext createCreateSeqMsg(
			RMMsgContext applicationRMMsg, String internalSequenceId,
			String acksTo) throws SandeshaException {

		MessageContext applicationMsgContext = applicationRMMsg
				.getMessageContext();
		if (applicationMsgContext == null)
			throw new SandeshaException("Application message is null");
		ConfigurationContext context = applicationMsgContext
				.getConfigurationContext();
		if (context == null)
			throw new SandeshaException("Configuration Context is null");

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(applicationMsgContext.getEnvelope()));

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(context);
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();
		MessageContext createSeqmsgContext;
		try {
			// creating by copying common contents. (this will not set contexts
			// except for configCtx).
			AxisOperation createSequenceOperation = AxisOperationFactory
					.getAxisOperation(AxisOperation.MEP_CONSTANT_OUT_IN);

			createSeqmsgContext = SandeshaUtil.createNewRelatedMessageContext(
					applicationRMMsg, createSequenceOperation);

			initializeCreation(applicationMsgContext, createSeqmsgContext);

			OperationContext createSeqOpCtx = createSeqmsgContext
					.getOperationContext();
			String createSeqMsgId = SandeshaUtil.getUUID();
			createSeqmsgContext.setMessageID(createSeqMsgId);
			context.registerOperationContext(createSeqMsgId, createSeqOpCtx);

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		// setUpMessage(applicationMsgContext, createSeqmsgContext);

		AxisOperation appMsgOperationDesc = applicationMsgContext
				.getAxisOperation();

		AxisOperation createSeqOperation = createSeqmsgContext
				.getAxisOperation();
		
		createSeqOperation.setName(new QName("CreateSequenceOperation"));
		if (appMsgOperationDesc != null) {
			createSeqOperation.setPhasesOutFlow(appMsgOperationDesc
					.getPhasesOutFlow());
			createSeqOperation.setPhasesOutFaultFlow(appMsgOperationDesc
					.getPhasesOutFaultFlow());
			createSeqOperation.setPhasesInFaultFlow(appMsgOperationDesc
					.getPhasesInFaultFlow());
			createSeqOperation.setRemainingPhasesInFlow(appMsgOperationDesc
					.getRemainingPhasesInFlow());
		}

		createSeqmsgContext.setAxisOperation(createSeqOperation);

		createSeqmsgContext.setTo(applicationRMMsg.getTo());
		createSeqmsgContext.setReplyTo(applicationRMMsg.getReplyTo());

		RMMsgContext createSeqRMMsg = new RMMsgContext(createSeqmsgContext);

		CreateSequence createSequencePart = new CreateSequence(factory);

		// Adding sequence offer - if present
		OperationContext operationcontext = applicationMsgContext
				.getOperationContext();
		if (operationcontext != null) {
			String offeredSequence = (String) applicationMsgContext
					.getProperty(Sandesha2ClientAPI.OFFERED_SEQUENCE_ID);
			if (offeredSequence != null && !"".equals(offeredSequence)) {
				SequenceOffer offerPart = new SequenceOffer(factory);
				Identifier identifier = new Identifier(factory);
				identifier.setIndentifer(offeredSequence);
				offerPart.setIdentifier(identifier);
				createSequencePart.setSequenceOffer(offerPart);
			}
		}

		SequencePropertyBean replyToBean = seqPropMgr.retrieve(
				internalSequenceId,
				Sandesha2Constants.SequenceProperties.REPLY_TO_EPR);
		SequencePropertyBean toBean = seqPropMgr.retrieve(internalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR);

		if (toBean == null || toBean.getValue() == null)
			throw new SandeshaException("To EPR is not set.");

		EndpointReference toEPR = new EndpointReference(toBean.getValue());
		EndpointReference replyToEPR = null;
		EndpointReference acksToEPR = null;

		if (acksTo == null || "".equals(acksTo))
			acksTo = Sandesha2Constants.WSA.NS_URI_ANONYMOUS;

		acksToEPR = new EndpointReference(acksTo);

		if (replyToBean != null && replyToBean.getValue() != null)
			replyToEPR = new EndpointReference(replyToBean.getValue());

		createSeqRMMsg.setTo(toEPR);

		// ReplyTo will be set only if not null.
		if (replyToEPR != null)
			createSeqRMMsg.setReplyTo(replyToEPR);

		createSequencePart.setAcksTo(new AcksTo(
				new Address(acksToEPR, factory), factory));

		createSeqRMMsg.setMessagePart(
				Sandesha2Constants.MessageParts.CREATE_SEQ, createSequencePart);

		try {
			createSeqRMMsg.addSOAPEnvelope();
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		createSeqRMMsg
				.setAction(Sandesha2Constants.WSRM.Actions.ACTION_CREATE_SEQUENCE);
		createSeqRMMsg
				.setSOAPAction(Sandesha2Constants.WSRM.Actions.SOAP_ACTION_CREATE_SEQUENCE);

		finalizeCreation(applicationMsgContext, createSeqmsgContext);

		return createSeqRMMsg;
	}

	/**
	 * Creates a new TerminateSequence message.
	 * 
	 * @param referenceRMMessage
	 * @param sequenceId
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext createTerminateSequenceMessage(
			RMMsgContext referenceRMMessage, String sequenceId)
			throws SandeshaException {
		MessageContext referenceMessage = referenceRMMessage
				.getMessageContext();
		if (referenceMessage == null)
			throw new SandeshaException("MessageContext is null");

		AxisOperation terminateOperation = null;

		try {
			terminateOperation = AxisOperationFactory
					.getAxisOperation(AxisOperationFactory.MEP_CONSTANT_OUT_ONLY);
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		if (terminateOperation == null)
			throw new SandeshaException("Terminate Operation was null");

		MessageContext terminateMessage = SandeshaUtil
				.createNewRelatedMessageContext(referenceRMMessage,
						terminateOperation);

		initializeCreation(referenceMessage, terminateMessage);

		RMMsgContext terminateRMMessage = MsgInitializer
				.initializeMessage(terminateMessage);

		if (terminateMessage == null)
			throw new SandeshaException("MessageContext is null");

		// setUpMessage(referenceMessage, terminateMessage);

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(referenceMessage.getEnvelope()));

		terminateMessage.setMessageID(SandeshaUtil.getUUID());

		ConfigurationContext configCtx = referenceMessage
				.getConfigurationContext();
		if (configCtx == null)
			throw new SandeshaException("Configuration Context is null");

		AxisOperation referenceMsgOperation = referenceMessage
				.getAxisOperation();
		if (referenceMsgOperation != null) {
			ArrayList outPhases = referenceMsgOperation.getPhasesOutFlow();
			if (outPhases != null) {
				terminateOperation.setPhasesOutFlow(outPhases);
				terminateOperation.setPhasesOutFaultFlow(outPhases);
			}
		}
		
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		terminateRMMessage.setSOAPEnvelop(envelope);

		TerminateSequence terminateSequencePart = new TerminateSequence(factory);
		Identifier identifier = new Identifier(factory);
		identifier.setIndentifer(sequenceId);
		terminateSequencePart.setIdentifier(identifier);
		terminateRMMessage.setMessagePart(
				Sandesha2Constants.MessageParts.TERMINATE_SEQ,
				terminateSequencePart);

		finalizeCreation(referenceMessage, terminateMessage);
		
		terminateMessage.setProperty(MessageContext.TRANSPORT_IN,null);   //no need for an incoming transport for an terminate
																		  //message. If this is put, sender will look for an response.
		
		return terminateRMMessage;
	}

	/**
	 * Create a new CreateSequenceResponse message.
	 * 
	 * @param createSeqMessage
	 * @param outMessage
	 * @param newSequenceID
	 * @return
	 * @throws AxisFault
	 */
	public static RMMsgContext createCreateSeqResponseMsg(
			RMMsgContext createSeqMessage, MessageContext outMessage,
			String newSequenceID) throws AxisFault {

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(createSeqMessage.getSOAPEnvelope()));

		IOMRMElement messagePart = createSeqMessage
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		CreateSequence cs = (CreateSequence) messagePart;

		CreateSequenceResponse response = new CreateSequenceResponse(factory);

		Identifier identifier = new Identifier(factory);
		identifier.setIndentifer(newSequenceID);

		response.setIdentifier(identifier);

		SequenceOffer offer = cs.getSequenceOffer();
		if (offer != null) {
			String outSequenceId = offer.getIdentifer().getIdentifier();

			if (outSequenceId != null && !"".equals(outSequenceId)) {

				Accept accept = new Accept(factory);
				EndpointReference acksToEPR = createSeqMessage.getTo();
				AcksTo acksTo = new AcksTo(factory);
				Address address = new Address(factory);
				address.setEpr(acksToEPR);
				acksTo.setAddress(address);
				accept.setAcksTo(acksTo);
				response.setAccept(accept);
			}

		}

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		response.toOMElement(envelope.getBody());
		outMessage
				.setWSAAction(Sandesha2Constants.WSRM.Actions.ACTION_CREATE_SEQUENCE_RESPONSE);
		outMessage
				.setSoapAction(Sandesha2Constants.WSRM.Actions.SOAP_ACTION_CREATE_SEQUENCE_RESPONSE);

		String newMessageId = SandeshaUtil.getUUID();
		outMessage.setMessageID(newMessageId);

		outMessage.setEnvelope(envelope);

		initializeCreation(createSeqMessage.getMessageContext(), outMessage);

		RMMsgContext createSeqResponse = null;
		try {
			createSeqResponse = MsgInitializer.initializeMessage(outMessage);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		finalizeCreation(createSeqMessage.getMessageContext(), outMessage);

		return createSeqResponse;
	}

	/**
	 * Adds an ack message to the given application message.
	 * 
	 * @param applicationMsg
	 * @param sequenceId
	 * @throws SandeshaException
	 */
	public static void addAckMessage(RMMsgContext applicationMsg,
			String sequenceId) throws SandeshaException {

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(applicationMsg.getSOAPEnvelope()));

		SOAPEnvelope envelope = applicationMsg.getSOAPEnvelope();
		if (envelope == null) {
			SOAPEnvelope newEnvelope = factory.getDefaultEnvelope();
			applicationMsg.setSOAPEnvelop(newEnvelope);
		}
		envelope = applicationMsg.getSOAPEnvelope();

		SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement(
				factory);
		Identifier id = new Identifier(factory);
		id.setIndentifer(sequenceId);
		sequenceAck.setIdentifier(id);

		ConfigurationContext ctx = applicationMsg.getMessageContext()
				.getConfigurationContext();
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(ctx);
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean seqBean = seqPropMgr.retrieve(sequenceId,
				Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);
		String msgNoList = (String) seqBean.getValue();

		ArrayList ackRangeArrayList = SandeshaUtil.getAckRangeArrayList(
				msgNoList, factory);
		Iterator iterator = ackRangeArrayList.iterator();
		while (iterator.hasNext()) {
			AcknowledgementRange ackRange = (AcknowledgementRange) iterator
					.next();
			sequenceAck.addAcknowledgementRanges(ackRange);
		}

		sequenceAck.toOMElement(envelope.getHeader());
		applicationMsg
				.setAction(Sandesha2Constants.WSRM.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT);
		applicationMsg
				.setSOAPAction(Sandesha2Constants.WSRM.Actions.SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT);
		applicationMsg.setMessageId(SandeshaUtil.getUUID());

	}

	/**
	 * Create a new Acknowledgement message.
	 * 
	 * @param applicationRMMsgCtx
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext createAckMessage(RMMsgContext applicationRMMsgCtx)
			throws SandeshaException {
		try {
			MessageContext applicationMsgCtx = applicationRMMsgCtx
					.getMessageContext();

			AxisOperation ackOperation = AxisOperationFactory
					.getAxisOperation(AxisOperationFactory.MEP_CONSTANT_OUT_ONLY);

			MessageContext ackMsgCtx = SandeshaUtil
					.createNewRelatedMessageContext(applicationRMMsgCtx,
							ackOperation);
			RMMsgContext ackRMMsgCtx = MsgInitializer
					.initializeMessage(ackMsgCtx);

			initializeCreation(applicationMsgCtx, ackMsgCtx);

			Sequence reqSequence = (Sequence) applicationRMMsgCtx
					.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			if (reqSequence == null)
				throw new SandeshaException(
						"Sequence part of application message is null");

			String sequenceId = reqSequence.getIdentifier().getIdentifier();

			addAckMessage(ackRMMsgCtx, sequenceId);

			finalizeCreation(applicationMsgCtx, ackMsgCtx);

			return ackRMMsgCtx;
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}
	}

}