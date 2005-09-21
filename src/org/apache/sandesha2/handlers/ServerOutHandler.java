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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.log4j.lf5.viewer.configure.MRUFileManager;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.RMException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.msgprocessors.MsgProcessorException;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLConstants;

/**
 * @author Sanka
 * @author Chamikara
 * @author Jaliya 
 */
public class ServerOutHandler extends AbstractHandler {

	public void invoke(MessageContext msgCtx) throws AxisFault {
		System.out.println ("Server out handler called");
		
		RMMsgContext rmMsgCtx;
        try {
        	rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
        }catch (RMException ex) {
        	throw new AxisFault ("Cant initialize the message");
        }
	
	
		//getting the request message.
		MessageContext reqMsgCtx = msgCtx.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN);
		RMMsgContext requestRMMsgCtx;
        try {
        	requestRMMsgCtx = MsgInitializer.initializeMessage(reqMsgCtx);
        }catch (RMException ex) {
        	throw new AxisFault ("Cant initialize the message");
        }
        
        
		if (requestRMMsgCtx.getMessageType()!=Constants.MESSAGE_TYPE_CREATE_SEQ)
			RMMsgCreator.addAckMessage(rmMsgCtx);
		
		if (rmMsgCtx.getMessageType()==Constants.MESSAGE_TYPE_UNKNOWN) {
			//This is a possible response message.
			System.out.println ("GOT Possible Response Message");
			
			processResponseMessage(rmMsgCtx,requestRMMsgCtx);
		}
		
		SOAPEnvelope env = msgCtx.getEnvelope();
		
		try {
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			//env.serialize(writer);
		}catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private void processResponseMessage (RMMsgContext msg,RMMsgContext reqMsg) throws AxisFault {
		if (msg==null || reqMsg==null)
			throw new AxisFault ("Message or reques message is null");
		
		Sequence sequence = (Sequence) reqMsg.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
		//check weather the seq is is available.
		
		if (sequence==null)
			throw new AxisFault ("Sequence part is null");
		
		String incomingSeqId = sequence.getIdentifier().getIdentifier();
		if (incomingSeqId==null || incomingSeqId=="")
			throw new AxisFault ("Invalid seqence Id");
		
		AbstractContext context = msg.getContext ();
		if (context==null)
			throw new AxisFault ("Context is null");
		
		System.out.println ("INCOMING SEQUENCE ID:" + incomingSeqId);
		SequencePropertyBeanMgr mgr = AbstractBeanMgrFactory.getInstance(context).getSequencePropretyBeanMgr();
		
		SequencePropertyBean bean = mgr.retrieve(incomingSeqId,Constants.SEQ_PROPERTY_OUT_SEQUENCE_ID);
		
		if(bean.getValue()==null) {
			//sequence id is not present
			//add a create sequence
			
			RMMsgContext createSeqMessage = RMMsgCreator.createCreateSeqMsg (reqMsg);
			
			
			//add msg to retransmitter with send=false;
		}else {
			//Sequence id is present
			//set sequence part
			//add message to retransmitter table with send=true;
		}
		
	}
}
