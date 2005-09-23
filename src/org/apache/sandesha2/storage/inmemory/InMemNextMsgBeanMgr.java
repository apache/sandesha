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

import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class InMemNextMsgBeanMgr /* implements NextMsgBeanMgr */{
	/*
	 * private static Hashtable table = new Hashtable();
	 * 
	 * public InMemNextMsgBeanMgr() { }
	 * 
	 * public boolean delete(String sequenceId) { return
	 * table.remove(sequenceId) != null; }
	 * 
	 * public NextMsgBean retrieve(String sequenceId) { return (NextMsgBean)
	 * table.get(sequenceId); }
	 * 
	 * public boolean insert(NextMsgBean bean) { table.put(bean.getSequenceId(),
	 * bean); return true; }
	 * 
	 * public ResultSet find(String query) { throw new
	 * UnsupportedOperationException("selectRS() is not supported"); }
	 * 
	 * public Collection find(NextMsgBean bean) { ArrayList beans = new
	 * ArrayList(); Iterator iterator = table.values().iterator();
	 * 
	 * NextMsgBean temp; while (iterator.hasNext()) { temp = (NextMsgBean)
	 * iterator.next();
	 * 
	 * if ((bean.getSequenceId() != null &&
	 * bean.getSequenceId().equals(temp.getSequenceId()))
	 *  && (bean.getNextMsgNoToProcess() > 0) ) {
	 * 
	 * beans.add(temp); }
	 *  } return beans; }
	 * 
	 * public boolean update(NextMsgBean bean) { return
	 * table.put(bean.getSequenceId(), bean) != null ; }
	 * 
	 * 
	 * public Collection retrieveAll() { return table.values(); }
	 */
}