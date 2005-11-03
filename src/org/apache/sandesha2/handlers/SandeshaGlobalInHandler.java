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

package org.apache.sandesha2.handlers;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.msgprocessors.ApplicationMsgProcessor;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;

public class SandeshaGlobalInHandler extends AbstractHandler {

	public void invoke(MessageContext msgContext) throws AxisFault {

		RMMsgContext rmMessageContext = MsgInitializer
				.initializeMessage(msgContext);

		ConfigurationContext context = rmMessageContext.getMessageContext()
				.getSystemContext();

		Object debug = context.getProperty(Constants.SANDESHA_DEBUG_MODE);
		if (debug != null && "on".equals(debug)) {
			System.out.println("DEBUG: SandeshaGlobalInHandler got a '"
					+ SandeshaUtil.getMessageTypeString(rmMessageContext
							.getMessageType()) + "' message.");
		}

		//Dropping duplicates
		boolean dropped = dropIfDuplicate(rmMessageContext);
		if (dropped) {
			if (debug != null && "on".equals(debug)) {
				System.out.println("DEBUG: SandeshaGlobalInHandler DROPPED a '"
						+ SandeshaUtil.getMessageTypeString(rmMessageContext
								.getMessageType()) + "' message.");
			}

			processDroppedMessage(rmMessageContext);
			return;
		}

		//Process if global processing possible. - Currently none
		if (SandeshaUtil.isGloballyProcessableMessageType(rmMessageContext
				.getMessageType())) {
			doGlobalProcessing(rmMessageContext);
		}

	}

	private boolean dropIfDuplicate(RMMsgContext rmMsgContext)
			throws SandeshaException {

		boolean drop = false;

		if (rmMsgContext.getMessageType() == Constants.MessageTypes.APPLICATION) {
			Sequence sequence = (Sequence) rmMsgContext
					.getMessagePart(Constants.MessageParts.SEQUENCE);
			String sequenceId = null;

			if (sequence != null) {
				sequenceId = sequence.getIdentifier().getIdentifier();
			}

			long msgNo = sequence.getMessageNumber().getMessageNumber();

			if (sequenceId != null && msgNo > 0) {
				StorageManager storageManager = SandeshaUtil
						.getSandeshaStorageManager(rmMsgContext
								.getMessageContext().getSystemContext());
				SequencePropertyBeanMgr seqPropMgr = storageManager
						.getSequencePropretyBeanMgr();
				SequencePropertyBean receivedMsgsBean = seqPropMgr.retrieve(
						sequenceId,
						Constants.SequenceProperties.RECEIVED_MESSAGES);
				if (receivedMsgsBean != null) {
					String receivedMsgStr = (String) receivedMsgsBean
							.getValue();
					ArrayList msgNoArrList = SandeshaUtil
							.getSplittedMsgNoArraylist(receivedMsgStr);

					if (msgNoArrList.contains(new Long(msgNo).toString())) {
						drop = true;
					}
				}
			}
		}

		if (drop) {
			rmMsgContext.getMessageContext().setPausedTrue(getName());
			return true;
		}

		return false;
	}

	private void processDroppedMessage(RMMsgContext rmMsgContext)
			throws SandeshaException {
		if (rmMsgContext.getMessageType() == Constants.MessageTypes.APPLICATION) {
			Sequence sequence = (Sequence) rmMsgContext
					.getMessagePart(Constants.MessageParts.SEQUENCE);
			String sequenceId = null;

			if (sequence != null) {
				sequenceId = sequence.getIdentifier().getIdentifier();
			}

			StorageManager storageManager = SandeshaUtil
					.getSandeshaStorageManager(rmMsgContext.getMessageContext()
							.getSystemContext());
			SequencePropertyBeanMgr seqPropMgr = storageManager
					.getSequencePropretyBeanMgr();
			SequencePropertyBean receivedMsgsBean = seqPropMgr.retrieve(
					sequenceId, Constants.SequenceProperties.RECEIVED_MESSAGES);
			String receivedMsgStr = (String) receivedMsgsBean.getValue();

			ApplicationMsgProcessor ackProcessor = new ApplicationMsgProcessor();
			//Even though the duplicate message is dropped, hv to send the ack
			// if needed.
			ackProcessor.sendAckIfNeeded(rmMsgContext, receivedMsgStr);

		}
	}

	private void doGlobalProcessing(RMMsgContext rmMsgCtx)
			throws SandeshaException {
		switch (rmMsgCtx.getMessageType()) {
		case Constants.MessageTypes.ACK:
			rmMsgCtx.setRelatesTo(null); //Removing the relatesTo part from
		// ackMessageIf present.
		//Some Frameworks tend to send this.
		}
	}

	public QName getName() {
		return new QName(Constants.GLOBAL_IN_HANDLER_NAME);
	}
}