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
 * This bean is used at the sending side (of both server and client)
 * There is one eatry for each message to be sent.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class SenderBean implements RMBean {

	/**
	 * Comment for <code>messageId</code>
	 * The message id of the representing message.
	 * 
	 */
	private String messageId;

	/**
	 * Comment for <code>key</code>
	 * Key retrieved by the storage mechanism after storing the message.
	 */
	private String key;

	/**
	 * Comment for <code>Send</code>
	 * The sender will not send the message unless this property is true.
	 */
	private boolean Send;

	/**
	 * Comment for <code>internalSequenceId</code>
	 * Please see the comment of CreateSeqBean.
	 */
	private String internalSequenceId;

	/**
	 * Comment for <code>sentCount</code>
	 * The number of times current message has been sent.
	 */
	private int sentCount = 0;

	/**
	 * Comment for <code>messageNumber</code>
	 * The message number of the current message.
	 */
	private long messageNumber = 0;

	/**
	 * Comment for <code>reSend</code>
	 * If this property if false. The message has to be sent only once. The entry has to be deleted after sending.
	 */
	private boolean reSend = true;

	/**
	 * Comment for <code>timeToSend</code>
	 * Message has to be sent only after this time.
	 */
	private long timeToSend = 0;
	
	/**
	 * Comment for <code>messagetype</code>
	 * The type of the current message.
	 * Possible types are given in Sandesha2Constants.MessageTypes interface.
	 */
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