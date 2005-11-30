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

import java.util.Collection;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
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

	/**
	 * Piggybacks any available acks of the same sequence to the given application message.
	 * 
	 * @param applicationRMMsgContext
	 * @throws SandeshaException
	 */
	public static void piggybackAckIfPresent(
			RMMsgContext applicationRMMsgContext) throws SandeshaException {
		ConfigurationContext configurationContext = applicationRMMsgContext
				.getMessageContext().getSystemContext();
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(configurationContext);
		SenderBeanMgr retransmitterBeanMgr = storageManager
				.getRetransmitterBeanMgr();
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager
				.getSequencePropretyBeanMgr();

		SenderBean findBean = new SenderBean();

		Sequence sequence = (Sequence) applicationRMMsgContext
				.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		if (sequence == null)
			throw new SandeshaException(
					"Application message does not contain a sequence part");

		String sequenceId = sequence.getIdentifier().getIdentifier();

		SequencePropertyBean internalSequenceBean = sequencePropertyBeanMgr
				.retrieve(sequenceId,
						Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		if (internalSequenceBean == null)
			throw new SandeshaException("Temp Sequence is not set");

		String internalSequenceId = (String) internalSequenceBean.getValue();
		findBean.setInternalSequenceId(internalSequenceId);
		findBean.setMessagetype(Sandesha2Constants.MessageTypes.ACK);

		Collection collection = retransmitterBeanMgr.find(findBean);
		Iterator it = collection.iterator();

		if (it.hasNext()) {
			SenderBean ackBean = (SenderBean) it.next();

			//deleting the ack entry.
			retransmitterBeanMgr.delete(ackBean.getMessageId());

			//Adding the ack to the application message
			MessageContext ackMsgContext = SandeshaUtil
					.getStoredMessageContext(ackBean.getKey());
			RMMsgContext ackRMMsgContext = MsgInitializer
					.initializeMessage(ackMsgContext);
			if (ackRMMsgContext.getMessageType() != Sandesha2Constants.MessageTypes.ACK)
				throw new SandeshaException("Invalid ack message entry");

			SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) ackRMMsgContext
					.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
			applicationRMMsgContext.setMessagePart(
					Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT,
					sequenceAcknowledgement);

			applicationRMMsgContext.addSOAPEnvelope();
		}

	}
}