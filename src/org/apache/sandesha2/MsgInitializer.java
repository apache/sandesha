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

import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.wsrm.RMElements;


/**
 *@ author
 */

public class MsgInitializer {

	public static RMMsgContext initializeMessage (MessageContext ctx) {
		RMMsgContext rmMsgCtx = new RMMsgContext (ctx);
		populateRMMsgContext(ctx,rmMsgCtx);
		return rmMsgCtx;
	}
	
	public static void populateRMMsgContext (MessageContext msgCtx, RMMsgContext rmMsgContext) {
		//TODO set message parts
		
		RMElements elements = new RMElements ();
		elements.fromSOAPEnvelope(msgCtx.getEnvelope());
		
		rmMsgContext.setMessagePart(Constants.MESSAGE_PART_CREATE_SEQ,elements.getCreateSequence());
		rmMsgContext.setMessagePart(Constants.MESSAGE_PART_CREATE_SEQ_RESPONSE,elements.getCreateSequenceResponse());
		rmMsgContext.setMessagePart(Constants.MESSAGE_PART_SEQUENCE ,elements.getSequence());
		rmMsgContext.setMessagePart(Constants.MESSAGE_PART_SEQ_ACKNOWLEDGEMENT,elements.getSequenceAcknowledgement());
		rmMsgContext.setMessagePart(Constants.MESSAGE_PART_TERMINATE_SEQ,elements.getTerminateSequence());
		
		
		
	}
}
