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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.SpecSpecificConstants;

/**
 * Adds the SequenceAcknowledgement header block.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class SequenceAcknowledgement implements IOMRMPart {
	
	private OMElement sequenceAcknowledgementElement;
	private Identifier identifier;
	private ArrayList acknowledgementRangeList;
	private ArrayList nackList;
	OMNamespace rmNamespace = null;
	SOAPFactory factory;
	String namespaceValue = null;
	private boolean mustUnderstand = true;
	private AckNone ackNone = null;
	private AckFinal ackFinal = null;
	
	public SequenceAcknowledgement(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.namespaceValue = namespaceValue;
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		sequenceAcknowledgementElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK, rmNamespace);
		acknowledgementRangeList = new ArrayList();
		nackList = new ArrayList();
	}

	public OMElement getOMElement() throws OMException {
		return sequenceAcknowledgementElement;
	}

	public Object fromOMElement(OMElement element) throws OMException,SandeshaException {

		if (element == null || !(element instanceof SOAPHeader))
			throw new OMException(
					"Cant get sequence acknowlegement from a non-header element");

		SOAPHeader header = (SOAPHeader) element;
		OMElement sequenceAckPart = header.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK));

		if (sequenceAckPart == null)
			throw new OMException(
					"The passed element does not contain a seqence ackknowledgement Part");

		identifier = new Identifier(factory,namespaceValue);
		identifier.fromOMElement(sequenceAckPart);

		Iterator ackRangeParts = sequenceAckPart.getChildrenWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.ACK_RANGE));

		while (ackRangeParts.hasNext()) {
			OMElement ackRangePart = (OMElement) ackRangeParts.next();

			AcknowledgementRange ackRange = new AcknowledgementRange(factory,namespaceValue);
			ackRange.fromOMElement(ackRangePart);
			acknowledgementRangeList.add(ackRange);
		}

		Iterator nackParts = sequenceAckPart.getChildrenWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NACK));

		while (nackParts.hasNext()) {
			OMElement nackPart = (OMElement) nackParts.next();
			Nack nack = new Nack(factory,namespaceValue);
			nack.fromOMElement(nackPart);
			nackList.add(nack);
		}

		String rmSpecVersion = SpecSpecificConstants.getSpecVersionString (namespaceValue);
		
		if (SpecSpecificConstants.isAckFinalAllowed(rmSpecVersion)) {
			OMElement ackFinalPart = sequenceAckPart.getFirstChildWithName(new QName (namespaceValue,Sandesha2Constants.WSRM_COMMON.FINAL));
			if (ackFinalPart!=null) {
				ackFinal = new AckFinal (factory,namespaceValue);
				ackFinal.fromOMElement(sequenceAckPart);
			}
		}
		
		if (SpecSpecificConstants.isAckNoneAllowed(rmSpecVersion)) {
			OMElement ackNonePart = sequenceAckPart.getFirstChildWithName(new QName (namespaceValue,Sandesha2Constants.WSRM_COMMON.NONE));
			if (ackNonePart!=null) {
				ackNone = new AckNone (factory,namespaceValue);
				ackNone.fromOMElement(sequenceAckPart);
			}
		}
		
		sequenceAcknowledgementElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK, rmNamespace);


		return this;
	}

	public OMElement toOMElement(OMElement header) throws OMException,SandeshaException {

		if (header == null || !(header instanceof SOAPHeader))
			throw new OMException();

		SOAPHeader SOAPHeader = (SOAPHeader) header;

		SOAPHeaderBlock sequenceAcknowledgementHeaderBlock = SOAPHeader.addHeaderBlock(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK,rmNamespace);
		
		if (sequenceAcknowledgementHeaderBlock == null)
			throw new OMException(
					"Cant set sequence acknowledgement since the element is null");

		if (identifier == null)
			throw new OMException(
					"Cant set the sequence since Identifier is null");

		sequenceAcknowledgementHeaderBlock.setMustUnderstand(isMustUnderstand());
		identifier.toOMElement(sequenceAcknowledgementHeaderBlock);

		Iterator ackRangeIt = acknowledgementRangeList.iterator();
		while (ackRangeIt.hasNext()) {
			AcknowledgementRange ackRange = (AcknowledgementRange) ackRangeIt
					.next();
			ackRange.toOMElement(sequenceAcknowledgementHeaderBlock);
		}

		Iterator nackIt = nackList.iterator();
		while (nackIt.hasNext()) {
			Nack nack = (Nack) nackIt.next();
			nack.toOMElement(sequenceAcknowledgementHeaderBlock);
		}
		
		String rmSpecVersion = SpecSpecificConstants.getSpecVersionString(namespaceValue);

		//setting a 'None' when nothing is there (for the correct RM version)
		if (ackNone==null && acknowledgementRangeList.size()==0 && nackList.size()==0 && SpecSpecificConstants.isAckNoneAllowed(rmSpecVersion)) {
			ackNone = new AckNone (factory,namespaceValue);
		}
		
		if (ackNone!=null) {
			if (!SpecSpecificConstants.isAckNoneAllowed(rmSpecVersion)) {
				throw new SandeshaException ("The given namespace does not allow the 'None' part to be added to the sequenceAcknowledgement element");
			}
			
			if (acknowledgementRangeList.size()>0) {
				throw new SandeshaException ("The 'None' element cannot be present when there are acknowledgement range elements under the sequenceAcknowledgement");
			}
			
			if (nackList.size()>0) {
				throw new SandeshaException ("The 'None' element cannot be present when there are Nack elements under the sequenceAcknowledgement");
			}
			
			ackNone.toOMElement(sequenceAcknowledgementHeaderBlock);
		}
		
		if (ackFinal!=null) {
			if (!SpecSpecificConstants.isAckFinalAllowed(rmSpecVersion)) {
				throw new SandeshaException ("The given namespace does not allow the 'Final' part to be added to the sequenceAcknowledgement element");
			}
			
			if (nackList.size()>0) {
				throw new SandeshaException ("The 'Final' element cannot be present when there are Nack elements under the sequenceAcknowledgement");
			}
			
			ackFinal.toOMElement(sequenceAcknowledgementHeaderBlock);
		}
		
		SOAPHeader.addChild(sequenceAcknowledgementHeaderBlock);
		

		sequenceAcknowledgementElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK, rmNamespace);

		return header;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public void setAckRanges(ArrayList acknowledgementRagngesList) {
		acknowledgementRangeList = acknowledgementRagngesList;
	}

	public Nack addNackRangges(Nack nack) {
		nackList.add(nack);
		return nack;
	}

	public AcknowledgementRange addAcknowledgementRanges(
			AcknowledgementRange ackRange) {
		acknowledgementRangeList.add(ackRange);
		return ackRange;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public List getAcknowledgementRanges() {
		return acknowledgementRangeList;
	}

	public List getNackList() {
		return nackList;
	}

	public void addChildElement(OMElement element) {
		acknowledgementRangeList.add(element);
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) throws SandeshaException {
		SOAPHeader header = envelope.getHeader();

		//detach if already exist.
		OMElement elem = header.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK));
		if (elem != null)
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

	public AckFinal getAckFinal() {
		return ackFinal;
	}

	public void setAckFinal(AckFinal ackFinal) {
		this.ackFinal = ackFinal;
	}

	public AckNone getAckNone() {
		return ackNone;
	}

	public void setAckNone(AckNone ackNone) {
		this.ackNone = ackNone;
	}
}