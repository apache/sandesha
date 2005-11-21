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

package org.apache.sandesha2.msgprocessors;

import javax.xml.namespace.QName;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.TerminateManager;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class TerminateSeqMsgProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext terminateSeqRMMSg)
			throws SandeshaException {

		MessageContext terminateSeqMsg = terminateSeqRMMSg.getMessageContext();
		//Processing for ack if any
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) terminateSeqRMMSg
				.getMessagePart(Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck != null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor();
			ackProcessor.processMessage(terminateSeqRMMSg);
		}

		//Processing the terminate message
		//TODO Add terminate sequence message logic.
		TerminateSequence terminateSequence = (TerminateSequence) terminateSeqRMMSg.getMessagePart(Constants.MessageParts.TERMINATE_SEQ);
		if (terminateSequence==null)
			throw new SandeshaException ("Terminate Sequence part is not available");
		
		String sequenceId = terminateSequence.getIdentifier().getIdentifier();
		if (sequenceId==null || "".equals(sequenceId))
			throw new SandeshaException ("Invalid sequence id");
		
		ConfigurationContext context = terminateSeqMsg.getSystemContext();

		
		TerminateManager.terminateReceivingSide(context,sequenceId);
		

		terminateSeqMsg.setPausedTrue(new QName(Constants.IN_HANDLER_NAME));

	}
}