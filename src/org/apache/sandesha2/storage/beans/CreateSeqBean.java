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

public class CreateSeqBean implements RMBean {
	private String CreateSeqMsgId;
	private String SequenceId;
	
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
	 * @return Returns the sequenceId.
	 */
	public String getSequenceId() {
		return SequenceId;
	}
	/**
	 * @param sequenceId The sequenceId to set.
	 */
	public void setSequenceId(String sequenceId) {
		SequenceId = sequenceId;
	}
}
