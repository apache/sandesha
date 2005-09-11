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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.RMException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.msgprocessors.MsgProcessor;
import org.apache.sandesha2.msgprocessors.MsgProcessorException;
import org.apache.sandesha2.msgprocessors.MsgProcessorFactory;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */
public class ServerInHandler extends AbstractHandler {

//	public ServerInHandler (){
//		System.out.println ("NEW SERVER IN HANDLER");
//	}
	
	public QName getName (){
		return new QName (Constants.IN_HANDLER_NAME);
	}
	
	public void invoke(MessageContext msgCtx) throws AxisFault {
		System.out.println  ("In server Handler 1");
		
//		String opearaitonName = msgCtx.getOperationContext().getAxisOperation().getName().getLocalPart();
//		System.out.println ("Operation is:" + opearaitonName);
//		
		
		RMMsgContext rmMsgCtx = null;

        try {
        	rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
        }catch (RMException ex) {
        	throw new AxisFault ("Cant initialize the message");
        }
		
		MsgProcessor msgProcessor = MsgProcessorFactory.getMessageProcessor(rmMsgCtx.getMessageType());
					
		try {
			msgProcessor.processMessage(rmMsgCtx);
		}catch (MsgProcessorException mpe) {
			mpe.printStackTrace();
			throw new AxisFault ("Error in processing the message");
		}
		
	}
	
}
