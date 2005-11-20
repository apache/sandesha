/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */

package org.apache.sandesha2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.CallbackReceiver;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.client.MEPClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.transport.TransportListener;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

public class SandeshaMepClient extends MEPClient {

	//protected long timeOutInMilliSeconds = 2000;

	protected TransportListener listener;

	protected TransportOutDescription senderTransport;

	protected TransportInDescription listenerTransport;

	protected boolean hasAsyncResponse = false;

	protected EndpointReference to;

	protected CallbackReceiver callbackReceiver;

	public SandeshaMepClient(ServiceContext serviceContext) {
		super(serviceContext, WSDLConstants.MEP_URI_OUT_IN);
		//service context has the engine context set in to it !
		callbackReceiver = new CallbackReceiver();
	}

	public MessageContext invokeDual(AxisOperation axisop,
			final MessageContext msgctx) throws AxisFault {

		prepareInvocation(axisop, msgctx);

		String messageID = msgctx.getMessageID();
		if (messageID == null) {
			messageID = String.valueOf(System.currentTimeMillis());
			msgctx.setMessageID(messageID);
		}

		SandeshaCallback callback = new SandeshaCallback();

		if (hasAsyncResponse) {
			checkTransport(msgctx);
			axisop.setMessageReceiver(callbackReceiver);
			callbackReceiver.addCallback(messageID, callback);
			msgctx.setReplyTo(ListenerManager.replyToEPR(serviceContext
					.getAxisService().getName().getLocalPart()
					+ "/" + axisop.getName().getLocalPart(), listenerTransport
					.getName().getLocalPart()));
		}
		
		msgctx.setTo(to);
		msgctx.setServiceContext(serviceContext);
		ConfigurationContext syscontext = serviceContext.getConfigurationContext();
		msgctx.setConfigurationContext(syscontext);

		checkTransport(msgctx);

		OperationContext operationContext = new OperationContext(axisop,
				serviceContext);
		axisop.registerOperationContext(msgctx, operationContext);

		//Send the SOAP Message and receive a response if present.
		MessageContext response = TwoWayOptionalTransportBasedSender.send(
				msgctx, listenerTransport);

		//give a sync response only if present.
		if (response != null) {
			//check for a fault and return the result
			SOAPEnvelope resenvelope = response.getEnvelope();
			if (resenvelope.getBody().hasFault()) {
				SOAPFault soapFault = resenvelope.getBody().getFault();
				Exception ex = soapFault.getException();

				if (isExceptionToBeThrownOnSOAPFault) {
					//does the SOAPFault has a detail element for Excpetion
					if (ex != null) {
						throw new AxisFault(ex);
					} else {
						//if detail element not present create a new Exception
						// from the detail
						String message = "";
						message = message + "Code =" + soapFault.getCode() == null ? ""
								: soapFault.getCode().getValue() == null ? ""
										: soapFault.getCode().getValue()
												.getText();
						message = message + "Reason =" + soapFault.getReason() == null ? ""
								: soapFault.getReason().getSOAPText() == null ? ""
										: soapFault.getReason().getSOAPText()
												.getText();
						throw new AxisFault(message);
					}
				}
			}
		}
		return response;

	}

	public void setTo(EndpointReference to) {
		this.to = to;
	}

	public void setTransportInfo(String senderTransport,
			String listenerTransport, boolean hasAsyncResponse)
			throws AxisFault {
		//here we check for a legal combination, for and example if the
		// sendertransport is http and listner
		//transport is smtp the invocation must using seperate transport
		if (!hasAsyncResponse) {
			boolean isTransportsEqual = senderTransport
					.equals(listenerTransport);
			boolean isATwoWaytransport = Constants.TRANSPORT_HTTP
					.equals(senderTransport)
					|| Constants.TRANSPORT_TCP.equals(senderTransport);
			if ((!isTransportsEqual || !isATwoWaytransport)) {
				throw new AxisFault(Messages
						.getMessage("useSeparateListenerLimited"));
			}
		} else {
			this.hasAsyncResponse = hasAsyncResponse;

		}

		//find and set the transport details
		AxisConfiguration axisConfig = serviceContext.getConfigurationContext()
				.getAxisConfiguration();
		this.listenerTransport = axisConfig.getTransportIn(new QName(
				listenerTransport));
		this.senderTransport = axisConfig.getTransportOut(new QName(
				senderTransport));
		if (this.senderTransport == null) {
			throw new AxisFault(Messages.getMessage("unknownTransport",
					senderTransport));
		}
		if (this.listenerTransport == null) {
			throw new AxisFault(Messages.getMessage("unknownTransport",
					listenerTransport));
		}

		//if seperate transport is used, start the required listeners
		if (hasAsyncResponse) {
			if (!serviceContext.getConfigurationContext().getAxisConfiguration()
					.isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
				throw new AxisFault(Messages
						.getMessage("2channelNeedAddressing"));
			}
			ListenerManager.makeSureStarted(listenerTransport, serviceContext
					.getConfigurationContext());
		}
	}

	private void checkTransport(MessageContext msgctx) throws AxisFault {
		if (senderTransport == null) {
			senderTransport = inferTransport(to);
		}
		if (listenerTransport == null) {
			listenerTransport = serviceContext.getConfigurationContext()
					.getAxisConfiguration().getTransportIn(
							senderTransport.getName());
		}

		if (msgctx.getTransportIn() == null) {
			msgctx.setTransportIn(listenerTransport);
		}
		if (msgctx.getTransportOut() == null) {
			msgctx.setTransportOut(senderTransport);
		}

	}

	public void close() throws AxisFault {
		ListenerManager.stop(listenerTransport.getName().getLocalPart());
	}

}