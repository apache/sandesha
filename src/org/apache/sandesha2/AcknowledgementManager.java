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

package org.apache.sandesha2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Contains logic for managing acknowledgements.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class AcknowledgementManager {

	private static Log log = LogFactory.getLog(AcknowledgementManager.class);
	
	/**
	 * Piggybacks any available acks of the same sequence to the given
	 * application message.
	 * 
	 * @param applicationRMMsgContext
	 * @throws SandeshaException
	 */
	public static void piggybackAckIfPresent(
			RMMsgContext applicationRMMsgContext) throws SandeshaException {
		ConfigurationContext configurationContext = applicationRMMsgContext
				.getMessageContext().getConfigurationContext();
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(configurationContext);

		SenderBeanMgr retransmitterBeanMgr = storageManager
				.getRetransmitterBeanMgr();
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager
				.getSequencePropretyBeanMgr();

		SenderBean findBean = new SenderBean();

		Sequence sequence = (Sequence) applicationRMMsgContext
				.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		if (sequence == null) {
			String message = "Application message does not contain a sequence part";
			log.debug(message);
			throw new SandeshaException(message);
		}

		String sequenceId = sequence.getIdentifier().getIdentifier();

		SequencePropertyBean internalSequenceBean = sequencePropertyBeanMgr
				.retrieve(
						sequenceId,
						Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		if (internalSequenceBean == null) {
			String message = "Temp Sequence is not set";
			log.debug(message);
			throw new SandeshaException(message);
		}

		String internalSequenceId = (String) internalSequenceBean.getValue();
		findBean.setInternalSequenceID(internalSequenceId);
		findBean.setMessageType(Sandesha2Constants.MessageTypes.ACK);
		findBean.setSend(true);
		findBean.setReSend(false);
		Collection collection = retransmitterBeanMgr.find(findBean);
		Iterator it = collection.iterator();

		if (it.hasNext()) {

			SenderBean ackBean = (SenderBean) it.next();

			long timeNow = System.currentTimeMillis();
			if (ackBean.getTimeToSend() > timeNow) { 
				//Piggybacking will happen only if the end of ack interval (timeToSend) is not reached.

				//deleting the ack entry.
				retransmitterBeanMgr.delete(ackBean.getMessageID());

				//Adding the ack to the application message
				MessageContext ackMsgContext = storageManager
						.retrieveMessageContext(ackBean
								.getMessageContextRefKey(),configurationContext);
				RMMsgContext ackRMMsgContext = MsgInitializer
						.initializeMessage(ackMsgContext);
				if (ackRMMsgContext.getMessageType() != Sandesha2Constants.MessageTypes.ACK) {
					String message = "Invalid ack message entry";
					log.debug(message);
					throw new SandeshaException(message);
				}

				SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) ackRMMsgContext
						.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
				applicationRMMsgContext.setMessagePart(
						Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT,
						sequenceAcknowledgement);

				applicationRMMsgContext.addSOAPEnvelope();
			}
		}
	}
	
	/**this is used to get the acked messages of a sequence. If this is an outgoing message the sequenceIdentifier should
	 * be the internal sequenceID.
	 * 
	 * @param sequenceIdentifier
	 * @param outGoingMessage
	 * @return
	 */
	public static ArrayList getCompletedMessagesList (String sequenceIdentifier,ConfigurationContext configurationContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		SequencePropertyBean completedMessagesBean = sequencePropertyBeanMgr.retrieve(sequenceIdentifier,Sandesha2Constants.SequenceProperties.COMPLETED_MESSAGES);
		
		ArrayList completedMsgList = null;
		if (completedMessagesBean!=null) {
			completedMsgList = SandeshaUtil.getArrayListFromString(completedMessagesBean.getValue());
		}
		
		return completedMsgList;
	}
	
	public static void sendSyncAck () {
		
	}
	
	public static void sendAsyncAck () {
		
	}
}