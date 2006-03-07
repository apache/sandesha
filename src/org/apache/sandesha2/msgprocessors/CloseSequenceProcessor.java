package org.apache.sandesha2.msgprocessors;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.Utils;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.FaultManager;
import org.apache.sandesha2.util.RMMsgCreator;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.CloseSequence;
import org.apache.sandesha2.wsrm.CloseSequenceResponse;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;

public class CloseSequenceProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		ConfigurationContext configCtx = rmMsgCtx.getMessageContext().getConfigurationContext();
		CloseSequence closeSequence = (CloseSequence) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.CLOSE_SEQUENCE);
		
		MessageContext msgCtx = rmMsgCtx.getMessageContext();
		
		String sequenceID = closeSequence.getIdentifier().getIdentifier();
		
		FaultManager faultManager = new FaultManager();
		RMMsgContext faultMessageContext = faultManager.checkForUnknownSequence(rmMsgCtx,sequenceID);
		if (faultMessageContext != null) {
			ConfigurationContext configurationContext = msgCtx.getConfigurationContext();
			AxisEngine engine = new AxisEngine(configurationContext);
			
			try {
				engine.sendFault(faultMessageContext.getMessageContext());
			} catch (AxisFault e) {
				throw new SandeshaException ("Could not send the fault message",e);
			}
			
			return;
		}
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);
		
		Transaction closeSequenceTransaction = storageManager.getTransaction();
		
		SequencePropertyBeanMgr sequencePropMgr = storageManager.getSequencePropretyBeanMgr();
		SequencePropertyBean sequenceClosedBean = new SequencePropertyBean ();
		sequenceClosedBean.setSequenceID(sequenceID);
		sequenceClosedBean.setName(Sandesha2Constants.SequenceProperties.SEQUENCE_CLOSED);
		sequenceClosedBean.setValue(Sandesha2Constants.VALUE_TRUE);
		
		sequencePropMgr.insert(sequenceClosedBean);
		
		RMMsgContext ackRMMsgCtx = AcknowledgementManager.generateAckMessage(rmMsgCtx,sequenceID);
		
		MessageContext ackMsgCtx = ackRMMsgCtx.getMessageContext();
		
		String rmNamespaceValue = rmMsgCtx.getRMNamespaceValue();
		ackRMMsgCtx.setRMNamespaceValue(rmNamespaceValue);
		

		SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(SandeshaUtil
				.getSOAPVersion(rmMsgCtx.getSOAPEnvelope()));
		
		//Setting new envelope
		SOAPEnvelope envelope = factory.getDefaultEnvelope();
		try {
			ackMsgCtx.setEnvelope(envelope);
		} catch (AxisFault e3) {
			throw new SandeshaException(e3.getMessage());
		}
		
		//adding the ack part to the envelope.
		SequenceAcknowledgement sequenceAcknowledgement = (SequenceAcknowledgement) ackRMMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
	
		MessageContext closeSequenceMsg = rmMsgCtx.getMessageContext();
		
		MessageContext closeSequenceResponseMsg = null;
		closeSequenceResponseMsg = Utils.createOutMessageContext(closeSequenceMsg);
		
		RMMsgContext closeSeqResponseRMMsg = RMMsgCreator
				.createCloseSeqResponseMsg(rmMsgCtx, closeSequenceResponseMsg);
		
		closeSeqResponseRMMsg.setMessagePart(Sandesha2Constants.MessageParts.SEQ_ACKNOWLEDGEMENT,sequenceAcknowledgement); 
		
		closeSeqResponseRMMsg.setFlow(MessageContext.OUT_FLOW);
		closeSeqResponseRMMsg.setProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE,"true");

		closeSequenceResponseMsg.setResponseWritten(true);
		
		closeSeqResponseRMMsg.addSOAPEnvelope();
		
		AxisEngine engine = new AxisEngine (closeSequenceMsg.getConfigurationContext());
		
		try {
			engine.send(closeSequenceResponseMsg);
		} catch (AxisFault e) {
			String message = "Could not send the terminate sequence response";
			throw new SandeshaException (message,e);
		}
		
		
		closeSequenceTransaction.commit();
	}
	


	
	
}
