/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.apache.sandesha2.storage.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.context.AbstractContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class InMemorySenderBeanMgr implements SenderBeanMgr {
	
	Log log = LogFactory.getLog(getClass());
	private Hashtable table = null;

	public InMemorySenderBeanMgr(AbstractContext context) {
		Object obj = context.getProperty(Sandesha2Constants.BeanMAPs.RETRANSMITTER);
		if (obj != null) {
			table = (Hashtable) obj;
		} else {
			table = new Hashtable();
			context.setProperty(Sandesha2Constants.BeanMAPs.RETRANSMITTER, table);
		}
	}

	public boolean delete(String MessageId) {
		return table.remove(MessageId) != null;
	}

	public SenderBean retrieve(String MessageId) {
		return (SenderBean) table.get(MessageId);
	}

	public boolean insert(SenderBean bean) throws SandeshaStorageException {
		if (bean.getMessageID() == null)
			throw new SandeshaStorageException("Key (MessageId) is null. Cant insert.");
		table.put(bean.getMessageID(), bean);
		return true;
	}

	public Collection find(String internalSequenceID) {
		
		ArrayList arrayList = new ArrayList ();
		if (internalSequenceID==null || "".equals(internalSequenceID))
			return arrayList;
		
		Iterator iterator = table.keySet().iterator();
		
		while (iterator.hasNext()) {
			SenderBean senderBean = (SenderBean) table.get(iterator.next());
			if (internalSequenceID.equals(senderBean.getInternalSequenceID())) 
					arrayList.add(senderBean);
		}
		
		return arrayList;
	}

	public Collection find(SenderBean bean) {
		ArrayList beans = new ArrayList();
		Iterator iterator = table.values().iterator();

		SenderBean temp;
		while (iterator.hasNext()) {

			temp = (SenderBean) iterator.next();

			boolean add = true;

			if (bean.getMessageContextRefKey() != null && !bean.getMessageContextRefKey().equals(temp.getMessageContextRefKey()))
				add = false;

			if (bean.getTimeToSend() > 0
					&& bean.getTimeToSend() != temp.getTimeToSend())
				add = false;

			if (bean.getMessageID() != null
					&& !bean.getMessageID().equals(temp.getMessageID()))
				add = false;

			if (bean.getInternalSequenceID() != null
					&& !bean.getInternalSequenceID().equals(
							temp.getInternalSequenceID()))
				add = false;

			if (bean.getMessageNumber() > 0
					&& bean.getMessageNumber() != temp.getMessageNumber())
				add = false;

			if (bean.getMessageType() != Sandesha2Constants.MessageTypes.UNKNOWN
					&& bean.getMessageType() != temp.getMessageType())
				add = false;
			
			if (bean.isSend() != temp.isSend())
				add = false;

			if (bean.isReSend() != temp.isReSend())
				add = false;
			
			if (add)
				beans.add(temp);
		}

		return beans;
	}

	public Collection findMsgsToSend() {
		ArrayList beans = new ArrayList();
		Iterator iterator = table.keySet().iterator();

		SenderBean temp;

		while (iterator.hasNext()) {
			Object key = iterator.next();
			temp = (SenderBean) table.get(key);

			if (temp.isSend()) {

				long timeToSend = temp.getTimeToSend();
				long timeNow = System.currentTimeMillis();
				if ((timeNow >= timeToSend)) {
					beans.add(temp);
				}
			}
		}

		return beans;
	}

	private ArrayList findBeansWithMsgNo(ArrayList list, long msgNo) {
		ArrayList beans = new ArrayList();

		Iterator it = list.iterator();
		while (it.hasNext()) {
			SenderBean bean = (SenderBean) it.next();
			if (bean.getMessageNumber() == msgNo)
				beans.add(bean);
		}

		return beans;
	}

	public boolean update(SenderBean bean) {
		if (!table.contains(bean))
			return false;

		return true; //No need to update. Being a reference does the job.
	}
	
	public SenderBean findUnique(SenderBean bean) throws SandeshaException {
		Collection coll = find(bean);
		if (coll.size()>1) {
			String message = "Non-Unique result";
			log.error(message);
			throw new SandeshaException (message);
		}
		
		Iterator iter = coll.iterator();
		if (iter.hasNext())
			return (SenderBean) iter.next();
		else 
			return null;
	}

	public SenderBean retrieveFromMessageRefKey(String messageContextRefKey) {
		
		Iterator iter = table.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			SenderBean bean = (SenderBean) table.get(key);
			if (bean.getMessageContextRefKey().equals(messageContextRefKey)) {
				return bean;
			}
		}
		
		return null;
	}
	
	

}