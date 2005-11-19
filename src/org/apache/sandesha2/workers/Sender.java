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
package org.apache.sandesha2.workers;

import java.util.Collection;
import java.util.Iterator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.util.MessageRetransmissionAdjuster;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;

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
			try {
				if (context == null)
					throw new SandeshaException(
							"Can't continue the Sender. Context is null");

				StorageManager storageManager = SandeshaUtil
						.getSandeshaStorageManager(context);

				RetransmitterBeanMgr mgr = storageManager
						.getRetransmitterBeanMgr();
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

						Object debug = context
								.getProperty(Constants.SANDESHA_DEBUG_MODE);
						if (debug != null && "on".equals(debug)) {
							System.out.println("DEBUG: Sender is sending a '"
									+ SandeshaUtil
											.getMessageTypeString(rmMsgCtx
													.getMessageType())
									+ "' message.");
						}
						
						try {
							new AxisEngine(context).send(msgCtx);
						}catch (Exception e) {
							//Exception is sending. retry later
							System.out.println("Exception thrown in sending...");
							e.printStackTrace();
						}
						
						MessageRetransmissionAdjuster  retransmitterAdjuster = new MessageRetransmissionAdjuster ();
						retransmitterAdjuster.adjustRetransmittion(bean);
						
						mgr.update(bean);
						
						if (!msgCtx.isServerSide())
							checkForSyncResponses(msgCtx);

					} catch (AxisFault e1) {
						e1.printStackTrace();
					} catch (Exception e3) {
						e3.printStackTrace();
					}

					//changing the values of the sent bean.
					//bean.setLastSentTime(System.currentTimeMillis());
					//bean.setSentCount(bean.getSentCount() + 1);

					//update if resend=true otherwise delete. (reSend=false
					// means
					// send only once).
					if (bean.isReSend())
						mgr.update(bean);
					else
						mgr.delete(bean.getMessageId());

				}
			} catch (SandeshaException e) {
				e.printStackTrace();
				return;
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				//e1.printStackTrace();
				System.out.println("Sender was interupted...");
				e1.printStackTrace();
				System.out.println("End printing Interrupt...");
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
			if (rmMsgCtx.getMessageContext().getAxisOperation()
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
			response
					.setServiceGroupContextId(msgCtx.getServiceGroupContextId());
			response.setServiceGroupContext(msgCtx.getServiceGroupContext());
			response.setServiceContext(msgCtx.getServiceContext());
			response.setAxisService(msgCtx.getAxisService());
			response.setAxisServiceGroup(msgCtx.getAxisServiceGroup());

			//setting the in-flow.
			//ArrayList inPhaseHandlers =
			// response.getAxisOperation().getRemainingPhasesInFlow();
			/*
			 * if (inPhaseHandlers==null || inPhaseHandlers.isEmpty()) {
			 * ArrayList phases =
			 * msgCtx.getSystemContext().getAxisConfiguration().getInPhasesUptoAndIncludingPostDispatch();
			 * response.getAxisOperation().setRemainingPhasesInFlow(phases); }
			 */

			//Changed following from TransportUtils to SandeshaUtil since op.
			// context is anavailable.
			SOAPEnvelope resenvelope = null;
			try {
				resenvelope = SandeshaUtil.createSOAPMessage(response, msgCtx
						.getEnvelope().getNamespace().getName());
			} catch (AxisFault e) {
				//TODO: change to log.debug
				e.printStackTrace();
			}

			if (resenvelope != null) {
				AxisEngine engine = new AxisEngine(msgCtx.getSystemContext());
				response.setEnvelope(resenvelope);
				engine.receive(response);
			}
		}
	}

}