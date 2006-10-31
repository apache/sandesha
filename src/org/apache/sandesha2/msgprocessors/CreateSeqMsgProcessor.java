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

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.addressing.AddressingFinalInHandler;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SandeshaListener;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.security.SecurityManager;
import org.apache.sandesha2.security.SecurityToken;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.FaultManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;
import org.apache.sandesha2.util.SpecSpecificConstants;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Endpoint;
import org.apache.sandesha2.wsrm.SequenceOffer;

/**
 * Responsible for processing an incoming Create Sequence message.
 */

public class CreateSeqMsgProcessor implements MsgProcessor {

	private static final Log log = LogFactory.getLog(CreateSeqMsgProcessor.class);

	public boolean processInMessage(RMMsgContext createSeqRMMsg) throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: CreateSeqMsgProcessor::processInMessage");

		MessageContext createSeqMsg = createSeqRMMsg.getMessageContext();
		CreateSequence createSeqPart = (CreateSequence) createSeqRMMsg
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSeqPart == null) {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.noCreateSeqParts);
			log.debug(message);
			throw new SandeshaException(message);
		}

		ConfigurationContext context = createSeqMsg.getConfigurationContext();
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(context, context.getAxisConfiguration());

		FaultManager faultManager = new FaultManager();
		SandeshaException fault = faultManager.checkForCreateSequenceRefused(createSeqMsg, storageManager);
		if (fault != null) {
			throw fault;
		}
		
		// If the inbound CreateSequence includes a SecurityTokenReference then
		// ask the security manager to resolve that to a token for us. We also
		// check that the Create was secured using the token.
		OMElement theSTR = createSeqPart.getSecurityTokenReference();
		SecurityToken token = null;
		if(theSTR != null) {
			SecurityManager secManager = SandeshaUtil.getSecurityManager(context);
			MessageContext msgcontext = createSeqRMMsg.getMessageContext();
			token = secManager.getSecurityToken(theSTR, msgcontext);
			
			// The create must be the body part of this message, so we check the
			// security of that element.
			OMElement body = msgcontext.getEnvelope().getBody();
			secManager.checkProofOfPossession(token, body, msgcontext);
		}

		MessageContext outMessage = null;
		try {
			outMessage = Utils.createOutMessageContext(createSeqMsg); // createing
																		// a new
																		// response
																		// message.
		} catch (AxisFault e1) {
			throw new SandeshaException(e1);
		}
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropertyBeanMgr();

		try {
			String newSequenceId = SequenceManager.setupNewSequence(createSeqRMMsg, storageManager); // newly
																										// created
																										// sequnceID.

			
			
			
			RMMsgContext createSeqResponse = RMMsgCreator.createCreateSeqResponseMsg(createSeqRMMsg, outMessage,
					newSequenceId, storageManager); // converting the blank out
													// message in to a create
			// sequence response.
			createSeqResponse.setFlow(MessageContext.OUT_FLOW);

			createSeqResponse.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE, "true"); // for
																									// making
																									// sure
																									// that
																									// this
																									// wont
																									// be
																									// processed
																									// again.
			CreateSequenceResponse createSeqResPart = (CreateSequenceResponse) createSeqResponse
					.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE);

			// OFFER PROCESSING
			SequenceOffer offer = createSeqPart.getSequenceOffer();
			if (offer != null) {
				Accept accept = createSeqResPart.getAccept();
				if (accept == null) {
					String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.noAcceptPart);
					log.debug(message);
					throw new SandeshaException(message);
				}

				String offeredSequenceID = offer.getIdentifer().getIdentifier(); // offered
																					// seq.
																					// id.

				boolean offerEcepted = offerAccepted(offeredSequenceID, context, createSeqRMMsg, storageManager);

				if (offerEcepted) {
					// Setting the CreateSequence table entry for the outgoing
					// side.
					CreateSeqBean createSeqBean = new CreateSeqBean();
					createSeqBean.setSequenceID(offeredSequenceID);
					String outgoingSideInternalSequenceId = SandeshaUtil
							.getOutgoingSideInternalSequenceID(newSequenceId);
					createSeqBean.setInternalSequenceID(outgoingSideInternalSequenceId);
					createSeqBean.setCreateSeqMsgID(SandeshaUtil.getUUID()); // this
																				// is a
																				// dummy
																				// value.
					
					
					String outgoingSideSequencePropertyKey = outgoingSideInternalSequenceId;

					CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();
					createSeqMgr.insert(createSeqBean);

					// Setting sequence properties for the outgoing sequence.
					// Only will be used by the server side response path. Will
					// be wasted properties for the client side.

					// setting the out_sequence_id
					SequencePropertyBean outSequenceBean = new SequencePropertyBean();
					outSequenceBean.setName(Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
					outSequenceBean.setValue(offeredSequenceID);
					outSequenceBean.setSequencePropertyKey(outgoingSideSequencePropertyKey);
					seqPropMgr.insert(outSequenceBean);

					// setting the internal_sequence_id
					SequencePropertyBean internalSequenceBean = new SequencePropertyBean();
					internalSequenceBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
					internalSequenceBean.setSequencePropertyKey(offeredSequenceID);
					internalSequenceBean.setValue(outgoingSideInternalSequenceId);
					seqPropMgr.insert(internalSequenceBean);
					
					Endpoint endpoint = offer.getEndpoint();
					if (endpoint!=null) {
						// setting the OfferedEndpoint
						SequencePropertyBean offeredEndpointBean = new SequencePropertyBean();
						offeredEndpointBean.setName(Sandesha2Constants.SequenceProperties.OFFERED_ENDPOINT);
					
						//currently we can only serialize the Address part of the Endpoint.
						//TODO correct this to serialize the whole EPR.
						offeredEndpointBean.setValue(endpoint.getEPR().getAddress());  
						offeredEndpointBean.setSequencePropertyKey(outgoingSideSequencePropertyKey);
						seqPropMgr.insert(offeredEndpointBean);
					}
				} else {
					// removing the accept part.
					createSeqResPart.setAccept(null);
					createSeqResponse.addSOAPEnvelope();
				}
			}

			EndpointReference acksTo = createSeqPart.getAcksTo().getEPR();
			if (acksTo == null || acksTo.getAddress() == null || "".equals(acksTo.getAddress())) {
				String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.noAcksToPartInCreateSequence);
				log.debug(message);
				throw new AxisFault(message);
			}
			
			//TODO add createSequenceResponse message as the referenceMessage to the NextMsgBean.
			

			SequencePropertyBean acksToBean = new SequencePropertyBean(newSequenceId,
					Sandesha2Constants.SequenceProperties.ACKS_TO_EPR, acksTo.getAddress());

			seqPropMgr.insert(acksToBean);

			outMessage.setResponseWritten(true);

			// commiting tr. before sending the response msg.

			SequenceManager.updateLastActivatedTime(newSequenceId, storageManager);

			AxisEngine engine = new AxisEngine(context);
			engine.send(outMessage);

			SequencePropertyBean toBean = seqPropMgr.retrieve(newSequenceId,
					Sandesha2Constants.SequenceProperties.TO_EPR);
			if (toBean == null) {
				String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.toEPRNotValid, null);
				log.debug(message);
				throw new SandeshaException(message);
			}

			EndpointReference toEPR = new EndpointReference(toBean.getValue());

			String addressingNamespaceURI = SandeshaUtil.getSequenceProperty(newSequenceId,
					Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE, storageManager);
			String anonymousURI = SpecSpecificConstants.getAddressingAnonymousURI(addressingNamespaceURI);

			if (anonymousURI.equals(toEPR.getAddress())) {
				createSeqMsg.getOperationContext().setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN, "true");
			} else {
				createSeqMsg.getOperationContext().setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN, "false");
			}

		} catch (AxisFault e1) {
			throw new SandeshaException(e1);
		}

		createSeqRMMsg.pause();

		if (log.isDebugEnabled())
			log.debug("Exit: CreateSeqMsgProcessor::processInMessage " + Boolean.TRUE);
		return true;
	}

	private boolean offerAccepted(String sequenceId, ConfigurationContext configCtx, RMMsgContext createSeqRMMsg,
			StorageManager storageManager) throws SandeshaException {
		if (log.isDebugEnabled())
			log.debug("Enter: CreateSeqMsgProcessor::offerAccepted, " + sequenceId);

		if ("".equals(sequenceId)) {
			if (log.isDebugEnabled())
				log.debug("Exit: CreateSeqMsgProcessor::offerAccepted, " + false);
			return false;
		}

		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();

		CreateSeqBean createSeqFindBean = new CreateSeqBean();
		createSeqFindBean.setSequenceID(sequenceId);
		Collection arr = createSeqMgr.find(createSeqFindBean);

		if (arr.size() > 0) {
			if (log.isDebugEnabled())
				log.debug("Exit: CreateSeqMsgProcessor::offerAccepted, " + false);
			return false;
		}
		if (sequenceId.length() <= 1) {
			if (log.isDebugEnabled())
				log.debug("Exit: CreateSeqMsgProcessor::offerAccepted, " + false);
			return false; // Single character offers are NOT accepted.
		}

		if (log.isDebugEnabled())
			log.debug("Exit: CreateSeqMsgProcessor::offerAccepted, " + true);
		return true;
	}

	public boolean processOutMessage(RMMsgContext rmMsgCtx) throws AxisFault {
	
		if (log.isDebugEnabled())
			log.debug("Enter: CreateSeqMsgProcessor::processOutMessage");

		boolean returnValue = false;
		
		MessageContext msgCtx = rmMsgCtx.getMessageContext();

		// adding the SANDESHA_LISTENER
		SandeshaListener faultCallback = (SandeshaListener) msgCtx.getOptions().getProperty(
				SandeshaClientConstants.SANDESHA_LISTENER);
		if (faultCallback != null) {
			OperationContext operationContext = msgCtx.getOperationContext();
			if (operationContext != null) {
				operationContext.setProperty(SandeshaClientConstants.SANDESHA_LISTENER, faultCallback);
			}
		}
						
		if (log.isDebugEnabled())
			log.debug("Exit: CreateSeqMsgProcessor::processOutMessage " + returnValue);
	
		return returnValue;

	}
	
}
