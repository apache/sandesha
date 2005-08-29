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
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.receivers.AbstractMessageReceiver;
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

/**
 * @author
 */

public class RMMessageReceiver extends AbstractMessageReceiver {

	public void invokeBusinessLogic(MessageContext inMessage,
			MessageContext outMessage) throws AxisFault {

		System.out.println("RM Msg Receiver was called");
		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(inMessage);
		MsgValidator.validateMessage(rmMsgCtx);

		if (rmMsgCtx.getMessageType() == Constants.MESSAGE_TYPE_CREATE_SEQ) {

			RMMsgContext createSeqResponse = RMMsgCreator
					.createCreateSeqResponseMsg(rmMsgCtx, outMessage);

			//createSeqResponse.serializeSOAPEnvelop();
			outMessage.setResponseWritten(true);
		} else {
			outMessage.setResponseWritten(true);
		}

	}

	public final void receive(MessageContext messgeCtx) throws AxisFault {

		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(messgeCtx);
		MsgValidator.validateMessage(rmMsgCtx);

		if (rmMsgCtx.getMessageType() == Constants.MESSAGE_TYPE_CREATE_SEQ) {
			MessageContext newmsgCtx = new MessageContext(messgeCtx
					.getSystemContext(), messgeCtx.getSessionContext(),
					messgeCtx.getTransportIn(), messgeCtx.getTransportOut());

			newmsgCtx
					.setMessageInformationHeaders(new MessageInformationHeaders());
			MessageInformationHeaders oldMessageInfoHeaders = messgeCtx
					.getMessageInformationHeaders();
			MessageInformationHeaders messageInformationHeaders = new MessageInformationHeaders();
			messageInformationHeaders.setTo(oldMessageInfoHeaders.getReplyTo());
			messageInformationHeaders.setFaultTo(oldMessageInfoHeaders
					.getFaultTo());
			messageInformationHeaders.setFrom(oldMessageInfoHeaders.getTo());
			messageInformationHeaders
					.setRelatesTo(new RelatesTo(
							oldMessageInfoHeaders.getMessageId(),
							AddressingConstants.Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE));
			newmsgCtx.setMessageInformationHeaders(messageInformationHeaders);
			newmsgCtx.setOperationContext(messgeCtx.getOperationContext());
			newmsgCtx.setServiceContext(messgeCtx.getServiceContext());
			newmsgCtx.setProperty(MessageContext.TRANSPORT_OUT, messgeCtx
					.getProperty(MessageContext.TRANSPORT_OUT));
			newmsgCtx.setProperty(HTTPConstants.HTTPOutTransportInfo, messgeCtx
					.getProperty(HTTPConstants.HTTPOutTransportInfo));

			//Setting the charater set encoding
			newmsgCtx
					.setProperty(
							MessageContext.CHARACTER_SET_ENCODING,
							messgeCtx
									.getProperty(MessageContext.CHARACTER_SET_ENCODING));

			newmsgCtx.setDoingREST(messgeCtx.isDoingREST());
			newmsgCtx.setDoingMTOM(messgeCtx.isDoingMTOM());
			newmsgCtx.setServerSide(messgeCtx.isServerSide());

			invokeBusinessLogic(messgeCtx, newmsgCtx);

			AxisEngine engine = new AxisEngine(messgeCtx.getOperationContext()
					.getServiceContext().getEngineContext());
			engine.send(newmsgCtx);
		} else {
			//TODO: Check weather terminate
			  
			//TODO: Do terminate processing
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

}