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
package org.apache.sandesha2.storage.beanmanagers;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.beans.NextMsgBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class NextMsgBeanMgr {

	private Hashtable table = null;

	/**
	 *  
	 */
	public NextMsgBeanMgr(AbstractContext context) {
		Object obj = context.getProperty(Constants.BeanMAPs.NEXT_MESSAGE);

		if (obj != null) {
			table = (Hashtable) obj;
		} else {
			table = new Hashtable();
			context.setProperty(Constants.BeanMAPs.NEXT_MESSAGE, table);
		}
	}

	public boolean delete(String sequenceId) {
		return table.remove(sequenceId) != null;
	}

	public NextMsgBean retrieve(String sequenceId) {
		return (NextMsgBean) table.get(sequenceId);
	}

	public boolean insert(NextMsgBean bean) {
		table.put(bean.getSequenceId(), bean);
		return true;
	}

	public ResultSet find(String query) {
		throw new UnsupportedOperationException("selectRS() is not supported");
	}

	public Collection find(NextMsgBean bean) {
		ArrayList beans = new ArrayList();
		Iterator iterator = table.values().iterator();

		if (bean==null)
			return beans;
		
		NextMsgBean temp;
		while (iterator.hasNext()) {
			temp = (NextMsgBean) iterator.next();

//			if ((bean.getSequenceId() != null && bean.getSequenceId().equals(
//					temp.getSequenceId()))
//					/*
//					 * && (bean.getNextMsgNoToProcess() != null &&
//					 * bean.getNextMsgNoToProcess().equals(temp.getNextMsgNoToProcess()))
//					 */
//					&& (bean.getNextMsgNoToProcess() > 0)) {
//
//				beans.add(temp);
//			}
			
			
			boolean equal = true;
			
			if (bean.getNextMsgNoToProcess()>0 && bean.getNextMsgNoToProcess()!=temp.getNextMsgNoToProcess())
				equal = false;
			
			if (bean.getSequenceId()!=null && !bean.getSequenceId().equals(temp.getSequenceId()))
				equal = false;
			
			if (equal)
				beans.add(temp);
			

		}
		return beans;
	}

	public boolean update(NextMsgBean bean) {
		if (!table.contains(bean))
			return false;
		
		return table.put(bean.getSequenceId(), bean) != null;
	}

	public Collection retrieveAll() {
		return table.values();
	}
}