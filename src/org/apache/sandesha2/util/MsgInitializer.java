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

package org.apache.sandesha2.util;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.CloseSequence;
import org.apache.sandesha2.wsrm.CloseSequenceResponse;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.RMElements;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.TerminateSequence;
import org.apache.sandesha2.wsrm.TerminateSequenceResponse;

/**
 * This class is used to create an RMMessageContext out of an MessageContext.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class MsgInitializer {

	/**
	 * Called to create a rmMessageContext out of an message context. Finds out things like rm version and message type
	 * as well.
	 * 
	 * @param ctx
	 * @param assumedRMNamespace
	 * this is used for validation (to find out weather the rmNamespace of the current message
	 * is equal to the regietered rmNamespace of the sequence). 
	 * If null validation will not happen.
	 * 
	 * @return
	 * @throws SandeshaException
	 */
	public static RMMsgContext initializeMessage(MessageContext ctx)
			throws SandeshaException {
		RMMsgContext rmMsgCtx = new RMMsgContext(ctx);
		
		populateRMMsgContext(ctx, rmMsgCtx);
		validateMessage(rmMsgCtx);
		return rmMsgCtx;
	}

	/**
	 * Adds the message parts the the RMMessageContext.
	 * 
	 * @param msgCtx
	 * @param rmMsgContext
	 */
	private static void populateRMMsgContext(MessageContext msgCtx,
			RMMsgContext rmMsgContext) throws SandeshaException {

		String addressingNamespace = (String) msgCtx.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
			
		RMElements elements = new RMElements(addressingNamespace);
		elements.fromSOAPEnvelope(msgCtx.getEnvelope(), msgCtx.getWSAAction());

		String rmNamespace = null;
		
		if (elements.getCreateSequence() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ,
					elements.getCreateSequence());
			rmNamespace = elements.getCreateSequence().getNamespaceValue();
		}

		if (elements.getCreateSequenceResponse() != null) {
			rmMsgContext.setMessagePart(
					Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE, elements
							.getCreateSequenceResponse());
			rmNamespace = elements.getCreateSequenceResponse().getNamespaceValue();
		}

		if (elements.getSequence() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.SEQUENCE,
					elements.getSequence());
			rmNamespace = elements.getSequence().getNamespaceValue();	
		}

		if (elements.getSequenceAcknowledgement() != null) {
			rmMsgContext.setMessagePart(
					Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT, elements
							.getSequenceAcknowledgement());
			rmNamespace = elements.getSequenceAcknowledgement().getNamespaceValue();
		}

		if (elements.getTerminateSequence() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ,
					elements.getTerminateSequence());
			rmNamespace = elements.getTerminateSequence().getNamespaceValue();
		}
		
		if (elements.getTerminateSequenceResponse() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ_RESPONSE,
					elements.getTerminateSequenceResponse());
			rmNamespace = elements.getTerminateSequenceResponse().getNamespaceValue();
		}

		if (elements.getAckRequested() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST,
					elements.getAckRequested());
			rmNamespace = elements.getAckRequested().getNamespaceValue();
		}
		
		if (elements.getCloseSequence() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE,
					elements.getCloseSequence());
			rmNamespace = elements.getCloseSequence().getNamespaceValue();
		}
		
		if (elements.getCloseSequenceResponse() != null) {
			rmMsgContext.setMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE_RESPONSE,
					elements.getCloseSequenceResponse());
			rmNamespace = elements.getCloseSequenceResponse().getNamespaceValue();
		}
		
		rmMsgContext.setRMNamespaceValue(rmNamespace);
		
		String addressingNamespaceValue = elements.getAddressingNamespaceValue();
		if (addressingNamespaceValue!=null)
			rmMsgContext.setAddressingNamespaceValue(addressingNamespaceValue);
	}

	/**
	 * This is used to validate the message.
	 * Also set an Message type. Possible types are given in the Sandesha2Constants.MessageTypes interface.
	 * 
	 * @param rmMsgCtx
	 * @return
	 * @throws SandeshaException
	 */
	private static boolean validateMessage(RMMsgContext rmMsgCtx)
			throws SandeshaException {

		ConfigurationContext configContext = rmMsgCtx.getMessageContext().getConfigurationContext();
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		String sequenceID = null;
		
		CreateSequence createSequence = (CreateSequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		CreateSequenceResponse createSequenceResponse = (CreateSequenceResponse) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ_RESPONSE);
		TerminateSequence terminateSequence = (TerminateSequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ);
		TerminateSequenceResponse terminateSequenceResponse = (TerminateSequenceResponse) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.TERMINATE_SEQ_RESPONSE);
		SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		Sequence sequence = (Sequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		AckRequested ackRequest = (AckRequested) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.ACK_REQUEST);
		CloseSequence closeSequence = (CloseSequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE);
		CloseSequenceResponse closeSequenceResponse = (CloseSequenceResponse) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE_RESPONSE);
		
		//Setting message type.
		if (createSequence != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.CREATE_SEQ);
		}else if (createSequenceResponse != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.CREATE_SEQ_RESPONSE);
			sequenceID = createSequenceResponse.getIdentifier().getIdentifier();
		}else if (terminateSequence != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.TERMINATE_SEQ);
			sequenceID = terminateSequence.getIdentifier().getIdentifier();
		}else if (terminateSequenceResponse != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.TERMINATE_SEQ_RESPONSE);
			sequenceID = terminateSequenceResponse.getIdentifier().getIdentifier();
		}else if (rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE) != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.APPLICATION);
			sequenceID = sequence.getIdentifier().getIdentifier();
		} else if (sequenceAcknowledgement != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.ACK);
			sequenceID = sequenceAcknowledgement.getIdentifier().getIdentifier();
		} else if (ackRequest != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.ACK_REQUEST);
			sequenceID = ackRequest.getIdentifier().getIdentifier(); 
		} else if (closeSequence != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.CLOSE_SEQUENCE);
			sequenceID = closeSequence.getIdentifier().getIdentifier(); 
		} else if (closeSequenceResponse != null) {
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.CLOSE_SEQUENCE_RESPONSE);
			sequenceID = closeSequenceResponse.getIdentifier().getIdentifier(); 
		} else
			rmMsgCtx.setMessageType(Sandesha2Constants.MessageTypes.UNKNOWN);

		String propertyKey = null;
		if (rmMsgCtx.getMessageContext().getFLOW()==MessageContext.IN_FLOW) {
			propertyKey = sequenceID;
		} else {
			SequencePropertyBean internalSequenceIDBean = sequencePropertyBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
			if (internalSequenceIDBean!=null) {
				propertyKey = internalSequenceIDBean.getValue();
			}
		}
		
        String rmNamespace = rmMsgCtx.getRMNamespaceValue();
        if (sequenceID!=null) {
        	String specVersion = SandeshaUtil.getRMVersion(propertyKey,rmMsgCtx.getMessageContext().getConfigurationContext());
    		
        	String sequenceRMNamespace = null;
        	if (specVersion!=null)
    			sequenceRMNamespace = SpecSpecificConstants.getRMNamespaceValue(specVersion);
    		if (sequenceRMNamespace!=null && rmNamespace!=null) {
    			if (!sequenceRMNamespace.equals(rmNamespace)) {
    				throw new SandeshaException ("Given message has rmNamespace value, which is different from the " +
    						"reqistered namespace for the sequence");
    			}
    		}
        }
		
		return true;
	}

}