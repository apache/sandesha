/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.sandesha.ws.rm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.ws.utility.Identifier;

/**
 * @author 
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class SequenceAcknowledgement
	extends MessageElement
	implements IRmElement {

	private MessageElement seqAck; //this element
	private List ackRanges;
	private List nackList;
	private Identifier identifier;

	public SequenceAcknowledgement() {
		ackRanges = new LinkedList();
		nackList = new LinkedList();
		seqAck = new MessageElement();
		seqAck.setName("wsrm:SequenceAcknowledgement");
	}

	/* (non-Javadoc)
	 * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
	 */
	public MessageElement getSoapElement() throws SOAPException {
		Iterator ite = ackRanges.iterator();
		while (ite.hasNext()) {
			MessageElement element = (MessageElement) ite.next();
			seqAck.addChildElement(element);
		}
		ite = nackList.iterator();
		while (ite.hasNext()) {
			MessageElement element = (MessageElement) ite.next();
			seqAck.addChildElement(element);
		}
		seqAck.addChildElement(identifier.getSoapElement());
		return seqAck;
	}

	public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelop)
		throws SOAPException {
		SOAPEnvelope env = envelop;
		if (env.getHeader() == null) {
			env.addHeader();
		}

		Name name =
			env.createName(
				"",
				"wsrm",
				"http://schemas.xmlsoap.org/ws/2004/03/rm");

		SOAPHeaderElement headerElement =
			(SOAPHeaderElement) env.getHeader().addHeaderElement(name);
		//.setActor(null);
		headerElement.setActor(null);
		headerElement.setName("SequenceAcknowledgement");

		Iterator iterator = ackRanges.iterator();
		while (iterator.hasNext()) {
			//System.out.println(ite.next().getClass());
			AcknowledgementRange ackRange =
				(AcknowledgementRange) iterator.next();
			headerElement.addChildElement(ackRange.getSoapElement());
		}
		iterator = nackList.iterator();
		while (iterator.hasNext()) {
			Nack nack = (Nack) iterator.next();
			headerElement.addChildElement(nack.getSoapElement());
		}
		if (identifier != null) {
			headerElement.addChildElement(identifier.getSoapElement());
		}

		//env.addHeader((SOAPHeaderElement)seqAck);
		return env;
	}
	public SequenceAcknowledgement fromSOAPEnveploe(SOAPHeaderElement headerElement) {
		System.out.println("fromSOAPEnveploe");
		Iterator iterator = headerElement.getChildElements();
		MessageElement childElement;
		while (iterator.hasNext()) {
			//System.out.println(iterator.next());
			childElement = (MessageElement) iterator.next();
			//System.out.println("from SeqAck " + childElement.getName());
			if (childElement.getName().equals("wsu:Identifier")) {
				identifier = new Identifier();
				identifier.fromSOAPEnvelope(childElement);
			}
			if (childElement.getName().equals("Identifier")) {
				//System.out.println("childElement.getName().equals(\"Identifier\")");
				identifier = new Identifier();
				identifier.fromSOAPEnvelope(childElement);
			}
			if (childElement.getName().equals("wsrm:AcknowledgementRange")) {
				AcknowledgementRange ackRange = new AcknowledgementRange();
				ackRange.fromSOAPEnvelope(childElement);
				ackRanges.add(ackRange);
			}
			if (childElement.getName().equals("AcknowledgementRange")) {
				AcknowledgementRange ackRange = new AcknowledgementRange();
				ackRange.fromSOAPEnvelope(childElement);
				ackRanges.add(ackRange);
			}
			if (childElement.getName().equals("wsrm:Nack")) {
				Nack nack = new Nack();
				nack.fromSOAPEnvelope(childElement);
			}
			if (childElement.getName().equals("Nack")) {
				Nack nack = new Nack();
				nack.fromSOAPEnvelope(childElement);
			}

		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
	 */
	public void addChildElement(MessageElement element) throws SOAPException {
		seqAck.addChildElement(element);
	}

	/**
	 * @return
	 */
	public List getAckRanges() {
		return ackRanges;
	}

	/**
	 * @return
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * @return
	 */
	public List getNackList() {
		return nackList;
	}

	/**
	 * @param list
	 */
	public AcknowledgementRange addAckRanges(AcknowledgementRange ackRange) {
		ackRanges.add(ackRange);
		return ackRange;
	}
	public Nack addNackRanges(Nack nack) {
		nackList.add(nack);
		return nack;
	}
	/*public void setAckRanges(List list) {
		ackRanges = list;
	}*/

	/**
	 * @param identifier
	 */
	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * @param list
	 */
	/*public void setNackList(List list) {
		nackList = list;
	}*/

	/**
	 * @param list
	 * 
	 * TODO:
	 */
	public void setAckRanges(List list) {
		ackRanges = list;
	}

}
