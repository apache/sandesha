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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.sandesha2.FaultData;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.Sandesha2Constants.SOAPFaults;
import org.apache.sandesha2.Sandesha2Constants.SOAPVersion;
import org.apache.sandesha2.Sandesha2Constants.SOAPFaults.FaultType;
import org.apache.sandesha2.wsrm.FaultCode;
import org.apache.sandesha2.wsrm.SequenceFault;

/**
 * Used to create an SOAP Envelope for a RM Related Fault.
 * Support both SOAP 1.1 and SOAP 1.2 encoding.
 * 
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SOAPFaultEnvelopeCreator {

	/**
	 * Adding the SOAP Fault Envelope. 
	 * 
	 * @param faultMsgContext
	 * @param SOAPVersion
	 * @param faultData
	 * @throws SandeshaException
	 */
	public static void addSOAPFaultEnvelope(MessageContext faultMsgContext,
			int SOAPVersion, FaultData faultData, String rmNamespaceValue) throws SandeshaException {

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SOAPVersion);
		SOAPEnvelope env = factory.getDefaultFaultEnvelope();

		try {
			faultMsgContext.setEnvelope(env);
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		if (SOAPVersion == Sandesha2Constants.SOAPVersion.v1_1)
			doSOAP11Encoding(faultMsgContext, faultData, rmNamespaceValue);
		else
			doSOAP12Encoding(faultMsgContext, faultData, rmNamespaceValue);

	}

	/**
	 * To find out weather this is a sequence fault. These faults are handeled differently 
	 * accodting to the RM spec.
	 * 
	 * @param faultData
	 * @return
	 */
	private static boolean isSequenceFault(FaultData faultData) {

		boolean sequenceFault = false;

		int faultType = faultData.getType();

		if (faultType <= 0)
			return false;

		if (faultType == Sandesha2Constants.SOAPFaults.FaultType.CREATE_SEQUENCE_REFUSED)
			sequenceFault = true;

		if (faultType == Sandesha2Constants.SOAPFaults.FaultType.UNKNOWN_SEQUENCE)
			sequenceFault = true;

		return sequenceFault;

	}

	/**
	 * Adding the SequenceFault header. Only for Sequence faults.
	 * 
	 * @param faultMessageContext
	 * @param faultData
	 * @param factory
	 */
	private static void addSequenceFaultHeader(
			MessageContext faultMessageContext, FaultData faultData,
			SOAPFactory factory, String rmNamespaceValue) throws SandeshaException {

		SequenceFault sequenceFault = new SequenceFault(factory, rmNamespaceValue);

		FaultCode faultCode = new FaultCode(factory, rmNamespaceValue);
		faultCode.setFaultCode(faultData.getSubcode());
		sequenceFault.setFaultCode(faultCode);
	}

	/**
	 * Building the envelope with SOAP 1.1
	 * 
	 * @param faultMsgContext
	 * @param data
	 * @throws SandeshaException
	 */
	private static void doSOAP11Encoding(MessageContext faultMsgContext,
			FaultData data, String rmNamespaceValue) throws SandeshaException {

		SOAPEnvelope faultMsgEnvelope = faultMsgContext.getEnvelope();
		if (faultMsgEnvelope == null)
			throw new SandeshaException("SOAP Envelope is null");

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(faultMsgEnvelope));

		SOAPFault fault = faultMsgEnvelope.getBody().getFault();
		SOAPFaultCode faultCode = fault.getCode();

		if (isSequenceFault(data)) {
			faultCode.setText(data.getCode());
		} else {
			faultCode.setText(data.getSubcode());
		}

		SOAPFaultReason faultReason = fault.getReason();

		OMNamespace namespace = factory.createOMNamespace(
				OMConstants.XMLNS_URI, OMConstants.XMLNS_PREFIX);
		faultReason.getSOAPFaultText("en").addAttribute("lang", "en", namespace);

		faultReason.setText(data.getReason());
		faultCode.getValue().setText(data.getSubcode());
		faultReason.getSOAPFaultText("en").setText(data.getReason());

		//SequenceFault header is added only for SOAP 1.1
		if (isSequenceFault(data))
			addSequenceFaultHeader(faultMsgContext, data, factory, rmNamespaceValue);

	}

	
	/**
	 * Building the envelope with SOAP 1.2
	 * 
	 * @param faultMsgContext
	 * @param data
	 * @throws SandeshaException
	 */
	private static void doSOAP12Encoding(MessageContext faultMsgContext,
			FaultData data, String rmNamespaceValue) throws SandeshaException {

		SOAPEnvelope faultEnvelope = faultMsgContext.getEnvelope();
		if (faultEnvelope == null)
			throw new SandeshaException("SOAP Envelope is null");

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(faultEnvelope));

		SOAPFault fault = faultEnvelope.getBody().getFault();
		if (fault == null)
			throw new SandeshaException("Fault part is null");

		SOAPFaultCode faultCode = fault.getCode();
		SOAPFaultValue codeValue = faultCode.getValue();
		codeValue.setText(data.getCode());

		SOAPFaultSubCode faultSubCode = factory
				.createSOAPFaultSubCode(faultCode);
		SOAPFaultValue subCodeValue = factory
				.createSOAPFaultValue(faultSubCode);
		subCodeValue.setText(data.getSubcode());

		SOAPFaultReason faultReason = fault.getReason();
		SOAPFaultText faultText = faultReason.getSOAPFaultText("en");
		faultText.setText(data.getReason());

		SOAPFaultDetail faultDetail = fault.getDetail();

		OMElement detailElement = data.getDetail();

		if (detailElement != null)
			faultDetail.addChild(detailElement);

		
		faultMsgContext.setWSAAction(AddressingConstants.Final.WSA_FAULT_ACTION);
	}

}