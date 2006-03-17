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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.FaultManager;
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
	
	public void processInMessage(RMMsgContext createSeqRMMsg)
			throws SandeshaException {

		MessageContext createSeqMsg = createSeqRMMsg.getMessageContext();
		CreateSequence createSeqPart = (CreateSequence) createSeqRMMsg.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSeqPart == null) {
			String message = "No create sequence part is present in the create sequence message"; 
			log.debug(message);
			throw new SandeshaException(message);
		}

		FaultManager faultManager = new FaultManager();
		RMMsgContext faultMessageContext = faultManager.checkForCreateSequenceRefused(createSeqMsg);
		if (faultMessageContext != null) {
			ConfigurationContext configurationContext = createSeqMsg.getConfigurationContext();
			AxisEngine engine = new AxisEngine(configurationContext);
			
			try {
				engine.sendFault(faultMessageContext.getMessageContext());
			} catch (AxisFault e) {
				throw new SandeshaException ("Could not send the fault message",e);
			}
			
			return;
		}
		
		
		MessageContext outMessage = null;
		outMessage = Utils.createOutMessageContext(createSeqMsg);  //createing a new response message.
		
		ConfigurationContext context = createSeqMsg.getConfigurationContext();
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(context);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		Transaction createSequenceTransaction = storageManager.getTransaction();   //begining of a new transaction

		try {
			String newSequenceId = SequenceManager.setupNewSequence(createSeqRMMsg);  //newly created sequnceID.
			
			RMMsgContext createSeqResponse = RMMsgCreator.createCreateSeqResponseMsg(
					createSeqRMMsg, outMessage,newSequenceId);    // converting the blank out message in to a create
			                                                      // sequence response.
			createSeqResponse.setFlow(MessageContext.OUT_FLOW);
			
			createSeqResponse.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");  //for making sure that this wont be processed again.
			CreateSequenceResponse createSeqResPart = (CreateSequenceResponse) createSeqResponse.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE);

			
			//OFFER PROCESSING
			SequenceOffer offer = createSeqPart.getSequenceOffer();
			if (offer != null) {
				Accept accept = createSeqResPart.getAccept();
				if (accept == null) {
					String message = "An accept part has not been generated for the create seq request with an offer part"; 
					log.debug(message);
					throw new SandeshaException(message);
				}

				String offeredSequenceID = offer.getIdentifer().getIdentifier(); //offered seq. id.
				
				boolean offerEcepted = offerAccepted (offeredSequenceID,context);
				
				if (offerEcepted)  {
					//Setting the CreateSequence table entry for the outgoing side.
					CreateSeqBean createSeqBean = new CreateSeqBean();
					createSeqBean.setSequenceID(offeredSequenceID);
					String outgoingSideInternalSequenceID = SandeshaUtil.getInternalSequenceID(newSequenceId);
					createSeqBean.setInternalSequenceID(outgoingSideInternalSequenceID);
					createSeqBean.setCreateSeqMsgID(SandeshaUtil.getUUID()); //this is a dummy value.
				
					CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();
					createSeqMgr.insert(createSeqBean);
				
					//Setting sequence properties for the outgoing sequence. 
					//Only will be used by the server side response path. Will be wasted properties for the client side.
				
					//setting the out_sequence_id
					SequencePropertyBean outSequenceBean = new SequencePropertyBean();
					outSequenceBean.setName(Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
					outSequenceBean.setValue(offeredSequenceID);
					outSequenceBean.setSequenceID(outgoingSideInternalSequenceID);
					seqPropMgr.insert(outSequenceBean);

					//setting the internal_sequence_id
					SequencePropertyBean internalSequenceBean = new SequencePropertyBean();
					internalSequenceBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
					internalSequenceBean.setSequenceID(offeredSequenceID);
					internalSequenceBean.setValue(outgoingSideInternalSequenceID);
					seqPropMgr.insert(internalSequenceBean);
				} else {
					//removing the accept part.
					createSeqResPart.setAccept(null);
					createSeqResponse.addSOAPEnvelope();
				}
			}

			EndpointReference acksTo = createSeqPart.getAcksTo().getAddress().getEpr();
			if (acksTo == null || acksTo.getAddress() == null
					|| acksTo.getAddress() == "") {
				String message = "Acks to not present in the create sequence message";
				log.debug(message);
				throw new AxisFault(message);
			}

			SequencePropertyBean acksToBean = new SequencePropertyBean(
					newSequenceId, Sandesha2Constants.SequenceProperties.ACKS_TO_EPR,acksTo.getAddress());

			seqPropMgr.insert(acksToBean);
			
			outMessage.setResponseWritten(true);

			//commiting tr. before sending the response msg.
			createSequenceTransaction.commit();
			
			Transaction updateLastActivatedTransaction = storageManager.getTransaction();
			SequenceManager.updateLastActivatedTime(newSequenceId,createSeqRMMsg.getMessageContext().getConfigurationContext());
			updateLastActivatedTransaction.commit();
			
			AxisEngine engine = new AxisEngine(context);
			engine.send(outMessage);
			
			SequencePropertyBean toBean = seqPropMgr.retrieve(newSequenceId,Sandesha2Constants.SequenceProperties.TO_EPR);
			if (toBean==null) {
				String message = "Internal Error: wsa:To value is not set";
				log.debug(message);
				throw new SandeshaException (message);
			}
			
			EndpointReference toEPR = new EndpointReference (toBean.getValue());
			
			if (Sandesha2Constants.WSA.NS_URI_ANONYMOUS.equals(
					toEPR.getAddress())) {
				createSeqMsg.getOperationContext().setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN, "true");
			} else {
				createSeqMsg.getOperationContext().setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN, "false");
			}
			
		} catch (AxisFault e1) {
			throw new SandeshaException(e1);
		}

		createSeqRMMsg.pause();
	}
	
	private boolean offerAccepted (String sequenceID, ConfigurationContext configCtx) throws SandeshaException {
		if ("".equals(sequenceID)) 
			return false;
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);
		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();
		
		CreateSeqBean createSeqFindBean = new CreateSeqBean ();	
		createSeqFindBean.setSequenceID(sequenceID);
		Collection arr = createSeqMgr.find(createSeqFindBean);
		
		if (arr.size()>0)
			return false;
		
		return true;
	}
	
	public void processOutMessage(RMMsgContext rmMsgCtx) throws SandeshaException {
		
	}
}