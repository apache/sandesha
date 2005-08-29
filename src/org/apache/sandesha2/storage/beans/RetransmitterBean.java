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

/**
 * @author 
 * 
 */
public class RetransmitterBean implements RMBean{

	private String MessageId;
	private String Key;
	private String LastSentTime;
	private boolean Send; 
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
		return Key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		Key = key;
	}
	/**
	 * @return Returns the lastSentTime.
	 */
	public String getLastSentTime() {
		return LastSentTime;
	}
	/**
	 * @param lastSentTime The lastSentTime to set.
	 */
	public void setLastSentTime(String lastSentTime) {
		LastSentTime = lastSentTime;
	}
	/**
	 * @return Returns the messageId.
	 */
	public String getMessageId() {
		return MessageId;
	}
	/**
	 * @param messageId The messageId to set.
	 */
	public void setMessageId(String messageId) {
		MessageId = messageId;
	}
	/**
	 * @return Returns the send.
	 */
	public boolean isSend() {
		return Send;
	}
	/**
	 * @param send The send to set.
	 */
	public void setSend(boolean send) {
		Send = send;
	}
}
