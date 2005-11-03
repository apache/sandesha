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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.Utils;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.SequenceOffer;

public class CreateSeqMsgProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext createSeqRMMsg)
			throws SandeshaException {

		MessageContext createSeqMsg = createSeqRMMsg.getMessageContext();
		CreateSequence createSeqPart = (CreateSequence) createSeqRMMsg
				.getMessagePart(Constants.MessageParts.CREATE_SEQ);
		if (createSeqPart == null)
			throw new SandeshaException(
					"No create sequence part is present in the create sequence message");

		MessageContext outMessage = null;
		try {
			outMessage = Utils.createOutMessageContext(createSeqMsg);
		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

		try {
			String newSequenceId = SequenceManager
					.setupNewSequence(createSeqRMMsg);
			ConfigurationContext context = createSeqRMMsg.getMessageContext()
					.getSystemContext();
			if (newSequenceId == null)
				throw new AxisFault(
						"Internal error - Generated sequence id is null");

			RMMsgContext createSeqResponse = RMMsgCreator
					.createCreateSeqResponseMsg(createSeqRMMsg, outMessage,
							newSequenceId);
			CreateSequenceResponse createSeqResPart = (CreateSequenceResponse) createSeqResponse
					.getMessagePart(Constants.MessageParts.CREATE_SEQ_RESPONSE);

			//If an offer is accepted do necessary procesing.
			Accept accept = createSeqResPart.getAccept();
			if (accept != null) {
				SequenceOffer offer = createSeqPart.getSequenceOffer();
				if (offer == null)
					throw new SandeshaException(
							"Internal error - no offer for the response message with Accept");

				//Setting the CreateSequence table entry.
				String incomingSeqId = createSeqResPart.getIdentifier()
						.getIdentifier();
				String outSequenceId = offer.getIdentifer().getIdentifier();
				CreateSeqBean createSeqBean = new CreateSeqBean();
				createSeqBean.setSequenceId(outSequenceId);
				createSeqBean.setTempSequenceId(newSequenceId);
				createSeqBean.setCreateSeqMsgId(SandeshaUtil.getUUID()); //this
				// is a
				// dummy
				// value.

				StorageManager storageManager = SandeshaUtil
						.getSandeshaStorageManager(context);
				CreateSeqBeanMgr createSeqMgr = storageManager
						.getCreateSeqBeanMgr();

				//Setting sequence properties.
				SequencePropertyBeanMgr seqPropMgr = storageManager
						.getSequencePropretyBeanMgr();
				SequencePropertyBean outSequenceBean = new SequencePropertyBean();
				outSequenceBean
						.setName(Constants.SequenceProperties.OUT_SEQUENCE_ID);
				outSequenceBean.setValue(outSequenceId);
				outSequenceBean.setSequenceId(newSequenceId);
				seqPropMgr.insert(outSequenceBean);

				//Temp sequence id should be set for the server side.
				//If temp sequence id is not set. this implies server side.
				SequencePropertyBean tempSeqBean = seqPropMgr.retrieve(
						outSequenceId,
						Constants.SequenceProperties.TEMP_SEQUENCE_ID);
				if (tempSeqBean == null) {
					SequencePropertyBean tempSequenceBean = new SequencePropertyBean();
					tempSequenceBean
							.setName(Constants.SequenceProperties.TEMP_SEQUENCE_ID);
					tempSequenceBean.setSequenceId(outSequenceId);
					tempSequenceBean.setValue(newSequenceId);
					seqPropMgr.insert(tempSequenceBean);
				}

			}

			CreateSequence createSeq = (CreateSequence) createSeqRMMsg
					.getMessagePart(Constants.MessageParts.CREATE_SEQ);
			if (createSeq == null)
				throw new AxisFault(
						"Create sequence part not present in the create sequence message");

			EndpointReference acksTo = createSeq.getAcksTo().getAddress()
					.getEpr();
			if (acksTo == null || acksTo.getAddress() == null
					|| acksTo.getAddress() == "")
				throw new AxisFault(
						"Acks to not present in the create sequence message");

			SequencePropertyBean seqPropBean = new SequencePropertyBean(
					newSequenceId, Constants.SequenceProperties.ACKS_TO_EPR,
					acksTo);
			//		InMemorySequencePropertyBeanMgr beanMgr = new
			// InMemorySequencePropertyBeanMgr
			// (Constants.DEFAULT_STORAGE_TYPE);
			//		beanMgr.create(seqPropBean);

			StorageManager storageManager = SandeshaUtil
					.getSandeshaStorageManager(context);
			SequencePropertyBeanMgr seqPropMgr = storageManager
					.getSequencePropretyBeanMgr();
			seqPropMgr.insert(seqPropBean);
			outMessage.setResponseWritten(true);

			AxisEngine engine = new AxisEngine(context);
			engine.send(outMessage);
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		createSeqMsg.setPausedTrue(new QName(Constants.IN_HANDLER_NAME));
	}
}