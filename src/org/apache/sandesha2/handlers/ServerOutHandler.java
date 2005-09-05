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

package org.apache.sandesha2.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLConstants;

/**
 * @author 
 */
public class ServerOutHandler extends AbstractHandler {

	public void invoke(MessageContext msgCtx) throws AxisFault {
		System.out.println ("Server out handler called");
		
		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		MsgValidator.validateMessage(rmMsgCtx);
	
	
		//getting the request message.
		MessageContext reqMsgCtx = msgCtx.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN);
		RMMsgContext requestRMMsgCtx = MsgInitializer.initializeMessage(reqMsgCtx);
		MsgValidator.validateMessage(requestRMMsgCtx);
		

		
		if(requestRMMsgCtx.getMessageType()!=Constants.MESSAGE_TYPE_CREATE_SEQ){
			//set acknowledgement
			RMMsgCreator.createAckMessage (rmMsgCtx);	
		}
		
	}
}
