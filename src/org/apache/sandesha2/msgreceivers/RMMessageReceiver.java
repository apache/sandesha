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
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;

import com.bea.xml.stream.XMLWriterBase;

/**
 * @author
 */

public class RMMessageReceiver extends AbstractMessageReceiver {

	
	public void setCreateSequence(MessageContext inMessage,
			MessageContext outMessage) throws AxisFault {

		System.out.println("RM Msg Receiver was called");
		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
		MsgValidator.validateMessage(rmMsgCtx);

		if (rmMsgCtx.getMessageType() == Constants.MESSAGE_TYPE_CREATE_SEQ) {

			RMMsgContext createSeqResponse = RMMsgCreator
					.createCreateSeqResponseMsg(rmMsgCtx, outMessage);
			
			CreateSequenceResponse createSeqResPart = (CreateSequenceResponse) createSeqResponse.
								getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ_RESPONSE);
			
			String newSequenceId = createSeqResPart.getIdentifier().getIdentifier();
			
			if (newSequenceId==null)
				throw new AxisFault ("Internal error - Generated sequence id is null");
			
			
			//TODO: put below to SequenceManager.setUpNewSequence ();
			ArrayList arr;
			SequencePropertyBean seqPropBean = new SequencePropertyBean (newSequenceId,Constants.SEQ_PROPERTY_RECEIVED_MSG_LIST,arr = new ArrayList());
			SequencePropertyBeanMgr beanMgr = new SequencePropertyBeanMgr (Constants.STORAGE_TYPE_IN_MEMORY);
			System.out.println ("put --" + newSequenceId + "--" + Constants.SEQ_PROPERTY_RECEIVED_MSG_LIST);
			beanMgr.create(seqPropBean);
			
			arr.add("bbb");

			outMessage.setResponseWritten(true);
		} else {
			outMessage.setResponseWritten(true);
		}

	}

	public final void receive(MessageContext messgeCtx) throws AxisFault {

		System.out.println ("within RM Msg receiver");
		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(messgeCtx);
		MsgValidator.validateMessage(rmMsgCtx);

		AbstractMessageReceiver msgReceiver = null;
		if (rmMsgCtx.getMessageType() == Constants.MESSAGE_TYPE_CREATE_SEQ) {
			
			CreateSequence createSeqPart = (CreateSequence) rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ);
			if (createSeqPart==null)
				throw new AxisFault ("No create sequence part in create sequence message");
			
			//String acksTo = createSeqPart.getAcksTo().getEndPointReference().toString();
			String from = messgeCtx.getFrom().getAddress();
			if(from.equals(Constants.WSA.NS_URI_ANONYMOUS)) {
				msgReceiver = new RMInOutSyncMsgReceiver ();
			} else {
				msgReceiver = new RMInOutAsyncMsgReceiver ();
			}
			
		} else {
			//TODO: Check weather terminate
			msgReceiver = new RMInMsgReceiver ();
		}
		
		if(msgReceiver!=null) {
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
		
		public void invokeBusinessLogic(MessageContext inMessage) throws AxisFault {
			RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
			MsgValidator.validateMessage(rmMsgCtx);
			
			//TODO check for terminate sequence.
			//TODO handle terminate sequence.
			
			
		}
	}
	
	private class RMInOutSyncMsgReceiver extends AbstractInOutSyncMessageReceiver {
		
		public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
			
			RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
			MsgValidator.validateMessage(rmMsgCtx);
			
			if (rmMsgCtx.getMessageType()==Constants.MESSAGE_TYPE_CREATE_SEQ) {
				//TODO handle sync create seq.
				setCreateSequence(inMessage,outMessage);
			}
			
		}
}
	
	private class RMInOutAsyncMsgReceiver extends AbstractInOutAsyncMessageReceiver {
		
		public void invokeBusinessLogic(MessageContext inMessage,
				MessageContext outMessage, ServerCallback callback) throws AxisFault {
			
			RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
			MsgValidator.validateMessage(rmMsgCtx);
			
			if (rmMsgCtx.getMessageType()==Constants.MESSAGE_TYPE_CREATE_SEQ) {
				//TODO handle async create seq.
				setCreateSequence(inMessage,outMessage);
			}
		}
	}

}