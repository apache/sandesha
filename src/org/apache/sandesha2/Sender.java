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

import java.util.Collection;
import java.util.Iterator;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;

import com.sun.rsasign.m;

public class Sender extends Thread {

	private boolean senderStarted = false;
	private ConfigurationContext context = null;
	
	public synchronized void stopSender () {
	    senderStarted = false;
	}
	
	public synchronized boolean isSenderStarted () {
		return senderStarted;
	}
	
	public void run () {
		
		while (senderStarted) {
			System.out.println ("|-|");
			try {
				if (context==null)
					throw new SandeshaException ("Can't continue the Sender. Context is null");
			} catch (SandeshaException e) {
				e.printStackTrace();
				return;
			}
			
			RetransmitterBeanMgr mgr = AbstractBeanMgrFactory.getInstance(context).getRetransmitterBeanMgr();
			Collection coll = mgr.findMsgsToSend();
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				 RetransmitterBean bean = (RetransmitterBean) iter.next();
				 String key = (String) bean.getKey();
				 MessageContext msgCtx = SandeshaUtil.getStoredMessageContext(key);
					
				 try {	 
				 	updateMessage (msgCtx);
					new AxisEngine(context).send(msgCtx);
				} catch (AxisFault e1) {
					e1.printStackTrace();
				}catch (SandeshaException e2){
					e2.printStackTrace();
				}
				
				//changing the values of the sent bean.
			    bean.setLastSentTime(System.currentTimeMillis());
			    bean.setSentCount(bean.getSentCount()+1);
			    mgr.update(bean);
				
				
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	public void start (ConfigurationContext context) {
		System.out.println ("Starting the sender......");
		senderStarted = true;
		this.context = context;
		super.start();
	}
	
	private void updateMessage (MessageContext msgCtx) throws SandeshaException {
		try {
			RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
			
			if (rmMsgCtx.getMessageType()==Constants.MessageTypes.CREATE_SEQ) {
				if (msgCtx.getOperationDescription()==null)
					throw new SandeshaException ("Operation description is null");
				
				msgCtx.getOperationDescription().setMessageReceiver(new RMMessageReceiver ());
				OperationContext createSeqOpCtx = new OperationContext (msgCtx.getOperationDescription());
				createSeqOpCtx.addMessageContext(msgCtx);
				msgCtx.setOperationContext(createSeqOpCtx);
				if (msgCtx.getSystemContext()!=null)
					msgCtx.getSystemContext().registerOperationContext(msgCtx.getMessageID(),createSeqOpCtx);
				else
					throw new SandeshaException ("System context is null");
			}
		} catch (AxisFault e) {
			throw new SandeshaException ("Exception in updating contexts");
		}
	}
	
}
