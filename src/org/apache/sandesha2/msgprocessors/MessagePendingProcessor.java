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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.polling.PollingManager;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.MessagePending;
import org.apache.sandesha2.wsrm.Sequence;

public class MessagePendingProcessor {

	private static final Log log = LogFactory.getLog(MessagePendingProcessor.class);
	
	public boolean processMessagePendingHeaders (MessageContext message) throws AxisFault {
		
		if (log.isDebugEnabled())
			log.debug("Enter: MessagePendingProcessor::processMessagePendingHeaders");

		boolean messagePaused = false;
		
		RMMsgContext rmMsgContext = MsgInitializer.initializeMessage(message);
		Sequence sequence = (Sequence) rmMsgContext.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
		MessagePending messagePending = (MessagePending) rmMsgContext.getMessagePart(Sandesha2Constants.MessageParts.MESSAGE_PENDING);
		
		if (sequence!=null) {
			String sequenceId = sequence.getIdentifier().getIdentifier();
			
			if (messagePending!=null) {
				boolean pending = messagePending.isPending();
				if (pending) {
					PollingManager pollingManager = SandeshaUtil.getPollingManager(message.getConfigurationContext());
					if (pollingManager!=null) {
						pollingManager.shedulePollingRequest(sequenceId);
					}
				}
			}
		}
			
		
		
		if (log.isDebugEnabled())
			log.debug("Exit: MessagePendingProcessor::processMessagePendingHeaders");

		return messagePaused;
	}

}
