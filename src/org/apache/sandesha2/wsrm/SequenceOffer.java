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
import org.apache.axiom.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class SequenceOffer implements IOMRMElement {
	
	private OMElement sequenceOfferElement;
	private Identifier identifier = null;
	private Expires expires = null;
	SOAPFactory factory;
	OMNamespace rmNamespace = null; 
	String namespaceValue = null;

	public SequenceOffer(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNamespace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		sequenceOfferElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_OFFER, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return sequenceOfferElement;
	}

	public Object fromOMElement(OMElement createSequenceElement)
			throws OMException,SandeshaException {
		OMElement sequenceOfferPart = createSequenceElement
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.SEQUENCE_OFFER));
		if (sequenceOfferPart == null)
			throw new OMException(
					"The passed element does not contain a SequenceOffer part");

		sequenceOfferElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_OFFER, rmNamespace);

		identifier = new Identifier(factory,namespaceValue);
		identifier.fromOMElement(sequenceOfferPart);

		OMElement expiresPart = sequenceOfferPart
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.EXPIRES));
		if (expiresPart != null) {
			expires = new Expires(factory,namespaceValue);
			expires.fromOMElement(sequenceOfferElement);
		}

		return this;
	}

	public OMElement toOMElement(OMElement createSequenceElement)
			throws OMException {
		if (sequenceOfferElement == null)
			throw new OMException(
					"Cant set sequnceoffer. Offer element is null");
		if (identifier == null)
			throw new OMException(
					"Cant set sequnceOffer since identifier is null");

		identifier.toOMElement(sequenceOfferElement);

		if (expires != null) {
			expires.toOMElement(sequenceOfferElement);
		}

		createSequenceElement.addChild(sequenceOfferElement);

		sequenceOfferElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_OFFER, rmNamespace);

		return createSequenceElement;
	}

	public Identifier getIdentifer() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}

}