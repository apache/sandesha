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
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.CreateSequence;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya
 */
public class SequenceManager {

	public static String setupNewSequence(RMMsgContext createSequenceMsg) throws AxisFault {
		//		SequencePropertyBean seqPropBean = new SequencePropertyBean
		// (sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES,"");
		//		InMemorySequencePropertyBeanMgr beanMgr = new InMemorySequencePropertyBeanMgr
		// (Constants.DEFAULT_STORAGE_TYPE);
		//		beanMgr.create(seqPropBean);

		String sequenceId = SandeshaUtil.getUUID();
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

		StorageManager storageManager = null;
		
		try {
			storageManager = SandeshaUtil.getSandeshaStorageManager(createSequenceMsg.getMessageContext().getSystemContext());
		} catch (SandeshaException e) {
			e.printStackTrace();
		}
		
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();

		//TODO - recheck following 
		//incoming To - reply address of response messages
		//imcoming replyTo - to address of response messages
		SequencePropertyBean receivedMsgBean = new SequencePropertyBean(
				sequenceId, Constants.SequenceProperties.RECEIVED_MESSAGES, "");
		SequencePropertyBean toBean = new SequencePropertyBean (sequenceId,
				Constants.SequenceProperties.TO_EPR,replyTo);
		SequencePropertyBean replyToBean = new SequencePropertyBean(sequenceId,
				Constants.SequenceProperties.REPLY_TO_EPR, to);
		SequencePropertyBean acksToBean = new SequencePropertyBean(sequenceId,
				Constants.SequenceProperties.ACKS_TO_EPR, acksTo);

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
	
	public static void setupNewClientSequence (MessageContext firstAplicationMsgCtx, String tempSequenceId) throws SandeshaException {
		
		AbstractContext context = firstAplicationMsgCtx.getSystemContext();
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(firstAplicationMsgCtx.getSystemContext());
		
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		
		EndpointReference toEPR = firstAplicationMsgCtx.getTo();
		EndpointReference replyToEPR = firstAplicationMsgCtx.getReplyTo();
		String acksTo = (String) firstAplicationMsgCtx.getProperty(Constants.ClientProperties.AcksTo);
		
		if (toEPR==null)
			throw new SandeshaException ("WS-Addressing To is null");
		
		SequencePropertyBean toBean = new SequencePropertyBean (tempSequenceId, Constants.SequenceProperties.TO_EPR,toEPR);
		
		//Default value for acksTo is anonymous
		if (acksTo==null)
			acksTo = Constants.WSA.NS_URI_ANONYMOUS;
		
		EndpointReference acksToEPR = new EndpointReference (acksTo);
		SequencePropertyBean acksToBean = new SequencePropertyBean (tempSequenceId, Constants.SequenceProperties.ACKS_TO_EPR,acksToEPR);
		
//		//TODO - make default for replyTo anonymous
//		if (replyToEPR==null)
//			replyToEPR = new EndpointReference (Constants.WSA.NS_URI_ANONYMOUS);
		
		seqPropMgr.insert(toBean);
		seqPropMgr.insert(acksToBean);
		
		
		
	}
}