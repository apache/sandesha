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

package org.apache.sandesha2.wsrm;

import javax.xml.namespace.QName;

import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

/**
 * Adds the Terminate Sequence Response body part.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class TerminateSequenceResponse implements IOMRMPart {

	private OMElement terminateSequenceResponseElement;
	private Identifier identifier;
	OMNamespace rmNameSpace = null;
	SOAPFactory factory;
	String namespaceValue = null;
	
	
	public TerminateSequenceResponse(SOAPFactory factory, String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNameSpace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		terminateSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE_RESPONSE, rmNameSpace);
	}
	
	public OMElement getOMElement() throws OMException {
		return terminateSequenceResponseElement;
	}

	public Object fromOMElement(OMElement body) throws OMException,SandeshaException {

		if (!(body instanceof SOAPBody))
			throw new OMException(
					"Cant add terminate sequence response to a non body element");

		OMElement terminateSeqResponsePart = body.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE_RESPONSE));

		if (terminateSeqResponsePart == null)
			throw new OMException(
					"passed element does not contain a terminate sequence response part");

		identifier = new Identifier(factory,namespaceValue);
		identifier.fromOMElement(terminateSeqResponsePart);

		terminateSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE_RESPONSE, rmNameSpace);

		return this;
	}

	public OMElement toOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant add terminate sequence response to a nonbody element");

		if (terminateSequenceResponseElement == null)
			throw new OMException(
					"Cant add terminate sequnce response since the internal element is null");

		if (identifier == null)
			throw new OMException(
					"Cant add terminate sequence response since identifier is not set");

		identifier.toOMElement(terminateSequenceResponseElement);
		body.addChild(terminateSequenceResponseElement);

		terminateSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE_RESPONSE, rmNameSpace);

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
		OMElement elem = body.getFirstChildWithName(new QName(namespaceValue,
				Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE_RESPONSE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(body);
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
	
	
}
