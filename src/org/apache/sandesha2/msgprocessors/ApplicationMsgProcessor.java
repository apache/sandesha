/*
 * Created on Aug 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
		SequencePropertyBean bean = (SequencePropertyBean) mgr.retrieve( sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MSG_LIST);
		System.out.println ("get --" + sequenceId + "--" + Constants.SEQ_PROPERTY_RECEIVED_MSG_LIST);
		int i = 1;
		ArrayList list = (ArrayList) bean.getValue();
		System.out.println ("aaa is:" + list.get(0));
	}
}
