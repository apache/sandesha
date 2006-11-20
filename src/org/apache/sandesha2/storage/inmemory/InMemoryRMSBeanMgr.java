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
import java.util.List;

import org.apache.axis2.context.AbstractContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.storage.beanmanagers.RMSBeanMgr;
import org.apache.sandesha2.storage.beans.RMSBean;

public class InMemoryRMSBeanMgr implements RMSBeanMgr {

	private static final Log log = LogFactory.getLog(InMemoryRMSBeanMgr.class);
	private Hashtable table = null;
	

	public InMemoryRMSBeanMgr(AbstractContext context) {
		Object obj = context.getProperty(Sandesha2Constants.BeanMAPs.CREATE_SEQUECE);
		if (obj != null) {
			table = (Hashtable) obj;
		} else {
			table = new Hashtable();
			context.setProperty(Sandesha2Constants.BeanMAPs.CREATE_SEQUECE, table);
		}
	}

	public synchronized boolean insert(RMSBean bean) {
		table.put(bean.getCreateSeqMsgID(), bean);
		return true;
	}

	public synchronized boolean delete(String msgId) {
		return table.remove(msgId) != null;
	}

	public synchronized RMSBean retrieve(String msgId) {
		return (RMSBean) table.get(msgId);
	}

	public synchronized boolean update(RMSBean bean) {
		if (table.get(bean.getCreateSeqMsgID())==null)
			return false;

		return table.put(bean.getCreateSeqMsgID(), bean) != null;
	}

	public synchronized List find(RMSBean bean) {
		ArrayList beans = new ArrayList();
		if (bean == null)
			return beans;

		Iterator iterator = table.values().iterator();

		RMSBean temp;
		while (iterator.hasNext()) {
			temp = (RMSBean) iterator.next();

			boolean equal = true;

			if (bean.getCreateSeqMsgID() != null
					&& !bean.getCreateSeqMsgID().equals(
							temp.getCreateSeqMsgID()))
				equal = false;

			if (bean.getSequenceID() != null
					&& !bean.getSequenceID().equals(temp.getSequenceID()))
				equal = false;

			if (bean.getInternalSequenceID() != null
					&& !bean.getInternalSequenceID().equals(
							temp.getInternalSequenceID()))
				equal = false;

			if (equal)
				beans.add(temp);

		}
		return beans;
	}

	public synchronized ResultSet find(String query) {
		throw new UnsupportedOperationException(SandeshaMessageHelper.getMessage(
				SandeshaMessageKeys.selectRSNotSupported));
	}
	
	public synchronized RMSBean findUnique (RMSBean bean) throws SandeshaException {
		Collection coll = find(bean);
		if (coll.size()>1) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.nonUniqueResult);
			log.error(message);
			throw new SandeshaException (message);
		}
		
		Iterator iter = coll.iterator();
		if (iter.hasNext())
			return (RMSBean) iter.next();
		else 
			return null;
	}

}
