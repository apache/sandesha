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
 * Represent the CreateSequence body element.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class CreateSequence implements IOMRMPart {
	
	private OMElement createSequenceElement;
	private AcksTo acksTo = null;
	private Expires expires = null;
	private SequenceOffer sequenceOffer = null;
	private SOAPFactory factory;
	OMNamespace rmNamespace = null;
	String namespaceValue = null;

	public CreateSequence(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNamespace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		createSequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE, rmNamespace);
	}
	
	public CreateSequence (AcksTo acksTo,SOAPFactory factory,String namespaceValue) throws SandeshaException {
		this (factory,namespaceValue);
		this.acksTo = acksTo;
	}

	public OMElement getOMElement() throws OMException {
		return createSequenceElement;
	}

	public Object fromOMElement(OMElement bodyElement) throws OMException,SandeshaException {

		OMElement createSequencePart = bodyElement
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE));
		if (createSequencePart == null)
			throw new OMException(
					"Create sequence is not present in the passed element");

		createSequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE, rmNamespace);

		acksTo = new AcksTo(factory,namespaceValue);
		acksTo.fromOMElement(createSequencePart);

		OMElement offerPart = createSequencePart
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.SEQUENCE_OFFER));
		if (offerPart != null) {
			sequenceOffer = new SequenceOffer(factory,namespaceValue);
			sequenceOffer.fromOMElement(createSequencePart);
		}

		OMElement expiresPart = createSequenceElement
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.EXPIRES));
		if (expiresPart != null) {
			expires = new Expires(factory,namespaceValue);
			expires.fromOMElement(createSequencePart);
		}

		return this;
	}

	public OMElement toOMElement(OMElement bodyElement) throws OMException {

		if (bodyElement == null || !(bodyElement instanceof SOAPBody))
			throw new OMException(
					"Cant add Create Sequence Part to a non-body element");

		if (acksTo == null)
			throw new OMException(
					"Cant add create seqeunce part, having acks to as null");

		SOAPBody soapBody = (SOAPBody) bodyElement;
		acksTo.toOMElement(createSequenceElement);

		if (sequenceOffer != null) {
			sequenceOffer.toOMElement(createSequenceElement);
		}

		if (expires != null) {
			expires.toOMElement(createSequenceElement);
		}

		soapBody.addChild(createSequenceElement);

		createSequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE, rmNamespace);
		return soapBody;
	}

	public void setAcksTo(AcksTo acksTo) {
		this.acksTo = acksTo;
	}

	public void setSequenceOffer(SequenceOffer sequenceOffer) {
		this.sequenceOffer = sequenceOffer;
	}

	public AcksTo getAcksTo() {
		return acksTo;
	}

	public SequenceOffer getSequenceOffer() {
		return sequenceOffer;
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) {
		SOAPBody body = envelope.getBody();
		
		//detach if already exist.
		OMElement elem = body.getFirstChildWithName(new QName(namespaceValue,
				Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE));
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