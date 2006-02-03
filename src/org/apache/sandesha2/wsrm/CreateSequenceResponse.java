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

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAPBody;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;

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

	OMNamespace createSeqResNoNamespace = null;

	public CreateSequenceResponse(SOAPFactory factory) {
		this.factory = factory;
		createSeqResNoNamespace = factory.createOMNamespace(
				Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.NS_PREFIX_RM);
		createSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM.CREATE_SEQUENCE_RESPONSE,
				createSeqResNoNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return createSequenceResponseElement;
	}

	public Object fromOMElement(OMElement bodyElement) throws OMException {

		if (bodyElement == null || !(bodyElement instanceof SOAPBody))
			throw new OMException(
					"Cant get create sequnce response from a non-body element");

		SOAPBody SOAPBody = (SOAPBody) bodyElement;

		OMElement createSeqResponsePart = SOAPBody
				.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
						Sandesha2Constants.WSRM.CREATE_SEQUENCE_RESPONSE));
		if (createSeqResponsePart == null)
			throw new OMException(
					"The passed element does not contain a create seqence response part");

		createSequenceResponseElement = factory.createOMElement(
				Sandesha2Constants.WSRM.CREATE_SEQUENCE_RESPONSE,
				createSeqResNoNamespace);

		identifier = new Identifier(factory);
		identifier.fromOMElement(createSeqResponsePart);

		OMElement expiresPart = createSeqResponsePart
				.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
						Sandesha2Constants.WSRM.EXPIRES));
		if (expiresPart != null) {
			expires = new Expires(factory);
			expires.fromOMElement(createSeqResponsePart);
		}

		OMElement acceptPart = createSeqResponsePart
				.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
						Sandesha2Constants.WSRM.ACCEPT));
		if (acceptPart != null) {
			accept = new Accept(factory);
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
				Sandesha2Constants.WSRM.CREATE_SEQUENCE_RESPONSE,
				createSeqResNoNamespace);

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
		OMElement elem = body.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
				Sandesha2Constants.WSRM.CREATE_SEQUENCE_RESPONSE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(body);
	}
}