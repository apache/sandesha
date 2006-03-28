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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class Sequence implements IOMRMPart {

	private OMElement sequenceElement;
	private Identifier identifier;
	private MessageNumber messageNumber;
	private LastMessage lastMessage = null;
	private SOAPFactory factory;
	OMNamespace seqNoNamespace = null;
	private boolean mustUnderstand = true;
	String namespaceValue = null;
	
	public Sequence(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		seqNoNamespace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		sequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE, seqNoNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return sequenceElement;
	}

	public Object fromOMElement(OMElement headerElement) throws OMException,SandeshaException {

		SOAPHeader header = (SOAPHeader) headerElement;
		if (header == null)
			throw new OMException(
					"Sequence element cannot be added to non-header element");

		OMElement sequencePart = sequenceElement = headerElement
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.SEQUENCE));
		if (sequencePart == null)
			throw new OMException(
					"Cannot find Sequence element in the given element");

		sequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE, seqNoNamespace);

		identifier = new Identifier(factory,namespaceValue);
		messageNumber = new MessageNumber(factory,namespaceValue);
		identifier.fromOMElement(sequencePart);
		messageNumber.fromOMElement(sequencePart);

		OMElement lastMessageElement = sequencePart
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.LAST_MSG));

		if (lastMessageElement != null) {
			lastMessage = new LastMessage(factory,namespaceValue);
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


		SOAPHeaderBlock sequenceHeaderBlock = soapHeader.addHeaderBlock(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE, seqNoNamespace);
		//soapHeader.addChild(sequenceHeaderBlock);
		//OMElement elem1 = factory.createOMElement("test","http://test1","test2");
		//soapHeader.addChild(elem1)
		
		
		sequenceHeaderBlock.setMustUnderstand(isMustUnderstand());
		identifier.toOMElement(sequenceHeaderBlock);
		messageNumber.toOMElement(sequenceHeaderBlock);
		if (lastMessage != null)
			lastMessage.toOMElement(sequenceHeaderBlock);


		//resetting the element. So that subsequest toOMElement calls will
		// attach a different object.
		this.sequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE, seqNoNamespace);

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
		
		//detach if already exist.
		OMElement elem = header.getFirstChildWithName(new QName(namespaceValue,
				Sandesha2Constants.WSRM_COMMON.SEQUENCE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(header);
	}

	public boolean isMustUnderstand() {
		return mustUnderstand;
	}

	public void setMustUnderstand(boolean mustUnderstand) {
		this.mustUnderstand = mustUnderstand;
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}

}