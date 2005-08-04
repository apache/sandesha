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

public class Sequence implements IOMRMElement {

	private OMElement sequenceElement;
	private Identifier identifier;
	private MessageNumber messageNumber;
	private LastMessage lastMessage;
	
	OMNamespace seqNoNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public Sequence(){
		sequenceElement = OMAbstractFactory.getSOAP11Factory().createOMElement(Constants.WSRM.SEQUENCE,seqNoNamespace);
	}
	public OMElement getSOAPElement() throws OMException {
		sequenceElement.addChild(identifier.getSOAPElement());
		sequenceElement.addChild(messageNumber.getSOAPElement());
		sequenceElement.addChild(lastMessage.getSOAPElement());
		return sequenceElement;
	}

	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		identifier = new Identifier();
		lastMessage = new LastMessage();
		messageNumber = new MessageNumber();
		identifier.fromSOAPEnvelope(envelope);
		messageNumber.fromSOAPEnvelope(envelope);
		lastMessage.fromSOAPEnvelope(envelope);
		return this;
	}
	
	public OMElement toSOAPEnvelope(OMElement envelope) throws OMException {
		//soapelevement will be given here. 
		SOAPEnvelope soapEnvelope = (SOAPEnvelope)envelope;
		
		SOAPHeader soapHeader = soapEnvelope.getHeader();
		SOAPHeaderBlock soapHeaderBlock = soapHeader.addHeaderBlock(
				Constants.WSRM.SEQUENCE,seqNoNamespace);
		soapHeaderBlock.setMustUnderstand(true);
		
		if (lastMessage != null) {
            lastMessage.toSOAPEnvelope(soapHeaderBlock);
        }

        if (identifier != null) {
            identifier.toSOAPEnvelope(soapHeaderBlock);
        }

        if (messageNumber != null) {
            messageNumber.toSOAPEnvelope(soapHeaderBlock);
        }
		
		return envelope;
	}
	public void addChildElement(OMElement element) throws OMException{
		sequenceElement.addChild(element);
	}
	
	public Identifier getIdentifier(){
		return identifier;
	}
	public LastMessage getLastMessage(){
		return lastMessage;
	}
	public MessageNumber getMessageNumber(){
		return messageNumber;
	}
	public void setIdentifier(Identifier identifier){
		this.identifier = identifier;
	}
	public void setLastMessage(LastMessage lastMessage){
		this.lastMessage = lastMessage;
	}
	public void setMessageNumber(MessageNumber messageNumber){
		this.messageNumber = messageNumber;
	}
	

}
