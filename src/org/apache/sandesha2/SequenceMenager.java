/*
 * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2;

import java.util.ArrayList;

import javax.naming.Context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.CreateSequence;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya
 */
public class SequenceMenager {

	public static void setUpNewSequence(String sequenceId,
			RMMsgContext createSequenceMsg) throws AxisFault {
		//		SequencePropertyBean seqPropBean = new SequencePropertyBean
		// (sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES,"");
		//		SequencePropertyBeanMgr beanMgr = new SequencePropertyBeanMgr
		// (Constants.DEFAULT_STORAGE_TYPE);
		//		beanMgr.create(seqPropBean);

		AbstractContext context = createSequenceMsg.getContext();
		
		EndpointReference to = createSequenceMsg.getTo();
		if (to==null)
			throw new AxisFault ("To is null");
		
		
		EndpointReference replyTo = createSequenceMsg.getReplyTo();
		if (replyTo == null)
			throw new AxisFault("ReplyTo is null");

		CreateSequence createSequence = (CreateSequence) createSequenceMsg
				.getMessagePart(Constants.MessageParts.CREATE_SEQ);
		if (createSequence == null)
			throw new AxisFault("Create Sequence Part is null");

		EndpointReference acksTo = createSequence.getAcksTo().getAddress()
				.getEpr();

		if (acksTo == null)
			throw new AxisFault("AcksTo is null");

		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(context).getSequencePropretyBeanMgr();

		SequencePropertyBean receivedMsgBean = new SequencePropertyBean(
				sequenceId, Constants.SequenceProperties.RECEIVED_MESSAGES, "");
		SequencePropertyBean toBean = new SequencePropertyBean (sequenceId,
				Constants.SequenceProperties.TO_EPR,to);
		SequencePropertyBean replyToBean = new SequencePropertyBean(sequenceId,
				Constants.SequenceProperties.REPLY_TO_EPR, replyTo);
		SequencePropertyBean acksToBean = new SequencePropertyBean(sequenceId,
				Constants.SequenceProperties.ACKS_TO_EPR, acksTo);

		seqPropMgr.insert(receivedMsgBean);
		seqPropMgr.insert(toBean);
		seqPropMgr.insert(replyToBean);
		seqPropMgr.insert(acksToBean);

		NextMsgBeanMgr nextMsgMgr = AbstractBeanMgrFactory.getInstance(context)
				.getNextMsgBeanMgr();
		nextMsgMgr.insert(new NextMsgBean(sequenceId, 1)); // 1 will be the next
														   // message to invoke
		//this will apply for only in-order invocations.
	}

	public void removeSequence(String sequence) {

	}
}