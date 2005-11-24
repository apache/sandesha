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
import org.apache.sandesha2.Constants;
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
		Object obj = context.getProperty(Constants.BeanMAPs.RETRANSMITTER);
		if (obj != null) {
			table = (Hashtable) obj;
		} else {
			table = new Hashtable();
			context.setProperty(Constants.BeanMAPs.RETRANSMITTER, table);
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

			if (bean.getMessagetype() != Constants.MessageTypes.UNKNOWN
					&& bean.getMessagetype() != temp.getMessagetype())
				add = false;

			if (add)
				beans.add(temp);
		}

		return beans;
	}

	public Collection findMsgsToSend() {
		ArrayList beans = new ArrayList();
		Iterator iterator = table.values().iterator();

		SenderBean temp;

		while (iterator.hasNext()) {
			temp = (SenderBean) iterator.next();

			if (temp.isSend()) {

				long timeToSend = temp.getTimeToSend();
				long timeNow = System.currentTimeMillis();
				if ((timeNow >= timeToSend)) {
					beans.add(temp);
				}
			}
		}

		//		beans = sort (beans);
		//		beans = reverse (beans);

		return beans;
	}

	private ArrayList reverse(ArrayList beans) {
		ArrayList newBeans = new ArrayList();
		int count = beans.size();

		for (int i = count; i > 0; i--) {
			newBeans.add(beans.get((i - 1)));
		}

		return newBeans;
	}

	//FIXME - not complete
	//SENDER SORTING
	//--------------
	//Sender Sorting is used to arrange the messages that get sent.
	//This sending order may get dsturbed due to network latencies.
	//But doing the sort here, could improve the server preformance when
	// network latencies are low (this is the common case).
	//Sender sorting will be enabled, when invocation type is InOrder.
	private ArrayList sort(ArrayList beans) {
		ArrayList newBeans = new ArrayList();
		HashMap tempHash = new HashMap();

		Iterator iter1 = beans.iterator();
		while (iter1.hasNext()) {
			SenderBean bean = (SenderBean) iter1.next();
			if (!(bean.getMessageNumber() > 0)) {
				newBeans.add(bean);
			}
		}

		Iterator iter2 = beans.iterator();
		long maxMsgNo = 0;
		long minMsgNo = 0;
		while (iter2.hasNext()) {
			SenderBean bean = (SenderBean) iter2.next();

			if (bean.getMessageNumber() > 0) {
				maxMsgNo = bean.getMessageNumber();
				minMsgNo = bean.getMessageNumber();
				break;
			}
		}

		//finding Max and Min msg numbers present in the current list.
		while (iter2.hasNext()) {
			SenderBean bean = (SenderBean) iter2.next();
			long msgNo = bean.getMessageNumber();
			if (msgNo > 0) {
				//tempHash.put(new Long (bean.getMessageNumber()),bean);
				if (msgNo > maxMsgNo)
					maxMsgNo = msgNo;

				if (msgNo < minMsgNo)
					minMsgNo = msgNo;
			}
		}

		for (long msgNo = minMsgNo; msgNo <= maxMsgNo; msgNo++) {
			ArrayList beansOfMsgNo = findBeansWithMsgNo(beans, msgNo);
			Iterator iter = beansOfMsgNo.iterator();
			while (iter.hasNext()) {

			}
		}

		return newBeans;
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

		return true; //No need to update. Being a reference does the job:)
					 // //table.put(bean.getMessageId(), bean) != null;
	}

}