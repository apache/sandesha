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
import org.apache.sandesha2.RMException;
import org.apache.sandesha2.storage.beans.RetransmitterBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class RetransmitterBeanMgr {
	private Hashtable table = null;
	
	/**
	 * 
	 */
	public RetransmitterBeanMgr(AbstractContext context)  {
		Object obj = context.getProperty(Constants.RETRANSMITTER_BEAN_MAP);
		if (obj!=null) {
			table = (Hashtable) obj;
		}else {
			table = new Hashtable ();
			context.setProperty(Constants.RETRANSMITTER_BEAN_MAP,table);
		}
	}

	public boolean delete(String MessageId) {	
		return table.remove(MessageId) != null;
	}
	
	public RetransmitterBean retrieve(String MessageId){
		return (RetransmitterBean) table.get(MessageId);
	}

	public boolean insert(RetransmitterBean bean) {
		table.put(bean.getMessageId(), bean);
		return true;
	}

	public ResultSet find(String query)  {
		throw new UnsupportedOperationException("selectRS() is not supported");
	}

	public Collection find(RetransmitterBean bean)  {
		ArrayList beans = new ArrayList();
		Iterator iterator = table.values().iterator();
		
		RetransmitterBean temp;
		while (iterator.hasNext()) {
			temp = (RetransmitterBean) iterator.next();
			if (!(bean.getMessageId() != null 
					&& bean.getMessageId().equals(temp.getMessageId()))
					&& (bean.getCreateSeqMsgId() != null
					&& bean.getCreateSeqMsgId().equals(temp.getCreateSeqMsgId()))
					&& (bean.getKey() != null 
					&& bean.getKey().equals(temp.getKey()))
					&& (bean.getLastSentTime() != -1 
					&& bean.getLastSentTime() == temp.getLastSentTime())){
				
				beans.add(temp);				
			}
		}
		
		return beans;
	}

	public boolean update(RetransmitterBean bean) {
		return table.put(bean.getMessageId(), bean) != null;
	}

}
