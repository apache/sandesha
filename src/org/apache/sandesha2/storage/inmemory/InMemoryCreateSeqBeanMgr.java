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
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.context.AbstractContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class InMemoryCreateSeqBeanMgr implements CreateSeqBeanMgr {

	private Hashtable table = null;

	//TODO change the primary key.
	//When offered. msgId==null
	/**
	 *  
	 */
	public InMemoryCreateSeqBeanMgr(AbstractContext context) {
		Object obj = context.getProperty(Constants.BeanMAPs.CREATE_SEQUECE);
		if (obj != null) {
			table = (Hashtable) obj;
		} else {
			table = new Hashtable();
			context.setProperty(Constants.BeanMAPs.CREATE_SEQUECE, table);
		}
	}

	public boolean insert(CreateSeqBean bean) {
		table.put(bean.getCreateSeqMsgId(), bean);
		return true;
	}

	public boolean delete(String msgId) {
		return table.remove(msgId) != null;
	}

	public CreateSeqBean retrieve(String msgId) {
		return (CreateSeqBean) table.get(msgId);
	}

	public boolean update(CreateSeqBean bean) {
		if (!table.contains(bean))
			return false;

		return table.put(bean.getCreateSeqMsgId(), bean) != null;
	}

	public Collection find(CreateSeqBean bean) {
		ArrayList beans = new ArrayList();
		if (bean == null)
			return beans;

		Iterator iterator = table.values().iterator();

		CreateSeqBean temp;
		while (iterator.hasNext()) {
			temp = (CreateSeqBean) iterator.next();
			//			if ((bean.getCreateSeqMsgId() != null && bean.getCreateSeqMsgId()
			//					.equals(temp.getCreateSeqMsgId()))
			//					&& (bean.getSequenceId() != null && bean.getSequenceId()
			//							.equals(bean.getSequenceId()))) {
			//				beans.add(temp);
			//
			//			}

			boolean equal = true;

			if (bean.getCreateSeqMsgId() != null
					&& !bean.getCreateSeqMsgId().equals(
							temp.getCreateSeqMsgId()))
				equal = false;

			if (bean.getSequenceId() != null
					&& !bean.getSequenceId().equals(temp.getSequenceId()))
				equal = false;

			if (bean.getTempSequenceId() != null
					&& !bean.getTempSequenceId().equals(
							temp.getTempSequenceId()))
				equal = false;

			if (equal)
				beans.add(temp);

		}
		return beans;
	}

	public ResultSet find(String query) {
		throw new UnsupportedOperationException("selectRS() is not supported");
	}

}