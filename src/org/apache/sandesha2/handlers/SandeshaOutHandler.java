/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *  
 */

package org.apache.sandesha2.handlers;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.SOAPAbstractFactory;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.LastMessage;
import org.apache.sandesha2.wsrm.MessageNumber;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.wsdl.WSDLConstants;

public class SandeshaOutHandler extends AbstractHandler {

	public void invoke(MessageContext msgCtx) throws AxisFault {
		System.out.println("Sandesha out handler called");

		String DONE = (String) msgCtx
				.getProperty(Constants.APPLICATION_PROCESSING_DONE);
		if (null != DONE && "true".equals(DONE))
			return;

		//Strating the sender.
		ConfigurationContext context = msgCtx.getSystemContext();
		SandeshaUtil.startSenderIfStopped(context);

		//getting rm message
		RMMsgContext rmMsgCtx = null;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		//TODO recheck
		//continue only if an possible application message
		if (!(rmMsgCtx.getMessageType() == Constants.MessageTypes.UNKNOWN)) {
			return;
		}
		boolean serverSide = msgCtx.isServerSide();
		String tempSequenceId = null;

		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(context).getSequencePropretyBeanMgr();

		//setting temp sequence id (depending on the server side and the client
		// side)
		//temp sequence id is the one used to refer to the sequence (since
		// actual sequence
		// id is not available when first msg arrives)
		//server side - sequenceId if the incoming sequence , client side -
		// xxxxxxxxx
		if (!serverSide) {
			//Code for the client side

		} else {
			try {
				//code for the server side
				//getting the request message and rmMessage.
				MessageContext reqMsgCtx = msgCtx
						.getOperationContext()
						.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

				RMMsgContext requestRMMsgCtx = MsgInitializer
						.initializeMessage(reqMsgCtx);

				Sequence reqSequence = (Sequence) requestRMMsgCtx
						.getMessagePart(Constants.MessageParts.SEQUENCE);
				if (reqSequence == null)
					throw new SandeshaException("Sequence part is null");

				String incomingSeqId = reqSequence.getIdentifier()
						.getIdentifier();
				if (incomingSeqId == null || incomingSeqId == "")
					throw new SandeshaException("Invalid seqence Id");

				tempSequenceId = incomingSeqId;

			} catch (SandeshaException e1) {
				throw new AxisFault(e1.getMessage());
			}
		}

		try {
			if (rmMsgCtx.getMessageType() == Constants.MessageTypes.UNKNOWN) {
				System.out.println("GOT Possible Response Message");

				SOAPEnvelope env = rmMsgCtx.getSOAPEnvelope();
				if (env == null) {
					SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
							Constants.SOAPVersion.DEFAULT).getDefaultEnvelope();
					rmMsgCtx.setSOAPEnvelop(envelope);
				}

				SOAPBody soapBody = rmMsgCtx.getSOAPEnvelope().getBody();
				if (soapBody == null)
					throw new SandeshaException(
							"Invalid SOAP message. Body is not present");

				//TODO - Is this a correct way to find out validity of app.
				// messages.
				boolean validAppMessage = false;
				if (soapBody.getChildElements().hasNext())
					validAppMessage = true;

				if (validAppMessage) {

					//valid response

					//FIXME - do not copy application messages. Coz u loose
					// properties etc.
					RMMsgContext newRMMsgCtx = SandeshaUtil.deepCopy(rmMsgCtx);
					MessageContext newMsgCtx = newRMMsgCtx.getMessageContext();

					//setting contexts
					newMsgCtx.setServiceGroupContext(msgCtx
							.getServiceGroupContext());
					newMsgCtx.setServiceGroupContextId(msgCtx
							.getServiceGroupContextId());
					newMsgCtx.setServiceContext(msgCtx.getServiceContext());
					newMsgCtx.setServiceContextID(msgCtx.getServiceContextID());
					OperationContext newOpContext = new OperationContext(
							newMsgCtx.getOperationDescription());

					//if server side add request message
					if (msgCtx.isServerSide()) {
						MessageContext reqMsgCtx = msgCtx.getOperationContext()
								.getMessageContext(
										WSDLConstants.MESSAGE_LABEL_IN_VALUE);
						newOpContext.addMessageContext(reqMsgCtx);
					}

					newOpContext.addMessageContext(newMsgCtx);
					newMsgCtx.setOperationContext(newOpContext);

					//processing the response
					processResponseMessage(newRMMsgCtx, tempSequenceId);

					if (serverSide) {

						MessageContext reqMsgCtx = msgCtx.getOperationContext()
								.getMessageContext(
										WSDLConstants.MESSAGE_LABEL_IN_VALUE);

						RMMsgContext requestRMMsgCtx = MsgInitializer
								.initializeMessage(reqMsgCtx);

						//let the request end with 202 if a ack has not been
						// written in the incoming thread.
						if (reqMsgCtx.getProperty(Constants.ACK_WRITTEN) == null
								|| !"true".equals(reqMsgCtx
										.getProperty(Constants.ACK_WRITTEN)))
							reqMsgCtx
									.getOperationContext()
									.setProperty(
											org.apache.axis2.Constants.RESPONSE_WRITTEN,
											"false");
						msgCtx.setPausedTrue(getName());

						SOAPEnvelope env123 = msgCtx.getEnvelope();
						try {
							XMLStreamWriter writer = XMLOutputFactory
									.newInstance().createXMLStreamWriter(
											System.out);
							env123.serialize(writer);
						} catch (XMLStreamException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (FactoryConfigurationError e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					} else {
						//client side wait
					}
				}
			}
		} catch (SandeshaException e) {
			throw new AxisFault(e.getMessage());
		}
	}

	private void processResponseMessage(RMMsgContext rmMsg,
			String tempSequenceId) throws SandeshaException {

		MessageContext msg = rmMsg.getMessageContext();

		if (rmMsg == null)
			throw new SandeshaException("Message or reques message is null");

		AbstractContext context = rmMsg.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		SequencePropertyBeanMgr sequencePropertyMgr = AbstractBeanMgrFactory
				.getInstance(context).getSequencePropretyBeanMgr();
		RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
				.getInstance(context).getRetransmitterBeanMgr();

		SequencePropertyBean toBean = sequencePropertyMgr.retrieve(
				tempSequenceId, Constants.SequenceProperties.TO_EPR);
		SequencePropertyBean replyToBean = sequencePropertyMgr.retrieve(
				tempSequenceId, Constants.SequenceProperties.REPLY_TO_EPR);

		//again - looks weird in the client side - but consistent
		SequencePropertyBean outSequenceBean = sequencePropertyMgr.retrieve(
				tempSequenceId, Constants.SequenceProperties.OUT_SEQUENCE_ID);

		if (toBean == null)
			throw new SandeshaException("To is null");
		if (replyToBean == null)
			throw new SandeshaException("Replyto is null");

		EndpointReference incomingTo = (EndpointReference) toBean.getValue();
		EndpointReference incomingReplyTo = (EndpointReference) replyToBean
				.getValue();

		if (incomingTo == null || incomingTo.getAddress() == null
				|| incomingTo.getAddress() == "")
			throw new SandeshaException("To Property has an invalid value");
		if (incomingReplyTo == null || incomingReplyTo.getAddress() == null
				|| incomingReplyTo.getAddress() == "")
			throw new SandeshaException("ReplyTo is not set correctly");

		//FIXME - set toBean - replyto value, replytobean - tovalue

		rmMsg.setTo(incomingTo);
		rmMsg.setReplyTo(incomingReplyTo);

		//Retransmitter bean entry for the application message
		RetransmitterBean appMsgEntry = new RetransmitterBean();
		String key = SandeshaUtil
				.storeMessageContext(rmMsg.getMessageContext());
		appMsgEntry.setKey(key);
		appMsgEntry.setLastSentTime(0);
		appMsgEntry.setMessageId(rmMsg.getMessageId());

		Sequence sequence = new Sequence();

		long nextMsgNo = getNextMsgNo(rmMsg.getMessageContext()
				.getSystemContext(), tempSequenceId);
		MessageNumber msgNumber = new MessageNumber();
		msgNumber.setMessageNumber(nextMsgNo);
		sequence.setMessageNumber(msgNumber);

		appMsgEntry.setMessageNumber(nextMsgNo);

		//setting last message
		if (msg.isServerSide()) {
			//server side
			String incomingSeqId = tempSequenceId;
			MessageContext requestMsg = null;

			try {
				requestMsg = msg.getOperationContext().getMessageContext(
						WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}

			RMMsgContext reqRMMsgCtx = MsgInitializer
					.initializeMessage(requestMsg);
			Sequence requestSequence = (Sequence) reqRMMsgCtx
					.getMessagePart(Constants.MessageParts.SEQUENCE);
			if (requestSequence == null)
				throw new SandeshaException("Request Sequence is null");

			if (requestSequence.getLastMessage() != null) {
				//FIX ME - This fails if request last message has more than one
				// responses.
				sequence.setLastMessage(new LastMessage());

				//saving the last message no.
				SequencePropertyBean lastOutMsgBean = new SequencePropertyBean(
						tempSequenceId,
						Constants.SequenceProperties.LAST_OUT_MESSAGE,
						new Long(nextMsgNo));
				sequencePropertyMgr.insert(lastOutMsgBean);
			}

		} else {
			//client side
			Object obj = msg.getProperty(Constants.LAST_MESSAGE);
			if (obj != null && "true".equals(obj)) {
				sequence.setLastMessage(new LastMessage());
			}
		}

		if (outSequenceBean == null || outSequenceBean.getValue() == null) {

			if (tempSequenceId == null)
				throw new SandeshaException("TempSequenceId is not set");

			//Adding create sequence
			SequencePropertyBean responseCreateSeqAdded = sequencePropertyMgr
					.retrieve(
							tempSequenceId,
							Constants.SequenceProperties.OUT_CREATE_SEQUENCE_SENT);

			if (responseCreateSeqAdded == null
					|| responseCreateSeqAdded.getValue() == null
					|| "".equals(responseCreateSeqAdded.getValue())) {
				responseCreateSeqAdded = new SequencePropertyBean(
						tempSequenceId,
						Constants.SequenceProperties.OUT_CREATE_SEQUENCE_SENT,
						"true");
				sequencePropertyMgr.insert(responseCreateSeqAdded);
				addCreateSequenceMessage(rmMsg, tempSequenceId);
			}
			Identifier identifier = new Identifier();
			identifier.setIndentifer("uuid:tempID");
			sequence.setIdentifier(identifier);
			appMsgEntry.setSend(false);
		} else {
			Identifier identifier = new Identifier();
			identifier.setIndentifer((String) outSequenceBean.getValue());
			sequence.setIdentifier(identifier);
			appMsgEntry.setSend(true);
		}

		rmMsg.setMessagePart(Constants.MessageParts.SEQUENCE, sequence);
		try {
			rmMsg.addSOAPEnvelope();
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		appMsgEntry.setTempSequenceId(tempSequenceId);

		retransmitterMgr.insert(appMsgEntry);
	}

	private long getNextMsgNo(ConfigurationContext context,
			String tempSequenceId) {
		//FIXME set a correct message number.
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(context).getSequencePropretyBeanMgr();
		SequencePropertyBean nextMsgNoBean = seqPropMgr.retrieve(
				tempSequenceId,
				Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);
		long nextMsgNo = 1;
		boolean update = false;
		if (nextMsgNoBean != null) {
			update = true;
			Long nextMsgNoLng = (Long) nextMsgNoBean.getValue();
			nextMsgNo = nextMsgNoLng.longValue();
		} else {
			nextMsgNoBean = new SequencePropertyBean();
			nextMsgNoBean.setSequenceId(tempSequenceId);
			nextMsgNoBean
					.setName(Constants.SequenceProperties.NEXT_MESSAGE_NUMBER);
		}

		nextMsgNoBean.setValue(new Long(nextMsgNo + 1));
		if (update)
			seqPropMgr.update(nextMsgNoBean);
		else
			seqPropMgr.insert(nextMsgNoBean);

		return nextMsgNo;
	}

	public void addCreateSequenceMessage(RMMsgContext applicationRMMsg,
			String tempSequenceId) throws SandeshaException {
		MessageContext applicationMsg = applicationRMMsg.getMessageContext();
		if (applicationMsg == null)
			throw new SandeshaException("Message context is null");
		RMMsgContext createSeqRMMessage = RMMsgCreator
				.createCreateSeqMsg(applicationRMMsg);
		MessageContext createSeqMsg = createSeqRMMessage.getMessageContext();
		createSeqMsg.setRelatesTo(null); //create seq msg does not relateTo
		// anything
		AbstractContext context = applicationRMMsg.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		CreateSeqBeanMgr createSeqMgr = AbstractBeanMgrFactory.getInstance(
				context).getCreateSeqBeanMgr();
		CreateSeqBean createSeqBean = new CreateSeqBean(tempSequenceId,
				createSeqMsg.getMessageID(), null);
		createSeqMgr.insert(createSeqBean);

		System.out.println("Create sequence msg id:"
				+ createSeqRMMessage.getMessageId());

		RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
				.getInstance(context).getRetransmitterBeanMgr();
		String key = SandeshaUtil.storeMessageContext(createSeqRMMessage
				.getMessageContext());
		RetransmitterBean createSeqEntry = new RetransmitterBean();
		createSeqEntry.setKey(key);
		createSeqEntry.setLastSentTime(0);
		createSeqEntry.setMessageId(createSeqRMMessage.getMessageId());
		createSeqEntry.setSend(true);
		retransmitterMgr.insert(createSeqEntry);
	}

	public QName getName() {
		return new QName(Constants.OUT_HANDLER_NAME);
	}
}