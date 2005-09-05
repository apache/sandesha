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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
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

public class SequenceAcknowledgement implements IOMRMElement {
	private OMElement sequenceAcknowledgementElement;
	private Identifier identifier;
	private List acknowledgementRangeList;
	private List nackList;
	
	OMNamespace rmNamespace =
		SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	
	public SequenceAcknowledgement(){
		sequenceAcknowledgementElement = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.SEQUENCE_ACK,rmNamespace);
		acknowledgementRangeList = new LinkedList();
		nackList = new LinkedList();
	}
	
	public OMElement getOMElement() throws OMException {
		return sequenceAcknowledgementElement;
	}

	public Object fromOMElement(OMElement element) throws OMException {
		

		
		if (element==null || !(element instanceof SOAPHeader)) 
			throw new OMException ("Cant get sequence acknowlegement from a non-header element");
		
		SOAPHeader header = (SOAPHeader) element;
		OMElement sequenceAckPart = header.getFirstChildWithName(
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.SEQUENCE_ACK));
		
		if (sequenceAckPart==null)
			throw new OMException ("The passed element does not contain a seqence ackknowledgement Part");
		

		
		identifier = new Identifier ();
		identifier.fromOMElement(sequenceAckPart);
		

		Iterator ackRangeParts = sequenceAckPart.getChildrenWithName(
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.ACK_RANGE));
		
		while (ackRangeParts.hasNext()) {
			OMElement ackRangePart = (OMElement) ackRangeParts.next();	
			
			AcknowledgementRange ackRange = new AcknowledgementRange ();
			ackRange.fromOMElement (ackRangePart);
			acknowledgementRangeList.add(ackRange);
		}
		
		Iterator nackParts = sequenceAckPart.getChildrenWithName(
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.NACK));
		
		while (nackParts.hasNext()) {
			OMElement nackPart = (OMElement) nackParts.next();	
			Nack nack = new Nack ();
			nack.fromOMElement (nackPart);
			nackList.add(nack);
		}
		
		sequenceAcknowledgementElement = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.SEQUENCE_ACK,rmNamespace);
		
		return this;
	}

	public OMElement toOMElement(OMElement header) throws OMException {
		
		if (header==null || !(header instanceof SOAPHeader))
			throw new OMException ();
		
		SOAPHeader SOAPHeader = (SOAPHeader) header;
		
		if (sequenceAcknowledgementElement==null)
			throw new OMException ("Cant set sequence acknowledgement since the element is null");
		
		if (identifier==null)
			throw new OMException ("Cant set the sequence since Identifier is null");
		
		identifier.toOMElement(sequenceAcknowledgementElement);
		
		Iterator ackRangeIt = acknowledgementRangeList.iterator();
		while (ackRangeIt.hasNext()) {
			AcknowledgementRange ackRange = (AcknowledgementRange) ackRangeIt.next();
			ackRange.toOMElement (sequenceAcknowledgementElement);
		}
		
		Iterator nackIt = nackList.iterator();
		while (nackIt.hasNext()) {
			Nack nack = (Nack) nackIt.next();
			nack.toOMElement (sequenceAcknowledgementElement);
		}
		
		SOAPHeader.addChild(sequenceAcknowledgementElement);
		
		sequenceAcknowledgementElement = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.SEQUENCE_ACK,rmNamespace);
		
		return header;
	}
	
	public void setIdentifier(Identifier identifier){
		this.identifier = identifier;
	}
	
	public void setAckRanges(List acknowledgementRagngesList){
		acknowledgementRangeList = acknowledgementRagngesList;
	}
	
	public Nack addNackRangges(Nack nack){
		nackList.add(nack);
		return nack;
	}
	
	public AcknowledgementRange addAcknowledgementRanges(AcknowledgementRange ackRange){
		acknowledgementRangeList.add(ackRange);
		return ackRange;
	}
	
	public Identifier getIdentifier(){
		return identifier;
	}
	
	public List getAcknowledgementRanges(){
		return acknowledgementRangeList;
	}
	
	public List getNackList(){
		return nackList;
	}
	
	public void addChildElement(OMElement element){
		acknowledgementRangeList.add(element);
	}

}
