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
import java.util.LinkedList;
import java.util.List;

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
public class SequenceAcknowledgement implements IOMRMElement {
	private OMElement sequenceAcknowledgementElement;
	private Identifier identifier;
	private List acknowledgementRanges;
	private List nackList;
	OMNamespace seqAcksNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public SequenceAcknowledgement(){
		sequenceAcknowledgementElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.SEQUENCE_ACK,seqAcksNamespace);
		acknowledgementRanges = new LinkedList();
		nackList = new LinkedList();
	}
	public OMElement getSOAPElement() throws OMException {
		Iterator iterator = acknowledgementRanges.iterator();
		while(iterator.hasNext()){
			AcknowledgementRange range = (AcknowledgementRange)iterator.next();
			sequenceAcknowledgementElement.addChild(
					range.getSOAPElement());
			
		}
		iterator = nackList.iterator();
		while(iterator.hasNext()){
			Nack nack = (Nack)iterator.next();
			sequenceAcknowledgementElement.addChild(
					nack.getSOAPElement());
		}
		sequenceAcknowledgementElement.addChild(identifier.getSOAPElement());
		return sequenceAcknowledgementElement;
	}

	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		identifier = new Identifier();
		SOAPHeader soapHeader = envelope.getHeader();
		Iterator iterator = soapHeader.getChildren();
		while (iterator.hasNext()){
			OMElement omElement = (OMElement)iterator.next();
			if (omElement.getLocalName().equals(Constants.WSRM.SEQUENCE_ACK)){
				Iterator childIterator = omElement.getChildren();
				while (childIterator.hasNext()){
					OMElement childElement = (OMElement)childIterator.next();
					if (childElement.getLocalName().equals(Constants.WSRM.ACK_RANGE)){
						AcknowledgementRange ackRange = new AcknowledgementRange();
						ackRange.fromSOAPEnvelope(childElement);
						acknowledgementRanges.add(ackRange);
					}
					if (childElement.getLocalName().equals(Constants.WSRM.NACK)){
						Nack nack = new Nack();	
						nack.fromSOAPEnvelope(childElement);
						nackList.add(nack);
					}
					if ( childElement.getLocalName().equals(Constants.WSRM.IDENTIFIER)){
						identifier = new Identifier();
						identifier.fromSOAPEnvelope(envelope);
					}
				}
			}
		}
		
		return this;
	}

	public OMElement toSOAPEnvelope(OMElement envelope) throws OMException {
		SOAPEnvelope soapEnvelope = (SOAPEnvelope)envelope;
		SOAPHeader soapHeader = soapEnvelope.getHeader();
		SOAPHeaderBlock soapHeaderBlock = soapHeader.addHeaderBlock(
				Constants.WSRM.SEQUENCE_ACK,seqAcksNamespace);
		soapHeaderBlock.setMustUnderstand(true);
		//adding ackRanges
		Iterator iterator = acknowledgementRanges.iterator();
		while(iterator.hasNext()){
			AcknowledgementRange ackRange = (AcknowledgementRange)iterator.next();
			ackRange.toSOAPEnvelope(soapHeaderBlock);
		}
		iterator = nackList.iterator();
		while(iterator.hasNext()){
			Nack nack = (Nack)iterator.next();
			nack.toSOAPEnvelope(soapHeaderBlock);
		}
		if ( identifier != null){
			identifier.toSOAPEnvelope(soapHeaderBlock);
		}
		return envelope;
	}
	public void setIdentifier(Identifier identifier){
		this.identifier = identifier;
	}
	public void setAckRanges(List acknowledgementRagngesList){
		acknowledgementRanges = acknowledgementRagngesList;
	}
	public Nack addNackRangges(Nack nack){
		nackList.add(nack);
		return nack;
	}
	public AcknowledgementRange addAcknowledgementRanges(AcknowledgementRange ackRange){
		acknowledgementRanges.add(ackRange);
		return ackRange;
	}
	public Identifier getIdentifier(){
		return identifier;
	}
	public List getAcknowledgementRanges(){
		return acknowledgementRanges;
	}
	public List getNackList(){
		return nackList;
	}
	public void addChildElement(OMElement element){
		acknowledgementRanges.add(element);
	}

}
