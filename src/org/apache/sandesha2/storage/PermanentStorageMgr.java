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

import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;

/**
 * @author
 * 
 */
public class PermanentStorageMgr implements StorageManager {
	
	private static PermanentStorageMgr self;
	
	private PermanentStorageMgr() {
	}
	
	synchronized static PermanentStorageMgr getInstance() {
		if (self ==  null) {
			self = new PermanentStorageMgr();
		}
		return self;
	}
	
	public boolean createCreateSeq(CreateSeqBean bean) {
		return false;
	}
	
	public CreateSeqBean retriveCreateSeq(String key) {
		// retrieve the appropriate tuple form the table
		// use that data to create and return the Bean
		return null;
	}
	
	public boolean updataCreateSeq(CreateSeqBean bean) {
		// update the database using the data in bean
		return false;
	}
	
	public boolean deleteCreateSeq(String key) {
		// delete the recored which is identified by this key..
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#createNextMsg(org.apache.sandesha2.storage.beans.NextMsgBean)
	 */
	public boolean createNextMsg(NextMsgBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#createRetransmitterBean(org.apache.sandesha2.storage.beans.RetransmitterBean)
	 */
	public boolean createRetransmitterBean(RetransmitterBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#createStorageMapBean(org.apache.sandesha2.storage.beans.StorageMapBean)
	 */
	public boolean createStorageMapBean(StorageMapBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#deleteNextMsgBean(java.lang.String)
	 */
	public boolean deleteNextMsgBean(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#deleteRetransmitterBean(java.lang.String)
	 */
	public boolean deleteRetransmitterBean(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#deleteStorageMapBean(java.lang.String)
	 */
	public boolean deleteStorageMapBean(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retrieveNextMsgBean(java.lang.String)
	 */
	public NextMsgBean retrieveNextMsgBean(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retrieveRetransmitterBean(java.lang.String)
	 */
	public RetransmitterBean retrieveRetransmitterBean(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#retrieveStorageMapBean(java.lang.String)
	 */
	public StorageMapBean retrieveStorageMapBean(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updateCreateSeq(org.apache.sandesha2.storage.beans.CreateSeqBean)
	 */
	public boolean updateCreateSeq(CreateSeqBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updateNextMsgBean(org.apache.sandesha2.storage.beans.NextMsgBean)
	 */
	public boolean updateNextMsgBean(NextMsgBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updateRetransmitterBean(java.lang.String)
	 */
	public boolean updateRetransmitterBean(RetransmitterBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.StorageManager#updateStorageMapBean(org.apache.sandesha2.storage.beans.StorageMapBean)
	 */
	public boolean updateStorageMapBean(StorageMapBean bean) {
		// TODO Auto-generated method stub
		return false;
	}
}
