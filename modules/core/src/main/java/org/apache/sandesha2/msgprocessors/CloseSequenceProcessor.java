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

import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.security.SecurityManager;
import org.apache.sandesha2.security.SecurityToken;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beans.RMDBean;
import org.apache.sandesha2.util.AcknowledgementManager;
import org.apache.sandesha2.util.FaultManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SpecSpecificConstants;
import org.apache.sandesha2.util.WSRMMessageSender;
import org.apache.sandesha2.wsrm.CloseSequence;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;

/**
 * Responsible for processing an incoming Close Sequence message. (As introduced
 * by the WSRM 1.1 specification)
 */

public class CloseSequenceProcessor extends WSRMMessageSender implements MsgProcessor {

	private static final Log log = LogFactory.getLog(CloseSequenceProcessor.class);

	public boolean processInMessage(RMMsgContext rmMsgCtx, Transaction transaction) throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: CloseSequenceProcessor::processInMessage");

		ConfigurationContext configCtx = rmMsgCtx.getMessageContext().getConfigurationContext();
		CloseSequence closeSequence = (CloseSequence) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE);

		MessageContext msgCtx = rmMsgCtx.getMessageContext();

		String sequenceId = closeSequence.getIdentifier().getIdentifier();
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx, configCtx
				.getAxisConfiguration());

		RMDBean rmdBean = SandeshaUtil.getRMDBeanFromSequenceId(storageManager, sequenceId);

		// Check that the sender of this CloseSequence holds the correct token
		if(rmdBean != null && rmdBean.getSecurityTokenData() != null) {
			SecurityManager secManager = SandeshaUtil.getSecurityManager(msgCtx.getConfigurationContext());
			OMElement body = msgCtx.getEnvelope().getBody();
			SecurityToken token = secManager.recoverSecurityToken(rmdBean.getSecurityTokenData());
			secManager.checkProofOfPossession(token, body, msgCtx);
		}

		if (FaultManager.checkForUnknownSequence(rmMsgCtx, sequenceId, storageManager, false)) {
			if (log.isDebugEnabled())
				log.debug("Exit: CloseSequenceProcessor::processInMessage, Unknown sequence " + sequenceId);
			return false;
		}
		
		// throwing a fault if the sequence is terminated
		if (FaultManager.checkForSequenceTerminated(rmMsgCtx, sequenceId, rmdBean, false)) {
			if (log.isDebugEnabled())
				log.debug("Exit: CloseSequenceProcessor::processInMessage, Sequence terminated");
			return false;
		}

		rmdBean.setClosed(true);
		storageManager.getRMDBeanMgr().update(rmdBean);

		RMMsgContext ackRMMsgCtx = AcknowledgementManager.generateAckMessage(rmMsgCtx, rmdBean, sequenceId, storageManager, true);
		// adding the ack part(s) to the envelope.
		Iterator sequenceAckIter = ackRMMsgCtx
				.getMessageParts(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);

		MessageContext closeSequenceMsg = rmMsgCtx.getMessageContext();

		RMMsgContext closeSeqResponseRMMsg = RMMsgCreator.createCloseSeqResponseMsg(rmMsgCtx, rmdBean);
		MessageContext closeSequenceResponseMsg = closeSeqResponseRMMsg.getMessageContext();

		while (sequenceAckIter.hasNext()) {
			SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) sequenceAckIter.next();
			closeSeqResponseRMMsg.setMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT,
					sequenceAcknowledgement);
		}
		
		closeSeqResponseRMMsg.setFlow(MessageContext.OUT_FLOW);
		closeSeqResponseRMMsg.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE, "true");

		closeSequenceResponseMsg.setResponseWritten(true);

		closeSeqResponseRMMsg.addSOAPEnvelope();
		
		//
		// Now that we have generated the message we can commit the transaction
		//
		if(transaction != null && transaction.isActive()) {
			transaction.commit();
			transaction = null;
		}

		AxisEngine engine = new AxisEngine(closeSequenceMsg.getConfigurationContext());

		try {
			engine.send(closeSequenceResponseMsg);
		} catch (AxisFault e) {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.couldNotSendCloseResponse,
					sequenceId, e.toString());
			throw new SandeshaException(message, e);
		}

		if (log.isDebugEnabled())
			log.debug("Exit: CloseSequenceProcessor::processInMessage " + Boolean.FALSE);
		return false;
	}

	public boolean processOutMessage(RMMsgContext rmMsgCtx) throws AxisFault {
		if (log.isDebugEnabled()) 
			log.debug("Enter: CloseSequenceProcessor::processOutMessage");
		
		// Get the data from the message context
		setupOutMessage(rmMsgCtx);

		//write into the sequence proeprties that the client is now closed
		getRMSBean().setSequenceClosedClient(true);
		getStorageManager().getRMSBeanMgr().update(getRMSBean());

		AxisOperation closeOperation = SpecSpecificConstants.getWSRMOperation(
				Sandesha2Constants.MessageTypes.CLOSE_SEQUENCE,
				rmMsgCtx.getRMSpecVersion(),
				rmMsgCtx.getMessageContext().getAxisService());
		getMsgContext().setAxisOperation(closeOperation);


		OperationContext opcontext = ContextFactory.createOperationContext(closeOperation, getMsgContext().getServiceContext());
		opcontext.setParent(getMsgContext().getServiceContext());

		getConfigurationContext().registerOperationContext(rmMsgCtx.getMessageId(),opcontext);
		getMsgContext().setOperationContext(opcontext);
		
		CloseSequence closeSequencePart = (CloseSequence) rmMsgCtx
				.getMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE);
		Identifier identifier = closeSequencePart.getIdentifier();
		if (identifier==null) {
			identifier = new Identifier (closeSequencePart.getNamespaceValue());
			closeSequencePart.setIdentifier(identifier);
		}
		
		rmMsgCtx.setWSAAction(SpecSpecificConstants.getCloseSequenceAction(getRMVersion()));
		rmMsgCtx.setSOAPAction(SpecSpecificConstants.getCloseSequenceAction (getRMVersion()));

		// Send this outgoing message
		sendOutgoingMessage(rmMsgCtx, Sandesha2Constants.MessageTypes.CLOSE_SEQUENCE, 0);

		// Pause the message context
		rmMsgCtx.pause();

		if (log.isDebugEnabled())
			log.debug("Exit: CloseSeqMsgProcessor::processOutMessage " + Boolean.TRUE);
		
		return true;

	}

}
