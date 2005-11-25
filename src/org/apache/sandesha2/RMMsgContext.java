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

package org.apache.sandesha2;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.IOMRMPart;

/**
 * This class is used to hold a MessageContext within Sandesha. This is used to
 * easily manupilate the properties of a MessageContext.
 * 
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Jaliya Ekanayaka <jaliya@opensource.lk>
 */

public class RMMsgContext {

	private MessageContext msgContext;

	private HashMap rmMessageParts;

	private int messageType;

	public RMMsgContext() {
		rmMessageParts = new HashMap();
		messageType = Constants.MessageTypes.UNKNOWN;
	}

	public void setMessageContext(MessageContext msgCtx) {
		this.msgContext = msgCtx;
	}

	public RMMsgContext(MessageContext ctx) {
		this();
		this.msgContext = ctx;
	}

	/**
	 * To add a new SOAP envelope to the message. The generated envelope will belong 
	 * to the SOAP version of the MessageContext.
	 * 
	 * @throws SandeshaException
	 */
	public void addSOAPEnvelope() throws SandeshaException {
		int SOAPVersion = Constants.SOAPVersion.v1_1;

		if (!msgContext.isSOAP11())
			SOAPVersion = Constants.SOAPVersion.v1_2;

		if (msgContext.getEnvelope() == null) {
			try {
				msgContext.setEnvelope(SOAPAbstractFactory.getSOAPFactory(
						SOAPVersion).getDefaultEnvelope());
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}
		}

		SOAPEnvelope envelope = msgContext.getEnvelope();
		Iterator keys = rmMessageParts.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			IOMRMPart rmPart = (IOMRMPart) rmMessageParts.get(key);
			rmPart.toSOAPEnvelope(envelope);
		}
	}

	public int getMessageType() {
		return messageType;
	}

	
	/**
	 * The message type can be used to easily identify what this message is.
	 * Possible message types are given in the Constnts.MessageTypes interface.
	 * 
	 * @param msgType
	 */
	public void setMessageType(int msgType) {
		if (msgType >= 0 && msgType <= Constants.MessageTypes.MAX_MESSAGE_TYPE)
			this.messageType = msgType;
	}

	
	/**
	 * Sets an IRMPart object to the MessageContext. Possible parts are give in the 
	 * 
	 * 
	 * @param partId
	 * @param part
	 */
	public void setMessagePart(int partId, IOMRMPart part) {
		if (partId >= 0 && partId <= Constants.MessageParts.MAX_MSG_PART_ID)
			rmMessageParts.put(new Integer(partId), part);
	}

	public IOMRMElement getMessagePart(int partId) {
		return (IOMRMElement) rmMessageParts.get(new Integer(partId));
	}

	public EndpointReference getFrom() {
		return msgContext.getFrom();
	}

	public EndpointReference getTo() {
		return msgContext.getTo();
	}

	public EndpointReference getReplyTo() {
		return msgContext.getReplyTo();
	}

	public RelatesTo getRelatesTo() {
		return msgContext.getRelatesTo();
	}

	public String getMessageId() {
		return msgContext.getMessageID();
	}

	public void setFaultTo(EndpointReference epr) {
		msgContext.setFaultTo(epr);
	}

	public EndpointReference getFaultTo() {
		return msgContext.getFaultTo();
	}

	public SOAPEnvelope getSOAPEnvelope() {
		return msgContext.getEnvelope();
	}

	public void setSOAPEnvelop(SOAPEnvelope envelope) throws SandeshaException {

		try {
			msgContext.setEnvelope(envelope);
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}
	}

	public void test() {
		String opearaitonName = msgContext.getOperationContext()
				.getAxisOperation().getName().getLocalPart();
		System.out.println("Operation is:" + opearaitonName);
	}

	public void serializeSOAPEnvelop() {
		try {
			SOAPEnvelope envelop = msgContext.getEnvelope();
			XMLStreamWriter writer = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(System.out);
			envelop.serialize(writer);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setFrom(EndpointReference epr) {
		msgContext.setFrom(epr);
	}

	public void setTo(EndpointReference epr) {
		msgContext.setTo(epr);
	}

	public void setReplyTo(EndpointReference epr) {
		msgContext.setReplyTo(epr);
	}

	public void setMessageId(String messageId) {
		msgContext.setMessageID(messageId);
	}

	public void setAction(String action) {
		msgContext.setWSAAction(action);
	}

	public void setRelatesTo(RelatesTo relatesTo) {
		msgContext.setRelatesTo(relatesTo);
	}

	public void setWSAAction(String URI) {
		msgContext.setWSAAction(URI);
	}

	public String getWSAAction() {
		return msgContext.getWSAAction();
	}

	public MessageContext getMessageContext() {
		return msgContext;
	}

	public Object getProperty(String key) {
		if (msgContext == null)
			return null;

		return msgContext.getProperty(key);
	}

	public boolean setProperty(String key, Object val) {
		if (msgContext == null)
			return false;

		msgContext.setProperty(key, val);
		return true;
	}

	public AbstractContext getContext() {
		if (msgContext == null)
			return null;

		return msgContext.getSystemContext();
	}

	public void setSOAPAction(String SOAPAction) {
		msgContext.setSoapAction(SOAPAction);
	}
}