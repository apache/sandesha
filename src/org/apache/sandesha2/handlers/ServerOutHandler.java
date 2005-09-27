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

package org.apache.sandesha2.handlers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.InOutMEPClient;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.log4j.lf5.viewer.configure.MRUFileManager;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.SOAPAbstractFactory;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLConstants;

/**
 * @author Sanka
 * @author Chamikara
 * @author Jaliya
 */
public class ServerOutHandler extends AbstractHandler {

	public void invoke(MessageContext msgCtx) throws AxisFault {
		//log
		System.out.println("In server OutHandler");

		//Strating the sender.
		ConfigurationContext ctx = msgCtx.getSystemContext();
		SandeshaUtil.startSenderIfStopped(ctx);

		//getting rm message
		RMMsgContext rmMsgCtx;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		//getting the request message and rmMessage.
		MessageContext reqMsgCtx = msgCtx.getOperationContext()
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN);
		RMMsgContext requestRMMsgCtx;
		try {
			requestRMMsgCtx = MsgInitializer.initializeMessage(reqMsgCtx);
			if (rmMsgCtx.getMessageType() == Constants.MESSAGE_TYPE_UNKNOWN) {

				System.out.println("GOT Possible Response Message");
				AbstractContext context = rmMsgCtx.getContext();
				if (context == null)
					throw new SandeshaException("Context is null");
				
				Sequence sequence = (Sequence) requestRMMsgCtx
						.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
				if (sequence == null)
					throw new SandeshaException("Sequence part is null");

				String incomingSeqId = sequence.getIdentifier().getIdentifier();
				if (incomingSeqId == null || incomingSeqId == "")
					throw new SandeshaException("Invalid seqence Id");

				SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
						.getInstance(context).getSequencePropretyBeanMgr();
				SequencePropertyBean acksToBean = seqPropMgr.retrieve(
						incomingSeqId, Constants.SEQ_PROPERTY_ACKS_TO_EPR);
				if (acksToBean == null
						|| acksToBean.getValue() == null
						|| !(acksToBean.getValue() instanceof EndpointReference))
					throw new SandeshaException("Acksto is not set correctly");

				EndpointReference acksToEPR = (EndpointReference) acksToBean
						.getValue();
				if (acksToEPR.getAddress() == null
						|| acksToEPR.getAddress() == "")
					throw new SandeshaException("AcksTo not set correctly");

				SOAPEnvelope env = rmMsgCtx.getSOAPEnvelope();
				if (env == null) {
					SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
							Constants.DEFAULT_SOAP_VERSION)
							.getDefaultEnvelope();
					rmMsgCtx.setSOAPEnvelop(envelope);
				}
				SOAPBody soapBody = rmMsgCtx.getSOAPEnvelope().getBody();
				if (soapBody == null)
					throw new SandeshaException(
							"Invalid SOAP message. Body is not present");

				boolean validResponse = false;
				if (soapBody.getChildElements().hasNext())
					validResponse = true;

				if (!validResponse) { //TODO either change MsgReceiver or move
					if (Constants.WSA.NS_URI_ANONYMOUS.equals(acksToEPR
							.getAddress())) {
						RMMsgCreator.addAckMessage(rmMsgCtx);
					}
				} else {
					//valid response

					RMMsgContext ackRMMsgContext = RMMsgCreator
							.createAckMessage(rmMsgCtx);
					MessageContext ackMsgContext = ackRMMsgContext.getMessageContext();
					ackMsgContext.setServiceGroupContext(msgCtx.getServiceGroupContext());
					ackMsgContext.setServiceGroupContextId(msgCtx.getServiceGroupContextId());
					ackMsgContext.setServiceContext(msgCtx.getServiceContext());
					ackMsgContext.setServiceContextID(msgCtx.getServiceContextID());
					OperationContext ackOpContext = new OperationContext (ackMsgContext.getOperationDescription());
					ackOpContext.addMessageContext(ackMsgContext);
					ackMsgContext.setOperationContext(ackOpContext);
					RMMsgContext newRMMsgCtx = SandeshaUtil
							.copyRMMessageContext(rmMsgCtx);
					MessageContext newMsgCtx = newRMMsgCtx.getMessageContext();
					rmMsgCtx.setSOAPEnvelop(ackRMMsgContext.getSOAPEnvelope());

					//setting contexts
					newMsgCtx.setServiceGroupContext(msgCtx.getServiceGroupContext());
					newMsgCtx.setServiceGroupContextId(msgCtx.getServiceGroupContextId());
					newMsgCtx.setServiceContext(msgCtx.getServiceContext());
					newMsgCtx.setServiceContextID(msgCtx.getServiceContextID());
				    OperationContext newOpContext = new OperationContext (newMsgCtx.getOperationDescription());
				    newOpContext.addMessageContext(newMsgCtx);
				    newMsgCtx.setOperationContext(newOpContext);
				    
					//processing the response
					processResponseMessage(newRMMsgCtx, requestRMMsgCtx);
				}
			}
		} catch (SandeshaException e) {
			throw new AxisFault(e.getMessage());
		}
	}

	private void processResponseMessage(RMMsgContext msg, RMMsgContext reqMsg)
			throws SandeshaException {
		if (msg == null || reqMsg == null)
			throw new SandeshaException("Message or reques message is null");

		Sequence sequence = (Sequence) reqMsg
				.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
		if (sequence == null)
			throw new SandeshaException("Sequence part is null");

		String incomingSeqId = sequence.getIdentifier().getIdentifier();
		if (incomingSeqId == null || incomingSeqId == "")
			throw new SandeshaException("Invalid seqence Id");

		AbstractContext context = msg.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		SequencePropertyBeanMgr mgr = AbstractBeanMgrFactory.getInstance(
				context).getSequencePropretyBeanMgr();

		SequencePropertyBean toBean = mgr.retrieve(incomingSeqId,
				Constants.SEQ_PROPERTY_TO_EPR);
		SequencePropertyBean replyToBean = mgr.retrieve(incomingSeqId,
				Constants.SEQ_PROPERTY_ACKS_TO_EPR);
		SequencePropertyBean outSequenceBean = mgr.retrieve(incomingSeqId,
				Constants.SEQ_PROPERTY_OUT_SEQUENCE_ID);

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

		msg.setTo(incomingReplyTo);
		msg.setReplyTo(incomingTo);

		if (outSequenceBean == null || outSequenceBean.getValue() == null) {
			RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
					.getInstance(context).getRetransmitterBeanMgr();

			msg.getMessageContext().setPausedTrue(getName());

			RetransmitterBean appMsgEntry = new RetransmitterBean();
			String key = SandeshaUtil.storeMessageContext(msg
					.getMessageContext());
			appMsgEntry.setKey(key);
			appMsgEntry.setLastSentTime(0);
			appMsgEntry.setTempSequenceId(incomingSeqId);
			appMsgEntry.setSend(false);
			appMsgEntry.setMessageId(msg.getMessageId());
			retransmitterMgr.insert(appMsgEntry);

			addCreateSequenceMessage(msg);

		} else {
			//Sequence id is present
			//set sequence part
			//add message to retransmitter table with send=true;
		}

	}

	public void addCreateSequenceMessage(RMMsgContext applicationRMMsg)
			throws SandeshaException {
		MessageContext applicationMsg = applicationRMMsg.getMessageContext();
		if (applicationMsg==null)
			throw new SandeshaException ("Message context is null");
		RMMsgContext createSeqRMMessage = RMMsgCreator
				.createCreateSeqMsg(applicationRMMsg);
		MessageContext createSeqMsg = createSeqRMMessage.getMessageContext();
		AbstractContext context = applicationRMMsg.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		StorageMapBeanMgr storageMapMgr = AbstractBeanMgrFactory.getInstance(
				context).getStorageMapBeanMgr();
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