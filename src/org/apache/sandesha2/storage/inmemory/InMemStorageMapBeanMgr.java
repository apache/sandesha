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

import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class InMemStorageMapBeanMgr /* implements StorageMapBeanMgr */{

	/*
	 * private static Hashtable table = new Hashtable();
	 * 
	 * 
	 * public InMemStorageMapBeanMgr() { }
	 * 
	 * public boolean insert(StorageMapBean bean) { table.put(bean.getKey(),
	 * bean); return true; }
	 * 
	 * public boolean delete(String key) { return table.remove(key) != null; }
	 * 
	 * public StorageMapBean retrieve(String key) { return (StorageMapBean)
	 * table.get(key); }
	 * 
	 * public ResultSet find(String query) { throw new
	 * UnsupportedOperationException("selectRS() is not implemented"); }
	 * 
	 * public Collection find(StorageMapBean bean) { ArrayList beans = new
	 * ArrayList(); Iterator iterator = table.values().iterator();
	 * 
	 * StorageMapBean temp = new StorageMapBean(); while (iterator.hasNext()) {
	 * temp = (StorageMapBean) iterator.next(); boolean select = true;
	 * 
	 * //}
	 * 
	 * if (bean.getKey()!=null && !bean.getKey().equals(temp.getKey()))
	 * select=false;
	 * 
	 * if (bean.getMsgNo()!=0 && bean.getMsgNo()!=temp.getMsgNo()) select=false;
	 * 
	 * if (bean.getSequenceId()!=null &&
	 * !bean.getSequenceId().equals(temp.getSequenceId())) select=false;
	 * 
	 * if (select) beans.add(temp); } return beans; }
	 * 
	 * public boolean update(StorageMapBean bean) { return
	 * table.put(bean.getKey(), bean) != null; }
	 */

}