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
package org.apache.sandesha;

import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPEnvelope;

/**
 * @author JEkanayake
*/
public class RMMessageContext {
	
	private MessageContext msgContext;
	private SOAPEnvelope reqEnv;
	private SOAPEnvelope resEnv;
	private Object obj;
	private String sequenceID;
	private String messageID;

	/**
	 * @return
	 */
	public MessageContext getMsgContext() {
		return msgContext;
	}

	/**
	 * @return
	 */
	public Object getObj() {
		return obj;
	}

	/**
	 * @return
	 */
	public SOAPEnvelope getReqEnv() {
		return reqEnv;
	}

	/**
	 * @return
	 */
	public SOAPEnvelope getResEnv() {
		return resEnv;
	}

	/**
	 * @return
	 */
	public String getSequenceID() {
		return sequenceID;
	}

	/**
	 * @param context
	 */
	public void setMsgContext(MessageContext msgContext) {
		this.msgContext = msgContext;
	}

	/**
	 * @param object
	 */
	public void setObj(Object obj) {
		this.obj = obj;
	}

	/**
	 * @param envelope
	 */
	public void setReqEnv(SOAPEnvelope reqEnv) {
		this.reqEnv = reqEnv;
	}

	/**
	 * @param envelope
	 */
	public void setResEnv(SOAPEnvelope resEnv) {
		this.resEnv = resEnv;
	}

	/**
	 * @param string
	 */
	public void setSequenceID(String sequenceID) {
		this.sequenceID = sequenceID;
	}

}
