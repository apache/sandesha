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

import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

/**
 * Adds the Close Sequence Response body part.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */
public class CloseSequenceResponse implements IOMRMPart {

	private OMElement closeSequenceResponseElement;
	private Identifier identifier;
	OMNamespace rmNameSpace = null;
	SOAPFactory factory;
	String namespaceValue = null;
	
	public CloseSequenceResponse(SOAPFactory factory, String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNameSpace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		closeSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE_RESPONSE, rmNameSpace);
	}

	public OMElement getOMElement() throws OMException {
		return closeSequenceResponseElement;
	}

	public Object fromOMElement(OMElement body) throws OMException,SandeshaException {

		if (!(body instanceof SOAPBody))
			throw new OMException(
					"Cant add 'close sequence response' to a non body element");

		OMElement closeSeqResponsePart = body.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE_RESPONSE));

		if (closeSeqResponsePart == null)
			throw new OMException(
					"passed element does not contain a 'close sequence response' part");

		identifier = new Identifier(factory,namespaceValue);
		identifier.fromOMElement(closeSeqResponsePart);

		closeSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE_RESPONSE, rmNameSpace);

		return this;
	}

	public OMElement toOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant add close sequence response to a nonbody element");

		if (closeSequenceResponseElement == null)
			throw new OMException(
					"Cant add close sequnce response since the internal element is null");

		if (identifier == null)
			throw new OMException(
					"Cant add close sequence response since identifier is not set");

		identifier.toOMElement(closeSequenceResponseElement);
		body.addChild(closeSequenceResponseElement);

		closeSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE_RESPONSE, rmNameSpace);

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
				Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE_RESPONSE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(body);
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}


}
