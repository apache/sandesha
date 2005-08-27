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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * @author 
 */
public class MsgValidator {

	public static void validateMessage(RMMsgContext rmMsgCtx) throws AxisFault{
        //TODO: Validate message
		
		//Setting message type.
		if(rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_CREATE_SEQ);
		else if (rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ_RESPONSE)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_CREATE_SEQ_RESPONSE);
		else if (rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_TERMINATE_SEQ)!=null)
			rmMsgCtx.setMessageType(Constants.MESSAGE_TYPE_TERMINATE_SEQ);
			
    }
}
