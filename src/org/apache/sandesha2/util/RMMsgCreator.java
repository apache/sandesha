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

import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
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
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
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

public class RMMsgCreator {

	public static RMMsgContext createCreateSeqMsg(
			RMMsgContext applicationRMMsg, String tempSequenceId, String acksTo)
			throws SandeshaException {

		MessageContext applicationMsgContext = applicationRMMsg
				.getMessageContext();
		if (applicationMsgContext == null)
			throw new SandeshaException("Application message is null");
		ConfigurationContext context = applicationMsgContext.getSystemContext();
		if (context == null)
			throw new SandeshaException("Configuration Context is null");

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion ( applicationMsgContext.getEnvelope()));
		
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(context);
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();
		MessageContext createSeqmsgContext;
		try {
			//creating by copying common contents. (this will not set contexts
			// except for configCtx).
			createSeqmsgContext = SandeshaUtil
					.shallowCopy(applicationMsgContext);
		} catch (SandeshaException e) {
			throw new SandeshaException(e.getMessage());
		}

		//setting contexts
		createSeqmsgContext.setServiceGroupContext(applicationMsgContext
				.getServiceGroupContext());
		createSeqmsgContext.setServiceGroupContextId(applicationMsgContext
				.getServiceGroupContextId());
		createSeqmsgContext.setServiceContext(applicationMsgContext
				.getServiceContext());
		createSeqmsgContext.setServiceContextID(applicationMsgContext
				.getServiceContextID());

		String createSeqMsgId = SandeshaUtil.getUUID();
		try {
			AxisOperation appMsgOperationDesc = applicationMsgContext
					.getAxisOperation();
			AxisOperation createSeqOperationDesc = AxisOperationFactory
					.getOperetionDescription(AxisOperationFactory.MEP_URI_OUT_IN);
			
			createSeqOperationDesc.setName(new QName ("CreateSequenceOperation"));
			if (appMsgOperationDesc != null) {
				createSeqOperationDesc.setPhasesOutFlow(appMsgOperationDesc
						.getPhasesOutFlow());
				createSeqOperationDesc
						.setPhasesOutFaultFlow(appMsgOperationDesc
								.getPhasesOutFaultFlow());
				createSeqOperationDesc.setPhasesInFaultFlow(appMsgOperationDesc
						.getPhasesInFaultFlow());
				createSeqOperationDesc
						.setRemainingPhasesInFlow(appMsgOperationDesc
								.getRemainingPhasesInFlow());
			}

			createSeqmsgContext.setAxisOperation(createSeqOperationDesc);
			//TODO set a suitable ope. description
			OperationContext createSeqOpContext = new OperationContext(
					createSeqmsgContext.getAxisOperation());
			
			createSeqmsgContext.setOperationContext(createSeqOpContext);
			createSeqOpContext.addMessageContext(createSeqmsgContext);
			//registering opearion context
			context
					.registerOperationContext(createSeqMsgId,
							createSeqOpContext);

			//Setting a new SOAP Envelop.
			
			SOAPEnvelope envelope = factory.getDefaultEnvelope();

			createSeqmsgContext.setEnvelope(envelope);
			//			createSeqOpContext.addMessageContext(createSeqmsgContext);
			//			createSeqmsgContext.setOperationContext(createSeqOpContext);
		} catch (AxisFault e2) {
			throw new SandeshaException(e2.getMessage());
		}

		createSeqmsgContext.setTo(applicationRMMsg.getTo());
		createSeqmsgContext.setReplyTo(applicationRMMsg.getReplyTo());

		RMMsgContext createSeqRMMsg = new RMMsgContext(createSeqmsgContext);

		CreateSequence createSequencePart = new CreateSequence(factory);

		//Adding sequence offer - if present
		String offeredSequence = (String) context
				.getProperty(Constants.OFFERED_SEQUENCE_ID);
		if (offeredSequence != null && !"".equals(offeredSequence)) {
			SequenceOffer offerPart = new SequenceOffer(factory);
			Identifier identifier = new Identifier(factory);
			identifier.setIndentifer(offeredSequence);
			offerPart.setIdentifier(identifier);
			createSequencePart.setSequenceOffer(offerPart);
		}

		//TODO decide - where to send create seq. Acksto or replyTo
		SequencePropertyBean replyToBean = seqPropMgr.retrieve(tempSequenceId,
				Constants.SequenceProperties.REPLY_TO_EPR);
		SequencePropertyBean toBean = seqPropMgr.retrieve(tempSequenceId,
				Constants.SequenceProperties.TO_EPR);

		if (toBean == null || toBean.getValue() == null)
			throw new SandeshaException("To EPR is not set.");

		EndpointReference toEPR = (EndpointReference) toBean.getValue();
		EndpointReference replyToEPR = null;
		EndpointReference acksToEPR = null;

		//AcksTo value is replyto value (if set). Otherwise anonymous.
		//		if (replyToBean==null || replyToBean.getValue()==null){
		//			if (acksTo==null)
		//			acksToEPR = new EndpointReference (Constants.WSA.NS_URI_ANONYMOUS);
		//		}else {
		//			acksToEPR = (EndpointReference) replyToBean.getValue();
		//		}

		if (acksTo == null || "".equals(acksTo))
			acksTo = Constants.WSA.NS_URI_ANONYMOUS;

		acksToEPR = new EndpointReference(acksTo);

		if (replyToBean != null && replyToBean.getValue() != null)
			replyToEPR = (EndpointReference) replyToBean.getValue();

		createSeqRMMsg.setTo(toEPR);

		//ReplyTo will be set only if not null.
		if (replyToEPR != null)
			createSeqRMMsg.setReplyTo(replyToEPR);

		//FIXME - Give user a seperate way to set acksTo (client side)
		createSequencePart.setAcksTo(new AcksTo(new Address(acksToEPR,factory),factory));

		createSeqRMMsg.setMessagePart(Constants.MessageParts.CREATE_SEQ,
				createSequencePart);

		try {
			createSeqRMMsg.addSOAPEnvelope();
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		createSeqRMMsg.setAction(Constants.WSRM.Actions.ACTION_CREATE_SEQUENCE);
		createSeqRMMsg.setSOAPAction(Constants.WSRM.Actions.SOAP_ACTION_CREATE_SEQUENCE);
		createSeqRMMsg.setMessageId(createSeqMsgId);

		MessageContext createSeqMsg = createSeqRMMsg.getMessageContext();
		MessageContext applicationMsg = applicationRMMsg.getMessageContext();
		createSeqMsg.setServiceGroupContext(applicationMsg
				.getServiceGroupContext());
		createSeqMsg.setServiceGroupContextId(applicationMsg
				.getServiceGroupContextId());
		createSeqMsg.setServiceContext(applicationMsg.getServiceContext());
		createSeqMsg.setServiceContextID(applicationMsg.getServiceContextID());

		return createSeqRMMsg;
	}

	public static RMMsgContext createTerminateSequenceMessage(
			RMMsgContext referenceRMMessage, String sequenceId)
			throws SandeshaException {
		MessageContext referenceMessage = referenceRMMessage
				.getMessageContext();
		if (referenceMessage == null)
			throw new SandeshaException("MessageContext is null");

		RMMsgContext terminateRMMessage = SandeshaUtil
				.shallowCopy(referenceRMMessage);
		MessageContext terminateMessage = terminateRMMessage
				.getMessageContext();
		if (terminateMessage == null)
			throw new SandeshaException("MessageContext is null");

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion ( referenceMessage.getEnvelope()));

		MessageInformationHeaders newMessageInfoHeaders = new MessageInformationHeaders();
		terminateMessage.setMessageInformationHeaders(newMessageInfoHeaders);
		terminateMessage.setMessageID(SandeshaUtil.getUUID());

		ConfigurationContext configCtx = referenceMessage.getSystemContext();
		if (configCtx == null)
			throw new SandeshaException("Configuration Context is null");

		AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
		AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
		AxisService service = new AxisService(new QName("RMClientService")); // This
																			 // is a
																			 // dummy
																			 // service.
		ServiceGroupContext serviceGroupContext = new ServiceGroupContext(
				configCtx, serviceGroup);
		ServiceContext serviceContext = new ServiceContext(service,
				serviceGroupContext);

		terminateMessage.setAxisServiceGroup(serviceGroup);
		terminateMessage.setServiceGroupContext(serviceGroupContext);
		terminateMessage.setAxisService(service);
		terminateMessage.setServiceContext(serviceContext);

		try {
			AxisOperation terminateOperaiton = AxisOperationFactory
					.getOperetionDescription(AxisOperationFactory.MEP_URI_IN_ONLY);
			AxisOperation referenceMsgOperation = referenceMessage
					.getAxisOperation();
			if (referenceMsgOperation != null) {
				ArrayList outPhases = referenceMsgOperation.getPhasesOutFlow();
				if (outPhases != null) {
					terminateOperaiton.setPhasesOutFlow(outPhases);
					terminateOperaiton.setPhasesOutFaultFlow(outPhases);
				}
			}

			OperationContext terminateOpContext = new OperationContext(
					terminateOperaiton);
			terminateMessage.setAxisOperation(terminateOperaiton);
			terminateMessage.setOperationContext(terminateOpContext);
			terminateOpContext.addMessageContext(terminateMessage);
			terminateMessage.setOperationContext(terminateOpContext);

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		terminateRMMessage.setSOAPEnvelop(envelope);

		TerminateSequence terminateSequencePart = new TerminateSequence(factory);
		Identifier identifier = new Identifier(factory);
		identifier.setIndentifer(sequenceId);
		terminateSequencePart.setIdentifier(identifier);
		terminateRMMessage.setMessagePart(Constants.MessageParts.TERMINATE_SEQ,
				terminateSequencePart);

		return terminateRMMessage;
	}

	public static RMMsgContext createCreateSeqResponseMsg(
			RMMsgContext createSeqMessage, MessageContext outMessage,
			String newSequenceID) throws AxisFault {

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion ( createSeqMessage.getSOAPEnvelope()));
		
		IOMRMElement messagePart = createSeqMessage
				.getMessagePart(Constants.MessageParts.CREATE_SEQ);
		CreateSequence cs = (CreateSequence) messagePart;

		CreateSequenceResponse response = new CreateSequenceResponse(factory);

		Identifier identifier = new Identifier(factory);
		identifier.setIndentifer(newSequenceID);

		response.setIdentifier(identifier);

		SequenceOffer offer = cs.getSequenceOffer();
		if (offer != null) {
			String outSequenceId = offer.getIdentifer().getIdentifier();

			//TODO do a better validation for the offered out sequence id.
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
		outMessage.setWSAAction(Constants.WSRM.Actions.ACTION_CREATE_SEQUENCE_RESPONSE);
		outMessage.setSoapAction(Constants.WSRM.Actions.SOAP_ACTION_CREATE_SEQUENCE_RESPONSE);

		String newMessageId = SandeshaUtil.getUUID();
		outMessage.setMessageID(newMessageId);

		outMessage.setEnvelope(envelope);

		RMMsgContext createSeqResponse = null;
		try {
			createSeqResponse = MsgInitializer.initializeMessage(outMessage);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		return createSeqResponse;
	}

	//Adds a ack message to the following message.
	public static void addAckMessage(RMMsgContext applicationMsg,
			String sequenceId) throws SandeshaException {
		
		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion ( applicationMsg.getSOAPEnvelope()));

		SOAPEnvelope envelope = applicationMsg.getSOAPEnvelope();
		if (envelope == null) {
			SOAPEnvelope newEnvelope = factory.getDefaultEnvelope();
			applicationMsg.setSOAPEnvelop(newEnvelope);
		}
		envelope = applicationMsg.getSOAPEnvelope();

		SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement(factory);
		Identifier id = new Identifier(factory);
		id.setIndentifer(sequenceId);
		sequenceAck.setIdentifier(id);

		ConfigurationContext ctx = applicationMsg.getMessageContext()
				.getSystemContext();
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(ctx);
		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean seqBean = seqPropMgr.retrieve(sequenceId,
				Constants.SequenceProperties.RECEIVED_MESSAGES);
		String msgNoList = (String) seqBean.getValue();

		AcknowledgementRange[] ackRangeArr = SandeshaUtil
				.getAckRangeArray(msgNoList,factory);

		int length = ackRangeArr.length;

		for (int i = 0; i < length; i++)
			sequenceAck.addAcknowledgementRanges(ackRangeArr[i]);

		sequenceAck.toOMElement(envelope.getHeader());
		applicationMsg.setAction(Constants.WSRM.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT);
		applicationMsg.setSOAPAction(Constants.WSRM.Actions.SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT);
		applicationMsg.setMessageId(SandeshaUtil.getUUID());

	}

	public static RMMsgContext createAckMessage(RMMsgContext applicationRMMsgCtx)
			throws SandeshaException {
		try {
			MessageContext applicationMsgCtx = applicationRMMsgCtx
					.getMessageContext();
			MessageContext ackMsgCtx = SandeshaUtil
					.shallowCopy(applicationMsgCtx);
			ackMsgCtx.setServiceGroupContext(applicationMsgCtx
					.getServiceGroupContext());
			ackMsgCtx.setServiceGroupContextId(applicationMsgCtx
					.getServiceGroupContextId());
			ackMsgCtx.setServiceContext(applicationMsgCtx.getServiceContext());
			ackMsgCtx.setServiceContextID(applicationMsgCtx
					.getServiceContextID());

			RMMsgContext ackRMMsgCtx = new RMMsgContext(ackMsgCtx);

			//TODO set a suitable description
			OperationContext ackOpCtx = new OperationContext(applicationMsgCtx
					.getAxisOperation());

			ackMsgCtx.setOperationContext(ackOpCtx);
			ackOpCtx.addMessageContext(ackMsgCtx);

			Sequence reqSequence = (Sequence) applicationRMMsgCtx
					.getMessagePart(Constants.MessageParts.SEQUENCE);
			if (reqSequence == null)
				throw new SandeshaException(
						"Sequence part of application message is null");

			String sequenceId = reqSequence.getIdentifier().getIdentifier();

			addAckMessage(ackRMMsgCtx, sequenceId);
			return ackRMMsgCtx;
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}
	}
}