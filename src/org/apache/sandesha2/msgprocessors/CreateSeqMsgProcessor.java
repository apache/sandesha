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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
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

/**
 * Responsible for processing an incoming Create Sequence message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class CreateSeqMsgProcessor implements MsgProcessor {

	private Log log = LogFactory.getLog(getClass());
	
	public void processMessage(RMMsgContext createSeqRMMsg)
			throws SandeshaException {

		MessageContext createSeqMsg = createSeqRMMsg.getMessageContext();
		CreateSequence createSeqPart = (CreateSequence) createSeqRMMsg
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSeqPart == null) {
			String message = "No create sequence part is present in the create sequence message"; 
			log.debug(message);
			throw new SandeshaException(message);
		}

		MessageContext outMessage = null;
		outMessage = Utils.createOutMessageContext(createSeqMsg);
		
		ConfigurationContext context = createSeqRMMsg.getMessageContext()
			.getConfigurationContext();
		
		StorageManager storageManager = SandeshaUtil
		.getSandeshaStorageManager(context);

		Transaction createSequenceTransaction = storageManager.getTransaction();

		try {
			String newSequenceId = SequenceManager
					.setupNewSequence(createSeqRMMsg);
			if (newSequenceId == null)
				throw new AxisFault(
						"Internal error - Generated sequence id is null");

			RMMsgContext createSeqResponse = RMMsgCreator
					.createCreateSeqResponseMsg(createSeqRMMsg, outMessage,
							newSequenceId);
			
			createSeqResponse.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");
			CreateSequenceResponse createSeqResPart = (CreateSequenceResponse) createSeqResponse
					.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE);

			//If an offer is accepted do necessary procesing.
			Accept accept = createSeqResPart.getAccept();
			if (accept != null) {
				SequenceOffer offer = createSeqPart.getSequenceOffer();
				if (offer == null) {
					String message = "Internal error - no offer for the response message with Accept"; 
					log.debug(message);
					throw new SandeshaException(message);
				}

				//Setting the CreateSequence table entry.
				String incomingSeqId = createSeqResPart.getIdentifier()
						.getIdentifier();
				String outSequenceId = offer.getIdentifer().getIdentifier();
				CreateSeqBean createSeqBean = new CreateSeqBean();
				createSeqBean.setSequenceID(outSequenceId);
				createSeqBean.setInternalSequenceID(newSequenceId);
				createSeqBean.setCreateSeqMsgID(SandeshaUtil.getUUID()); //this is a dummy value.
				
				CreateSeqBeanMgr createSeqMgr = storageManager
						.getCreateSeqBeanMgr();

				//Setting sequence properties.
				SequencePropertyBeanMgr seqPropMgr = storageManager
						.getSequencePropretyBeanMgr();
				SequencePropertyBean outSequenceBean = new SequencePropertyBean();
				outSequenceBean
						.setName(Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
				outSequenceBean.setValue(outSequenceId);
				outSequenceBean.setSequenceID(newSequenceId);
				seqPropMgr.insert(outSequenceBean);

				//Temp sequence id should be set for the server side.
				//If internal sequence id is not set. this implies server side.
				SequencePropertyBean internalSeqBean = seqPropMgr.retrieve(
						outSequenceId,
						Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
				if (internalSeqBean == null) {
					SequencePropertyBean internalSequenceBean = new SequencePropertyBean();
					internalSequenceBean
							.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
					internalSequenceBean.setSequenceID(outSequenceId);
					internalSequenceBean.setValue(newSequenceId);
					seqPropMgr.insert(internalSequenceBean);
				}

			}

			CreateSequence createSeq = (CreateSequence) createSeqRMMsg
					.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
			if (createSeq == null) {
				String message = "Create sequence part not present in the create sequence message";
				log.debug(message);
				throw new AxisFault(message);
			}

			EndpointReference acksTo = createSeq.getAcksTo().getAddress()
					.getEpr();
			if (acksTo == null || acksTo.getAddress() == null
					|| acksTo.getAddress() == "") {
				String message = "Acks to not present in the create sequence message";
				log.debug(message);
				throw new AxisFault(message);
			}

			SequencePropertyBean seqPropBean = new SequencePropertyBean(
					newSequenceId, Sandesha2Constants.SequenceProperties.ACKS_TO_EPR,
					acksTo.getAddress());

			SequencePropertyBeanMgr seqPropMgr = storageManager
					.getSequencePropretyBeanMgr();
			seqPropMgr.insert(seqPropBean);
			outMessage.setResponseWritten(true);

			Object obj1 = createSeqMsg.getOperationContext().getProperty(
					org.apache.axis2.Constants.RESPONSE_WRITTEN);

			AxisEngine engine = new AxisEngine(context);
			engine.send(outMessage);

			Object obj = createSeqMsg.getOperationContext().getProperty(
					org.apache.axis2.Constants.RESPONSE_WRITTEN);
			
			SequencePropertyBean toBean = seqPropMgr.retrieve(newSequenceId,Sandesha2Constants.SequenceProperties.TO_EPR);
			
			if (toBean==null) {
				String message = "Internal Error: wsa:To value is not set";
				log.debug(message);
				throw new SandeshaException (message);
			}
			
			EndpointReference toEPR = new EndpointReference (toBean.getValue());
			
			if (Sandesha2Constants.WSA.NS_URI_ANONYMOUS.equals(toEPR
					.getAddress())) {
				createSeqMsg.getOperationContext().setProperty(
						org.apache.axis2.Constants.RESPONSE_WRITTEN, "true");
			} else {
				createSeqMsg.getOperationContext().setProperty(
						org.apache.axis2.Constants.RESPONSE_WRITTEN, "false");
			}
		} catch (AxisFault e1) {
			throw new SandeshaException(e1.getMessage());
		}

		createSeqRMMsg.pause();
		
		createSequenceTransaction.commit();
	}
}