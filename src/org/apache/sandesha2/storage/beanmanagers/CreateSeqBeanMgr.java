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

package org.apache.sandesha2.storage.beanmanagers;

import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.StorageManagerFactory;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.RMBean;

/**
 * @author 
 * 
 */
public class CreateSeqBeanMgr implements CRUD {
	
	private StorageManager storageMgr;
	
	public CreateSeqBeanMgr(int storageType) {
		storageMgr = StorageManagerFactory.getStorageManager(storageType);
	}
	
	public boolean create(RMBean bean) {
		if (!(bean instanceof CreateSeqBean)) {
			throw new IllegalArgumentException("argument should be CreateSeqBean type");
		}
		return storageMgr.createCreateSeq((CreateSeqBean) bean);
	}

	public boolean delete(String key) {
		return storageMgr.deleteCreateSeq(key);
	}
	
	public RMBean retrieve(String key) {
		return storageMgr.retrieveCreateSeq(key);
	}
	
	public boolean update(RMBean bean) {
		return storageMgr.updateCreateSeq((CreateSeqBean) bean);
	}
}
