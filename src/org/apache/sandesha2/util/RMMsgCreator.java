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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.OperationDescriptionFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.impl.MIMEOutputUtils;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.wsdl.builder.wsdl4j.WSDL11MEPFinder;
import org.apache.log4j.spi.Configurator;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.Constants.MessageParts;
import org.apache.sandesha2.Constants.SOAPVersion;
import org.apache.sandesha2.Constants.SequenceProperties;
import org.apache.sandesha2.Constants.WSA;
import org.apache.sandesha2.Constants.WSRM;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.Address;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.IOMRMPart;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.sandesha2.wsrm.TerminateSequence;
import org.apache.wsdl.WSDLConstants;


/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya
 */

public class RMMsgCreator {

	public static RMMsgContext createCreateSeqMsg(RMMsgContext applicationRMMsg, String tempSequenceId, String acksTo)
			throws SandeshaException {
				
		MessageContext applicationMsgContext = applicationRMMsg
				.getMessageContext();
		if (applicationMsgContext == null)
			throw new SandeshaException("Application message is null");
		ConfigurationContext context = applicationMsgContext.getSystemContext();
		if (context == null)
			throw new SandeshaException("Configuration Context is null");

		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory.getInstance(context).getSequencePropretyBeanMgr();
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
			OperationDescription appMsgOperationDesc = applicationMsgContext.getOperationDescription();
			OperationDescription createSeqOperationDesc = OperationDescriptionFactory.getOperetionDescription(OperationDescriptionFactory.MEP_CONSTANT_OUT_IN);
			createSeqOperationDesc.setPhasesOutFlow(appMsgOperationDesc.getPhasesOutFlow());
			createSeqOperationDesc.setPhasesOutFaultFlow(appMsgOperationDesc.getPhasesOutFaultFlow());
			createSeqOperationDesc.setPhasesInFaultFlow(appMsgOperationDesc.getPhasesInFaultFlow());
			createSeqOperationDesc.setRemainingPhasesInFlow(appMsgOperationDesc.getRemainingPhasesInFlow());
			
			createSeqmsgContext.setOperationDescription(createSeqOperationDesc);
			//TODO set a suitable ope. description
			OperationContext createSeqOpContext = new OperationContext(
					createSeqmsgContext.getOperationDescription());
			createSeqmsgContext.setOperationContext(createSeqOpContext);
			createSeqOpContext.addMessageContext(createSeqmsgContext);
			//registering opearion context
			context.registerOperationContext(createSeqMsgId,createSeqOpContext);

			
			//Setting a new SOAP Envelop.
			SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
					Constants.SOAPVersion.DEFAULT).getDefaultEnvelope();

			createSeqmsgContext.setEnvelope(envelope);
//			createSeqOpContext.addMessageContext(createSeqmsgContext);
//			createSeqmsgContext.setOperationContext(createSeqOpContext);
		} catch (AxisFault e2) {
			throw new SandeshaException(e2.getMessage());
		}

		createSeqmsgContext.setTo(applicationRMMsg.getTo());
		createSeqmsgContext.setReplyTo(applicationRMMsg.getReplyTo());

		RMMsgContext createSeqRMMsg = new RMMsgContext(createSeqmsgContext);

		CreateSequence createSequencePart = new CreateSequence();

		
		//Adding sequence offer - if present
		String offeredSequence = (String) context.getProperty(Constants.OFFERED_SEQUENCE_ID);
		if (offeredSequence!=null && !"".equals(offeredSequence))
		{
			SequenceOffer offerPart = new SequenceOffer ();
			Identifier identifier = new Identifier ();
			identifier.setIndentifer(offeredSequence);
			offerPart.setIdentifier(identifier);
			createSequencePart.setSequenceOffer(offerPart);
		}
			
		//TODO decide - where to send create seq. Acksto or replyTo
		SequencePropertyBean replyToBean = seqPropMgr.retrieve(tempSequenceId, Constants.SequenceProperties.REPLY_TO_EPR);
		SequencePropertyBean toBean = seqPropMgr.retrieve(tempSequenceId,Constants.SequenceProperties.TO_EPR);

		if (toBean==null || toBean.getValue()==null)
			throw new SandeshaException ("To EPR is not set.");
		
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
		
		if (acksTo==null || "".equals(acksTo))
			acksTo = Constants.WSA.NS_URI_ANONYMOUS;
			
		acksToEPR = new EndpointReference (acksTo);	
		
		if (replyToBean!=null && replyToBean.getValue()!=null)
			replyToEPR = (EndpointReference) replyToBean.getValue();

		createSeqRMMsg.setTo(toEPR);
		
		//ReplyTo will be set only if not null.
		if(replyToEPR!=null) 
			createSeqRMMsg.setReplyTo(replyToEPR);
		
		
		//FIXME - Give user a seperate way to set acksTo (client side)
		createSequencePart.setAcksTo(new AcksTo(new Address(acksToEPR)));
		
		createSeqRMMsg.setMessagePart(Constants.MessageParts.CREATE_SEQ,
				createSequencePart);

		try {
			createSeqRMMsg.addSOAPEnvelope();
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		createSeqRMMsg.setAction(Constants.WSRM.ACTION_CREATE_SEQ);
		
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
	
	public static RMMsgContext createTerminateSequenceMessage (RMMsgContext referenceRMMessage, String sequenceId) throws SandeshaException {
		MessageContext referenceMessage = referenceRMMessage.getMessageContext();
		if (referenceMessage==null)
			throw new SandeshaException ("MessageContext is null");
		
		RMMsgContext terminateRMMessage = SandeshaUtil.shallowCopy(referenceRMMessage);
		MessageContext terminateMessage = terminateRMMessage.getMessageContext();
		if (terminateMessage==null)
			throw new SandeshaException ("MessageContext is null");
			
		MessageInformationHeaders newMessageInfoHeaders = new MessageInformationHeaders ();
		terminateMessage.setMessageInformationHeaders(newMessageInfoHeaders);
		terminateMessage.setMessageID(SandeshaUtil.getUUID());
		
		terminateMessage.setServiceGroupContext(referenceMessage
				.getServiceGroupContext());
		terminateMessage.setServiceGroupContextId(referenceMessage
				.getServiceGroupContextId());
		terminateMessage.setServiceContext(referenceMessage
				.getServiceContext());
		terminateMessage.setServiceContextID(referenceMessage
				.getServiceContextID());
		
		terminateMessage.setOperationDescription(referenceMessage.getOperationDescription());
		OperationContext newOperationCtx = new OperationContext (terminateMessage.getOperationDescription());
		try {
			newOperationCtx.addMessageContext(terminateMessage);
		} catch (AxisFault e) {
			throw new SandeshaException (e.getMessage());
		}
		
		terminateMessage.setOperationContext(newOperationCtx);
		
		ConfigurationContext configCtx = terminateMessage.getSystemContext();
		if (configCtx==null)
			throw new SandeshaException ("Configuration Context is null");
		configCtx.registerOperationContext(terminateMessage.getMessageID(),newOperationCtx);

		SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.DEFAULT).getDefaultEnvelope();
		terminateRMMessage.setSOAPEnvelop(envelope);
		
		TerminateSequence terminateSequencePart = new TerminateSequence ();
		Identifier identifier = new Identifier ();
		identifier.setIndentifer(sequenceId);
		terminateSequencePart.setIdentifier(identifier);
		terminateRMMessage.setMessagePart(Constants.MessageParts.TERMINATE_SEQ,terminateSequencePart);
		
		return terminateRMMessage;
	}

	public static RMMsgContext createCreateSeqResponseMsg(
			RMMsgContext createSeqMessage, MessageContext outMessage, String newSequenceID)
			throws AxisFault {

		IOMRMElement messagePart = createSeqMessage
				.getMessagePart(Constants.MessageParts.CREATE_SEQ);
		CreateSequence cs = (CreateSequence) messagePart;

		CreateSequenceResponse response = new CreateSequenceResponse();

		Identifier identifier = new Identifier();
		identifier.setIndentifer(newSequenceID);

		response.setIdentifier(identifier);

		SequenceOffer offer = cs.getSequenceOffer();
		if (offer!=null) {
			String outSequenceId = offer.getIdentifer().getIdentifier();
			
			//TODO do a better validation for the offered out sequence id.
			if (outSequenceId!=null && !"".equals(outSequenceId)) {
				
				Accept accept = new Accept();
				EndpointReference acksToEPR = createSeqMessage.getTo();
				AcksTo acksTo = new AcksTo();
				Address address = new Address();
				address.setEpr(acksToEPR);
				acksTo.setAddress(address);
				accept.setAcksTo(acksTo);
				response.setAccept(accept);	
			}
			
			
		}


		SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).getDefaultEnvelope();
		response.toOMElement(envelope.getBody());
		outMessage.setWSAAction(Constants.WSRM.NS_URI_CREATE_SEQ_RESPONSE);

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
		SOAPEnvelope envelope = applicationMsg.getSOAPEnvelope();
		if (envelope == null) {
			SOAPEnvelope newEnvelope = SOAPAbstractFactory.getSOAPFactory(
					Constants.SOAPVersion.DEFAULT).getDefaultEnvelope();
			applicationMsg.setSOAPEnvelop(newEnvelope);
		}
		envelope = applicationMsg.getSOAPEnvelope();

		SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement();
		Identifier id = new Identifier();
		id.setIndentifer(sequenceId);
		sequenceAck.setIdentifier(id);

		ConfigurationContext ctx = applicationMsg.getMessageContext()
				.getSystemContext();
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(ctx).getSequencePropretyBeanMgr();

		SequencePropertyBean seqBean = seqPropMgr.retrieve(sequenceId,
				Constants.SequenceProperties.RECEIVED_MESSAGES);
		String msgNoList = (String) seqBean.getValue();
		
		AcknowledgementRange[] ackRangeArr = SandeshaUtil
				.getAckRangeArray(msgNoList);

		int length = ackRangeArr.length;

		for (int i = 0; i < length; i++)
			sequenceAck.addAcknowledgementRanges(ackRangeArr[i]);

		sequenceAck.toOMElement(envelope.getHeader());
		applicationMsg.setAction(Constants.WSRM.ACTION_SEQ_ACK);
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
					.getOperationDescription());

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