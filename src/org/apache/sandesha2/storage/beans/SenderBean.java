/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.sandesha2.storage.beans;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class SenderBean implements RMBean {

	private String messageId;

	private String key;

//	private long LastSentTime;

	private boolean Send;

	private String internalSequenceId;

	private int sentCount = 0;

	private long messageNumber = 0;

	private boolean reSend = true;

	private long timeToSend = 0;
	
	private int messagetype =0;
	
	public SenderBean() {

	}

	public SenderBean(String messageId, String key,
			boolean send,long timeToSend, String internalSequenceId, long messageNumber) {
		this.messageId = messageId;
		this.key = key;
		//this.LastSentTime = lastSentTime;
		this.timeToSend = timeToSend;
		this.Send = send;
		this.internalSequenceId = internalSequenceId;
		this.messageNumber = messageNumber;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

//	public long getLastSentTime() {
//		return LastSentTime;
//	}
//
//	public void setLastSentTime(long lastSentTime) {
//		LastSentTime = lastSentTime;
//	}

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

	public String getInternalSequenceId() {
		return internalSequenceId;
	}

	public void setInternalSequenceId(String internalSequenceId) {
		this.internalSequenceId = internalSequenceId;
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

	public boolean isReSend() {
		return reSend;
	}

	public void setReSend(boolean reSend) {
		this.reSend = reSend;
	}
	
	public long getTimeToSend() {
		return timeToSend;
	}
	
	public void setTimeToSend(long timeToSend) {
		this.timeToSend = timeToSend;
	}
	
	
	public int getMessagetype() {
		return messagetype;
	}
	
	public void setMessagetype(int messagetype) {
		this.messagetype = messagetype;
	}
}