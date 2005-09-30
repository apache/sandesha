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
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class AckRequested implements IOMRMPart {
	private OMElement ackRequestedElement;

	private Identifier identifier;

	private MessageNumber messageNumber;

	OMNamespace rmNamespace = SOAPAbstractFactory.getSOAPFactory(
			Constants.SOAPVersion.DEFAULT).createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public AckRequested() {
		ackRequestedElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.ACK_REQUESTED, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return ackRequestedElement;
	}

	public Object fromOMElement(OMElement header) throws OMException {

		if (header == null || !(header instanceof SOAPHeader))
			throw new OMException(
					"Cant add the Ack Requested part to a non-header element");

		OMElement ackReqPart = header.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.ACK_REQUESTED));

		if (ackReqPart == null)
			throw new OMException(
					"the passed element does not contain an ack requested part");

		identifier = new Identifier();
		identifier.fromOMElement(ackReqPart);

		OMElement msgNoPart = ackReqPart.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.MSG_NUMBER));

		if (msgNoPart != null) {
			messageNumber = new MessageNumber();
			messageNumber.fromOMElement(ackReqPart);
		}

		ackRequestedElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.ACK_REQUESTED, rmNamespace);

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
				Constants.WSRM.ACK_REQUESTED, rmNamespace);
		ackReqHdrBlock.setMustUnderstand(true);

		identifier.toOMElement(ackReqHdrBlock);

		if (messageNumber != null) {
			messageNumber.toOMElement(ackReqHdrBlock);
		}

		ackRequestedElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.ACK_REQUESTED, rmNamespace);

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
		toOMElement(header);
	}

}