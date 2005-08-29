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

package org.apache.sandesha2.storage;

import java.awt.Stroke;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;

/**
 * @author
 * 
 */
public class InMemoryStorageMgr implements StorageManager {
	
	private static InMemoryStorageMgr self;
	
	Hashtable createSeqTable = new Hashtable(),
			  nextMsgTable	 = new Hashtable(),
			  retransmitterBeanTable = new Hashtable(),
			  storageMapBeanTable = new Hashtable();
	
	private InMemoryStorageMgr() {
	}

	synchronized static InMemoryStorageMgr getInstance() {
		if (self == null) {
			self = new InMemoryStorageMgr();
		}
		return self;
	}
	
	public boolean createCreateSeq(CreateSeqBean bean) {
		createSeqTable.put(bean.getCreateSeqMsgId(), bean);
		return true;
	}

	public boolean deleteCreateSeq(String key) {
		createSeqTable.remove(key);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retriveCreateSeq(java.lang.String)
	 */
	public CreateSeqBean retriveCreateSeq(String key) {
		if (createSeqTable.containsKey(key)) {
			return (CreateSeqBean) createSeqTable.get(key);
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updataCreateSeq(org.apache.sandesha2.storage.beans.CreateSeqBean)
	 */
	public boolean updataCreateSeq(CreateSeqBean bean) {
		createSeqTable.put(bean.getSequenceId(), bean);
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#createNextMsg(org.apache.sandesha2.storage.beans.NextMsgBean)
	 */
	public boolean createNextMsg(NextMsgBean bean) {
		nextMsgTable.put(bean.getSequenceId(), bean);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#createRetransmitterBean(org.apache.sandesha2.storage.beans.RetransmitterBean)
	 */
	public boolean createRetransmitterBean(RetransmitterBean bean) {
		retransmitterBeanTable.put(bean.getMessageId(), bean);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#createStorageMapBean(org.apache.sandesha2.storage.beans.StorageMapBean)
	 */
	public boolean createStorageMapBean(StorageMapBean bean) {
		storageMapBeanTable.put(bean.getKey(), bean);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#deleteNextMsgBean(java.lang.String)
	 */
	public boolean deleteNextMsgBean(String key) {
		nextMsgTable.remove(key);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#deleteRetransmitterBean(java.lang.String)
	 */
	public boolean deleteRetransmitterBean(String key) {
		retransmitterBeanTable.remove(key);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#deleteStorageMapBean(java.lang.String)
	 */
	public boolean deleteStorageMapBean(String key) {
		storageMapBeanTable.remove(key);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retrieveNextMsgBean(java.lang.String)
	 */
	public NextMsgBean retrieveNextMsgBean(String key) {
		return (NextMsgBean) nextMsgTable.get(key);
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retrieveRetransmitterBean(java.lang.String)
	 */
	public RetransmitterBean retrieveRetransmitterBean(String key) {
		return (RetransmitterBean) retransmitterBeanTable.get(key);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retrieveStorageMapBean(java.lang.String)
	 */
	public StorageMapBean retrieveStorageMapBean(String key) {
		return (StorageMapBean) storageMapBeanTable.get(key);
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updateCreateSeq(org.apache.sandesha2.storage.beans.CreateSeqBean)
	 */
	public boolean updateCreateSeq(CreateSeqBean bean) {
		createSeqTable.put(bean.getCreateSeqMsgId(), bean);
		return true;
	}
	
	public boolean updateNextMsgBean(NextMsgBean bean) {
		nextMsgTable.put(bean.getSequenceId(), bean);
		return true;
	}
	
	public boolean updateRetransmitterBean(RetransmitterBean bean) {
		retransmitterBeanTable.put(bean.getMessageId(), bean);
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updateStorageMapBean(org.apache.sandesha2.storage.beans.StorageMapBean)
	 */
	public boolean updateStorageMapBean(StorageMapBean bean) {
		storageMapBeanTable.put(bean.getKey(), bean);
		// TODO Auto-generated method stub
		return false;
	}
}
