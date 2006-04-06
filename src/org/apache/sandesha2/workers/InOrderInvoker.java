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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.InvokerBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.TerminateManager;
import org.apache.sandesha2.wsrm.Sequence;

/**
 * This is used when InOrder invocation is required. This is a seperated Thread that keep running
 * all the time. At each iteration it checks the InvokerTable to find weather there are any messages to
 * me invoked.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class InOrderInvoker extends Thread {
	
	private boolean runInvoker = false;
	private ArrayList workingSequences = new ArrayList();
	private ConfigurationContext context = null;
	private Log log = LogFactory.getLog(getClass());
	
	public synchronized void stopInvokerForTheSequence(String sequenceID) {
		workingSequences.remove(sequenceID);
		if (workingSequences.size()==0) {
			//runInvoker = false;
		}
	}

	public synchronized boolean isInvokerStarted() {
		return runInvoker;
	}

	public void setConfugurationContext(ConfigurationContext context) {
		this.context = context;
	}

	public synchronized void runInvokerForTheSequence(ConfigurationContext context, String sequenceID) {
		
		if (!workingSequences.contains(sequenceID))
			workingSequences.add(sequenceID);

		if (!isInvokerStarted()) {
			this.context = context;
			runInvoker = true;     //so that isSenderStarted()=true.
			super.start();
		}
	}

	public void run() {

		while (runInvoker) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				log.debug("Invoker was Inturrepted....");
				log.debug(ex.getMessage());
			}

			try {
				StorageManager storageManager = SandeshaUtil
						.getSandeshaStorageManager(context);
				NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();

				InvokerBeanMgr storageMapMgr = storageManager
						.getStorageMapBeanMgr();

				SequencePropertyBeanMgr sequencePropMgr = storageManager
						.getSequencePropretyBeanMgr();

				Transaction preInvocationTransaction = storageManager.getTransaction();
				
				//Getting the incomingSequenceIdList
				SequencePropertyBean allSequencesBean = (SequencePropertyBean) sequencePropMgr
						.retrieve(
								Sandesha2Constants.SequenceProperties.ALL_SEQUENCES,
								Sandesha2Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);
				if (allSequencesBean == null)
					continue;

				ArrayList allSequencesList = SandeshaUtil.getArrayListFromString (allSequencesBean
						.getValue());
				
				preInvocationTransaction.commit();
				
				Iterator allSequencesItr = allSequencesList.iterator();

				currentIteration: while (allSequencesItr.hasNext()) {

					String sequenceId = (String) allSequencesItr.next();
					
					Transaction invocationTransaction = storageManager.getTransaction();   //Transaction based invocation
					
					NextMsgBean nextMsgBean = nextMsgMgr.retrieve(sequenceId);
					if (nextMsgBean == null) {

						String message = "Next message not set correctly. Removing invalid entry.";
						log.debug(message);
						allSequencesItr.remove();
						
						//cleaning the invalid data of the all sequences.
						allSequencesBean.setValue(allSequencesList.toString());
						sequencePropMgr.update(allSequencesBean);	
						
						throw new SandeshaException (message);
					}

					long nextMsgno = nextMsgBean.getNextMsgNoToProcess();
					if (nextMsgno <= 0) { 
						String message = "Invalid messaage number as the Next Message Number. Removing invalid entry";
						
						throw new SandeshaException(message);
					}

					Iterator stMapIt = storageMapMgr.find(
							new InvokerBean(null, nextMsgno, sequenceId))
							.iterator();
					
					boolean invoked = false;
					
					while (stMapIt.hasNext()) {

						InvokerBean stMapBean = (InvokerBean) stMapIt
								.next();
						String key = stMapBean.getMessageContextRefKey();


						MessageContext msgToInvoke = storageManager.retrieveMessageContext(key,context);

						RMMsgContext rmMsg = MsgInitializer
								.initializeMessage(msgToInvoke);
						Sequence seq = (Sequence) rmMsg
								.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);

						long msgNo = seq.getMessageNumber().getMessageNumber();

						try {
							//Invoking the message.

							//currently Transaction based invocation can be supplied only for the in-only case.
							
							if (!AxisOperationFactory.MEP_URI_IN_ONLY.equals(msgToInvoke.getAxisOperation().getMessageExchangePattern())) {
								invocationTransaction.commit();
							}
							
							new AxisEngine (msgToInvoke.getConfigurationContext())
									.resume(msgToInvoke);
							invoked = true;
							
							if (!AxisOperationFactory.MEP_URI_IN_ONLY.equals(msgToInvoke.getAxisOperation().getMessageExchangePattern())) {
								invocationTransaction = storageManager.getTransaction();
							}						

							storageMapMgr.delete(key);
						} catch (AxisFault e) {
							throw new SandeshaException(e);
						}

						//Transaction postInvocationTransaction = storageManager.getTransaction();
						//undating the next msg to invoke


						//terminate (AfterInvocation)
						if (rmMsg.getMessageType() == Sandesha2Constants.MessageTypes.APPLICATION) {
							Sequence sequence = (Sequence) rmMsg
									.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
							if (sequence.getLastMessage() != null) {
								
								TerminateManager.cleanReceivingSideAfterInvocation(context, sequenceId);
								
								//this sequence has no more invocations
								stopInvokerForTheSequence(sequenceId);
								
								//exit from current iteration. (since an entry was removed)
								invocationTransaction.commit();
								break currentIteration;
							}
						}
					}

					if (invoked) {
						nextMsgno++;
						nextMsgBean.setNextMsgNoToProcess(nextMsgno);
						nextMsgMgr.update(nextMsgBean);
						invocationTransaction.commit();
					}
				}
				
			} catch (SandeshaException e1) {
				e1.printStackTrace();
			}
		}
	}
}