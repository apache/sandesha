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
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class SequenceOffer implements IOMRMElement {
	private OMElement sequenceOfferElement;

	private Identifier identifier = null;

	private Expires expires = null;

	OMNamespace rmNamespace = SOAPAbstractFactory.getSOAPFactory(
			Constants.SOAPVersion.DEFAULT).createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public SequenceOffer() {
		sequenceOfferElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.SEQUENCE_OFFER, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return sequenceOfferElement;
	}

	public Object fromOMElement(OMElement createSequenceElement)
			throws OMException {
		OMElement sequenceOfferPart = createSequenceElement
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.SEQUENCE_OFFER));
		if (sequenceOfferPart == null)
			throw new OMException(
					"The passed element does not contain a SequenceOffer part");

		sequenceOfferElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.SEQUENCE_OFFER, rmNamespace);

		identifier = new Identifier();
		identifier.fromOMElement(sequenceOfferPart);

		OMElement expiresPart = sequenceOfferPart
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.EXPIRES));
		if (expiresPart != null) {
			expires = new Expires();
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

		sequenceOfferElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.SEQUENCE_OFFER, rmNamespace);

		return createSequenceElement;
	}

	public Identifier getIdentifer() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

}