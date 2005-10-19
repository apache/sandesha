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
package org.apache.sandesha2;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.clientapi.InOutMEPClient;
import org.apache.axis2.clientapi.MessageSender;
import org.apache.axis2.clientapi.TwoWayTransportBasedSender;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.sandesha2.client.SandeshaMepClient;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;

public class Sender extends Thread {

	private boolean senderStarted = false;

	private ConfigurationContext context = null;

	public synchronized void stopSender() {
		senderStarted = false;
	}

	public synchronized boolean isSenderStarted() {
		return senderStarted;
	}

	public void run() {

		while (senderStarted) {
			//System.out.println ("|-|");
			try {
				if (context == null)
					throw new SandeshaException(
							"Can't continue the Sender. Context is null");
			} catch (SandeshaException e) {
				e.printStackTrace();
				return;
			}

			RetransmitterBeanMgr mgr = AbstractBeanMgrFactory.getInstance(
					context).getRetransmitterBeanMgr();
			Collection coll = mgr.findMsgsToSend();
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				RetransmitterBean bean = (RetransmitterBean) iter.next();
				String key = (String) bean.getKey();
				MessageContext msgCtx = SandeshaUtil
						.getStoredMessageContext(key);

				try {
					RMMsgContext rmMsgCtx = MsgInitializer
							.initializeMessage(msgCtx);
					updateMessage(msgCtx);

					new AxisEngine(context).send(msgCtx);
					if (!msgCtx.isServerSide())
						checkForSyncResponses(msgCtx);

				} catch (AxisFault e1) {
					e1.printStackTrace();
				} catch (SandeshaException e2) {
					e2.printStackTrace();
				}

				//changing the values of the sent bean.
				bean.setLastSentTime(System.currentTimeMillis());
				bean.setSentCount(bean.getSentCount() + 1);

				//update if resend=true otherwise delete. (reSend=false means
				// send only once).
				if (bean.isReSend())
					mgr.update(bean);
				else
					mgr.delete(bean.getMessageId());

			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				//e1.printStackTrace();
				System.out.println("Sender was interupted...");
			}
		}

	}

	private boolean isResponseExpected(RMMsgContext rmMsgCtx) {
		boolean responseExpected = false;

		if (rmMsgCtx.getMessageType() == Constants.MessageTypes.CREATE_SEQ) {
			responseExpected = true;
		}
		if (rmMsgCtx.getMessageType() == Constants.MessageTypes.APPLICATION) {
			//a ack may arrive. (not a application response)
			if (rmMsgCtx.getMessageContext().getOperationDescription()
					.getMessageExchangePattern().equals(
							org.apache.wsdl.WSDLConstants.MEP_URI_IN_OUT)) {
				responseExpected = true;
			}
		}

		return true;
	}

	public void start(ConfigurationContext context) {
		senderStarted = true;
		this.context = context;
		super.start();
	}

	private void updateMessage(MessageContext msgCtx1) throws SandeshaException {
		try {
			RMMsgContext rmMsgCtx1 = MsgInitializer.initializeMessage(msgCtx1);
			rmMsgCtx1.addSOAPEnvelope();

		} catch (AxisFault e) {
			throw new SandeshaException("Exception in updating contexts");
		}

	}

	private void checkForSyncResponses(MessageContext msgCtx) throws AxisFault {

		boolean responsePresent = (msgCtx
				.getProperty(MessageContext.TRANSPORT_IN) != null);

		if (responsePresent) {
			//create the response
			MessageContext response = new MessageContext(msgCtx
					.getSystemContext(), msgCtx.getSessionContext(), msgCtx
					.getTransportIn(), msgCtx.getTransportOut());
			response.setProperty(MessageContext.TRANSPORT_IN, msgCtx
					.getProperty(MessageContext.TRANSPORT_IN));

			response.setServerSide(false);

			//If request is REST we assume the response is REST, so set the
			// variable
			response.setDoingREST(msgCtx.isDoingREST());
			response.setServiceGroupContextId(msgCtx.getServiceGroupContextId());
			response.setServiceGroupContext(msgCtx.getServiceGroupContext());
			response.setServiceContext(msgCtx.getServiceContext());
			response.setServiceDescription(msgCtx.getServiceDescription());
			response.setServiceGroupDescription(msgCtx.getServiceGroupDescription());
			
			//Changed following from TransportUtils to SandeshaUtil since op. context is anavailable.
			SOAPEnvelope resenvelope = SandeshaUtil.createSOAPMessage(
					response, msgCtx.getEnvelope().getNamespace().getName());

			if (resenvelope != null) {
				AxisEngine engine = new AxisEngine(msgCtx.getSystemContext());
				response.setEnvelope(resenvelope);
				engine.receive(response);
			} else {
				throw new AxisFault(Messages
						.getMessage("blockInvocationExpectsRes="));
			}
		}
	}

}