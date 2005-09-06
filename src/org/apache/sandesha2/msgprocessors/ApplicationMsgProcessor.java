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
package org.apache.sandesha2.msgprocessors;

import java.util.ArrayList;

import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.Sequence;

/**
 * @author 
 * 
 */
public class ApplicationMsgProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext rmMsgCtx)
			throws MsgProcessorException {

		System.out.println ("Application msg processor called");
		
		//setting ack range
		Sequence sequence = (Sequence) rmMsgCtx.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		SequencePropertyBeanMgr mgr = new SequencePropertyBeanMgr (Constants.STORAGE_TYPE_IN_MEMORY);
		
		SequencePropertyBean msgsBean = (SequencePropertyBean) mgr.retrieve( sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES);
		SequencePropertyBean acksToBean = (SequencePropertyBean) mgr.retrieve( sequenceId,Constants.SEQ_PROPERTY_ACKS_TO);
		
		
		String messagesStr =  (String) msgsBean.getValue();
		
		String acksToStr = null;
		try {
		    acksToStr = (String) acksToBean.getValue();
		}catch (Exception e) {
			e.printStackTrace();
		}
	
		System.out.println ("Messages received:" + Constants.SEQ_PROPERTY_RECEIVED_MESSAGES);
		System.out.println ("Acks To:" + Constants.SEQ_PROPERTY_ACKS_TO);
		
		if (acksToStr==null || messagesStr==null)
			throw new MsgProcessorException ("Seqeunce properties are not set correctly");
		
		if (acksToStr!=Constants.WSA.NS_URI_ANONYMOUS) {
			//TODO Add async Ack
		}
	}
}
