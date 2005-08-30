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

import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.StorageManagerFactory;
import org.apache.sandesha2.storage.beans.RMBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class SequencePropertyBeanMgr implements CRUD {

	private StorageManager storageMgr;
	
	public SequencePropertyBeanMgr(int storageType){
		storageMgr = StorageManagerFactory.getStorageManager(storageType);
	}
	
	public boolean create(RMBean key) {
		if (!(key instanceof SequencePropertyBean)) {
			throw new IllegalArgumentException();
		}
		return storageMgr.createSequencePropertyBean((SequencePropertyBean) key);
	}

	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.beanmanagers.CRUD#retrieve(java.lang.String)
	 */
	public RMBean retrieve(String key) {
		return storageMgr.retrieveSequencePropertyBean(key);
	}
	
	public RMBean retrieve(String sequenceId, String name) {
		return retrieve(sequenceId + name);
	}

	/* (non-Javadoc)
	 * @see org.apache.sandesha2.storage.beanmanagers.CRUD#update(org.apache.sandesha2.storage.beans.RMBean)
	 */
	public boolean update(RMBean bean) {
		if (!(bean instanceof SequencePropertyBean)) {
			throw new IllegalArgumentException();
		}
		return storageMgr.updateSequencePropertyBean((SequencePropertyBean) bean);
	}

	public boolean delete(String key) {
		return storageMgr.deleteSequencePropertyBean(key);
	}

}
