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

import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.soap.SOAPFactory;

import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * Responsible for processing an incoming Create Sequence Response message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class CreateSeqResponseMsgProcessor implements MsgProcessor {
	public void processMessage(RMMsgContext createSeqResponseRMMsgCtx)
			throws SandeshaException {

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(createSeqResponseRMMsgCtx.getSOAPEnvelope()));

		ConfigurationContext configCtx = createSeqResponseRMMsgCtx
			.getMessageContext().getConfigurationContext();		
		StorageManager storageManager = SandeshaUtil
			.getSandeshaStorageManager(configCtx);
		//Processing for ack if any
		
		Transaction ackProcessTransaction = storageManager.getTransaction();
		
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) createSeqResponseRMMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck != null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor();
			ackProcessor.processMessage(createSeqResponseRMMsgCtx);
		}

		ackProcessTransaction.commit();
		
		
		//Processing the create sequence response.
		
		Transaction createSeqResponseTransaction = storageManager.getTransaction();
		
		CreateSequenceResponse createSeqResponsePart = (CreateSequenceResponse) createSeqResponseRMMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE);
		if (createSeqResponsePart == null)
			throw new SandeshaException("Create Sequence Response part is null");

		String newOutSequenceId = createSeqResponsePart.getIdentifier()
				.getIdentifier();
		if (newOutSequenceId == null)
			throw new SandeshaException("New sequence Id is null");

		String createSeqMsgId = createSeqResponseRMMsgCtx.getMessageContext()
				.getRelatesTo().getValue();



		SenderBeanMgr retransmitterMgr = storageManager
				.getRetransmitterBeanMgr();
		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();

		CreateSeqBean createSeqBean = createSeqMgr.retrieve(createSeqMsgId);
		if (createSeqBean == null)
			throw new SandeshaException("Create Sequence entry is not found");

		String internalSequenceId = createSeqBean.getInternalSequenceID();
		if (internalSequenceId == null || "".equals(internalSequenceId))
			throw new SandeshaException("TempSequenceId has is not set");

		//deleting the create sequence entry.
		retransmitterMgr.delete(createSeqMsgId);

		//storing new out sequence id
		SequencePropertyBeanMgr sequencePropMgr = storageManager
				.getSequencePropretyBeanMgr();
		SequencePropertyBean outSequenceBean = new SequencePropertyBean(
				internalSequenceId, Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID,
				newOutSequenceId);
		SequencePropertyBean internalSequenceBean = new SequencePropertyBean(
				newOutSequenceId,
				Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID, internalSequenceId);
		
		
		sequencePropMgr.insert(outSequenceBean);
		sequencePropMgr.insert(internalSequenceBean);

		createSeqResponseTransaction.commit();
		
		
		Transaction offerProcessTransaction = storageManager.getTransaction();
		
		//processing for accept (offer has been sent)
		Accept accept = createSeqResponsePart.getAccept();
		if (accept != null) {
			//Find offered sequence from internal sequence id.
			SequencePropertyBean offeredSequenceBean = sequencePropMgr
					.retrieve(internalSequenceId,
							Sandesha2Constants.SequenceProperties.OFFERED_SEQUENCE);

			//TODO this should be detected in the Fault manager.
			if (offeredSequenceBean == null)
				throw new SandeshaException(
						"No offered sequence. But an accept was received");

			String offeredSequenceId = (String) offeredSequenceBean.getValue();

			EndpointReference acksToEPR = accept.getAcksTo().getAddress()
					.getEpr();
			SequencePropertyBean acksToBean = new SequencePropertyBean();
			acksToBean.setName(Sandesha2Constants.SequenceProperties.ACKS_TO_EPR);
			acksToBean.setSequenceID(offeredSequenceId);
			acksToBean.setValue(acksToEPR.getAddress());

			sequencePropMgr.insert(acksToBean);

			NextMsgBean nextMsgBean = new NextMsgBean();
			nextMsgBean.setSequenceID(offeredSequenceId);
			nextMsgBean.setNextMsgNoToProcess(1);

			NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();
			nextMsgMgr.insert(nextMsgBean);
		}

		offerProcessTransaction.commit();
		
		
		Transaction updateAppMessagesTransaction = storageManager.getTransaction();
		
		SenderBean target = new SenderBean();
		target.setInternalSequenceID(internalSequenceId);
		target.setSend(false);
		target.setReSend(true);

		Iterator iterator = retransmitterMgr.find(target).iterator();
		while (iterator.hasNext()) {
			SenderBean tempBean = (SenderBean) iterator.next();

			//updating the application message
			String key = tempBean.getMessageContextRefKey();
			MessageContext applicationMsg = SandeshaUtil
					.getStoredMessageContext(key);

			RMMsgContext applicaionRMMsg = MsgInitializer
					.initializeMessage(applicationMsg);

			Sequence sequencePart = (Sequence) applicaionRMMsg
					.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
			if (sequencePart == null)
				throw new SandeshaException("Sequence part is null");

			Identifier identifier = new Identifier(factory);
			identifier.setIndentifer(newOutSequenceId);

			sequencePart.setIdentifier(identifier);

			AckRequested ackRequestedPart = (AckRequested) applicaionRMMsg
					.getMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST);
			if (ackRequestedPart != null) {
				Identifier id1 = new Identifier(factory);
				id1.setIndentifer(newOutSequenceId);
				ackRequestedPart.setIdentifier(id1);
			}

			try {
				applicaionRMMsg.addSOAPEnvelope();
			} catch (AxisFault e) {
				throw new SandeshaException(e.getMessage());
			}

			//asking to send the application msssage
			tempBean.setSend(true);
			retransmitterMgr.update(tempBean);
		}

		updateAppMessagesTransaction.commit();
		
		createSeqResponseRMMsgCtx.getMessageContext().getOperationContext()
				.setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN,
						"false");

		//createSeqResponseRMMsgCtx.getMessageContext().pause();
		createSeqResponseRMMsgCtx.getMessageContext().setPausedTrue(new QName (Sandesha2Constants.IN_HANDLER_NAME));
	}
}