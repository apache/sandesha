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

package org.apache.sandesha2.msgreceivers;

import java.util.ArrayList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.receivers.AbstractInOutAsyncMessageReceiver;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.receivers.ServerCallback;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.SequenceMenager;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;

import com.bea.xml.stream.XMLWriterBase;

/**
 * @author
 */

public class RMMessageReceiver extends AbstractMessageReceiver {

	//TODO move this code to create seq msg processor.
	public void setCreateSequence(MessageContext inMessage,
			MessageContext outMessage) throws AxisFault {

		System.out.println("set create seq was called in RM Msg receiver");

		RMMsgContext createSeqMsg = null;
		try {
			createSeqMsg = MsgInitializer.initializeMessage(inMessage);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		if (createSeqMsg.getMessageType() != Constants.MessageTypes.CREATE_SEQ)
			throw new AxisFault("Wrong message type");

		RMMsgContext createSeqResponse = RMMsgCreator
				.createCreateSeqResponseMsg(createSeqMsg, outMessage);
		CreateSequenceResponse createSeqResPart = (CreateSequenceResponse) createSeqResponse
				.getMessagePart(Constants.MessageParts.CREATE_SEQ_RESPONSE);

		String newSequenceId = createSeqResPart.getIdentifier().getIdentifier();
		if (newSequenceId == null)
			throw new AxisFault(
					"Internal error - Generated sequence id is null");

		ConfigurationContext configCtx = inMessage.getSystemContext();
		SequenceMenager.setUpNewSequence(newSequenceId, createSeqMsg);

		CreateSequence createSeq = (CreateSequence) createSeqMsg
				.getMessagePart(Constants.MessageParts.CREATE_SEQ);
		if (createSeq == null)
			throw new AxisFault(
					"Create sequence part not present in the create sequence message");

		EndpointReference acksTo = createSeq.getAcksTo().getAddress().getEpr();
		if (acksTo == null || acksTo.getAddress() == null
				|| acksTo.getAddress() == "")
			throw new AxisFault(
					"Acks to not present in the create sequence message");

		SequencePropertyBean seqPropBean = new SequencePropertyBean(
				newSequenceId, Constants.SequenceProperties.ACKS_TO_EPR, acksTo);
		//		SequencePropertyBeanMgr beanMgr = new SequencePropertyBeanMgr
		// (Constants.DEFAULT_STORAGE_TYPE);
		//		beanMgr.create(seqPropBean);

		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(configCtx).getSequencePropretyBeanMgr();
		seqPropMgr.insert(seqPropBean);
		outMessage.setResponseWritten(true);

	}

	public final void receive(MessageContext messgeCtx) throws AxisFault {

		System.out.println("within RM Msg receiver");

		//intitializing the message.
		RMMsgContext rmMsgCtx = null;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(messgeCtx);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		AbstractMessageReceiver msgReceiver = null;

		String replyTo = messgeCtx.getFrom().getAddress();
		if (rmMsgCtx.getMessageType() == Constants.MessageTypes.TERMINATE_SEQ)
			msgReceiver = new RMInMsgReceiver();
		else if (rmMsgCtx.getMessageType() == Constants.MessageTypes.CREATE_SEQ
				&& ((replyTo == null) || replyTo
						.equals(Constants.WSA.NS_URI_ANONYMOUS)))
			msgReceiver = new RMInOutSyncMsgReceiver();
		else if (rmMsgCtx.getMessageType() == Constants.MessageTypes.CREATE_SEQ)
			msgReceiver = new RMInOutAsyncMsgReceiver();

		if (msgReceiver != null) {
			msgReceiver.receive(messgeCtx);
		}
	}

	private SOAPFactory getSOAPFactory(MessageContext inMessage)
			throws AxisFault {
		String nsURI = inMessage.getEnvelope().getNamespace().getName();
		if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
			fac = OMAbstractFactory.getSOAP12Factory();
		} else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
			fac = OMAbstractFactory.getSOAP11Factory();
		} else {
			throw new AxisFault(
					"Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
		}
		return fac;
	}

	private class RMInMsgReceiver extends AbstractInMessageReceiver {

		public void invokeBusinessLogic(MessageContext inMessage)
				throws AxisFault {
			RMMsgContext rmMsgCtx = null;
			try {
				rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
			} catch (SandeshaException ex) {
				throw new AxisFault("Cant initialize the message");
			}

			//TODO check for terminate sequence.
			//TODO handle terminate sequence.

		}
	}

	private class RMInOutSyncMsgReceiver extends
			AbstractInOutSyncMessageReceiver {

		public void invokeBusinessLogic(MessageContext inMessage,
				MessageContext outMessage) throws AxisFault {

			RMMsgContext rmMsgCtx = null;
			try {
				rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
			} catch (SandeshaException ex) {
				throw new AxisFault("Cant initialize the message");
			}

			if (rmMsgCtx.getMessageType() == Constants.MessageTypes.CREATE_SEQ) {
				//TODO handle sync create seq.
				setCreateSequence(inMessage, outMessage);
			}

		}
	}

	private class RMInOutAsyncMsgReceiver extends
			AbstractInOutAsyncMessageReceiver {

		public void invokeBusinessLogic(MessageContext inMessage,
				MessageContext outMessage, ServerCallback callback)
				throws AxisFault {

			RMMsgContext rmMsgCtx = null;
			try {
				rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
			} catch (SandeshaException ex) {
				throw new AxisFault("Cant initialize the message");
			}

			if (rmMsgCtx.getMessageType() == Constants.MessageTypes.CREATE_SEQ) {
				//TODO handle async create seq.
				setCreateSequence(inMessage, outMessage);
				ConfigurationContext configCtx = outMessage.getSystemContext();
				AxisEngine engine = new AxisEngine(configCtx);
				engine.send(outMessage);
			}
		}
	}

}