/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *  
 */

package org.apache.sandesha2.storage.inmemory;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class InMemoryStorageManager extends StorageManager {

	private static InMemoryStorageManager instance = null;

	public InMemoryStorageManager(ConfigurationContext context) {
		super(context);
	}

	public Transaction getTransaction() {
		return new InMemoryTransaction();
	}

	public CreateSeqBeanMgr getCreateSeqBeanMgr() {
		return new InMemoryCreateSeqBeanMgr(getContext());
	}

	public NextMsgBeanMgr getNextMsgBeanMgr() {
		return new InMemoryNextMsgBeanMgr(getContext());
	}

	public RetransmitterBeanMgr getRetransmitterBeanMgr() {
		return new InMemoryRetransmitterBeanMgr(getContext());
	}

	public SequencePropertyBeanMgr getSequencePropretyBeanMgr() {
		return new InMemorySequencePropertyBeanMgr(getContext());
	}

	public StorageMapBeanMgr getStorageMapBeanMgr() {
		return new InMemoryStorageMapBeanMgr(getContext());
	}

	//	public static StorageManager getBeanMgrFactory (int storageType)
	// {
	//		switch (storageType) {
	//			case Constants.STORAGE_TYPE_PERSISTANCE:
	//				return new PersistentBeanMgrFactory();
	//			case Constants.STORAGE_TYPE_IN_MEMORY:
	//				return new InMemBeanMgrFactory();
	//			default:
	//				return null;
	//		}
	//	}

	public void init(ConfigurationContext context) {
		setContext(context);
	}

	public static InMemoryStorageManager getInstance(
			ConfigurationContext context) {
		if (instance == null)
			instance = new InMemoryStorageManager(context);

		return instance;
	}
}