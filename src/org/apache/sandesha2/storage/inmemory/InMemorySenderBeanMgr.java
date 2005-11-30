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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.context.AbstractContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class InMemorySenderBeanMgr implements SenderBeanMgr {
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

	public boolean insert(SenderBean bean) throws SandeshaException {
		if (bean.getMessageId() == null)
			throw new SandeshaException("Key (MessageId) is null. Cant insert.");
		table.put(bean.getMessageId(), bean);
		return true;
	}

	public ResultSet find(String query) {
		throw new UnsupportedOperationException("selectRS() is not supported");
	}

	public Collection find(SenderBean bean) {
		ArrayList beans = new ArrayList();
		Iterator iterator = table.values().iterator();

		SenderBean temp;
		while (iterator.hasNext()) {

			temp = (SenderBean) iterator.next();

			boolean add = true;

			if (bean.getKey() != null && !bean.getKey().equals(temp.getKey()))
				add = false;

			if (bean.getTimeToSend() > 0
					&& bean.getTimeToSend() != temp.getTimeToSend())
				add = false;

			if (bean.getMessageId() != null
					&& !bean.getMessageId().equals(temp.getMessageId()))
				add = false;

			if (bean.getInternalSequenceId() != null
					&& !bean.getInternalSequenceId().equals(
							temp.getInternalSequenceId()))
				add = false;

			if (bean.getMessageNumber() > 0
					&& bean.getMessageNumber() != temp.getMessageNumber())
				add = false;

			if (bean.getMessagetype() != Sandesha2Constants.MessageTypes.UNKNOWN
					&& bean.getMessagetype() != temp.getMessagetype())
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

}