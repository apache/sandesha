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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

/**
 * Adds the Close Sequence body part.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class CloseSequence implements IOMRMPart {

	private Identifier identifier;
	
	private OMFactory defaultFactory;
	
	private String namespaceValue = null;
	
	public CloseSequence(OMFactory factory, String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.defaultFactory = factory;
		this.namespaceValue = namespaceValue;
	}

	public String getNamespaceValue() {
		return namespaceValue;
	}

	public Object fromOMElement(OMElement body) throws OMException,SandeshaException {

		if (!(body instanceof SOAPBody))
			throw new OMException("Cant extract 'close sequence' from a non body element");

		OMElement closeSeqPart = body.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE));

		if (closeSeqPart == null)
			throw new OMException("passed element does not contain a close sequence part");

		identifier = new Identifier(defaultFactory,namespaceValue);
		identifier.fromOMElement(closeSeqPart);

		return this;
	}

	public OMElement toOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant add close sequence to a nonbody element");

		if (identifier == null)
			throw new OMException("Cant add close sequence since identifier is not set");

		OMFactory factory = body.getOMFactory();
		if (factory==null)
			factory = defaultFactory;
		
		OMNamespace rmNamespace = factory.createOMNamespace(namespaceValue,Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		OMElement closeSequenceElement = factory.createOMElement(Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE, rmNamespace);
		
		identifier.toOMElement(closeSequenceElement);
		body.addChild(closeSequenceElement);

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
				Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE));
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
