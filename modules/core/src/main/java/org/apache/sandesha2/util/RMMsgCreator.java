/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sandesha2.util;

import java.util.ArrayList;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.policy.SandeshaPolicyBean;
import org.apache.sandesha2.security.SecurityManager;
import org.apache.sandesha2.security.SecurityToken;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beans.RMDBean;
import org.apache.sandesha2.storage.beans.RMSBean;
import org.apache.sandesha2.storage.beans.RMSequenceBean;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.CloseSequence;
import org.apache.sandesha2.wsrm.CloseSequenceResponse;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Endpoint;
import org.apache.sandesha2.wsrm.IOMRMPart;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.LastMessageNumber;
import org.apache.sandesha2.wsrm.MakeConnection;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.sandesha2.wsrm.TerminateSequence;
import org.apache.sandesha2.wsrm.TerminateSequenceResponse;
import org.apache.sandesha2.wsrm.UsesSequenceSTR;

/**
 * Used to create new RM messages.
 */

public class RMMsgCreator {

	private static Log log = LogFactory.getLog(RMMsgCreator.class);

	public static final String ACK_TO_BE_WRITTEN = "ackToBeWritten";

	/**
	 * Create a new CreateSequence message.
	 *
	 * @param applicationRMMsg
	 * @param internalSequenceId
	 * @param acksToEPR
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext createCreateSeqMsg(RMSBean rmsBean, RMMsgContext applicationRMMsg) throws AxisFault {
		if(log.isDebugEnabled()) log.debug("Entry: RMMsgCreator::createCreateSeqMsg " + applicationRMMsg);

		MessageContext applicationMsgContext = applicationRMMsg.getMessageContext();
		if (applicationMsgContext == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.appMsgIsNull));
		ConfigurationContext context = applicationMsgContext.getConfigurationContext();
		if (context == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.configContextNotSet));

		// creating by copying common contents. (this will not set contexts
		// except for configCtx).
		AxisOperation createSequenceOperation = SpecSpecificConstants.getWSRMOperation(
				Sandesha2Constants.MessageTypes.CREATE_SEQ,
				rmsBean.getRMVersion(),
				applicationMsgContext.getAxisService());

		MessageContext createSeqmsgContext = SandeshaUtil
				.createNewRelatedMessageContext(applicationRMMsg, createSequenceOperation);

		OperationContext createSeqOpCtx = createSeqmsgContext.getOperationContext();
		String createSeqMsgId = SandeshaUtil.getUUID();
		createSeqmsgContext.setMessageID(createSeqMsgId);
		context.registerOperationContext(createSeqMsgId, createSeqOpCtx);

		RMMsgContext createSeqRMMsg = new RMMsgContext(createSeqmsgContext);

		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmsBean.getRMVersion());

		// Decide which addressing version to use. We copy the version that the application
		// is already using (if set), and fall back to the level in the spec if that isn't
		// found.
		String addressingNamespace = (String) applicationMsgContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
		Boolean disableAddressing = (Boolean) applicationMsgContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
		if(addressingNamespace == null) {
			// Addressing may still be enabled, as it defaults to the final spec. The only time
			// we follow the RM spec is when addressing has been explicitly disabled.
			if(disableAddressing != null && disableAddressing.booleanValue())
				addressingNamespace = SpecSpecificConstants.getAddressingNamespace(rmNamespaceValue);
			else
				addressingNamespace = AddressingConstants.Final.WSA_NAMESPACE;
		}
		if(log.isDebugEnabled()) log.debug("RMMsgCreator:: addressing name space is " + addressingNamespace);

		// If acksTo has not been set, then default to anonymous, using the correct spec level
		EndpointReference acksToEPR = rmsBean.getAcksToEndpointReference();
		if(acksToEPR == null){
			acksToEPR = new EndpointReference(SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespace));
		}

		CreateSequence createSequencePart = new CreateSequence(rmNamespaceValue);

		// Check if this service includes 2-way operations
		boolean twoWayService = false;
		AxisService service = applicationMsgContext.getAxisService();
		if (service != null) {
			// if the user has specified this sequence as a one way sequence it should not
			// append the sequence offer.
			if (!JavaUtils.isTrue(applicationMsgContext.getOptions().getProperty(
				SandeshaClientConstants.ONE_WAY_SEQUENCE))) {
				Parameter p = service.getParameter(Sandesha2Constants.SERVICE_CONTAINS_OUT_IN_MEPS);
				if (p != null && p.getValue() != null) {
					twoWayService = ((Boolean) p.getValue()).booleanValue();
					if (log.isDebugEnabled()) log.debug("RMMsgCreator:: twoWayService " + twoWayService);
				}
			}
		}

		// Adding sequence offer - if present. We send an offer if the client has assigned an
		// id, or if the service contains out-in MEPs
		boolean autoOffer = twoWayService;

		//There may not have been a way to confirm if an OUT_IN MEP is being used.
		//Therefore doing an extra check to see what Axis is using.  If it's OUT_IN then we must offer.
		if(applicationMsgContext.getOperationContext() != null && applicationMsgContext.getOperationContext().getAxisOperation() != null){
			if(applicationMsgContext.getOperationContext().getAxisOperation().getAxisSpecificMEPConstant() == org.apache.axis2.wsdl.WSDLConstants.MEP_CONSTANT_OUT_IN
				|| applicationMsgContext.getOperationContext().getAxisOperation().getAxisSpecificMEPConstant() == org.apache.axis2.wsdl.WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN){
				autoOffer = true;
			}
		}

		// We also do some checking at this point to see if MakeConection is required to
		// enable WS-RM 1.1, and write a warning to the log if it has been disabled.
		if(Sandesha2Constants.SPEC_2007_02.NS_URI.equals(rmNamespaceValue)) {
			SandeshaPolicyBean policy = SandeshaUtil.getPropertyBean(context.getAxisConfiguration());
			if(twoWayService && !policy.isEnableMakeConnection()) {
				String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.makeConnectionWarning);
				log.warn(message);
			}
		}

		String offeredSequenceId = (String) applicationMsgContext.getProperty(SandeshaClientConstants.OFFERED_SEQUENCE_ID);
		if(autoOffer ||
		   (offeredSequenceId != null && offeredSequenceId.length() > 0))  {
			
			if (offeredSequenceId == null || offeredSequenceId.length() == 0) {
				offeredSequenceId = SandeshaUtil.getUUID();
			}

			SequenceOffer offerPart = new SequenceOffer(rmNamespaceValue);
			Identifier identifier = new Identifier(rmNamespaceValue);
			identifier.setIndentifer(offeredSequenceId);
			offerPart.setIdentifier(identifier);
			
			if (Sandesha2Constants.SPEC_2007_02.NS_URI.equals(rmNamespaceValue)) {
				// We are going to send an offer, so decide which endpoint to include
				EndpointReference offeredEndpoint = (EndpointReference) applicationMsgContext.getProperty(SandeshaClientConstants.OFFERED_ENDPOINT);
				//If the offeredEndpoint hasn't been set then use the acksTo of the RMSBean
				if (offeredEndpoint==null) {
					offeredEndpoint = rmsBean.getAcksToEndpointReference();
				}
				
				Endpoint endpoint = new Endpoint (offeredEndpoint, rmNamespaceValue, addressingNamespace);
				offerPart.setEndpoint(endpoint);
			}
			
			createSequencePart.setSequenceOffer(offerPart);
		}

		EndpointReference toEPR = rmsBean.getToEndpointReference();
		if (toEPR == null || toEPR.getAddress()==null) {
			String message = SandeshaMessageHelper
					.getMessage(SandeshaMessageKeys.toBeanNotSet);
			throw new SandeshaException(message);
		}
		createSeqRMMsg.setTo(toEPR);
		if(log.isDebugEnabled()) log.debug("RMMsgCreator:: toEPR=" + toEPR);

		EndpointReference replyToEPR = rmsBean.getReplyToEndpointReference();
		if(replyToEPR != null) {
			replyToEPR = SandeshaUtil.getEPRDecorator(createSeqRMMsg.getConfigurationContext()).decorateEndpointReference(replyToEPR);
			createSeqRMMsg.setReplyTo(replyToEPR);
			if(log.isDebugEnabled()) log.debug("RMMsgCreator:: replyToEPR=" + replyToEPR);
		}
		

		AcksTo acksTo = new AcksTo(acksToEPR, rmNamespaceValue, addressingNamespace);
		createSequencePart.setAcksTo(acksTo);
		if(log.isDebugEnabled()) log.debug("RMMsgCreator:: acksTo=" + acksTo);
		
		createSeqRMMsg.setCreateSequence(createSequencePart);

		// Find the token that should be used to secure this new sequence. If there is a token, then we
		// save it in the properties so that the caller can store the token within the create sequence
		// bean.
		SecurityManager secMgr = SandeshaUtil.getSecurityManager(context);
		SecurityToken token = secMgr.getSecurityToken(applicationMsgContext);
		if(token != null) {
			OMElement str = secMgr.createSecurityTokenReference(token, createSeqmsgContext);
			createSequencePart.setSecurityTokenReference(str);
			createSeqRMMsg.setProperty(Sandesha2Constants.MessageContextProperties.SECURITY_TOKEN, token);
			
			// If we are using token based security, and the 1.1 spec level, then we
			// should introduce a UsesSequenceSTR header into the message.
			if(createSequencePart.getNamespaceValue().equals(Sandesha2Constants.SPEC_2007_02.NS_URI)) {
				UsesSequenceSTR usesSeqStr = new UsesSequenceSTR();
				usesSeqStr.toHeader(createSeqmsgContext.getEnvelope().getHeader());
			}

			// Ensure that the correct token will be used to secure the outbound create sequence message.
			// We cannot use the normal helper method as we have not stored the token into the sequence bean yet.
			secMgr.applySecurityToken(token, createSeqRMMsg.getMessageContext());
		}

		createSeqRMMsg.setAction(SpecSpecificConstants.getCreateSequenceAction(rmsBean.getRMVersion()));
		createSeqRMMsg.setSOAPAction(SpecSpecificConstants.getCreateSequenceSOAPAction(rmsBean.getRMVersion()));

		createSeqRMMsg.addSOAPEnvelope();
		
		if(log.isDebugEnabled()) log.debug("Entry: RMMsgCreator::createCreateSeqMsg " + createSeqRMMsg);
		return createSeqRMMsg;
	}

	/**
	 * Creates a new TerminateSequence message.
	 * 
	 * @param referenceRMMessage
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext createTerminateSequenceMessage(RMMsgContext referenceRMMessage, RMSBean rmsBean,
			StorageManager storageManager) throws AxisFault {
		MessageContext referenceMessage = referenceRMMessage.getMessageContext();
		if (referenceMessage == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.msgContextNotSet));

		AxisOperation terminateOperation = SpecSpecificConstants.getWSRMOperation(
				Sandesha2Constants.MessageTypes.TERMINATE_SEQ,
				rmsBean.getRMVersion(),
				referenceMessage.getAxisService());

		ConfigurationContext configCtx = referenceMessage.getConfigurationContext();
		if (configCtx == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.configContextNotSet));

		MessageContext terminateMessage = SandeshaUtil.createNewRelatedMessageContext(referenceRMMessage,
				terminateOperation);

		if (terminateMessage == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.msgContextNotSet));

		if (terminateMessage.getMessageID()==null) {
			terminateMessage.setMessageID(SandeshaUtil.getUUID());
		}

		OperationContext operationContext = terminateMessage.getOperationContext();
		// to receive terminate sequence response messages correctly
		configCtx.registerOperationContext(terminateMessage.getMessageID(), operationContext); 
		
		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmsBean.getRMVersion());

		RMMsgContext terminateRMMessage = MsgInitializer.initializeMessage(terminateMessage);

		TerminateSequence terminateSequencePart = new TerminateSequence(rmNamespaceValue);
		Identifier identifier = new Identifier(rmNamespaceValue);
		identifier.setIndentifer(rmsBean.getSequenceID());
		terminateSequencePart.setIdentifier(identifier);

		terminateRMMessage.setTerminateSequence(terminateSequencePart);
		if(TerminateSequence.isLastMsgNumberRequired(rmNamespaceValue)){
			LastMessageNumber lastMsgNumber = new LastMessageNumber(rmNamespaceValue);
			lastMsgNumber.setMessageNumber(SandeshaUtil.getLastMessageNumber(rmsBean.getInternalSequenceID(), storageManager));
			terminateSequencePart.setLastMessageNumber(lastMsgNumber);
		}

		// no need for an incoming transport for a terminate
		// message. If this is put, sender will look for an response.
		terminateMessage.setProperty(MessageContext.TRANSPORT_IN, null); 

		terminateMessage.setTo(rmsBean.getToEndpointReference());
		
		// Ensure the correct token is used to secure the terminate sequence
		secureOutboundMessage(rmsBean, terminateMessage);
		
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
	public static RMMsgContext createCreateSeqResponseMsg(RMMsgContext createSeqMessage, RMSequenceBean rmSequenceBean) throws AxisFault {

		if(log.isDebugEnabled()) log.debug("Entry: RMMsgCreator::createCreateSeqResponseMsg " + rmSequenceBean);
		
		CreateSequence cs = createSeqMessage.getCreateSequence();
		String namespace = createSeqMessage.getRMNamespaceValue();

		CreateSequenceResponse response = new CreateSequenceResponse(namespace);
		Identifier identifier = new Identifier(namespace);
		identifier.setIndentifer(rmSequenceBean.getSequenceID());
		response.setIdentifier(identifier);

		SequenceOffer offer = cs.getSequenceOffer();
		if (offer != null) {
			if(log.isDebugEnabled()) log.debug("RMMsgCreator:: " + offer);
			String outSequenceId = offer.getIdentifer().getIdentifier();

			if (outSequenceId != null && !"".equals(outSequenceId)) {

				Accept accept = new Accept(namespace);

				// Putting the To EPR as the AcksTo for the response sequence. We echo back the
				// addressing version that the create used.
				String addressingNamespace = cs.getAddressingNamespaceValue();
				EndpointReference acksToEPR = createSeqMessage.getTo();
				if(acksToEPR != null) {
					acksToEPR = SandeshaUtil.cloneEPR(acksToEPR);
				} else {
					String anon = SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespace);
					acksToEPR = new EndpointReference(anon);
				}
				
				AcksTo acksTo = new AcksTo(acksToEPR, namespace, cs.getAddressingNamespaceValue());
				accept.setAcksTo(acksTo);
				response.setAccept(accept);
			}
		}

		String version = SpecSpecificConstants.getSpecVersionString(namespace);
		String action = SpecSpecificConstants.getCreateSequenceResponseAction(version);

		RMMsgContext returnRMContext = createResponseMsg(createSeqMessage, rmSequenceBean, response,
				Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE,action);

		returnRMContext.setTo(createSeqMessage.getReplyTo()); //CSResponse goes to the replyTo, NOT the acksTo
		
		if(log.isDebugEnabled()) log.debug("Exit: RMMsgCreator::createCreateSeqResponseMsg " + returnRMContext);
		return returnRMContext;
	}

	public static RMMsgContext createTerminateSeqResponseMsg(RMMsgContext terminateSeqRMMsg, RMSequenceBean rmSequenceBean) throws AxisFault {
		if(log.isDebugEnabled())
			log.debug("Entry: RMMsgCreator::createTerminateSeqResponseMsg " + rmSequenceBean);        
		TerminateSequence terminateSequence = terminateSeqRMMsg.getTerminateSequence();
		String sequenceID = terminateSequence.getIdentifier().getIdentifier();

		String namespace = terminateSeqRMMsg.getRMNamespaceValue();

		TerminateSequenceResponse terminateSequenceResponse = new TerminateSequenceResponse(namespace);
		Identifier identifier = new Identifier(namespace);
		identifier.setIndentifer(sequenceID);
		terminateSequenceResponse.setIdentifier(identifier);

		String version = SpecSpecificConstants.getSpecVersionString(namespace);
		String action = SpecSpecificConstants.getTerminateSequenceResponseAction(version);
		
		RMMsgContext returnRMContext = createResponseMsg(terminateSeqRMMsg, rmSequenceBean, terminateSequenceResponse,
				Sandesha2Constants.MessageParts.TERMINATE_SEQ_RESPONSE, action);
		if(rmSequenceBean.getAcksToEndpointReference()!=null){
			returnRMContext.setTo(rmSequenceBean.getAcksToEndpointReference()); //RSP requirement
		}
		if(log.isDebugEnabled())
			log.debug("Exit: RMMsgCreator::createTerminateSeqResponseMsg " + returnRMContext);
		return returnRMContext;
		
	}

	public static RMMsgContext createCloseSeqResponseMsg(RMMsgContext closeSeqRMMsg, RMSequenceBean rmSequenceBean) throws AxisFault {
		if(log.isDebugEnabled())
			log.debug("Entry: RMMsgCreator::createCloseSeqResponseMsg " + rmSequenceBean);
		CloseSequence closeSequence = closeSeqRMMsg.getCloseSequence();
		String sequenceID = closeSequence.getIdentifier().getIdentifier();

		String namespace = closeSeqRMMsg.getRMNamespaceValue();

		CloseSequenceResponse closeSequenceResponse = new CloseSequenceResponse(namespace);
		Identifier identifier = new Identifier(namespace);
		identifier.setIndentifer(sequenceID);
		closeSequenceResponse.setIdentifier(identifier);

		String version = SpecSpecificConstants.getSpecVersionString(namespace);
		String action = SpecSpecificConstants.getCloseSequenceResponseAction(version);

		RMMsgContext returnRMContext = createResponseMsg(closeSeqRMMsg, rmSequenceBean, closeSequenceResponse,
				Sandesha2Constants.MessageParts.CLOSE_SEQUENCE_RESPONSE, action);
		if(rmSequenceBean.getAcksToEndpointReference()!=null){
			returnRMContext.setTo(rmSequenceBean.getAcksToEndpointReference()); //RSP requirement
		}
		if(log.isDebugEnabled())
			log.debug("Exit: RMMsgCreator::createCloseSeqResponseMsg " + returnRMContext);
		return returnRMContext;
	}

	/**
	 * This will create a response message context using the Axis2 Util methods (where things like relatesTo transformation will
	 * happen). This will also  make sure that created out message is correctly secured using the Sequence Token Data of the sequence.
	 * 
	 * @param requestMsg The request message
	 * @param rmSequenceBean 
	 * @param part
	 * @param messagePartId
	 * @param action
	 * @return
	 * @throws AxisFault
	 */
	private static RMMsgContext createResponseMsg(RMMsgContext requestMsg, RMSequenceBean rmSequenceBean, IOMRMPart part, 
			int messagePartId, String action) throws AxisFault {

		MessageContext outMessage = MessageContextBuilder.createOutMessageContext (requestMsg.getMessageContext());
		RMMsgContext responseRMMsg = new RMMsgContext(outMessage);
		
		SOAPFactory factory = (SOAPFactory)requestMsg.getSOAPEnvelope().getOMFactory();

		String namespace = requestMsg.getRMNamespaceValue();
		responseRMMsg.setRMNamespaceValue(namespace);

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		responseRMMsg.setSOAPEnvelop(envelope);
		
		switch(messagePartId){
			case Sandesha2Constants.MessageParts.CLOSE_SEQUENCE_RESPONSE: responseRMMsg.setCloseSequenceResponse((CloseSequenceResponse) part);break;
			case Sandesha2Constants.MessageParts.TERMINATE_SEQ_RESPONSE: responseRMMsg.setTerminateSequenceResponse((TerminateSequenceResponse) part);break;
			case Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE: responseRMMsg.setCreateSequenceResponse((CreateSequenceResponse) part);break;
			default: throw new RuntimeException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.internalError));
		}

		outMessage.setWSAAction(action);
		outMessage.setSoapAction(action);

		responseRMMsg.addSOAPEnvelope();
		responseRMMsg.getMessageContext().setServerSide(true);

		// Ensure the correct token is used to secure the message
		secureOutboundMessage(rmSequenceBean, outMessage);
		
		return responseRMMsg;
	}

	/**
	 * Adds an Ack of specific sequence to the given application message.
	 * 
	 * @param applicationMsg The Message to which the Ack will be added
	 * @param sequenceId - The sequence to which we will be Acking
	 * @throws SandeshaException
	 */
	public static void addAckMessage(RMMsgContext applicationMsg, String sequenceId, RMDBean rmdBean, boolean addToEnvelope,
										boolean isPiggybacked)
			throws SandeshaException {
		if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled())
			log.debug("Entry: RMMsgCreator::addAckMessage " + sequenceId);
		
		String rmVersion = rmdBean.getRMVersion();
		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);
		ArrayList<Range> ackRangeArrayList = SandeshaUtil.getAckRangeArrayList(rmdBean.getServerCompletedMessages(), rmNamespaceValue);
		if(ackRangeArrayList!=null && ackRangeArrayList.size()!=0){
			if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled())
				log.debug("RMMsgCreator::addAckMessage : there are messages to ack " + ackRangeArrayList);
			//there are actually messages to ack
			SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement(rmNamespaceValue, isPiggybacked);
			Identifier id = new Identifier(rmNamespaceValue);
			id.setIndentifer(sequenceId);
			sequenceAck.setIdentifier(id);

			sequenceAck.setAckRanges(ackRangeArrayList);
			
			if (rmdBean.isClosed()) {
				// sequence is closed. so add the 'Final' part.
				if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled())
					log.debug("RMMsgCreator::addAckMessage : sequence closed");
				if (SpecSpecificConstants.isAckFinalAllowed(rmVersion)) {
					sequenceAck.setAckFinal(true);
				}
			}

			applicationMsg.addSequenceAcknowledgement(sequenceAck);

			if (applicationMsg.getWSAAction()==null) {
				applicationMsg.setAction(SpecSpecificConstants.getSequenceAcknowledgementAction(rmVersion));
				applicationMsg.setSOAPAction(SpecSpecificConstants.getSequenceAcknowledgementSOAPAction(rmVersion));
			}
			if(applicationMsg.getMessageId() == null) {
				applicationMsg.setMessageId(SandeshaUtil.getUUID());
			}
			
			if(addToEnvelope){
				// Write the ack into the soap envelope
				try {
					applicationMsg.addSOAPEnvelope();
				} catch(AxisFault e) {
					if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled()) log.debug("Caught AxisFault", e);
					throw new SandeshaException(e.getMessage(), e);
				}
			}else{
				// Should use a constant in the final fix.
				applicationMsg.setProperty(ACK_TO_BE_WRITTEN, Boolean.TRUE);
			}
			
			// Ensure the message also contains the token that needs to be used
			secureOutboundMessage(rmdBean, applicationMsg.getMessageContext());
		}
		
		if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled()) 
			log.debug("Exit: RMMsgCreator::addAckMessage " + applicationMsg);
	}

	/**
	 * Adds an Ack Request for a specific sequence to the given application message.
	 * 
	 * @param applicationMsg The Message to which the AckRequest will be added
	 * @param sequenceId - The sequence which we will request the ack for
	 * @throws SandeshaException
	 */
	public static void addAckRequest(RMMsgContext applicationMsg, String sequenceId, RMSBean rmsBean)
			throws SandeshaException {
		if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled())
			log.debug("Entry: RMMsgCreator::addAckRequest " + sequenceId);
		
		String rmVersion = rmsBean.getRMVersion();
		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);
		
		AckRequested ackRequest = new AckRequested(rmNamespaceValue);	
		
		Identifier id = new Identifier(rmNamespaceValue);
		id.setIndentifer(sequenceId);
		ackRequest.setIdentifier(id);
		ackRequest.setMustUnderstand(true);
		applicationMsg.addAckRequested(ackRequest);

		if (applicationMsg.getWSAAction()==null) {
			applicationMsg.setAction(SpecSpecificConstants.getAckRequestAction(rmVersion));
			applicationMsg.setSOAPAction(SpecSpecificConstants.getAckRequestSOAPAction(rmVersion));
		}
		
		if(applicationMsg.getMessageId() == null) {
			applicationMsg.setMessageId(SandeshaUtil.getUUID());
		}
				
		// Ensure the message also contains the token that needs to be used
		secureOutboundMessage(rmsBean, applicationMsg.getMessageContext());
			
		if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled()) 
			log.debug("Exit: RMMsgCreator::addAckRequest " + applicationMsg);
	}
	
	
	public static RMMsgContext createMakeConnectionMessage (RMMsgContext referenceRMMessage,
															RMSequenceBean bean,
															String makeConnectionSeqId,
															String makeConnectionAnonURI)
	throws AxisFault
	{
		
		MessageContext referenceMessage = referenceRMMessage.getMessageContext();
		String rmNamespaceValue = referenceRMMessage.getRMNamespaceValue();
		String rmVersion = referenceRMMessage.getRMSpecVersion();
		
		AxisOperation makeConnectionOperation = SpecSpecificConstants.getWSRMOperation(
				Sandesha2Constants.MessageTypes.MAKE_CONNECTION_MSG,
				rmVersion,
				referenceMessage.getAxisService());

		MessageContext makeConnectionMessageCtx = SandeshaUtil.createNewRelatedMessageContext(referenceRMMessage,makeConnectionOperation);
		RMMsgContext makeConnectionRMMessageCtx = MsgInitializer.initializeMessage(makeConnectionMessageCtx);
		
		MakeConnection makeConnection = new MakeConnection();
		if (makeConnectionSeqId!=null) {
			Identifier identifier = new Identifier (rmNamespaceValue);
			identifier.setIndentifer(makeConnectionSeqId);
			makeConnection.setIdentifier(identifier);
		}
		
		if (makeConnectionAnonURI!=null) {
			makeConnection.setAddress(makeConnectionAnonURI);
		}
		
		// Setting the addressing properties. As this is a poll we must send it to an non-anon
		// EPR, so we check both To and ReplyTo from the reference message
		EndpointReference epr = referenceMessage.getTo();
		if(epr.hasAnonymousAddress()) epr = referenceMessage.getReplyTo();
		
		makeConnectionMessageCtx.setTo(epr);
		makeConnectionMessageCtx.setWSAAction(SpecSpecificConstants.getMakeConnectionAction(rmVersion));
		makeConnectionMessageCtx.setMessageID(SandeshaUtil.getUUID());
		makeConnectionRMMessageCtx.setMakeConnection(makeConnection);
		
		//generating the SOAP Envelope.
		makeConnectionRMMessageCtx.addSOAPEnvelope();
		
		// Secure the message using the correct token for the sequence that we are polling
		secureOutboundMessage(bean, makeConnectionMessageCtx);		
		
		return makeConnectionRMMessageCtx;
	}

	/**
	 * This will add necessary data to a out-bound message to make sure that is is correctly secured.
	 * Security Token Data will be taken from the Sandesha2 security manager.
	 * 
	 * @param rmBean Sequence bean to identify the sequence. This could be an in-bound sequence or an out-bound sequence.
	 * @param message - The message which will be secured.
	 * @throws SandeshaException 
	 */
	public static void secureOutboundMessage(RMSequenceBean rmBean, MessageContext message)
	throws SandeshaException
	{
		if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled()) log.debug("Entry: RMMsgCreator::secureOutboundMessage");

		ConfigurationContext configCtx = message.getConfigurationContext();

		if(rmBean.getSecurityTokenData() != null) {
			if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled()) log.debug("Securing outbound message");
			SecurityManager secManager = SandeshaUtil.getSecurityManager(configCtx);
			SecurityToken token = secManager.recoverSecurityToken(rmBean.getSecurityTokenData());
			secManager.applySecurityToken(token, message);
		}

		if(LoggingControl.isAnyTracingEnabled() && log.isDebugEnabled()) log.debug("Exit: RMMsgCreator::secureOutboundMessage");
	}

}
