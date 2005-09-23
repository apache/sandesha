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
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.wsdl.WSDLConstants;
import org.ietf.jgss.MessageProp;

/**
 * @author  
 */
public class ApplicationMsgProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

		System.out.println("Application msg processor called");

		if (rmMsgCtx.getProperty(Constants.APPLICATION_PROCESSING_DONE) != null
				&& rmMsgCtx.getProperty(Constants.APPLICATION_PROCESSING_DONE)
						.equals("true")) {
			return;
		}

		//setting acked msg no range
		Sequence sequence = (Sequence) rmMsgCtx
				.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
		String sequenceId = sequence.getIdentifier().getIdentifier();
		ConfigurationContext configCtx = rmMsgCtx.getMessageContext()
				.getSystemContext();
		if (configCtx==null)
			throw new SandeshaException ("Configuration Context is null");
		
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
				.getInstance(configCtx).getSequencePropretyBeanMgr();
		SequencePropertyBean msgsBean = seqPropMgr.retrieve(sequenceId,
				Constants.SEQ_PROPERTY_RECEIVED_MESSAGES);

		long msgNo = sequence.getMessageNumber().getMessageNumber();
		if (msgNo == 0)
			throw new SandeshaException("Wrong message number");

		String messagesStr = (String) msgsBean.getValue();

		if (msgNoPresentInList(messagesStr, msgNo)
				&& (Constants.DEFAULT_INVOCATION_TYPE == Constants.EXACTLY_ONCE)) {
			//this is a duplicate message and the invocation type is
			// EXACTLY_ONCE.
			throw new SandeshaException(
					"Duplicate message - Invocation type is EXACTLY_ONCE");
		}

		if (messagesStr != "" && messagesStr != null)
			messagesStr = messagesStr + "," + Long.toString(msgNo);
		else
			messagesStr = Long.toString(msgNo);

		msgsBean.setValue(messagesStr);
		seqPropMgr.update(msgsBean);

		//Setting the ack depending on AcksTo.
		//TODO: Stop sending askc for every message.
		SequencePropertyBean acksToBean = seqPropMgr.retrieve(sequenceId,
				Constants.SEQ_PROPERTY_ACKS_TO_EPR);
		String acksToStr = null;
		try {
			EndpointReference acksTo = (EndpointReference) acksToBean.getValue();
			acksToStr = acksTo.getAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//TODO: remove folowing 2.
		System.out.println("Messages received:" + messagesStr);
		System.out.println("Acks To:" + acksToStr);

		if (acksToStr == null || messagesStr == null)
			throw new SandeshaException(
					"Seqeunce properties are not set correctly");

		if (acksToStr.equals(Constants.WSA.NS_URI_ANONYMOUS)) {

			//Adding sync ack
			//set acknowledgement
			//TODO stop adding acks to every message. Add acks only when
			// needed.
			
			
//			try {
//				MessageContext responseMsgCtx = rmMsgCtx.getMessageContext().getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT);		
//				if (responseMsgCtx==null){
//					responseMsgCtx = new MessageContext (configCtx,rmMsgCtx.getMessageContext().getTransportIn(),rmMsgCtx.getMessageContext().getTransportOut());
//					rmMsgCtx.getMessageContext().getOperationContext().addMessageContext(responseMsgCtx);
//					
//					//TODO following line is due to a bug in Axis2. Remove this when it is fixed.
//					responseMsgCtx.setOperationContext(rmMsgCtx.getMessageContext().getOperationContext());
//				}
//				
//				RMMsgContext responseRMMsg = new RMMsgContext (responseMsgCtx);
//				RMMsgCreator.addAckMessage(responseRMMsg);
//				
//			} catch (AxisFault af) {
//				throw new SandeshaException (af.getMessage());
//			} 

		} else {
			//TODO Add async Ack
		}

		//		Pause the messages bean if not the right message to invoke.
		NextMsgBeanMgr mgr = AbstractBeanMgrFactory.getInstance(configCtx)
				.getNextMsgBeanMgr();
		NextMsgBean bean = mgr.retrieve(sequenceId);

		if (bean == null)
			throw new SandeshaException("Error- The sequence does not exist");

		StorageMapBeanMgr storageMapMgr = AbstractBeanMgrFactory.getInstance(
				configCtx).getStorageMapBeanMgr();

		long nextMsgno = bean.getNextMsgNoToProcess();

		if (nextMsgno < msgNo) {

			//pause and store the message (since it is not the next message of
			// the order)
			//rmMsgCtx.getMessageContext().setPausedTrue(new QName
			// (Constants.IN_HANDLER_NAME));

			try {
				String key = SandeshaUtil.storeMessageContext(rmMsgCtx
						.getMessageContext());
				storageMapMgr
						.insert(new StorageMapBean(key, msgNo, sequenceId));

				//This will avoid performing application processing more than
				// once.
				rmMsgCtx.setProperty(Constants.APPLICATION_PROCESSING_DONE,
						"true");

			} catch (Exception ex) {
				throw new SandeshaException(ex.getMessage());
			}
		} else {
			//OK this is a correct message.
			//(nextMsgNo>msgNo can not happen if EXCTLY_ONCE is enabled. This
			// should have been
			//		detected previously)

			if (Constants.DEFAULT_DELIVERY_ASSURANCE == Constants.IN_ORDER) {
				//store and let invoker handle for IN_ORDER invocation
				//rmMsgCtx.getMessageContext().setPausedTrue(new QName
				// (Constants.IN_HANDLER_NAME));

				try {
					String key = SandeshaUtil.storeMessageContext(rmMsgCtx
							.getMessageContext());
					storageMapMgr.insert(new StorageMapBean(key, msgNo,
							sequenceId));

					//					This will avoid performing application processing more
					// than once.
					rmMsgCtx.setProperty(Constants.APPLICATION_PROCESSING_DONE,
							"true");

					System.out.println("paused");
				} catch (Exception ex) {
					throw new SandeshaException(ex.getMessage());
				}
			} else {
				//if IN_ORDER is not required. Simply let this invoke (by doing
				// nothing here :D )
			}
		}

		int i = 1;
	}

	//TODO convert following from INT to LONG
	private boolean msgNoPresentInList(String list, long no) {
		String[] msgStrs = list.split(",");

		int l = msgStrs.length;

		for (int i = 0; i < l; i++) {
			if (msgStrs[i].equals(Long.toString(no)))
				return true;
		}

		return false;
	}
}