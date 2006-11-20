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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.transport.Sandesha2TransportOutDesc;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Contains logic for managing acknowledgements.
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
	public static void piggybackAcksIfPresent(RMMsgContext rmMessageContext, StorageManager storageManager)
			throws SandeshaException {
		if (log.isDebugEnabled())
			log.debug("Enter: AcknowledgementManager::piggybackAcksIfPresent");

		ConfigurationContext configurationContext = rmMessageContext.getConfigurationContext();

		SenderBeanMgr senderBeanMgr = storageManager.getSenderBeanMgr();

		SenderBean findBean = new SenderBean();

		findBean.setMessageType(Sandesha2Constants.MessageTypes.ACK);
		findBean.setSend(true);
		findBean.setReSend(false);

		String carrietTo = rmMessageContext.getTo().getAddress();

		Collection collection = senderBeanMgr.find(findBean);

		Iterator it = collection.iterator();

		piggybackLoop: while (it.hasNext()) {
			SenderBean ackBean = (SenderBean) it.next();

			long timeNow = System.currentTimeMillis();
			if (ackBean.getTimeToSend() > timeNow) {
				// //Piggybacking will happen only if the end of ack interval
				// (timeToSend) is not reached.

				MessageContext ackMsgContext = storageManager.retrieveMessageContext(ackBean.getMessageContextRefKey(),
						configurationContext);

				// wsa:To has to match for piggybacking.
				String to = ackMsgContext.getTo().getAddress();
				if (!carrietTo.equals(to)) {
					continue piggybackLoop;
				}

				if (log.isDebugEnabled()) log.debug("Adding ack headers");

				// deleting the ack entry.
				senderBeanMgr.delete(ackBean.getMessageID());

				// Adding the ack(s) to the application message
				boolean acks = false;
				SOAPHeader appMsgHeaders = rmMessageContext.getMessageContext().getEnvelope().getHeader();
				
				SOAPHeader headers = ackMsgContext.getEnvelope().getHeader();
				if(headers != null) {
					for(int i = 0; i < Sandesha2Constants.SPEC_NS_URIS.length; i++) {

						QName name = new QName(Sandesha2Constants.SPEC_NS_URIS[i], Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK);
						Iterator iter = headers.getChildrenWithName(name);
						while(iter.hasNext()) {
							OMElement ackElement = (OMElement) iter.next();

							SequenceAcknowledgement sequenceAcknowledgement = new SequenceAcknowledgement (Sandesha2Constants.SPEC_NS_URIS[i]);
							sequenceAcknowledgement.fromOMElement(ackElement);
							
							sequenceAcknowledgement.toOMElement(appMsgHeaders);
							acks = true;
						}
					}
				}
				
				if (!acks) {
					String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.invalidAckMessageEntry,
							ackMsgContext.getEnvelope().toString());
					log.debug(message);
					throw new SandeshaException(message);
				}
			}
		}

		if (log.isDebugEnabled())
			log.debug("Exit: AcknowledgementManager::piggybackAcksIfPresent");
	}

	/**
	 * this is used to get the acked messages of a sequence. If this is an
	 * outgoing message the sequenceIdentifier should be the internal
	 * sequenceID.
	 * 
	 * @param sequenceIdentifier
	 * @param outGoingMessage
	 * @return
	 */
	public static ArrayList getClientCompletedMessagesList(String sequenceID, SequencePropertyBeanMgr seqPropMgr)
			throws SandeshaException {
		if (log.isDebugEnabled())
			log.debug("Enter: AcknowledgementManager::getClientCompletedMessagesList");

		// first trying to get it from the internal sequence id.
		SequencePropertyBean internalSequenceBean = seqPropMgr.retrieve(sequenceID,
				Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		String internalSequenceID = null;
		if (internalSequenceBean != null)
			internalSequenceID = internalSequenceBean.getValue();

		SequencePropertyBean completedMessagesBean = null;
		if (internalSequenceID != null)
			completedMessagesBean = seqPropMgr.retrieve(internalSequenceID,
					Sandesha2Constants.SequenceProperties.CLIENT_COMPLETED_MESSAGES);

		if (completedMessagesBean == null)
			completedMessagesBean = seqPropMgr.retrieve(sequenceID,
					Sandesha2Constants.SequenceProperties.CLIENT_COMPLETED_MESSAGES);

		ArrayList completedMsgList = null;
		if (completedMessagesBean != null) {
			completedMsgList = SandeshaUtil.getArrayListFromString(completedMessagesBean.getValue());
		} else {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.completedMsgBeanIsNull, sequenceID);
			SandeshaException e = new SandeshaException(message);
			if(log.isDebugEnabled()) log.debug("Throwing exception", e);
			throw e;
		}

		if (log.isDebugEnabled())
			log.debug("Exit: AcknowledgementManager::getClientCompletedMessagesList");
		return completedMsgList;
	}

	public static ArrayList getServerCompletedMessagesList(String sequenceID, SequencePropertyBeanMgr seqPropMgr)
			throws SandeshaException {
		if (log.isDebugEnabled())
			log.debug("Enter: AcknowledgementManager::getServerCompletedMessagesList");

		SequencePropertyBean completedMessagesBean = null;

		completedMessagesBean = seqPropMgr.retrieve(sequenceID,
				Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);

		ArrayList completedMsgList = null;
		if (completedMessagesBean != null) {
			completedMsgList = SandeshaUtil.getArrayListFromMsgsString(completedMessagesBean.getValue());
		} else {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.completedMsgBeanIsNull, sequenceID);
			SandeshaException e = new SandeshaException(message);
			if(log.isDebugEnabled()) log.debug("Throwing exception", e);
			throw e;
		}

		if (log.isDebugEnabled())
			log.debug("Exit: AcknowledgementManager::getServerCompletedMessagesList");
		return completedMsgList;
	}

	public static RMMsgContext generateAckMessage(RMMsgContext referenceRMMessage, String sequencePropertyKey ,String sequenceId,
			StorageManager storageManager) throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: AcknowledgementManager::generateAckMessage");

		MessageContext referenceMsg = referenceRMMessage.getMessageContext();

		ConfigurationContext configurationContext = referenceRMMessage.getMessageContext().getConfigurationContext();
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropertyBeanMgr();

		// Setting the ack depending on AcksTo.
		SequencePropertyBean acksToBean = seqPropMgr.retrieve(sequencePropertyKey,
				Sandesha2Constants.SequenceProperties.ACKS_TO_EPR);

		EndpointReference acksTo = new EndpointReference(acksToBean.getValue());
		String acksToStr = acksTo.getAddress();

		if (acksToStr == null)
			throw new SandeshaException(SandeshaMessageHelper.getMessage(SandeshaMessageKeys.acksToStrNotSet));

		AxisOperation ackOperation = null;

		ackOperation = AxisOperationFactory.getOperationDescription(WSDL20_2004Constants.MEP_URI_IN_ONLY);

		AxisOperation rmMsgOperation = referenceRMMessage.getMessageContext().getAxisOperation();
		if (rmMsgOperation != null) {
			ArrayList outFlow = rmMsgOperation.getPhasesOutFlow();
			if (outFlow != null) {
				ackOperation.setPhasesOutFlow(outFlow);
				ackOperation.setPhasesOutFaultFlow(outFlow);
			}
		}

		MessageContext ackMsgCtx = SandeshaUtil.createNewRelatedMessageContext(referenceRMMessage, ackOperation);
		ackMsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, referenceMsg
				.getProperty(AddressingConstants.WS_ADDRESSING_VERSION)); // TODO
																			// do
																			// this
																			// in
																			// the
																			// RMMsgCreator

		ackMsgCtx.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE, "true");

		RMMsgContext ackRMMsgCtx = MsgInitializer.initializeMessage(ackMsgCtx);
		ackRMMsgCtx.setRMNamespaceValue(referenceRMMessage.getRMNamespaceValue());

		ackMsgCtx.setMessageID(SandeshaUtil.getUUID());

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(referenceMsg.getEnvelope()));

		// Setting new envelope
		SOAPEnvelope envelope = factory.getDefaultEnvelope();

		ackMsgCtx.setEnvelope(envelope);

		ackMsgCtx.setTo(acksTo);

		// adding the SequenceAcknowledgement part.
		RMMsgCreator.addAckMessage(ackRMMsgCtx, sequencePropertyKey ,sequenceId, storageManager);

		ackMsgCtx.setProperty(MessageContext.TRANSPORT_IN, null);

		String addressingNamespaceURI = SandeshaUtil.getSequenceProperty(sequencePropertyKey,
				Sandesha2Constants.SequenceProperties.ADDRESSING_NAMESPACE_VALUE, storageManager);

		ackMsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, addressingNamespaceURI);

		if (log.isDebugEnabled())
			log.debug("Exit: AcknowledgementManager::generateAckMessage");
		return ackRMMsgCtx;
	}

	public static boolean verifySequenceCompletion(Iterator ackRangesIterator, long lastMessageNo) {
		if (log.isDebugEnabled())
			log.debug("Enter: AcknowledgementManager::verifySequenceCompletion");

		HashMap startMap = new HashMap();

		while (ackRangesIterator.hasNext()) {
			AcknowledgementRange temp = (AcknowledgementRange) ackRangesIterator.next();
			startMap.put(new Long(temp.getLowerValue()), temp);
		}

		long start = 1;
		boolean result = false;
		while (!result) {
			AcknowledgementRange temp = (AcknowledgementRange) startMap.get(new Long(start));
			if (temp == null) {
				break;
			}

			if (temp.getUpperValue() >= lastMessageNo)
				result = true;

			start = temp.getUpperValue() + 1;
		}

		if (log.isDebugEnabled())
			log.debug("Enter: AcknowledgementManager::verifySequenceCompletion " + result);
		return result;
	}
	
	public static void addFinalAcknowledgement () {
		
	}
}
