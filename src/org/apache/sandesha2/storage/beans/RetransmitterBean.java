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
 * 
 */
public class RetransmitterBean implements Serializable {

	private String messageId;
	private String key;
	private long LastSentTime;
	private Boolean Send; 
	private String CreateSeqMsgId;
	
	
	/**
	 * @return Returns the createSeqMsgId.
	 */
	public String getCreateSeqMsgId() {
		return CreateSeqMsgId;
	}
	/**
	 * @param createSeqMsgId The createSeqMsgId to set.
	 */
	public void setCreateSeqMsgId(String createSeqMsgId) {
		CreateSeqMsgId = createSeqMsgId;
	}
	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return Returns the lastSentTime.
	 */
	public long getLastSentTime() {
		return LastSentTime;
	}
	/**
	 * @param lastSentTime The lastSentTime to set.
	 */
	public void setLastSentTime(long lastSentTime) {
		LastSentTime = lastSentTime;
	}
	/**
	 * @return Returns the messageId.
	 */
	public String getMessageId() {
		return messageId;
	}
	/**
	 * @param messageId The messageId to set.
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	/**
	 * @return Returns the send.
	 */
	public Boolean isSend() {
		return Send;
	}
	/**
	 * @param send The send to set.
	 */
	public void setSend(Boolean send) {
		this.Send = send;
	}
}
