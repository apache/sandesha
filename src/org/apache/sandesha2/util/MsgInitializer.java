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

import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.wsrm.RMElements;

/**
 * This class is used to create an RMMessageContext out of an MessageContext.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class MsgInitializer {

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
			RMMsgContext rmMsgContext) {

		RMElements elements = new RMElements();
		elements.fromSOAPEnvelope(msgCtx.getEnvelope());

		if (elements.getCreateSequence() != null)
			rmMsgContext.setMessagePart(Constants.MessageParts.CREATE_SEQ,
					elements.getCreateSequence());

		if (elements.getCreateSequenceResponse() != null)
			rmMsgContext.setMessagePart(
					Constants.MessageParts.CREATE_SEQ_RESPONSE, elements
							.getCreateSequenceResponse());

		if (elements.getSequence() != null)
			rmMsgContext.setMessagePart(Constants.MessageParts.SEQUENCE,
					elements.getSequence());

		if (elements.getSequenceAcknowledgement() != null)
			rmMsgContext.setMessagePart(
					Constants.MessageParts.SEQ_ACKNOWLEDGEMENT, elements
							.getSequenceAcknowledgement());

		if (elements.getTerminateSequence() != null)
			rmMsgContext.setMessagePart(Constants.MessageParts.TERMINATE_SEQ,
					elements.getTerminateSequence());

		if (elements.getAckRequested() != null)
			rmMsgContext.setMessagePart(Constants.MessageParts.ACK_REQUEST,
					elements.getAckRequested());

	}

	/**
	 * This is used to validate the message.
	 * Also set an Message type. Possible types are given in the Constants.MessageTypes interface.
	 * 
	 * @param rmMsgCtx
	 * @return
	 * @throws SandeshaException
	 */
	private static boolean validateMessage(RMMsgContext rmMsgCtx)
			throws SandeshaException {

		//Setting message type.
		if (rmMsgCtx.getMessagePart(Constants.MessageParts.CREATE_SEQ) != null)
			rmMsgCtx.setMessageType(Constants.MessageTypes.CREATE_SEQ);
		else if (rmMsgCtx
				.getMessagePart(Constants.MessageParts.CREATE_SEQ_RESPONSE) != null)
			rmMsgCtx.setMessageType(Constants.MessageTypes.CREATE_SEQ_RESPONSE);
		else if (rmMsgCtx.getMessagePart(Constants.MessageParts.TERMINATE_SEQ) != null)
			rmMsgCtx.setMessageType(Constants.MessageTypes.TERMINATE_SEQ);
		else if (rmMsgCtx.getMessagePart(Constants.MessageParts.SEQUENCE) != null)
			rmMsgCtx.setMessageType(Constants.MessageTypes.APPLICATION);
		else if (rmMsgCtx
				.getMessagePart(Constants.MessageParts.SEQ_ACKNOWLEDGEMENT) != null)
			rmMsgCtx.setMessageType(Constants.MessageTypes.ACK);
		else
			rmMsgCtx.setMessageType(Constants.MessageTypes.UNKNOWN);

		return true;
	}

}