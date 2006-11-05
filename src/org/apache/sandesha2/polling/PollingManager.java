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

package org.apache.sandesha2.polling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * This class is responsible for sending MakeConnection requests. This is a seperate thread that
 * keeps running. Will do MakeConnection based on the request queue or randomly.
 */
public class PollingManager extends Thread {

	private ConfigurationContext configurationContext = null;
	private StorageManager storageManager = null;
	private boolean poll = false;
	/**
	 * By adding an entry to this, the PollingManager will be asked to do a polling request on this sequence.
	 */
	private HashMap sheduledPollingRequests = null;
	
	private final int POLLING_MANAGER_WAIT_TIME = 3000;
	
	public void run() {
		
		
		while (isPoll()) {
			
			try {
				
				NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();
				
				//geting the sequences to be polled.
				//if shedule contains any requests, do the earliest one.
				//else pick one randomly.
				
				String sequenceId = getNextSheduleEntry ();

				NextMsgBean nextMsgBean = null;
				
				if (sequenceId==null) {
					
					NextMsgBean findBean = new NextMsgBean ();
					findBean.setPollingMode(true);
					
					List results = nextMsgMgr.find(findBean);
					int size = results.size();
					if (size>0) {
						Random random = new Random ();
						int item = random.nextInt(size);
						nextMsgBean = (NextMsgBean) results.get(item);
					}
					
					
					
				} else {
					NextMsgBean findBean = new NextMsgBean ();
					findBean.setPollingMode(true);
					findBean.setSequenceID(sequenceId);
					
					nextMsgBean = nextMsgMgr.findUnique(findBean);
				}
				
				//If not valid entry is found, try again later.
				if (nextMsgBean==null)
					continue;

				sequenceId = nextMsgBean.getSequenceID();
				
				//create a MakeConnection message  
				String referenceMsgKey = nextMsgBean.getReferenceMessageKey();
				
				String sequencePropertyKey = sequenceId;
				String replyTo = SandeshaUtil.getSequenceProperty(sequencePropertyKey,
						Sandesha2Constants.SequenceProperties.REPLY_TO_EPR,storageManager);
				String WSRMAnonReplyToURI = null;
				if (SandeshaUtil.isWSRMAnonymousReplyTo(replyTo))
					WSRMAnonReplyToURI = replyTo;
				
				MessageContext referenceMessage = storageManager.retrieveMessageContext(referenceMsgKey,configurationContext);
				RMMsgContext referenceRMMessage = MsgInitializer.initializeMessage(referenceMessage);
				RMMsgContext makeConnectionRMMessage = RMMsgCreator.createMakeConnectionMessage(referenceRMMessage,
						sequenceId , WSRMAnonReplyToURI,storageManager);
				
				makeConnectionRMMessage.setProperty(MessageContext.TRANSPORT_IN,null);
				//storing the MakeConnection message.
				String makeConnectionMsgStoreKey = SandeshaUtil.getUUID();
				
				makeConnectionRMMessage.setProperty(Sandesha2Constants.MessageContextProperties.SEQUENCE_PROPERTY_KEY,
						sequencePropertyKey);
				
				storageManager.storeMessageContext(makeConnectionMsgStoreKey,makeConnectionRMMessage.getMessageContext());
				
				//add an entry for the MakeConnection message to the sender (with ,send=true, resend=false)
				SenderBean makeConnectionSenderBean = new SenderBean ();
//				makeConnectionSenderBean.setInternalSequenceID(internalSequenceId);
				makeConnectionSenderBean.setMessageContextRefKey(makeConnectionMsgStoreKey);
				makeConnectionSenderBean.setMessageID(makeConnectionRMMessage.getMessageId());
				makeConnectionSenderBean.setMessageType(Sandesha2Constants.MessageTypes.MAKE_CONNECTION_MSG);
				makeConnectionSenderBean.setReSend(false);
				makeConnectionSenderBean.setSend(true);
				makeConnectionSenderBean.setSequenceID(sequenceId);
				EndpointReference to = makeConnectionRMMessage.getTo();
				if (to!=null)
					makeConnectionSenderBean.setToAddress(to.getAddress());

				SenderBeanMgr senderBeanMgr = storageManager.getRetransmitterBeanMgr();
				
				//this message should not be sent until it is qualified. I.e. till it is sent through the Sandesha2TransportSender.
				makeConnectionRMMessage.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING, Sandesha2Constants.VALUE_FALSE);
				
				senderBeanMgr.insert(makeConnectionSenderBean);
				
				SandeshaUtil.executeAndStore(makeConnectionRMMessage, makeConnectionMsgStoreKey);
			} catch (SandeshaStorageException e) {
				e.printStackTrace();
			} catch (SandeshaException e) {
				e.printStackTrace();
			} catch (AxisFault e) {
				e.printStackTrace();
			} finally {
				try {
					Thread.sleep(POLLING_MANAGER_WAIT_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}
	
	private synchronized String getNextSheduleEntry () {
		String sequenceId = null;
		
		if (sheduledPollingRequests.size()>0) {
			sequenceId = (String) sheduledPollingRequests.keySet().iterator().next();
			Integer sequencEntryCount = (Integer) sheduledPollingRequests.get(sequenceId);
			
			Integer leftCount = new Integer (sequencEntryCount.intValue() -1 );
			if (leftCount.intValue()==0) 
				sheduledPollingRequests.remove(sequenceId);
			
		}
		
		return sequenceId;
	}
	
	/**
	 * Starts the PollingManager.
	 * 
	 * @param configurationContext
	 * @throws SandeshaException
	 */
	public synchronized void start (ConfigurationContext configurationContext) throws SandeshaException {
		this.configurationContext = configurationContext;
		this.sheduledPollingRequests = new HashMap ();
		this.storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext,configurationContext.getAxisConfiguration());
		setPoll(true);
		super.start();
	}
	
	/**
	 * Asks the PollingManager to stop its work.
	 *
	 */
	public synchronized void stopPolling () {
		setPoll(false);
	}
	
	public synchronized void setPoll (boolean poll) {
		this.poll = poll;
	}
	
	public synchronized boolean isPoll () {
		return poll;
	}
	
	public void start () {
		throw new UnsupportedOperationException ("You must use the oveerloaded start method");
	}
	
	/**
	 * Asking the polling manager to do a polling request on the sequence identified by the
	 * given InternalSequenceId.
	 * 
	 * @param sequenceId
	 */
	public synchronized void shedulePollingRequest (String sequenceId) {
		
		System.out.println("Polling request sheduled for sequence:" + sequenceId);
		
		if (sheduledPollingRequests.containsKey (sequenceId)) {
			Integer sequenceEntryCount = (Integer) sheduledPollingRequests.get(sequenceId);
			Integer newCount = new Integer (sequenceEntryCount.intValue()+1);
			sheduledPollingRequests.put(sequenceId,newCount);
		} else {
			Integer sequenceEntryCount = new Integer (1);
			sheduledPollingRequests.put(sequenceId, sequenceEntryCount);
		}
		
	}

	
}
