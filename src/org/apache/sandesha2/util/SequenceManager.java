/*
 * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.CreateSequence;

/**
 * This is used to set up a new sequence, both at the sending side and the receiving side.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SequenceManager {

	public static String setupNewSequence(RMMsgContext createSequenceMsg)
			throws AxisFault {

		String sequenceId = SandeshaUtil.getUUID();
		AbstractContext context = createSequenceMsg.getContext();

		EndpointReference to = createSequenceMsg.getTo();
		if (to == null)
			throw new AxisFault("To is null");

		EndpointReference replyTo = createSequenceMsg.getReplyTo();
		if (replyTo == null)
			throw new AxisFault("ReplyTo is null");

		CreateSequence createSequence = (CreateSequence) createSequenceMsg
				.getMessagePart(Sandesha2Constants.MessageParts.CREATE_SEQ);
		if (createSequence == null)
			throw new AxisFault("Create Sequence Part is null");

		EndpointReference acksTo = createSequence.getAcksTo().getAddress()
				.getEpr();

		if (acksTo == null)
			throw new AxisFault("AcksTo is null");

		StorageManager storageManager = null;

		try {
			storageManager = SandeshaUtil
					.getSandeshaStorageManager(createSequenceMsg
							.getMessageContext().getSystemContext());
		} catch (SandeshaException e) {
			e.printStackTrace();
		}

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		SequencePropertyBean receivedMsgBean = new SequencePropertyBean(
				sequenceId, Sandesha2Constants.SequenceProperties.RECEIVED_MESSAGES, "");
		SequencePropertyBean toBean = new SequencePropertyBean(sequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR, replyTo);
		SequencePropertyBean replyToBean = new SequencePropertyBean(sequenceId,
				Sandesha2Constants.SequenceProperties.REPLY_TO_EPR, to);
		SequencePropertyBean acksToBean = new SequencePropertyBean(sequenceId,
				Sandesha2Constants.SequenceProperties.ACKS_TO_EPR, acksTo);

		seqPropMgr.insert(receivedMsgBean);
		seqPropMgr.insert(toBean);
		seqPropMgr.insert(replyToBean);
		seqPropMgr.insert(acksToBean);

		NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();
		nextMsgMgr.insert(new NextMsgBean(sequenceId, 1)); // 1 will be the next
		// message to invoke
		//this will apply for only in-order invocations.

		return sequenceId;
	}

	public void removeSequence(String sequence) {

	}

	public static void setupNewClientSequence(
			MessageContext firstAplicationMsgCtx, String iternalSequenceId)
			throws SandeshaException {

		AbstractContext context = firstAplicationMsgCtx.getSystemContext();

		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(firstAplicationMsgCtx
						.getSystemContext());

		SequencePropertyBeanMgr seqPropMgr = storageManager
				.getSequencePropretyBeanMgr();

		EndpointReference toEPR = firstAplicationMsgCtx.getTo();
		EndpointReference replyToEPR = firstAplicationMsgCtx.getReplyTo();
		String acksTo = (String) firstAplicationMsgCtx
				.getProperty(Sandesha2Constants.ClientAPI.AcksTo);

		if (toEPR == null)
			throw new SandeshaException("WS-Addressing To is null");

		SequencePropertyBean toBean = new SequencePropertyBean(iternalSequenceId,
				Sandesha2Constants.SequenceProperties.TO_EPR, toEPR);

		//Default value for acksTo is anonymous
		if (acksTo == null)
			acksTo = Sandesha2Constants.WSA.NS_URI_ANONYMOUS;

		EndpointReference acksToEPR = new EndpointReference(acksTo);
		SequencePropertyBean acksToBean = new SequencePropertyBean(
				iternalSequenceId, Sandesha2Constants.SequenceProperties.ACKS_TO_EPR,
				acksToEPR);
		seqPropMgr.insert(toBean);
		seqPropMgr.insert(acksToBean);

	}
}