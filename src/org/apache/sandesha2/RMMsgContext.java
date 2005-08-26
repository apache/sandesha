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

import java.util.HashMap;
import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * @author
 */
public class RMMsgContext {
	
	private MessageContext msgContext;
	private HashMap rmMessageParts;
	private int messageType;
	
	public RMMsgContext (){
		rmMessageParts = new HashMap ();
		messageType = Constants.MESSAGE_PART_UNKNOWN;
	}
	
	public RMMsgContext (MessageContext ctx){
		this ();
		this.msgContext = ctx;
		MsgInitializer.populateRMMsgContext(ctx,this);
	}
	
	public void setMessagePart (int partId, IOMRMElement part){
		if (partId>=0 && partId<=Constants.MAX_MSG_PART_ID)
			rmMessageParts.put(new Integer (partId),part);
	}
}
