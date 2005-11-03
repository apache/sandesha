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

import java.math.BigInteger;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultDetail;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultText;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.FaultCode;
import org.apache.sandesha2.wsrm.SequenceFault;

/**
 * @author Sanka
 */

public class FaultMgr {

	private static final String WSA_ACTION_FAULT = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";

	public static final int UNKNOWN_SEQUENCE_TYPE = 1;

	public static final int MESSAGE_NUMBER_ROLLOVER_TYPE = 2;

	public static final int INVALID_ACKNOWLEDGEMENT_TYPE = 3;

	public static final String UNKNOW_SEQUENCE_CODE = "Sender";

	public static final String UNKNOW_SEQUENCE_SUBCODE = "wsrm:UnknowSequence";

	public static final String UNKNOW_SEQUENCE_RESON = "The vaule of wsrm:Identifier is not a known Sequence Identifer";

	public static final String MESSAGE_NUMBER_ROLLOVER_CODE = "Sender";

	public static final String MESSAGE_NUMBER_ROLLOVER_SUBCODE = "wsrm:MessageNumberRollover";

	public static final String MESSAGE_NUMBER_ROLLOVER_REASON = "The maximum value of wsrm:MessageNumber has been exceeded.";

	public static final String INVALID_ACKNOWLEDGEMENT_CODE = "Sender";

	public static final String INVALID_ACKNOWLEDGEMENT_SUBCODE = "wsrm:InvalidAcknowledgement";

	public static final String INVALID_ACKNOWLEDGEMENT_REASON = "The SequenceAcknowledgement violates the cumlative acknowledgement";

	protected class FaultData {
		int type;

		String code;

		String subcode;

		String reason;

		OMElement detail;

		MessageContext msgCtx;
	}

	public FaultMgr() {
	}

	/**
	 * 
	 * @param msgCtx
	 * @return @throws
	 *         SandeshaException
	 */
	public RMMsgContext check(MessageContext msgCtx) throws SandeshaException {
		int msgType = getMessageType(msgCtx);

		switch (msgType) {

		case Constants.MessageTypes.APPLICATION:

			/* Sequence */
			return checkSequenceMsg(msgCtx);

		case Constants.MessageTypes.ACK:

			/* SequenceAcknowledgement */
			return checkSequenceAcknowledgementMsg(msgCtx);

		default:

			/* TODO check for other message types */
			return null;
		}
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
	public RMMsgContext checkSequenceMsg(MessageContext msgCtx)
			throws SandeshaException {

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(msgCtx.getSystemContext());
		NextMsgBeanMgr mgr = storageManager.getNextMsgBeanMgr();
		SOAPEnvelope envelope = msgCtx.getEnvelope();

		OMElement element = envelope.getHeader().getFirstChildWithName(
				new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));
		OMElement identifier = element.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));

		String identifierString = identifier.getText().trim();

		if (mgr.retrieve(identifierString) == null) {

			// should throw an UnknownSequence fault
			return getFault(FaultMgr.UNKNOWN_SEQUENCE_TYPE, msgCtx);
		}

		OMElement msgNumber = element.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.MSG_NUMBER));

		BigInteger bigInteger = new BigInteger(msgNumber.getText().trim());

		// throws new MessageNumberRollover fault
		if (bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
			return getFault(FaultMgr.MESSAGE_NUMBER_ROLLOVER_TYPE, msgCtx);
		}

		// Phew !! no faults ..
		return null;
	}

	/**
	 * 
	 * @param msgCtx
	 * @return @throws
	 *         SandeshaException
	 */
	public RMMsgContext checkSequenceAcknowledgementMsg(MessageContext msgCtx)
			throws SandeshaException {

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(msgCtx.getSystemContext());

		NextMsgBeanMgr mgr = storageManager.getNextMsgBeanMgr();
		StorageMapBeanMgr smgr = storageManager.getStorageMapBeanMgr();
		SequencePropertyBeanMgr propertyBeanMgr = storageManager
				.getSequencePropretyBeanMgr();

		SOAPEnvelope envelope = msgCtx.getEnvelope();
		// this is a SequenceAcknowledgement message
		OMElement element = envelope.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE_ACK));
		OMElement identifier = element.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));

		if (mgr.retrieve(identifier.getText()) == null) {
			//throw UnknownSequenceFault
			return getFault(FaultMgr.UNKNOWN_SEQUENCE_TYPE, msgCtx);
		}

		String identifierString = identifier.getText().trim();
		SequencePropertyBean propertyBean = propertyBeanMgr
				.retrieve(identifierString,
						Constants.SequenceProperties.TEMP_SEQUENCE_ID);

		//TODO
		String acksString = ""; //propertyBean.getAcksString();
		String[] msgNumberStrs = acksString.split(",");

		//TODO move this to a util class
		long[] msgNumbers = new long[msgNumberStrs.length];
		for (int i = 0; i < msgNumbers.length; i++) {
			msgNumbers[i] = Long.parseLong(msgNumberStrs[i]);
		}

		Iterator acks = element.getChildrenWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.ACK_RANGE));
		while (acks.hasNext()) {
			OMElement ack = (OMElement) acks.next();

			OMAttribute lowerAttrib = ack.getAttribute(new QName(
					Constants.WSRM.LOWER));
			long lower = Long.parseLong(lowerAttrib.getAttributeValue());

			OMAttribute upperAttrib = ack.getAttribute(new QName(
					Constants.WSRM.UPPER));
			long upper = Long.parseLong(upperAttrib.getAttributeValue());

			for (; lower <= upper; lower++) {
				boolean found = false;
				for (int j = 0; j < msgNumbers.length; j++) {
					if (lower == msgNumbers[j]) {
						found = true;
						break;
					}
				}
				if (!found) {
					//TODO throw new InvalidAcknowledgement
				}
			}
		}

		Iterator nacks = element.getChildrenWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.NACK));

		while (nacks.hasNext()) {
			OMElement nack = (OMElement) nacks.next();
			long msgNo = Long.parseLong(nack.getText());

			boolean found = false;
			for (int j = 0; j < msgNumbers.length; j++) {
				if (msgNo == msgNumbers[j]) {
					found = true;
					break;
				}
			}
			if (!found) {
				//TODO throw new InvalidAcknowledgement
			}
		}
		return null;
	}

	/**
	 * 
	 * @param type
	 * @param msgCtx
	 * @return @throws
	 *         SandeshaException
	 */
	public RMMsgContext getFault(int type, MessageContext msgCtx)
			throws SandeshaException {

		FaultData data = new FaultData();

		switch (type) {
		case FaultMgr.UNKNOWN_SEQUENCE_TYPE:
			data.code = FaultMgr.UNKNOW_SEQUENCE_CODE;
			data.subcode = FaultMgr.UNKNOW_SEQUENCE_SUBCODE;
			data.reason = FaultMgr.UNKNOW_SEQUENCE_RESON;
			data.msgCtx = msgCtx;
			break;

		case FaultMgr.MESSAGE_NUMBER_ROLLOVER_TYPE:
			data.code = FaultMgr.MESSAGE_NUMBER_ROLLOVER_CODE;
			data.subcode = FaultMgr.MESSAGE_NUMBER_ROLLOVER_SUBCODE;
			data.reason = FaultMgr.MESSAGE_NUMBER_ROLLOVER_REASON;
			data.msgCtx = msgCtx;
			break;

		case FaultMgr.INVALID_ACKNOWLEDGEMENT_TYPE:
			data.code = FaultMgr.INVALID_ACKNOWLEDGEMENT_CODE;
			data.subcode = FaultMgr.INVALID_ACKNOWLEDGEMENT_SUBCODE;
			data.reason = FaultMgr.INVALID_ACKNOWLEDGEMENT_REASON;
			data.msgCtx = msgCtx;
			break;
		}

		data.type = getMessageType(msgCtx);

		MessageContext newMsgCtx = SandeshaUtil.shallowCopy(msgCtx);
		newMsgCtx.setServiceGroupContextId(msgCtx.getServiceGroupContextId());
		newMsgCtx.setServiceContext(msgCtx.getServiceContext());
		RMMsgContext newRMMsgCtx = new RMMsgContext(newMsgCtx);

		SequenceFault seqFault = new SequenceFault();
		FaultCode faultCode = new FaultCode();
		faultCode.setFaultCode(data.code);
		seqFault.setFaultCode(faultCode);

		//TODO
		//newRMMsgCtx.setMessagePart(Constants.MessageParts.SEQUENCE_FAULT,
		// seqFault);

		String str = msgCtx.getEnvelope().getNamespace().getName();

		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(msgCtx
				.getEnvelope().getNamespace().getName())) {
			doSOAP11Encoding(data, newMsgCtx);

		} else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(msgCtx
				.getEnvelope().getNamespace().getName())) {
			doSOAP12Encoding(data, newMsgCtx);

		} else {
			//TODO should I throw an exception ?
		}

		return newRMMsgCtx;
	}

	/**
	 * 
	 * @param data
	 * @param msgCtx
	 * @throws SandeshaException
	 */
	public void doSOAP11Encoding(FaultData data, MessageContext msgCtx)
			throws SandeshaException {

		SOAPFactory factory = SOAPAbstractFactory
				.getSOAPFactory(Constants.SOAPVersion.v1_1);
		SOAPEnvelope faultMsgEnvelope = factory.getDefaultFaultEnvelope();

		SOAPFault fault;
		SOAPFaultCode faultCode;
		SOAPFaultReason faultReason;
		SOAPFaultText faultText;

		switch (data.type) {
		case Constants.MessageTypes.CREATE_SEQ:

			/* CreateSequence */
			fault = faultMsgEnvelope.getBody().getFault();
			faultCode = fault.getCode();
			faultCode.getValue().setText(data.subcode);

			faultReason = fault.getReason();
			faultReason.getSOAPText().setText(data.reason);

			// TODO
			OMNamespace namespace = factory.createOMNamespace(
					OMConstants.XMLNS_URI, OMConstants.XMLNS_PREFIX);
			faultReason.getSOAPText().addAttribute("lang", "en", namespace);
			break;

		default:

			/* */
			fault = faultMsgEnvelope.getBody().getFault();
			faultCode = fault.getCode();
			faultCode.getValue().setText(data.code);

			faultReason = fault.getReason();
			faultReason.getSOAPText().setText(data.reason);
			break;
		}

		try {
			msgCtx.setEnvelope(faultMsgEnvelope);

		} catch (AxisFault axisFault) {
			throw new SandeshaException(axisFault.getMessage());
		}
	}

	/**
	 * 
	 * @param msgCtx
	 * @return
	 */
	public void doSOAP12Encoding(FaultData data, MessageContext msgCtx)
			throws SandeshaException {

		SOAPFactory factory = SOAPAbstractFactory
				.getSOAPFactory(Constants.SOAPVersion.v1_2);
		SOAPEnvelope envelope = factory.getDefaultFaultEnvelope();

		SOAPFault fault = envelope.getBody().getFault();
		SOAPFaultCode faultCode = fault.getCode();
		SOAPFaultValue codeValue = faultCode.getValue();
		codeValue.setText(data.code);

		SOAPFaultSubCode faultSubCode = factory
				.createSOAPFaultSubCode(faultCode);
		SOAPFaultValue subCodeValue = factory
				.createSOAPFaultValue(faultSubCode);
		subCodeValue.setText(data.subcode);

		SOAPFaultReason faultReason = fault.getReason();
		SOAPFaultText faultText = faultReason.getSOAPText();
		faultText.setText(data.reason);

		SOAPFaultDetail faultDetail = fault.getDetail();

		SOAPEnvelope ienvelope = data.msgCtx.getEnvelope();

		switch (data.type) {
		case Constants.MessageTypes.APPLICATION:

			/* Sequence */
			OMElement sequence = ienvelope.getHeader()
					.getFirstChildWithName(
							new QName(Constants.WSRM.NS_URI_RM,
									Constants.WSRM.SEQUENCE));
			OMElement sidentifier = sequence.getFirstChildWithName(new QName(
					Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));

			OMNamespace snamespace = factory.createOMNamespace(
					Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
			OMElement selement = factory.createOMElement(
					Constants.WSRM.IDENTIFIER, snamespace);

			selement.setText(sidentifier.getText());
			faultDetail.addChild(selement);
			break;

		case Constants.MessageTypes.ACK:

			/* SequenceAcknowledge */

			OMElement sequenceAck = ienvelope.getHeader()
					.getFirstChildWithName(
							new QName(Constants.WSRM.NS_URI_RM,
									Constants.WSRM.SEQUENCE_ACK));
			OMElement aidentifier = sequenceAck
					.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
							Constants.WSRM.IDENTIFIER));
			OMNamespace anamespace = factory.createOMNamespace(
					Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
			OMElement aelement = factory.createOMElement(
					Constants.WSRM.IDENTIFIER, anamespace);

			aelement.setText(aidentifier.getText());
			faultDetail.addChild(aelement);
			break;

		default:

			/* TODO for other message types */
			break;

		}

		msgCtx.setProperty(AddressingConstants.WSA_ACTION, WSA_ACTION_FAULT);

		try {
			msgCtx.setEnvelope(envelope);
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}
	}

	public int getMessageType(MessageContext msgCtx) {

		SOAPHeader header = msgCtx.getEnvelope().getHeader();
		SOAPBody body = msgCtx.getEnvelope().getBody();

		if (header.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
				Constants.WSRM.SEQUENCE)) != null) {
			return Constants.MessageTypes.APPLICATION;

		} else if (header.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE_ACK)) != null) {
			return Constants.MessageTypes.ACK;

		} else if (body.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.CREATE_SEQUENCE)) != null) {
			return Constants.MessageTypes.CREATE_SEQ;

		} else if (body.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM,
				Constants.WSRM.CREATE_SEQUENCE_RESPONSE)) != null) {
			return Constants.MessageTypes.CREATE_SEQ_RESPONSE;

		} else if (body.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.TERMINATE_SEQUENCE)) != null) {
			return Constants.MessageTypes.TERMINATE_SEQ;

		} else if (header.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.ACK_REQUESTED)) != null) {
			//TODO fix this ..
			return Constants.MessageTypes.UNKNOWN;

		} else {
			return Constants.MessageTypes.UNKNOWN;
		}

	}

}