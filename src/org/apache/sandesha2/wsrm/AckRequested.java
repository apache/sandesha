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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 *
 */
public class AckRequested implements IOMRMElement {
	private OMElement ackRequestedElement;
	private Identifier identifier;
	private MessageNumber messageNumber;
	OMNamespace ackReqNoNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public AckRequested(){
		ackRequestedElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.ACK_REQUESTED,ackReqNoNamespace);
	}
	public OMElement getSOAPElement() throws OMException {
		ackRequestedElement.addChild(identifier.getSOAPElement());
		ackRequestedElement.addChild(messageNumber.getSOAPElement());
		return ackRequestedElement;
	}

	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		identifier = new Identifier();
		messageNumber = new MessageNumber();
		identifier.fromSOAPEnvelope(envelope);
		messageNumber.fromSOAPEnvelope(envelope);
		return this;
	}

	public OMElement toSOAPEnvelope(OMElement envelope) throws OMException {
		SOAPEnvelope soapEnvelope = (SOAPEnvelope)envelope;
		SOAPHeader soapHeader = soapEnvelope.getHeader();
		SOAPHeaderBlock soapHeaderBlock = soapHeader.addHeaderBlock(
				Constants.WSRM.ACK_REQUESTED,ackReqNoNamespace);
		soapHeaderBlock.setMustUnderstand(true);
		if( identifier != null){
			identifier.toSOAPEnvelope(soapHeaderBlock);
		}
		if ( messageNumber != null){
			messageNumber.toSOAPEnvelope(soapHeaderBlock);
		}
		
		return envelope;
	}
	public void setIdentifier(Identifier identifier){
		this.identifier = identifier;
	}
	public void setMessageNumber(MessageNumber messageNumber){
		this.messageNumber = messageNumber;
	}
	public Identifier getIdentifier(){
		return identifier;
	}
	public MessageNumber getMessageNumber(){
		return messageNumber;
	}
	public void addChildElement(OMElement element){
		ackRequestedElement.addChild(element);
	}

}
