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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class Sequence implements IOMRMPart {

	private OMElement sequenceElement;

	private Identifier identifier;

	private MessageNumber messageNumber;

	private LastMessage lastMessage = null;

	OMNamespace seqNoNamespace = SOAPAbstractFactory.getSOAPFactory(
			Constants.SOAPVersion.DEFAULT).createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public Sequence() {
		sequenceElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.SEQUENCE, seqNoNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return sequenceElement;
	}

	public Object fromOMElement(OMElement headerElement) throws OMException {

		SOAPHeader header = (SOAPHeader) headerElement;
		if (header == null)
			throw new OMException(
					"Sequence element cannot be added to non-header element");

		OMElement sequencePart = sequenceElement = headerElement
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.SEQUENCE));
		if (sequencePart == null)
			throw new OMException(
					"Cannot find Sequence element in the given element");

		sequenceElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.SEQUENCE, seqNoNamespace);

		identifier = new Identifier();
		messageNumber = new MessageNumber();
		identifier.fromOMElement(sequencePart);
		messageNumber.fromOMElement(sequencePart);

		OMElement lastMessageElement = sequencePart
				.getFirstChildWithName(new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.LAST_MSG));

		if (lastMessageElement != null) {
			lastMessage = new LastMessage();
			lastMessage.fromOMElement(sequencePart);
		}

		return this;
	}

	public OMElement toOMElement(OMElement headerElement) throws OMException {

		if (headerElement == null || !(headerElement instanceof SOAPHeader))
			throw new OMException(
					"Cant add Sequence Part to a non-header element");

		SOAPHeader soapHeader = (SOAPHeader) headerElement;
		if (soapHeader == null)
			throw new OMException(
					"cant add the sequence part to a non-header element");
		if (sequenceElement == null)
			throw new OMException(
					"cant add Sequence Part since Sequence is null");
		if (identifier == null)
			throw new OMException(
					"Cant add Sequence part since identifier is null");
		if (messageNumber == null)
			throw new OMException(
					"Cant add Sequence part since MessageNumber is null");

		identifier.toOMElement(sequenceElement);
		messageNumber.toOMElement(sequenceElement);
		if (lastMessage != null)
			lastMessage.toOMElement(sequenceElement);

		SOAPHeaderBlock soapHeaderBlock = soapHeader.addHeaderBlock(
				Constants.WSRM.SEQUENCE, seqNoNamespace);
		soapHeaderBlock.setMustUnderstand(true);
		soapHeaderBlock.addChild(sequenceElement);

		//resetting the element. So that subsequest toOMElement calls will
		// attach a different object.
		sequenceElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.SEQUENCE, seqNoNamespace);

		return headerElement;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public LastMessage getLastMessage() {
		return lastMessage;
	}

	public MessageNumber getMessageNumber() {
		return messageNumber;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public void setLastMessage(LastMessage lastMessage) {
		this.lastMessage = lastMessage;
	}

	public void setMessageNumber(MessageNumber messageNumber) {
		this.messageNumber = messageNumber;
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) {
		SOAPHeader header = envelope.getHeader();
		toOMElement(header);
	}

}