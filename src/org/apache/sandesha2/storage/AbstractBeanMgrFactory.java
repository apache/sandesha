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
package org.apache.sandesha2.storage;

import org.apache.sandesha2.Constants;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.inmemory.InMemBeanMgrFactory;
import org.apache.sandesha2.storage.persistent.PersistentBeanMgrFactory;

import com.sun.corba.se.internal.core.Constant;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public abstract class AbstractBeanMgrFactory {

	
	public abstract CreateSeqBeanMgr getCreateSeqBeanMgr();
	
	public abstract NextMsgBeanMgr getNextMsgBean();
	
	public abstract RetransmitterBeanMgr getRetransmitterBeanMgr();
	
	public abstract SequencePropertyBeanMgr getSequencePropretyBeanMgr();
	
	public abstract StorageMapBeanMgr getStorageMapBeanMgr();
		
	public static AbstractBeanMgrFactory getBeanMgrFactory (int storageType) {
		switch (storageType) {
			case Constants.STORAGE_TYPE_PERSISTANCE:
				return new PersistentBeanMgrFactory();
			case Constants.STORAGE_TYPE_IN_MEMORY:
				return new  InMemBeanMgrFactory();
			default: 
				return null;
		}
	}
}
