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

package org.apache.sandesha2.msgprocessors;

import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.MessageNumber;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;

import java.util.Iterator;

public class CreateSeqResponseMsgProcessor implements MsgProcessor {
	public void processMessage(RMMsgContext createSeqResponseRMMsgCtx)
			throws SandeshaException {

		System.out.println("IN CREATE SEQ RESPONSE PROCESSOR");

		CreateSequenceResponse createSeqResponsePart = (CreateSequenceResponse) createSeqResponseRMMsgCtx
				.getMessagePart(Constants.MessageParts.CREATE_SEQ_RESPONSE);
		if (createSeqResponsePart == null)
			throw new SandeshaException("Create Sequence Response part is null");

		String newOutSequenceId = createSeqResponsePart.getIdentifier()
				.getIdentifier();
		if (newOutSequenceId == null)
			throw new SandeshaException("New sequence Id is null");

		ConfigurationContext configCtx = createSeqResponseRMMsgCtx
				.getMessageContext().getSystemContext();
		String createSeqMsgId = createSeqResponseRMMsgCtx.getMessageContext()
				.getRelatesTo().getValue();
		RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
				.getInstance(configCtx).getRetransmitterBeanMgr();
		CreateSeqBeanMgr createSeqMgr = AbstractBeanMgrFactory.getInstance(
				configCtx).getCreateSeqBeanMgr();

		CreateSeqBean createSeqBean = createSeqMgr.retrieve(createSeqMsgId);
		if (createSeqBean == null)
			throw new SandeshaException("Create Sequence entry is not found");

		String incomingSequenceId = createSeqBean.getTempSequenceId();
		if (incomingSequenceId == null || "".equals(incomingSequenceId))
			throw new SandeshaException("Incoming sequence ID has is not set");

		//deleting the create sequence entry.
		retransmitterMgr.delete(createSeqMsgId);

		//storing new out sequence id
		SequencePropertyBeanMgr sequencePropMgr = AbstractBeanMgrFactory
				.getInstance(configCtx).getSequencePropretyBeanMgr();
		SequencePropertyBean outSequenceBean = new SequencePropertyBean(
				incomingSequenceId,
				Constants.SequenceProperties.OUT_SEQUENCE_ID, newOutSequenceId);
		sequencePropMgr.insert(outSequenceBean);
		
		RetransmitterBean target = new RetransmitterBean();
		target.setTempSequenceId(incomingSequenceId);

		Iterator iterator = retransmitterMgr.find(target).iterator();
		while (iterator.hasNext()) {
			RetransmitterBean tempBean = (RetransmitterBean) iterator.next();

			//updating the application message
			String key = tempBean.getKey();
			MessageContext applicationMsg = SandeshaUtil
					.getStoredMessageContext(key);
			RMMsgContext applicaionRMMsg = MsgInitializer
					.initializeMessage(applicationMsg);

			Sequence sequence = new Sequence();
			Identifier identifier = new Identifier ();
			identifier.setIndentifer(newOutSequenceId);
			sequence.setIdentifier(identifier);
			
			//FIXME set a correct message number.
			MessageNumber msgNumber = new MessageNumber ();
			msgNumber.setMessageNumber(1);
			sequence.setMessageNumber(msgNumber);
			applicaionRMMsg.setMessagePart(Constants.MessageParts.SEQUENCE,sequence);

			//asking to send the application msssage
			tempBean.setSend(true);
			retransmitterMgr.update(tempBean);
		}
	}
}