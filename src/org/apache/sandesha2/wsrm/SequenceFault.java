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
package org.apache.sandesha2.wsrm;

import java.util.Iterator;

import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class SequenceFault implements IOMRMElement {
	private OMElement sequenceFaultElement;

	private FaultCode faultCode;

	OMNamespace rmNamespace = SOAPAbstractFactory.getSOAPFactory(
			Constants.DEFAULT_SOAP_VERSION).createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public SequenceFault() {
		sequenceFaultElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.SEQUENCE_FAULT, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return sequenceFaultElement;
	}

	public Object fromOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant get Sequence Fault part from a non-header element");

		OMElement sequenceFaultPart = body.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE_FAULT));

		if (sequenceFaultPart == null)
			throw new OMException(
					"The passed element does not contain a Sequence Fault element");

		OMElement faultCodePart = sequenceFaultPart
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.FAULT_CODE));

		if (faultCodePart != null) {
			faultCode = new FaultCode();
			faultCode.fromOMElement(sequenceFaultPart);
		}

		sequenceFaultElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.SEQUENCE_FAULT, rmNamespace);

		return this;

	}

	public OMElement toOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant get Sequence Fault part from a non-header element");

		if (sequenceFaultElement == null)
			throw new OMException(
					"Cant add the sequnce fault since the internal element is null");

		if (faultCode != null)
			faultCode.toOMElement(sequenceFaultElement);

		body.addChild(sequenceFaultElement);

		sequenceFaultElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.SEQUENCE_FAULT, rmNamespace);

		return body;
	}

	public void setFaultCode(FaultCode faultCode) {
		this.faultCode = faultCode;
	}

	public FaultCode getFaultCode() {
		return faultCode;
	}

	public void setSequenceFaultElement(OMElement sequenceFault) {
		sequenceFaultElement = sequenceFault;
	}

	public OMElement getSequenceFaultElement() {
		return sequenceFaultElement;
	}

}