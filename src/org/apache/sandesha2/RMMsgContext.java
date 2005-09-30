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
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.IOMRMPart;
import org.apache.sandesha2.wsrm.TerminateSequence;
import org.apache.wsdl.WSDLConstants;
import org.ietf.jgss.MessageProp;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya
 */

public class RMMsgContext {

	private MessageContext msgContext;

	private HashMap rmMessageParts;

	private int messageType;

	public RMMsgContext() {
		rmMessageParts = new HashMap();
		messageType = Constants.MessageTypes.UNKNOWN;
	}
	
	public void setMessageContext (MessageContext msgCtx) {
		this.msgContext = msgCtx;
	}

	public RMMsgContext(MessageContext ctx) {
		this();
		this.msgContext = ctx;

		//MsgInitializer.populateRMMsgContext(ctx,this);
	}

	public void addSOAPEnvelope() throws AxisFault {
		if (msgContext.getEnvelope() == null) {
			msgContext.setEnvelope(SOAPAbstractFactory.getSOAPFactory(
					Constants.SOAPVersion.DEFAULT).getDefaultEnvelope());
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

	public void setMessageType(int msgType) {
		if (msgType >= 0 && msgType <= Constants.MessageTypes.MAX_MESSAGE_TYPE)
			this.messageType = msgType;
	}

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

	public SOAPEnvelope getSOAPEnvelope() {
		return msgContext.getEnvelope();
	}

	public void setSOAPEnvelop(SOAPEnvelope envelope) throws SandeshaException {
		
		try {
			msgContext.setEnvelope(envelope);
		} catch (AxisFault e) {
			throw new SandeshaException (e.getMessage());
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
	
	public String getWSAAction () {
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

}