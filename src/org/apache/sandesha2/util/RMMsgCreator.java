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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.msgprocessors.CreateSeqResponseMsgProcessor;
import org.apache.sandesha2.security.SecurityManager;
import org.apache.sandesha2.security.SecurityToken;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AckFinal;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.Address;
import org.apache.sandesha2.wsrm.CloseSequence;
import org.apache.sandesha2.wsrm.CloseSequenceResponse;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Endpoint;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.MakeConnection;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.sandesha2.wsrm.TerminateSequence;
import org.apache.sandesha2.wsrm.TerminateSequenceResponse;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * Used to create new RM messages.
 */

public class RMMsgCreator {

	private static Log log = LogFactory.getLog(RMMsgCreator.class);

	private static void initializeCreation(MessageContext relatedMessage, MessageContext newMessage)
			throws SandeshaException {

		if (relatedMessage.getAxisService() != null && newMessage.getAxisService() != null
				&& newMessage.getAxisService() != relatedMessage.getAxisService()) {

			Parameter referencePolicyParam = relatedMessage.getAxisService().getParameter(
					Sandesha2Constants.SANDESHA_PROPERTY_BEAN);
			if (referencePolicyParam != null) {
				Parameter newPolicyParam = new Parameter();
				newPolicyParam.setName(Sandesha2Constants.SANDESHA_PROPERTY_BEAN);
				newPolicyParam.setValue(newPolicyParam.getValue());
			}
		}

		if (relatedMessage.getAxisOperation() != null && newMessage.getAxisOperation() != null
				&& newMessage.getAxisOperation() != relatedMessage.getAxisOperation()) {

			Parameter referencePolicyParam = relatedMessage.getAxisOperation().getParameter(
					Sandesha2Constants.SANDESHA_PROPERTY_BEAN);
			if (referencePolicyParam != null) {
				Parameter newPolicyParam = new Parameter();
				newPolicyParam.setName(Sandesha2Constants.SANDESHA_PROPERTY_BEAN);
				newPolicyParam.setValue(newPolicyParam.getValue());
			}
		}
	}

	private static void finalizeCreation(MessageContext relatedMessage, MessageContext newMessage)
			throws SandeshaException {

		newMessage.setServerSide(relatedMessage.isServerSide());

		// adding all parameters from old message to the new one.
		try {
			// axisOperation parameters
			AxisOperation oldAxisOperation = relatedMessage.getAxisOperation();
			if (oldAxisOperation != null) {
				ArrayList axisOpParams = oldAxisOperation.getParameters();
				if (axisOpParams != null) {
					AxisOperation newAxisOperation = newMessage.getAxisOperation();
					Iterator iter = axisOpParams.iterator();
					while (iter.hasNext()) {
						Parameter nextParam = (Parameter) iter.next();
						Parameter newParam = new Parameter();

						newParam.setName(nextParam.getName());
						newParam.setValue(nextParam.getValue());

						newAxisOperation.addParameter(newParam);
					}
				}
			}

		} catch (AxisFault e) {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.couldNotCopyParameters, e.getLocalizedMessage());
			log.error(message, e);
			throw new SandeshaException(message, e);
		}

		// TODO optimize by cloning the Map rather than copying one by one.

		// operationContext properties
		OperationContext oldOpContext = relatedMessage.getOperationContext();
		if (oldOpContext != null) {
			Map oldOpContextProperties = oldOpContext.getProperties();
			if (oldOpContextProperties != null) {
				OperationContext newOpContext = newMessage.getOperationContext();
				Iterator keyIter = oldOpContextProperties.keySet().iterator();
				while (keyIter.hasNext()) {
					String key = (String) keyIter.next();
					newOpContext.setProperty(key, oldOpContextProperties.get(key));
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
					newMessage.setProperty(key, oldMsgContextProperties.get(key));
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

			Map relatedMessageProperties = relatesMessageOptions.getProperties();
			Iterator keys = relatedMessageProperties.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				newMessageOptions.setProperty(key, relatedMessageProperties.get(key));
			}

			Options relatedMessageParentOptions = relatesMessageOptions.getParent();
			if (relatedMessageParentOptions != null) {
				Map relatedMessageParentProperties = relatedMessageParentOptions.getProperties();
				keys = relatedMessageParentProperties.keySet().iterator();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					newMessageOptions.setProperty(key, relatedMessageParentProperties.get(key));
				}
			}
		}
	}

	/**
	 * Create a new CreateSeqnence message.
	 * 
	 * @param applicationRMMsg
	 * @param internalSequenceId
	 * @param acksToEPR
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext createCreateSeqMsg(RMMsgContext applicationRMMsg, String sequencePropertyKey,
			EndpointReference acksToEPR, StorageManager storageManager) throws AxisFault {

		MessageContext applicationMsgContext = applicationRMMsg.getMessageContext();
		if (applicationMsgContext == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.appMsgIsNull));
		ConfigurationContext context = applicationMsgContext.getConfigurationContext();
		if (context == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.configContextNotSet));

		MessageContext createSeqmsgContext;
		try {
			// creating by copying common contents. (this will not set contexts
			// except for configCtx).
			AxisOperation createSequenceOperation = AxisOperationFactory
					.getAxisOperation(WSDL20_2004Constants.MEP_CONSTANT_OUT_IN);

			createSeqmsgContext = SandeshaUtil
					.createNewRelatedMessageContext(applicationRMMsg, createSequenceOperation);
			
			initializeCreation(applicationMsgContext, createSeqmsgContext);

			OperationContext createSeqOpCtx = createSeqmsgContext.getOperationContext();
			String createSeqMsgId = SandeshaUtil.getUUID();
			createSeqmsgContext.setMessageID(createSeqMsgId);
			context.registerOperationContext(createSeqMsgId, createSeqOpCtx);

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		AxisOperation appMsgOperationDesc = applicationMsgContext.getAxisOperation();

		AxisOperation createSeqOperation = createSeqmsgContext.getAxisOperation();

		createSeqOperation.setName(new QName("CreateSequenceOperation"));
		if (appMsgOperationDesc != null) {
			createSeqOperation.setPhasesOutFlow(appMsgOperationDesc.getPhasesOutFlow());
			createSeqOperation.setPhasesOutFaultFlow(appMsgOperationDesc.getPhasesOutFaultFlow());
			createSeqOperation.setPhasesInFaultFlow(appMsgOperationDesc.getPhasesInFaultFlow());
			createSeqOperation.setRemainingPhasesInFlow(appMsgOperationDesc.getRemainingPhasesInFlow());
		}

		createSeqmsgContext.setAxisOperation(createSeqOperation);
		        
		RMMsgContext createSeqRMMsg = new RMMsgContext(createSeqmsgContext);

		String rmVersion = SandeshaUtil.getRMVersion(sequencePropertyKey, storageManager);
		if (rmVersion == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.cannotDecideRMVersion));

		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);

		String addressingNamespaceValue = SandeshaUtil.getSequenceProperty(sequencePropertyKey,
				Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE, storageManager);

		CreateSequence createSequencePart = new CreateSequence(rmNamespaceValue, addressingNamespaceValue);

		// Adding sequence offer - if present
		OperationContext operationcontext = applicationMsgContext.getOperationContext();
		if (operationcontext != null) {
			String offeredSequence = (String) applicationMsgContext
					.getProperty(SandeshaClientConstants.OFFERED_SEQUENCE_ID);
			EndpointReference offeredEndpoint = (EndpointReference) applicationMsgContext
					.getProperty(SandeshaClientConstants.OFFERED_ENDPOINT);
			
			if (offeredEndpoint==null) {
				EndpointReference replyTo = applicationMsgContext.getReplyTo();  //using replyTo as the Endpoint if it is not specified
				
				if (replyTo!=null)
					offeredEndpoint = SandeshaUtil.cloneEPR(replyTo);
			}
			
			if (offeredSequence != null && !"".equals(offeredSequence)) {
				SequenceOffer offerPart = new SequenceOffer(rmNamespaceValue);
				Identifier identifier = new Identifier(rmNamespaceValue);
				identifier.setIndentifer(offeredSequence);
				offerPart.setIdentifier(identifier);
				createSequencePart.setSequenceOffer(offerPart);
				
				if (Sandesha2Constants.SPEC_2006_08.NS_URI.equals(rmNamespaceValue)) {
					if (offeredEndpoint!=null) {
						Endpoint endpoint = new Endpoint (rmNamespaceValue,addressingNamespaceValue);
						endpoint.setEPR (offeredEndpoint);
						offerPart.setEndpoint(endpoint);
					} else {
						String message = SandeshaMessageHelper.getMessage(
								SandeshaMessageKeys.invalidOfferNoResponseEndpoint);
						throw new SandeshaException (message);
					}
				}
			}
		}

		String to = SandeshaUtil.getSequenceProperty(sequencePropertyKey, 
										Sandesha2Constants.SequenceProperties.TO_EPR,storageManager);
		String replyTo = SandeshaUtil.getSequenceProperty(sequencePropertyKey, 
				 Sandesha2Constants.SequenceProperties.REPLY_TO_EPR,storageManager);
		
		if (replyTo==null) {
			//using wsa:Anonymous as ReplyTo
			
			String addressingNamespace = applicationRMMsg.getAddressingNamespaceValue();
			if (AddressingConstants.Submission.WSA_NAMESPACE.equals(addressingNamespace))
				replyTo = AddressingConstants.Submission.WSA_ANONYMOUS_URL;
			else
				replyTo = AddressingConstants.Final.WSA_ANONYMOUS_URL;
		}
		
		if (to==null) {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.toBeanNotSet);
			throw new SandeshaException (message);
		}
		
		//TODO store and retrieve a full EPR instead of just the address.
		EndpointReference toEPR = new EndpointReference (to);
		EndpointReference replyToEPR = new EndpointReference (replyTo);
		
		createSeqRMMsg.setTo(toEPR);
		createSeqRMMsg.setReplyTo(replyToEPR);
		
		String anonymousURI = SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespaceValue);

		if (acksToEPR==null || acksToEPR.getAddress() == null || "".equals(acksToEPR.getAddress()))
			acksToEPR = new EndpointReference(anonymousURI);

		AcksTo acksTo = new AcksTo(acksToEPR,rmNamespaceValue,addressingNamespaceValue);
		createSequencePart.setAcksTo(acksTo);
		
		// Find the token that should be used to secure this new sequence. If there is a token, then we
		// save it in the properties so that the caller can store the token within the create sequence
		// bean.
		SecurityManager secMgr = SandeshaUtil.getSecurityManager(context);
		SecurityToken token = secMgr.getSecurityToken(createSeqmsgContext);
		if(token != null) {
			OMElement str = secMgr.createSecurityTokenReference(token, createSeqmsgContext);
			createSequencePart.setSecurityTokenReference(str);
			createSeqRMMsg.setProperty(Sandesha2Constants.SequenceProperties.SECURITY_TOKEN, token);
			
			//adding the UseSequenceSTR header
			SOAPFactory createSeqFactory = (SOAPFactory) createSeqRMMsg.getSOAPEnvelope().getOMFactory();
			OMNamespace namespace = createSeqFactory.createOMNamespace(Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM, createSeqRMMsg.getRMNamespaceValue());
			createSeqFactory.createSOAPHeaderBlock(Sandesha2Constants.WSRM_COMMON.USES_SEQUENCE_STR, namespace);
		}

		createSeqRMMsg.setMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ, createSequencePart);

		try {
			createSeqRMMsg.addSOAPEnvelope();
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		createSeqRMMsg.setAction(SpecSpecificConstants.getCreateSequenceAction(SandeshaUtil.getRMVersion(
				sequencePropertyKey, storageManager)));
		createSeqRMMsg.setSOAPAction(SpecSpecificConstants.getCreateSequenceSOAPAction(SandeshaUtil.getRMVersion(
				sequencePropertyKey, storageManager)));

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
	public static RMMsgContext createTerminateSequenceMessage(RMMsgContext referenceRMMessage, String sequenceId,
			String sequencePropertyKey, StorageManager storageManager) throws AxisFault {
		MessageContext referenceMessage = referenceRMMessage.getMessageContext();
		if (referenceMessage == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.msgContextNotSet));

		AxisOperation terminateOperation = null;

		try {
			terminateOperation = AxisOperationFactory.getAxisOperation(WSDL20_2004Constants.MEP_CONSTANT_OUT_ONLY);
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		if (terminateOperation == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.terminateOpperationIsNull));

		ConfigurationContext configCtx = referenceMessage.getConfigurationContext();
		if (configCtx == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.configContextNotSet));

		MessageContext terminateMessage = SandeshaUtil.createNewRelatedMessageContext(referenceRMMessage,
				terminateOperation);

		OperationContext operationContext = terminateMessage.getOperationContext();
		configCtx.registerOperationContext(terminateMessage.getMessageID(), operationContext); // to
																								// receive
																								// terminate
																								// sequence
																								// response
																								// messages
																								// correctly.

		//using the Sandesha2 RMOutInOperation as the reference operation.
		AxisOperation referenceOperation = referenceMessage.getAxisService()
					.getOperation(new QName (Sandesha2Constants.RM_IN_OUT_OPERATION_NAME));
		
		AxisOperation terminateMsgOperation = terminateMessage.getAxisOperation();
		if (referenceOperation != null) {
			terminateMsgOperation.setPhasesOutFlow(referenceOperation.getPhasesOutFlow());
			terminateMsgOperation.setPhasesOutFaultFlow(referenceOperation.getPhasesOutFaultFlow());
			terminateMsgOperation.setPhasesInFaultFlow(referenceOperation.getPhasesInFaultFlow());
			terminateMsgOperation.setRemainingPhasesInFlow(referenceOperation.getRemainingPhasesInFlow());
		}

		String rmVersion = SandeshaUtil.getRMVersion(sequencePropertyKey, storageManager);
		if (rmVersion == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.cannotDecideRMVersion));

		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);

		initializeCreation(referenceMessage, terminateMessage);

		if (!SpecSpecificConstants.isTerminateSequenceResponseRequired(rmVersion)) {
			terminateMessage.setProperty(MessageContext.TRANSPORT_IN, null);
		}

		RMMsgContext terminateRMMessage = MsgInitializer.initializeMessage(terminateMessage);

		if (terminateMessage == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.msgContextNotSet));

		// setUpMessage(referenceMessage, terminateMessage);

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion(referenceMessage
				.getEnvelope()));

		terminateMessage.setMessageID(SandeshaUtil.getUUID());

		AxisOperation referenceMsgOperation = referenceMessage.getAxisOperation();
		if (referenceMsgOperation != null) {
			ArrayList outPhases = referenceMsgOperation.getPhasesOutFlow();
			if (outPhases != null) {
				terminateOperation.setPhasesOutFlow(outPhases);
				terminateOperation.setPhasesOutFaultFlow(outPhases);
			}
		}

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		terminateRMMessage.setSOAPEnvelop(envelope);

		TerminateSequence terminateSequencePart = new TerminateSequence(rmNamespaceValue);
		Identifier identifier = new Identifier(rmNamespaceValue);
		identifier.setIndentifer(sequenceId);
		terminateSequencePart.setIdentifier(identifier);
		terminateRMMessage.setMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ, terminateSequencePart);

		finalizeCreation(referenceMessage, terminateMessage);

		terminateMessage.setProperty(MessageContext.TRANSPORT_IN, null); // no
																			// need
																			// for
																			// an
																			// incoming
																			// transport
																			// for
																			// an
																			// terminate
		// message. If this is put, sender will look for an response.

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
	public static RMMsgContext createCreateSeqResponseMsg(RMMsgContext createSeqMessage, MessageContext outMessage,
			String newSequenceID, StorageManager storageManager) throws AxisFault {

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion(createSeqMessage
				.getSOAPEnvelope()));

		IOMRMElement messagePart = createSeqMessage.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		CreateSequence cs = (CreateSequence) messagePart;

		String rmVersion = SandeshaUtil.getRMVersion(newSequenceID, storageManager);
		if (rmVersion == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.cannotDecideRMVersion));

		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);
		String addressingNamespaceValue = SandeshaUtil.getSequenceProperty(newSequenceID,
				Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE, storageManager);

		CreateSequenceResponse response = new CreateSequenceResponse(rmNamespaceValue,
				addressingNamespaceValue);

		Identifier identifier = new Identifier(rmNamespaceValue);
		identifier.setIndentifer(newSequenceID);

		response.setIdentifier(identifier);

		SequenceOffer offer = cs.getSequenceOffer();
		if (offer != null) {
			String outSequenceId = offer.getIdentifer().getIdentifier();

			if (outSequenceId != null && !"".equals(outSequenceId)) {

				Accept accept = new Accept(rmNamespaceValue, addressingNamespaceValue);
				EndpointReference acksToEPR = SandeshaUtil.cloneEPR(createSeqMessage.getTo());
				
				//putting the To EPR as the AcksTo for the response sequence.
				AcksTo acksTo = new AcksTo(acksToEPR,rmNamespaceValue, addressingNamespaceValue);
				accept.setAcksTo(acksTo);
				response.setAccept(accept);
			}

		}

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		response.toOMElement(envelope.getBody());
		outMessage.setWSAAction(SpecSpecificConstants.getCreateSequenceResponseAction(SandeshaUtil.getRMVersion(
				newSequenceID, storageManager)));
		outMessage.setSoapAction(SpecSpecificConstants.getCreateSequenceResponseSOAPAction(SandeshaUtil.getRMVersion(
				newSequenceID, storageManager)));
		outMessage.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, addressingNamespaceValue);

		String newMessageId = SandeshaUtil.getUUID();
		outMessage.setMessageID(newMessageId);

		outMessage.setEnvelope(envelope);

		initializeCreation(createSeqMessage.getMessageContext(), outMessage);

		RMMsgContext createSeqResponse = null;
		try {
			createSeqResponse = MsgInitializer.initializeMessage(outMessage);
		} catch (SandeshaException ex) {
			throw new AxisFault(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.cannotInnitMessage), ex);
		}

		createSeqResponse.setMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE, response);

		finalizeCreation(createSeqMessage.getMessageContext(), outMessage);

		createSeqMessage.getMessageContext().setServerSide(true);
		return createSeqResponse;
	}

	public static RMMsgContext createTerminateSeqResponseMsg(RMMsgContext terminateSeqRMMsg, MessageContext outMessage,
			StorageManager storageManager) throws AxisFault {
        
		RMMsgContext terminateSeqResponseRMMsg = new RMMsgContext(outMessage);

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion(terminateSeqRMMsg
				.getSOAPEnvelope()));

		TerminateSequence terminateSequence = (TerminateSequence) terminateSeqRMMsg
				.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
		String sequenceID = terminateSequence.getIdentifier().getIdentifier();

		String namespace = terminateSeqRMMsg.getRMNamespaceValue();
		terminateSeqResponseRMMsg.setRMNamespaceValue(namespace);

		TerminateSequenceResponse terminateSequenceResponse = new TerminateSequenceResponse(namespace);
		Identifier identifier = new Identifier(namespace);
		identifier.setIndentifer(sequenceID);
		terminateSequenceResponse.setIdentifier(identifier);

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		terminateSeqResponseRMMsg.setSOAPEnvelop(envelope);
		terminateSeqResponseRMMsg.setMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ_RESPONSE,
				terminateSequenceResponse);

		outMessage.setWSAAction(SpecSpecificConstants.getTerminateSequenceResponseAction(SandeshaUtil.getRMVersion(
				sequenceID, storageManager)));
		outMessage.setSoapAction(SpecSpecificConstants.getTerminateSequenceResponseAction(SandeshaUtil.getRMVersion(
				sequenceID, storageManager)));

		initializeCreation(terminateSeqRMMsg.getMessageContext(), outMessage);

		terminateSeqResponseRMMsg.addSOAPEnvelope();

		finalizeCreation(terminateSeqRMMsg.getMessageContext(), outMessage);

//		terminateSeqResponseRMMsg.getMessageContext().setServerSide(true);
		return terminateSeqResponseRMMsg;
	}

	public static RMMsgContext createCloseSeqResponseMsg(RMMsgContext closeSeqRMMsg, MessageContext outMessage,
			StorageManager storageManager) throws AxisFault {

		RMMsgContext closeSeqResponseRMMsg = new RMMsgContext(outMessage);

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil.getSOAPVersion(closeSeqRMMsg
				.getSOAPEnvelope()));

		CloseSequence closeSequence = (CloseSequence) closeSeqRMMsg
				.getMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE);
		String sequenceID = closeSequence.getIdentifier().getIdentifier();

		String namespace = closeSeqRMMsg.getRMNamespaceValue();
		closeSeqResponseRMMsg.setRMNamespaceValue(namespace);

		CloseSequenceResponse closeSequenceResponse = new CloseSequenceResponse(namespace);
		Identifier identifier = new Identifier(namespace);
		identifier.setIndentifer(sequenceID);
		closeSequenceResponse.setIdentifier(identifier);

		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		closeSeqResponseRMMsg.setSOAPEnvelop(envelope);
		closeSeqResponseRMMsg.setMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE_RESPONSE,
				closeSequenceResponse);

		outMessage.setWSAAction(SpecSpecificConstants.getCloseSequenceResponseAction(SandeshaUtil.getRMVersion(
				sequenceID, storageManager)));
		outMessage.setSoapAction(SpecSpecificConstants.getCloseSequenceResponseAction(SandeshaUtil.getRMVersion(
				sequenceID, storageManager)));

		initializeCreation(closeSeqRMMsg.getMessageContext(), outMessage);

		closeSeqResponseRMMsg.addSOAPEnvelope();

		finalizeCreation(closeSeqRMMsg.getMessageContext(), outMessage);
		closeSeqResponseRMMsg.getMessageContext().setServerSide(true);
		return closeSeqResponseRMMsg;
	}

	/**
	 * Adds an ack message to the given application message.
	 * 
	 * @param applicationMsg
	 * @param sequenceId
	 * @throws SandeshaException
	 */
	public static void addAckMessage(RMMsgContext applicationMsg, String sequencePropertyKey ,String sequenceId, StorageManager storageManager)
			throws AxisFault {
		
		SOAPFactory factory = null;
		SOAPEnvelope envelope = applicationMsg.getSOAPEnvelope();
		factory = (SOAPFactory) envelope.getOMFactory();
		
		envelope = applicationMsg.getSOAPEnvelope();

		String rmVersion = SandeshaUtil.getRMVersion(sequencePropertyKey, storageManager);
		if (rmVersion == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.cannotDecideRMVersion));

		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmVersion);

		SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement(rmNamespaceValue);
		Identifier id = new Identifier(rmNamespaceValue);
		id.setIndentifer(sequenceId);
		sequenceAck.setIdentifier(id);

		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropertyBeanMgr();

		SequencePropertyBean seqBean = seqPropMgr.retrieve(sequencePropertyKey,
				Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);
		String msgNoList = (String) seqBean.getValue();

		ArrayList ackRangeArrayList = SandeshaUtil.getAckRangeArrayList(msgNoList, factory, rmNamespaceValue);
		Iterator iterator = ackRangeArrayList.iterator();
		while (iterator.hasNext()) {
			AcknowledgementRange ackRange = (AcknowledgementRange) iterator.next();
			sequenceAck.addAcknowledgementRanges(ackRange);
		}

		SequencePropertyBean sequenceClosedBean = seqPropMgr.retrieve(sequencePropertyKey,
				Sandesha2Constants.SequenceProperties.SEQUENCE_CLOSED);

		if (sequenceClosedBean != null && Sandesha2Constants.VALUE_TRUE.equals(sequenceClosedBean.getValue())) {
			// sequence is closed. so add the 'Final' part.
			if (SpecSpecificConstants.isAckFinalAllowed(rmVersion)) {
				AckFinal ackFinal = new AckFinal(rmNamespaceValue);
				sequenceAck.setAckFinal(ackFinal);
			}
		}

		applicationMsg.setMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT, sequenceAck);

		sequenceAck.toOMElement(envelope.getHeader());
		
		if (applicationMsg.getWSAAction()==null) {
			applicationMsg.setAction(SpecSpecificConstants.getSequenceAcknowledgementAction(SandeshaUtil.getRMVersion(
					sequenceId, storageManager)));
			applicationMsg.setSOAPAction(SpecSpecificConstants.getSequenceAcknowledgementSOAPAction(SandeshaUtil
					.getRMVersion(sequenceId, storageManager)));
		}
		
		applicationMsg.setMessageId(SandeshaUtil.getUUID());
		
		//generating the SOAP envelope.
		applicationMsg.addSOAPEnvelope();
	}
	
	public static RMMsgContext createMakeConnectionMessage (RMMsgContext referenceRMMessage,  String makeConnectionSeqId,
			String makeConnectionAnonURI, StorageManager storageManager) throws AxisFault {
		
		MessageContext referenceMessage = referenceRMMessage.getMessageContext();
		String rmNamespaceValue = referenceRMMessage.getRMNamespaceValue();
		String rmVersion = referenceRMMessage.getRMSpecVersion();
		
		AxisOperation makeConnectionOperation = AxisOperationFactory.getAxisOperation(
				WSDL20_2004Constants.MEP_CONSTANT_OUT_IN);
		
		MessageContext makeConnectionMessageCtx = SandeshaUtil.createNewRelatedMessageContext(referenceRMMessage,makeConnectionOperation);
		RMMsgContext makeConnectionRMMessageCtx = MsgInitializer.initializeMessage(makeConnectionMessageCtx);
		
		MakeConnection makeConnection = new MakeConnection (rmNamespaceValue);
		if (makeConnectionSeqId!=null) {
			Identifier identifier = new Identifier (rmNamespaceValue);
			identifier.setIndentifer(makeConnectionSeqId);
			makeConnection.setIdentifier(identifier);
		}
		
		if (makeConnectionAnonURI!=null) {
			Address address = new Address (rmNamespaceValue);
			address.setAddress (makeConnectionAnonURI);
			makeConnection.setAddress(address);
		}
		
		//setting the addressing properties
		makeConnectionMessageCtx.setTo(new EndpointReference (referenceMessage.getTo().getAddress()));
		makeConnectionMessageCtx.setWSAAction(SpecSpecificConstants.getMakeConnectionAction(rmVersion));
		makeConnectionMessageCtx.setMessageID(SandeshaUtil.getUUID());
		
		makeConnectionRMMessageCtx.setMessagePart(Sandesha2Constants.MessageParts.MAKE_CONNECTION,
				makeConnection);
		
		//generating the SOAP Envelope.
		makeConnectionRMMessageCtx.addSOAPEnvelope();
		
		return makeConnectionRMMessageCtx;
	}

}
