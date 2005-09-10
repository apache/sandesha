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

import org.apache.axis2.AxisFault;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
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

		//		SequencePropertyBeanMgr mgr = new SequencePropertyBeanMgr (Constants.STORAGE_TYPE_IN_MEMORY);
		
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory.getBeanMgrFactory(Constants.DEFAULT_STORAGE_TYPE).
				getSequencePropretyBeanMgr();

		SequencePropertyBean msgsBean = seqPropMgr.retrieve( sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES);
		SequencePropertyBean acksToBean = seqPropMgr.retrieve( sequenceId,Constants.SEQ_PROPERTY_ACKS_TO);
		
		long msgNo = sequence.getMessageNumber().getMessageNumber();
		if (msgNo==0)
			throw new MsgProcessorException ("Wrong message number");
		
		String messagesStr =  (String) msgsBean.getValue();
		if (messagesStr!="" && messagesStr!=null)
			messagesStr = messagesStr + "," + Long.toString(msgNo);
		else 
			messagesStr = Long.toString(msgNo);
		
		msgsBean.setValue(messagesStr);
		seqPropMgr.update(msgsBean);
			
		String acksToStr = null;
		try {
		    acksToStr = (String) acksToBean.getValue();
		}catch (Exception e) {
			e.printStackTrace();
		}
	
		System.out.println ("Messages received:" + messagesStr);
		System.out.println ("Acks To:" + acksToStr);
		
		if (acksToStr==null || messagesStr==null)
			throw new MsgProcessorException ("Seqeunce properties are not set correctly");
		
		if (acksToStr!=Constants.WSA.NS_URI_ANONYMOUS) {
			//TODO Add async Ack
		}
	}
}
