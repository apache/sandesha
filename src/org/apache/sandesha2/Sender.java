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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.util.SandeshaUtil;

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
					new AxisEngine(context).send(msgCtx);
				} catch (AxisFault e1) {
					e1.printStackTrace();
				}
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
	
}
