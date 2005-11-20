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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public abstract class StorageManager {

	private ConfigurationContext context;

	public StorageManager(ConfigurationContext context) {
		this.context = context;
	}

	public ConfigurationContext getContext() {
		return context;
	}

	public void setContext(ConfigurationContext context) {
		if (context != null)
			this.context = context;
	}

	public abstract Transaction getTransaction();

	public abstract CreateSeqBeanMgr getCreateSeqBeanMgr();

	public abstract NextMsgBeanMgr getNextMsgBeanMgr();

	public abstract RetransmitterBeanMgr getRetransmitterBeanMgr();

	public abstract SequencePropertyBeanMgr getSequencePropretyBeanMgr();

	public abstract StorageMapBeanMgr getStorageMapBeanMgr();

}