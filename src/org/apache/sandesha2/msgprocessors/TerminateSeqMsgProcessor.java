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

import java.io.SequenceInputStream;

import javax.xml.namespace.QName;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.inmemory.InMemoryCreateSeqBeanMgr;
import org.apache.sandesha2.storage.inmemory.InMemoryNextMsgBeanMgr;
import org.apache.sandesha2.storage.inmemory.InMemorySequencePropertyBeanMgr;
import org.apache.sandesha2.storage.inmemory.InMemoryStorageMapBeanMgr;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * @author Chamikara
 * @author Sanka
 */

public class TerminateSeqMsgProcessor implements MsgProcessor {

	public void processMessage(RMMsgContext terminateSeqRMMSg)
			throws SandeshaException {
		
		MessageContext terminateSeqMsg = terminateSeqRMMSg.getMessageContext();
		//Processing for ack if any
		SequenceAcknowledgement sequenceAck = (SequenceAcknowledgement) terminateSeqRMMSg.getMessagePart(Constants.MessageParts.SEQ_ACKNOWLEDGEMENT);
		if (sequenceAck!=null) {
			AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor ();
			ackProcessor.processMessage(terminateSeqRMMSg);
		}
		
		//Processing the terminate message
		//TODO Add terminate sequence message logic.
		
		terminateSeqMsg.setPausedTrue(new QName (Constants.IN_HANDLER_NAME));
		
	}
}
