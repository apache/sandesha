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
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2ClientAPI;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.TerminateManager;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.util.MessageRetransmissionAdjuster;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * This is responsible for sending and re-sending messages of Sandesha2. This
 * represent a thread that keep running all the time. This keep looking at the
 * Sender table to find out any entries that should be sent.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

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

		StorageManager storageManager = null;

		try {
			storageManager = SandeshaUtil.getSandeshaStorageManager(context);
		} catch (SandeshaException e2) {
			// TODO Auto-generated catch block
			System.out.println("ERROR: Could not start sender");
			e2.printStackTrace();
			return;
		}

		while (senderStarted) {
			try {
				if (context == null)
					throw new SandeshaException(
							"Can't continue the Sender. Context is null");

				Transaction pickMessagesToSendTransaction = storageManager.getTransaction(); //starting
																			   // a
																			   // new
																			   // transaction

				SenderBeanMgr mgr = storageManager.getRetransmitterBeanMgr();
				Collection coll = mgr.findMsgsToSend();

				pickMessagesToSendTransaction.commit();
				
				Iterator iter = coll.iterator();

				while (iter.hasNext()) {

					SenderBean bean = (SenderBean) iter.next();
					String key = (String) bean.getMessageContextRefKey();
					MessageContext msgCtx = SandeshaUtil
							.getStoredMessageContext(key);

					try {

						if (msgCtx == null) {
							System.out
									.println("ERROR: Sender has an Unavailable Message entry");
							break;
						}
						RMMsgContext rmMsgCtx = MsgInitializer
								.initializeMessage(msgCtx);

						updateMessage(msgCtx);

						ServiceContext serviceContext = msgCtx
								.getServiceContext();
						Object debug = null;
						if (serviceContext != null) {
							debug = msgCtx
									.getProperty(Sandesha2ClientAPI.SANDESHA_DEBUG_MODE);
							if (debug != null && "on".equals(debug)) {
								System.out
										.println("DEBUG: Sender is sending a '"
												+ SandeshaUtil
														.getMessageTypeString(rmMsgCtx
																.getMessageType())
												+ "' message.");
							}
						}
						
						Transaction preSendTransaction = storageManager.getTransaction();

						int messageType = rmMsgCtx.getMessageType();
						
						if (messageType == Sandesha2Constants.MessageTypes.APPLICATION) {
							
							Sequence sequence = (Sequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
							String sequenceID = sequence.getIdentifier().getIdentifier();
							//checking weather the sequence has been timed out.
							boolean sequenceTimedOut = SequenceManager.hasSequenceTimedOut (sequenceID, rmMsgCtx);;
							if (sequenceTimedOut) {
								//sequence has been timed out.
								//do time out processing.
								
								TerminateManager.terminateSendingSide(context,sequenceID);
								throw new SandeshaException ("Sequence timed out");
							}
							
							//piggybacking if an ack if available for the same
							// sequence.
							AcknowledgementManager
									.piggybackAckIfPresent(rmMsgCtx);
							
						}
						
						preSendTransaction.commit();

						try {
							
							AxisEngine engine = new AxisEngine(msgCtx
									.getConfigurationContext());
							engine.send(msgCtx);
							//							if (msgCtx.isPaused())
							//								engine.resumeSend(msgCtx);
							//							else
							//								engine.send(msgCtx);

						} catch (Exception e) {
							//Exception is sending. retry later
							System.out
									.println("Exception thrown in sending...");
							e.printStackTrace();
							//e.printStackTrace();

						}
						
						Transaction postSendTransaction = storageManager.getTransaction();

						MessageRetransmissionAdjuster retransmitterAdjuster = new MessageRetransmissionAdjuster();

						if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.APPLICATION) {
							Sequence sequence = (Sequence) rmMsgCtx
									.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
							long messageNo = sequence.getMessageNumber()
									.getMessageNumber();
						}

						retransmitterAdjuster.adjustRetransmittion(bean);

						//update or delete only if the object is still present.
						SenderBean bean1 = mgr.retrieve(bean.getMessageID());
						if (bean1 != null) {
							if (bean.isReSend())
								mgr.update(bean);
							else
								mgr.delete(bean.getMessageID());
						}

						postSendTransaction.commit(); //commiting the current
												  // transaction

						Transaction processResponseTransaction =
						storageManager.getTransaction();
						if (!msgCtx.isServerSide())
							checkForSyncResponses(msgCtx);
												
						processResponseTransaction.commit();

						Transaction terminateCleaningTransaction = storageManager
								.getTransaction();
						if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.TERMINATE_SEQ) {
							//terminate sending side.
							TerminateSequence terminateSequence = (TerminateSequence) rmMsgCtx
									.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
							String sequenceID = terminateSequence
									.getIdentifier().getIdentifier();
							ConfigurationContext configContext = msgCtx
									.getConfigurationContext();

							TerminateManager.terminateSendingSide(
									configContext, sequenceID);
						}

						terminateCleaningTransaction.commit();

					} catch (AxisFault e1) {
						e1.printStackTrace();
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}

			} catch (SandeshaException e) {
				e.printStackTrace();
				return;
			}

			try {
				Thread.sleep(Sandesha2Constants.SENDER_SLEEP_TIME);
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

		if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.CREATE_SEQ) {
			responseExpected = true;
		}
		if (rmMsgCtx.getMessageType() == Sandesha2Constants.MessageTypes.APPLICATION) {
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

	private void checkForSyncResponses(MessageContext msgCtx) {

		try {
			boolean responsePresent = (msgCtx
					.getProperty(MessageContext.TRANSPORT_IN) != null);

			if (responsePresent) {
				//create the response
				MessageContext response = new MessageContext(msgCtx
						.getConfigurationContext(), msgCtx.getSessionContext(),
						msgCtx.getTransportIn(), msgCtx.getTransportOut());
				response.setProperty(MessageContext.TRANSPORT_IN, msgCtx
						.getProperty(MessageContext.TRANSPORT_IN));

				response.setServerSide(false);

				//If request is REST we assume the response is REST, so set the
				// variable
				response.setDoingREST(msgCtx.isDoingREST());
				response.setServiceGroupContextId(msgCtx
						.getServiceGroupContextId());
				response
						.setServiceGroupContext(msgCtx.getServiceGroupContext());
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

				//Changed following from TransportUtils to SandeshaUtil since
				// op.
				// context is anavailable.
				SOAPEnvelope resenvelope = null;
				resenvelope = SandeshaUtil.createSOAPMessage(response, msgCtx
						.getEnvelope().getNamespace().getName());

				if (resenvelope != null) {
					AxisEngine engine = new AxisEngine(msgCtx
							.getConfigurationContext());
					response.setEnvelope(resenvelope);
					engine.receive(response);
				}
			}

		} catch (Exception e) {
			System.out
					.println("Exception was throws in processing the sync response...");
		}
	}

}