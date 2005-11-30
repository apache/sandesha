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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.util.Utils;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.FaultData;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SOAPFaultEnvelopeCreator;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceOffer;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * Has logic to check for possible RM related faults and create it.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class FaultManager {

	public FaultManager() {
	}

	public RMMsgContext checkForPossibleFaults(MessageContext msgCtx)
			throws SandeshaException {

		//Cannot initialize message before checking for MsgNoRoleover - since
		// initialization will give an exception
		//for rolled over messages.

		SOAPEnvelope envelope = msgCtx.getEnvelope();
		if (envelope == null)
			throw new SandeshaException("SOAP Envelope is null");

		RMMsgContext faultMessageContext = null;

		SOAPHeader header = envelope.getHeader();
		if (header != null) {
			OMElement sequenceHeaderBlock = header
					.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
							Sandesha2Constants.WSRM.SEQUENCE));
			if (sequenceHeaderBlock != null) {
				faultMessageContext = checkForMessageNumberRoleover(msgCtx);
				if (faultMessageContext != null)
					return faultMessageContext;
			}
		}

		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		int msgType = rmMsgCtx.getMessageType();

		if (msgType == Sandesha2Constants.MessageTypes.APPLICATION
				|| msgType == Sandesha2Constants.MessageTypes.TERMINATE_SEQ) {
			faultMessageContext = checkForUnknownSequence(msgCtx);
			if (faultMessageContext != null)
				return faultMessageContext;

		}

		if (msgType == Sandesha2Constants.MessageTypes.CREATE_SEQ) {
			faultMessageContext = checkForCreateSequenceRefused(msgCtx);
			if (faultMessageContext != null)
				return faultMessageContext;
		}

		if (msgType == Sandesha2Constants.MessageTypes.ACK) {
			faultMessageContext = checkForInvalidAcknowledgement(msgCtx);
			if (faultMessageContext != null)
				return faultMessageContext;
		}

		if (msgType == Sandesha2Constants.MessageTypes.APPLICATION) {
			faultMessageContext = checkForLastMsgNumberExceeded(msgCtx);
			if (faultMessageContext != null)
				return faultMessageContext;
		}

		return faultMessageContext;

	}

	/**
	 * Check weather the CreateSequence should be refused and generate the fault if it should.
	 * 
	 * @param messageContext
	 * @return
	 * @throws SandeshaException
	 */
	private RMMsgContext checkForCreateSequenceRefused(
			MessageContext messageContext) throws SandeshaException {

		RMMsgContext rmMsgCtx = MsgInitializer
				.initializeMessage(messageContext);

		CreateSequence createSequence = (CreateSequence) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSequence == null)
			throw new SandeshaException(
					"CreateSequence message does not have a CreateSequence part");

		ConfigurationContext context = messageContext.getSystemContext();
		StorageManager storageManager = (StorageManager) SandeshaUtil
				.getSandeshaStorageManager(context);
		if (storageManager == null)
			throw new SandeshaException("Storage Manager is null");

		boolean refuseSequence = false;
		String reason = "";

		SequenceOffer offer = createSequence.getSequenceOffer();
		if (offer != null) {
			String offeredSequenceId = offer.getIdentifer().getIdentifier();
			if (offeredSequenceId == null || "".equals(offeredSequenceId)) {
				refuseSequence = true;
				reason = "Offered sequenceId is invalid";
			} else {
				NextMsgBeanMgr nextMsgBeanMgr = storageManager
						.getNextMsgBeanMgr();
				Collection collection = nextMsgBeanMgr.retrieveAll();
				Iterator it = collection.iterator();
				while (it.hasNext()) {

					//checking weather a incoming sequence with the given id
					// exists.
					NextMsgBean nextMsgBean = (NextMsgBean) it.next();
					String sequenceId = nextMsgBean.getSequenceId();
					if (sequenceId.equals(offeredSequenceId)) {
						refuseSequence = true;
						reason = "A sequence with offered sequenceId, already axists";
					}

					//checking weather an outgoing sequence with the given id
					// exists.
					SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager
							.getSequencePropretyBeanMgr();
					SequencePropertyBean sequencePropertyBean = sequencePropertyBeanMgr
							.retrieve(
									sequenceId,
									Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
					if (sequencePropertyBean != null) {
						String outSequenceId = (String) sequencePropertyBean
								.getValue();
						if (outSequenceId != null
								&& outSequenceId.equals(offeredSequenceId)) {
							refuseSequence = true;
							reason = "A sequence with offered sequenceId, already axists";
						}

					}
				}
			}
		}

		//TODO - if (securityTokenReference is present RefuseCreateSequence)

		if (refuseSequence) {

			FaultData data = new FaultData();
			data
					.setType(Sandesha2Constants.SOAPFaults.FaultType.CREATE_SEQUENCE_REFUSED);

			int SOAPVersion = SandeshaUtil.getSOAPVersion(rmMsgCtx
					.getSOAPEnvelope());
			if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
				data.setCode(SOAP11Constants.FAULT_CODE_SENDER);
			else
				data.setCode(SOAP12Constants.FAULT_CODE_SENDER);

			data
					.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.CREATE_SEQUENCE_REFUSED);
			data.setReason(reason);
			return getFault(rmMsgCtx, data);
		}

		return null;

	}

	/**
	 * Check weather the LastMessage number has been exceeded and generate the fault if it is.
	 * 
	 * @param msgCtx
	 * @return
	 */
	private RMMsgContext checkForLastMsgNumberExceeded(MessageContext msgCtx) {
		return null;
	}

	private RMMsgContext checkForMessageNumberRoleover(
			MessageContext messageContext) {
		return null;
	}

	/**
	 * Check whether a Sequence message (a) belongs to a unknown sequence
	 * (generates an UnknownSequence fault) (b) message number exceeds a
	 * predifined limit ( genenrates a Message Number Rollover fault)
	 * 
	 * @param msgCtx
	 * @return @throws
	 *         SandeshaException
	 */
	public RMMsgContext checkForUnknownSequence(MessageContext messageContext)
			throws SandeshaException {

		RMMsgContext rmMsgCtx = MsgInitializer
				.initializeMessage(messageContext);
		String sequenceId = null;

		if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.APPLICATION) {
			Sequence sequence = (Sequence) rmMsgCtx
					.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			if (sequence == null)
				throw new SandeshaException(
						"Sequence part not found in the application message");

			sequenceId = sequence.getIdentifier().getIdentifier();

		} else if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.ACK) {
			SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) rmMsgCtx
					.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
			sequenceId = sequenceAcknowledgement.getIdentifier()
					.getIdentifier();
		} else if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.TERMINATE_SEQ) {
			TerminateSequence terminateSequence = (TerminateSequence) rmMsgCtx
					.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
			sequenceId = terminateSequence.getIdentifier().getIdentifier();
		} else {
			//sequenceId not found.
			return null;
		}

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(messageContext.getSystemContext());

		NextMsgBeanMgr mgr = storageManager.getNextMsgBeanMgr();
		SOAPEnvelope envelope = messageContext.getEnvelope();

		Collection coll = mgr.retrieveAll();
		Iterator it = coll.iterator();

		boolean validSequence = false;

		while (it.hasNext()) {
			NextMsgBean nextMsgBean = (NextMsgBean) it.next();
			String tempId = nextMsgBean.getSequenceId();
			if (tempId.equals(sequenceId)) {
				validSequence = true;
				break;
			}
		}

		if (!validSequence) {
			//Return an UnknownSequence error
			int SOAPVersion = SandeshaUtil.getSOAPVersion(envelope);

			FaultData data = new FaultData();
			if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
				data.setCode(SOAP11Constants.FAULT_CODE_SENDER);
			else
				data.setCode(SOAP12Constants.FAULT_CODE_SENDER);

			data.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.UNKNOWN_SEQUENCE);

			SOAPFactory factory = SOAPAbstractFactory
					.getSOAPFactory(SOAPVersion);
			Identifier identifier = new Identifier(factory);
			identifier.setIndentifer(sequenceId);
			OMElement identifierOMElem = identifier.getOMElement();
			data.setDetail(identifierOMElem);
			data
					.setReason("The value of wsrm:Identifier is not a known Sequence identifier");

			return getFault(rmMsgCtx, data);

		}

		return null;
	}

	/**
	 * Check weather the Acknowledgement is invalid and generate a fault if it is.
	 * 
	 * @param msgCtx
	 * @return @throws
	 *         SandeshaException
	 */
	public RMMsgContext checkForInvalidAcknowledgement(MessageContext msgCtx)
			throws SandeshaException {

		//check lower<=upper
		//TODO acked for not-send message
		RMMsgContext rmMsgContext = new RMMsgContext();
		if (rmMsgContext.getMessageType() != Sandesha2Constants.MessageTypes.ACK)
			return null;

		SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) rmMsgContext
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		List sequenceAckList = sequenceAcknowledgement
				.getAcknowledgementRanges();
		Iterator it = sequenceAckList.iterator();

		while (it.hasNext()) {
			AcknowledgementRange acknowledgementRange = (AcknowledgementRange) it
					.next();
			long upper = acknowledgementRange.getUpperValue();
			long lower = acknowledgementRange.getLowerValue();

			if (lower > upper) {
				//Invalid ack
				FaultData data = new FaultData();
				int SOAPVersion = SandeshaUtil.getSOAPVersion(msgCtx
						.getEnvelope());
				if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
					data.setCode(SOAP11Constants.FAULT_CODE_SENDER);
				else
					data.setCode(SOAP12Constants.FAULT_CODE_SENDER);

				data.setSubcode(Sandesha2Constants.SOAPFaults.Subcodes.INVALID_ACKNOWLEDGEMENT);
				data.setSubcode("The SequenceAcknowledgement is invalid. Lower value is larger than upper value");
				data.setDetail(sequenceAcknowledgement.getOMElement());

				return getFault(rmMsgContext, data);
			}
		}

		return null;
	}


	/**
	 * Returns a RMMessageContext for the fault message. Data for generating the fault is given in the data parameter.
	 * 
	 * @param referenceRMMsgContext
	 * @param data
	 * @return
	 * @throws SandeshaException
	 */
	public RMMsgContext getFault(RMMsgContext referenceRMMsgContext,
			FaultData data) throws SandeshaException {

		try {

			MessageContext referenceMessage = referenceRMMsgContext
					.getMessageContext();
			MessageContext faultMsgContext = Utils
					.createOutMessageContext(referenceMessage);

			StorageManager storageManager = SandeshaUtil
					.getSandeshaStorageManager(referenceMessage
							.getSystemContext());

			//setting contexts.
			faultMsgContext.setAxisServiceGroup(referenceMessage
					.getAxisServiceGroup());
			faultMsgContext.setAxisService(referenceMessage.getAxisService());
			faultMsgContext.setAxisServiceGroup(referenceMessage
					.getAxisServiceGroup());
			faultMsgContext.setServiceGroupContext(referenceMessage
					.getServiceGroupContext());
			faultMsgContext.setServiceGroupContextId(referenceMessage
					.getServiceGroupContextId());
			faultMsgContext.setServiceContext(referenceMessage
					.getServiceContext());
			faultMsgContext.setServiceContextID(referenceMessage
					.getServiceContextID());

			AxisOperation operation = AxisOperationFactory
					.getAxisOperation(AxisOperationFactory.MEP_CONSTANT_OUT_ONLY);

			OperationContext operationContext = new OperationContext(operation);

			faultMsgContext.setAxisOperation(operation);
			faultMsgContext.setOperationContext(operationContext);

			String acksToStr = null;
			if (referenceRMMsgContext.getMessageType() == Sandesha2Constants.MessageTypes.CREATE_SEQ) {
				CreateSequence createSequence = (CreateSequence) referenceRMMsgContext
						.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
				acksToStr = createSequence.getAcksTo().getAddress().getEpr()
						.getAddress();
			} else {
				SequencePropertyBeanMgr seqPropMgr = storageManager
						.getSequencePropretyBeanMgr();
				String sequenceId = data.getSequenceId();
				SequencePropertyBean acksToBean = seqPropMgr.retrieve(
						sequenceId, Sandesha2Constants.SequenceProperties.ACKS_TO_EPR);
				if (acksToBean != null) {
					EndpointReference epr = (EndpointReference) acksToBean
							.getValue();
					if (epr != null)
						acksToStr = epr.getAddress();
				}
			}

			if (acksToStr != null
					&& !acksToStr.equals(Sandesha2Constants.WSA.NS_URI_ANONYMOUS)) {
				faultMsgContext.setTo(new EndpointReference(acksToStr));
			}

			int SOAPVersion = SandeshaUtil.getSOAPVersion(referenceMessage
					.getEnvelope());

			SOAPFaultEnvelopeCreator.addSOAPFaultEnvelope(faultMsgContext,
					SOAPVersion, data);

			RMMsgContext faultRMMsgCtx = MsgInitializer
					.initializeMessage(faultMsgContext);

			return faultRMMsgCtx;

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

	}
}