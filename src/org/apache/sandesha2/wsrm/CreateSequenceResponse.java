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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

/**
 * Adds the CreateSequenceResponse body part.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class CreateSequenceResponse implements IOMRMPart {
	
	private OMElement createSequenceResponseElement;
	private Identifier identifier;
	private Accept accept;
	private Expires expires;
	SOAPFactory factory;
	OMNamespace rmNamespace = null;
	OMNamespace addressingNamespace = null;

	public CreateSequenceResponse(SOAPFactory factory, String rmNamespaceValue, String addressingNamespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(rmNamespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(
				rmNamespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		addressingNamespace = factory.createOMNamespace(
				addressingNamespaceValue, Sandesha2Constants.WSA.NS_PREFIX_ADDRESSING);
		
		createSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE_RESPONSE,
				rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return createSequenceResponseElement;
	}

	public Object fromOMElement(OMElement bodyElement) throws OMException,SandeshaException {

		if (bodyElement == null || !(bodyElement instanceof SOAPBody))
			throw new OMException(
					"Cant get create sequnce response from a non-body element");

		SOAPBody SOAPBody = (SOAPBody) bodyElement;

		OMElement createSeqResponsePart = SOAPBody
				.getFirstChildWithName(new QName(rmNamespace.getName(),
						Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE_RESPONSE));
		if (createSeqResponsePart == null)
			throw new OMException(
					"The passed element does not contain a create seqence response part");

		createSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE_RESPONSE,
				rmNamespace);

		identifier = new Identifier(factory,rmNamespace.getName());
		identifier.fromOMElement(createSeqResponsePart);

		OMElement expiresPart = createSeqResponsePart
				.getFirstChildWithName(new QName(rmNamespace.getName(),
						Sandesha2Constants.WSRM_COMMON.EXPIRES));
		if (expiresPart != null) {
			expires = new Expires(factory,rmNamespace.getName());
			expires.fromOMElement(createSeqResponsePart);
		}

		OMElement acceptPart = createSeqResponsePart
				.getFirstChildWithName(new QName(rmNamespace.getName(),
						Sandesha2Constants.WSRM_COMMON.ACCEPT));
		if (acceptPart != null) {
			accept = new Accept(factory,rmNamespace.getName(),addressingNamespace.getName());
			accept.fromOMElement(createSeqResponsePart);
		}

		return this;
	}

	public OMElement toOMElement(OMElement bodyElement) throws OMException {

		if (bodyElement == null || !(bodyElement instanceof SOAPBody))
			throw new OMException(
					"Cant get create sequnce response from a non-body element");

		SOAPBody SOAPBody = (SOAPBody) bodyElement;

		if (createSequenceResponseElement == null)
			throw new OMException(
					"cant set create sequnce response since the internal element is not set");
		if (identifier == null)
			throw new OMException(
					"cant set create sequnce response since the Identifier is not set");

		identifier.toOMElement(createSequenceResponseElement);

		if (expires != null) {
			expires.toOMElement(createSequenceResponseElement);
		}

		if (accept != null) {
			accept.toOMElement(createSequenceResponseElement);
		}

		SOAPBody.addChild(createSequenceResponseElement);

		createSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE_RESPONSE,
				rmNamespace);

		return SOAPBody;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public void setAccept(Accept accept) {
		this.accept = accept;
	}

	public Accept getAccept() {
		return accept;
	}

	public Expires getExpires() {
		return expires;
	}

	public void setExpires(Expires expires) {
		this.expires = expires;
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) {
		SOAPBody body = envelope.getBody();
		
		//detach if already exist.
		OMElement elem = body.getFirstChildWithName(new QName(rmNamespace.getName(),
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE_RESPONSE));
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