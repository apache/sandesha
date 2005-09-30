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

package org.apache.sandesha2.msgprocessors;

import org.apache.sandesha2.Constants;

/**
 * @author 
 * 
 */
public class MsgProcessorFactory {

	public static MsgProcessor getMessageProcessor (int msgType) {
		switch (msgType)
		{
			case (Constants.MessageTypes.CREATE_SEQ):
				return new CreateSeqMsgProcessor ();
			case (Constants.MessageTypes.TERMINATE_SEQ):
				return new TerminateSeqMsgProcessor ();
			case (Constants.MessageTypes.APPLICATION):
				return new ApplicationMsgProcessor ();
            case (Constants.MessageTypes.CREATE_SEQ_RESPONSE):
                return new CreateSeqResponseMsgProcessor();
            default:
				return null;
		}
	}
}
