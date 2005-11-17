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

package org.apache.sandesha2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultDetail;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultText;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.FaultCode;
import org.apache.sandesha2.wsrm.SequenceFault;

/**
 * @author chamikara
 * 
 */

public class SOAPFaultEnvelopeCreator {

	public static void addSOAPFaultEnvelope (MessageContext faultMsgContext, int SOAPVersion, FaultData faultData) throws SandeshaException {
		
		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SOAPVersion);
		SOAPEnvelope env = factory.getDefaultFaultEnvelope();
		
		try {
			faultMsgContext.setEnvelope (env);
		} catch (AxisFault e) {
			throw new SandeshaException (e.getMessage());
		}
		
		if (SOAPVersion==Constants.SOAPVersion.v1_1)
			doSOAP11Encoding(faultMsgContext,faultData);
		else
			doSOAP12Encoding(faultMsgContext,faultData);
		
	}
	
	private static boolean isSequenceFault (FaultData faultData) {
		
		boolean sequenceFault = false;
		
		int faultType = faultData.getType();
		
		if (faultType<=0)
			return false;
		
		if (faultType==Constants.SOAPFaults.FaultType.CREATE_SEQUENCE_REFUSED)
			sequenceFault = true;
		
		if (faultType==Constants.SOAPFaults.FaultType.UNKNOWN_SEQUENCE)
			sequenceFault = true;
		
		return sequenceFault;
		
	}
	
	private static void addSequenceFaultHeader (MessageContext faultMessageContext, FaultData faultData,SOAPFactory factory) {
		
		
		SequenceFault sequenceFault = new SequenceFault (factory);
		
		FaultCode faultCode = new FaultCode (factory);
		faultCode.setFaultCode(faultData.getSubcode());
		sequenceFault.setFaultCode(faultCode);
	}
	
	private static void doSOAP11Encoding(MessageContext faultMsgContext,FaultData data) throws SandeshaException {
		
		//MessageContext faultMsgContext = faultRMMsgContext.getMessageContext();

		SOAPEnvelope faultMsgEnvelope = faultMsgContext.getEnvelope();
		if (faultMsgEnvelope==null)
			throw new SandeshaException ("SOAP Envelope is null");
		
		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(faultMsgEnvelope));

		SOAPFault fault = faultMsgEnvelope.getBody().getFault();
		SOAPFaultCode faultCode = fault.getCode();

		if (isSequenceFault(data)) {
			faultCode.setText(data.getCode());
		}else {
			faultCode.setText(data.getSubcode());
		}
		
		SOAPFaultReason faultReason = fault.getReason();
		
		OMNamespace namespace = factory.createOMNamespace(
				OMConstants.XMLNS_URI, OMConstants.XMLNS_PREFIX);
		faultReason.getSOAPText().addAttribute("lang", "en", namespace);
		
		faultReason.setText(data.getReason());
		faultCode.getValue().setText(data.getSubcode());
		faultReason.getSOAPText().setText(data.getReason());
		
		//SequenceFault header is added only for SOAP 1.1
		if (isSequenceFault(data))
			addSequenceFaultHeader (faultMsgContext,data,factory);
		
	}

	private static void doSOAP12Encoding(MessageContext faultMsgContext, FaultData data) throws SandeshaException {
		//MessageContext faultMsgCtx = faultRMMsgContext.getMessageContext();
		
		SOAPEnvelope faultEnvelope = faultMsgContext.getEnvelope();
		if (faultEnvelope==null)
			throw new SandeshaException ("SOAP Envelope is null");
		
		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion (faultEnvelope));
		
		SOAPFault fault = faultEnvelope.getBody().getFault();
		if (fault==null)
			throw new SandeshaException ("Fault part is null");
		
		SOAPFaultCode faultCode = fault.getCode();
		SOAPFaultValue codeValue = faultCode.getValue();
		codeValue.setText(data.getCode());

		SOAPFaultSubCode faultSubCode = factory
				.createSOAPFaultSubCode(faultCode);
		SOAPFaultValue subCodeValue = factory
				.createSOAPFaultValue(faultSubCode);
		subCodeValue.setText(data.getSubcode());

		SOAPFaultReason faultReason = fault.getReason();
		SOAPFaultText faultText = faultReason.getSOAPText();
		faultText.setText(data.getReason());

		SOAPFaultDetail faultDetail = fault.getDetail();

		OMElement detailElement = data.getDetail();
		
		if (detailElement!=null)
			faultDetail.addChild(detailElement);
		
		faultMsgContext.setWSAAction(Constants.WSA.SOAP_FAULT_ACTION);
		
//		if (detailElement!=null) {
//			String subcode = data.getSubcode();
//			
//			if (Constants.SOAPFaults.Subcodes.SequenceTerminated.equals(subcode)) {
//				
//			}else if (Constants.SOAPFaults.Subcodes.UnknownSequence.equals(subcode)) {
//				
//			}else if (Constants.SOAPFaults.Subcodes.InvalidAcknowledgement.equals(subcode)) {
//				
//			}else if (Constants.SOAPFaults.Subcodes.MessageNumberRoleover.equals(subcode)) {
//				
//			}else if (Constants.SOAPFaults.Subcodes.LastMessageNumberExceeded.equals(subcode)) {
//				
//			}else if (Constants.SOAPFaults.Subcodes.CreateSequenceRefused.equals(subcode)) {
//				
//			}
		
//		switch (data.type) {
//		case Constants.MessageTypes.APPLICATION:
//
//			/* Sequence */
//			OMElement sequence = ienvelope.getHeader()
//					.getFirstChildWithName(
//							new QName(Constants.WSRM.NS_URI_RM,
//									Constants.WSRM.SEQUENCE));
//			OMElement sidentifier = sequence.getFirstChildWithName(new QName(
//					Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));
//
//			OMNamespace snamespace = factory.createOMNamespace(
//					Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
//			OMElement selement = factory.createOMElement(
//					Constants.WSRM.IDENTIFIER, snamespace);
//
//			selement.setText(sidentifier.getText());
//			faultDetail.addChild(selement);
//			break;
//
//		case Constants.MessageTypes.ACK:
//
//			/* SequenceAcknowledge */
//
//			OMElement sequenceAck = ienvelope.getHeader()
//					.getFirstChildWithName(
//							new QName(Constants.WSRM.NS_URI_RM,
//									Constants.WSRM.SEQUENCE_ACK));
//			OMElement aidentifier = sequenceAck
//					.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
//							Constants.WSRM.IDENTIFIER));
//			OMNamespace anamespace = factory.createOMNamespace(
//					Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
//			OMElement aelement = factory.createOMElement(
//					Constants.WSRM.IDENTIFIER, anamespace);
//
//			aelement.setText(aidentifier.getText());
//			faultDetail.addChild(aelement);
//			break;
//
//		default:
//
//			/* TODO for other message types */
//			break;
//
//		}
//		}

		

//		try {
//			msgCtx.setEnvelope(envelope);
//		} catch (AxisFault e) {
//			throw new SandeshaException(e.getMessage());
//		}
	}
	
}
