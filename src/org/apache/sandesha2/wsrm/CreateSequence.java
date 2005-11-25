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
import org.apache.sandesha2.Constants;

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

	//private SequritytokenReference;

	OMNamespace rmNamespace = null;

	public CreateSequence(SOAPFactory factory) {
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
		createSequenceElement = factory.createOMElement(
				Constants.WSRM.CREATE_SEQUENCE, rmNamespace);
	}
	
	public CreateSequence (AcksTo acksTo,SOAPFactory factory) {
		this (factory);
		this.acksTo = acksTo;
	}

	public OMElement getOMElement() throws OMException {
		return createSequenceElement;
	}

	public Object fromOMElement(OMElement bodyElement) throws OMException {

		OMElement createSequencePart = bodyElement
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.CREATE_SEQUENCE));
		if (createSequencePart == null)
			throw new OMException(
					"Create sequence is not present in the passed element");

		createSequenceElement = factory.createOMElement(
				Constants.WSRM.CREATE_SEQUENCE, rmNamespace);

		acksTo = new AcksTo(factory);
		acksTo.fromOMElement(createSequencePart);

		OMElement offerPart = createSequencePart
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.SEQUENCE_OFFER));
		if (offerPart != null) {
			sequenceOffer = new SequenceOffer(factory);
			sequenceOffer.fromOMElement(createSequencePart);
		}

		OMElement expiresPart = createSequenceElement
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.EXPIRES));
		if (expiresPart != null) {
			expires = new Expires(factory);
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
				Constants.WSRM.CREATE_SEQUENCE, rmNamespace);
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
		OMElement elem = body.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
				Constants.WSRM.CREATE_SEQUENCE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(body);
	}
}