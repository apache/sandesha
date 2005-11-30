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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;

/**
 * Adds the Terminate Sequence body part.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class TerminateSequence implements IOMRMPart {

	private OMElement terminateSequenceElement;

	private Identifier identifier;

	OMNamespace rmNameSpace = null;
	
	SOAPFactory factory;

	public TerminateSequence(SOAPFactory factory) {
		this.factory = factory;
		rmNameSpace = factory.createOMNamespace(
				Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.NS_PREFIX_RM);
		terminateSequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM.TERMINATE_SEQUENCE, rmNameSpace);
	}

	public OMElement getOMElement() throws OMException {
		return terminateSequenceElement;
	}

	public Object fromOMElement(OMElement body) throws OMException {

		if (!(body instanceof SOAPBody))
			throw new OMException(
					"Cant add terminate sequence to a non body element");

		OMElement terminateSeqPart = body.getFirstChildWithName(new QName(
				Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.TERMINATE_SEQUENCE));

		if (terminateSeqPart == null)
			throw new OMException(
					"passed element does not contain a terminate sequence part");

		identifier = new Identifier(factory);
		identifier.fromOMElement(terminateSeqPart);

		terminateSequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM.TERMINATE_SEQUENCE, rmNameSpace);

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

		terminateSequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM.TERMINATE_SEQUENCE, rmNameSpace);

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
		
		//detach if already exist.
		OMElement elem = body.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
				Sandesha2Constants.WSRM.TERMINATE_SEQUENCE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(body);
	}
}