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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.ietf.jgss.MessageProp;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya
 */

public class InOrderInvoker extends Thread {
	boolean stopInvoker = false;

	ConfigurationContext context = null;

	public synchronized void stopWork() {
		stopInvoker = true;
	}

	public synchronized boolean isStopped() {
		return stopInvoker;
	}

	public void setConfugurationContext(ConfigurationContext context) {
		this.context = context;
	}

	public void run() {

		while (!isStopped()) {

			System.out.print("|");
			NextMsgBeanMgr nextMsgMgr = AbstractBeanMgrFactory.getInstance(
					context).getNextMsgBeanMgr();

			StorageMapBeanMgr storageMapMgr = AbstractBeanMgrFactory
					.getInstance(context).getStorageMapBeanMgr();

			Collection coll = nextMsgMgr.retrieveAll();

			Iterator it = coll.iterator();

			while (it.hasNext()) {
				Object obj = it.next();
				NextMsgBean nextMsgBean = (NextMsgBean) obj;
				long msgNo = nextMsgBean.getNextMsgNoToProcess();
				boolean tryNext = true;

				while (tryNext) {
					String seqId = nextMsgBean.getSequenceId();
					Collection coll1 = storageMapMgr.find(new StorageMapBean(
							null, msgNo, seqId));
					if (coll1 == null || coll1.isEmpty()) {
						tryNext = false;
						continue;
					}

					StorageMapBean stMapBean = (StorageMapBean) coll1
							.iterator().next();
					if (stMapBean == null) {

						tryNext = false;
						continue;
					}

					String key = stMapBean.getKey();

					try {
						boolean done = resumeMessageContext(key);
						System.out.println("Resumed");
						if (!done) {
							tryNext = false;
							continue;
						}
					} catch (SandeshaException ex) {
						ex.printStackTrace();
						tryNext = false;
						continue;
					}

					msgNo++;
				}

				nextMsgBean.setNextMsgNoToProcess(msgNo);
				nextMsgMgr.update(nextMsgBean);
			}

			try {
				Thread.sleep(20000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private boolean resumeMessageContext(String key) throws SandeshaException {
		MessageContext ctx = SandeshaUtil.getStoredMessageContext(key);
		if (ctx == null)
			return false;

		ctx.setPausedTrue(new QName(Constants.IN_HANDLER_NAME)); //in case the
																 // pause is not
																 // set

		//resuming.
		try {
			new AxisEngine(ctx.getSystemContext()).receive(ctx);
		} catch (AxisFault ex) {
			throw new SandeshaException(ex.getMessage());
		}
		return true;
	}
}