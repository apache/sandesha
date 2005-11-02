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

package org.apache.sandesha2.storage.beans;

import java.io.Serializable;

/**
 * @author  
 */
public class StorageMapBean implements RMBean {

	private String Key;

	private long MsgNo;

	private String sequenceId;
	
	private boolean invoked = false;

	public StorageMapBean() {

	}

	public StorageMapBean(String key, long msgNo, String sequenceId) {
		this.Key = key;
		this.MsgNo = msgNo;
		this.sequenceId = sequenceId;
	}

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return Key;
	}

	/**
	 * @param key
	 *            The key to set.
	 */
	public void setKey(String key) {
		Key = key;
	}

	/**
	 * @return Returns the msgNo.
	 */
	public long getMsgNo() {
		return MsgNo;
	}

	/**
	 * @param msgNo
	 *            The msgNo to set.
	 */
	public void setMsgNo(long msgNo) {
		MsgNo = msgNo;
	}

	/**
	 * @return Returns the sequenceId.
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * @param sequenceId
	 *            The sequenceId to set.
	 */
	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public boolean isInvoked() {
		return invoked;
	}
	
	public void setInvoked(boolean invoked) {
		this.invoked = invoked;
	}
}