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

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class TerminateSequence implements IOMRMPart {

	private OMElement terminateSequenceElement;

	private Identifier identifier;

	OMNamespace rmNameSpace = SOAPAbstractFactory.getSOAPFactory(
			Constants.DEFAULT_SOAP_VERSION).createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public TerminateSequence() {
		terminateSequenceElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.TERMINATE_SEQUENCE, rmNameSpace);
	}

	public OMElement getOMElement() throws OMException {
		return terminateSequenceElement;
	}

	public Object fromOMElement(OMElement body) throws OMException {

		if (!(body instanceof SOAPBody))
			throw new OMException(
					"Cant add terminate sequence to a non body element");

		OMElement terminateSeqPart = body.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.TERMINATE_SEQUENCE));

		if (terminateSeqPart == null)
			throw new OMException(
					"passed element does not contain a terminate sequence part");

		identifier = new Identifier();
		identifier.fromOMElement(terminateSeqPart);

		terminateSequenceElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.TERMINATE_SEQUENCE, rmNameSpace);

		return this;
	}

	public OMElement toOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant add terminate sequence to a nonbody element");

		if (terminateSequenceElement == null)
			throw new OMException(
					"Cant add terminate sequnce since the internal element is null");

		if (identifier == null)
			throw new OMException(
					"Cant add terminate sequence since identifier is not set");

		identifier.toOMElement(terminateSequenceElement);
		body.addChild(terminateSequenceElement);

		terminateSequenceElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.TERMINATE_SEQUENCE, rmNameSpace);

		return body;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) {
		SOAPBody body = envelope.getBody();
		toOMElement(body);
	}
}