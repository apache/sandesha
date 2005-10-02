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

public class RetransmitterBean implements Serializable {

	private String messageId;

	private String key;

	private long LastSentTime;

	private boolean Send;

	private String tempSequenceId;

	private int sentCount = 0;
	
	private long messageNumber = 0;
	
	public RetransmitterBean () {
		
	}

	public RetransmitterBean(String messageId, String key, long lastSentTime,
			boolean send, String tempSequenceId, long messageNumber) {
		this.messageId = messageId;
		this.key = key;
		this.LastSentTime = lastSentTime;
		this.Send = send;
		this.tempSequenceId = tempSequenceId;
		this.messageNumber = messageNumber;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getLastSentTime() {
		return LastSentTime;
	}

	public void setLastSentTime(long lastSentTime) {
		LastSentTime = lastSentTime;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public boolean isSend() {
		return Send;
	}

	public void setSend(boolean send) {
		this.Send = send;
	}

	public String getTempSequenceId() {
		return tempSequenceId;
	}

	public void setTempSequenceId(String tempSequenceId) {
		this.tempSequenceId = tempSequenceId;
	}
	
	public int getSentCount() {
		return sentCount;
	}
	
	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}
	
	public long getMessageNumber() {
		return messageNumber;
	}
	
	public void setMessageNumber(long messageNumber) {
		this.messageNumber = messageNumber;
	}
	
}