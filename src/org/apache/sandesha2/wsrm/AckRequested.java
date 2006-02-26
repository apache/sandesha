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
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Sandesha2Constants;

/**
 * Represent the AckRequested header block.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class AckRequested implements IOMRMPart {
	
	private OMElement ackRequestedElement;
	private Identifier identifier;
	private MessageNumber messageNumber;
	private SOAPFactory factory;
	OMNamespace rmNamespace = null;
	String namespaceValue = null;

	public AckRequested(SOAPFactory factory,String namespaceValue) {
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNamespace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		ackRequestedElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return ackRequestedElement;
	}

	public Object fromOMElement(OMElement header) throws OMException {

		if (header == null || !(header instanceof SOAPHeader))
			throw new OMException(
					"Cant add the Ack Requested part to a non-header element");

		OMElement ackReqPart = header.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED));

		if (ackReqPart == null)
			throw new OMException(
					"the passed element does not contain an ack requested part");

		identifier = new Identifier(factory,namespaceValue);
		identifier.fromOMElement(ackReqPart);

		OMElement msgNoPart = ackReqPart.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.MSG_NUMBER));

		if (msgNoPart != null) {
			messageNumber = new MessageNumber(factory,namespaceValue);
			messageNumber.fromOMElement(ackReqPart);
		}

		ackRequestedElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED, rmNamespace);

		return this;
	}

	public OMElement toOMElement(OMElement header) throws OMException {

		if (header == null || !(header instanceof SOAPHeader))
			throw new OMException(
					"Cant add the Ack Requested part to a non-header element");

		if (identifier == null)
			throw new OMException(
					"Cant add ack Req block since the identifier is null");

		SOAPHeader SOAPHdr = (SOAPHeader) header;
		SOAPHeaderBlock ackReqHdrBlock = SOAPHdr.addHeaderBlock(
				Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED, rmNamespace);
		ackReqHdrBlock.setMustUnderstand(true);

		identifier.toOMElement(ackReqHdrBlock);

		if (messageNumber != null) {
			messageNumber.toOMElement(ackReqHdrBlock);
		}

		ackRequestedElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED, rmNamespace);

		return header;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public void setMessageNumber(MessageNumber messageNumber) {
		this.messageNumber = messageNumber;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public MessageNumber getMessageNumber() {
		return messageNumber;
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) {
		SOAPHeader header = envelope.getHeader();
		
		//detach if already exist.
		OMElement elem = header.getFirstChildWithName(new QName(namespaceValue,
				Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED));
		if (elem!=null)
			elem.detach();
		
		toOMElement(header);
	}

}