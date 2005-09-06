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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.wsrm.RMElements;


/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */


public class MsgInitializer {

	public static RMMsgContext initializeMessage (MessageContext ctx) throws RMException {
		RMMsgContext rmMsgCtx = new RMMsgContext (ctx);
		populateRMMsgContext(ctx,rmMsgCtx);
		validateMessage(rmMsgCtx);
		return rmMsgCtx;
	}
	
	private static void populateRMMsgContext (MessageContext msgCtx, RMMsgContext rmMsgContext) {
		//TODO set message parts
		
		RMElements elements = new RMElements ();
		elements.fromSOAPEnvelope(msgCtx.getEnvelope());
		
		if (elements.getCreateSequence()!=null) 
			rmMsgContext.setMessagePart(Constants.MESSAGE_PART_CREATE_SEQ,elements.getCreateSequence());
		
		if(elements.getCreateSequenceResponse()!=null)
			rmMsgContext.setMessagePart(Constants.MESSAGE_PART_CREATE_SEQ_RESPONSE,elements.getCreateSequenceResponse());
		
		if (elements.getSequence()!=null)
			rmMsgContext.setMessagePart(Constants.MESSAGE_PART_SEQUENCE ,elements.getSequence());
		
		if (elements.getSequenceAcknowledgement()!=null)
			rmMsgContext.setMessagePart(Constants.MESSAGE_PART_SEQ_ACKNOWLEDGEMENT,elements.getSequenceAcknowledgement());
		
		if (elements.getTerminateSequence()!=null)
			rmMsgContext.setMessagePart(Constants.MESSAGE_PART_TERMINATE_SEQ,elements.getTerminateSequence());
		
		if (elements.getAckRequested()!=null)
			rmMsgContext.setMessagePart(Constants.MESSAGE_PART_ACK_REQUEST,elements.getAckRequested());
		
		
		
	}
	
	private static boolean validateMessage (RMMsgContext rmMsgCtx) throws RMException {
        
		//TODO: performa validation
		
		//Setting message type.
		if(rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_CREATE_SEQ);
		else if (rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ_RESPONSE)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_CREATE_SEQ_RESPONSE);
		else if (rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_TERMINATE_SEQ)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_TERMINATE_SEQ);
		else if (rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_SEQUENCE)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_APPLICATION);
		else
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_UNKNOWN);
		
		return true;
	}
	
}
