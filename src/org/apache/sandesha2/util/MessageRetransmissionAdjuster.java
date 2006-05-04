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

package org.apache.sandesha2.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SandeshaListener;
import org.apache.sandesha2.client.SequenceReport;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beans.SenderBean;

/**
 * This is used to adjust retransmission infoamation after each time the message is sent.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class MessageRetransmissionAdjuster {

	Log log = LogFactory.getLog( getClass());
	
	public boolean adjustRetransmittion(
			SenderBean retransmitterBean,ConfigurationContext configContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		
		String storedKey = (String) retransmitterBean.getMessageContextRefKey();

		if (storedKey == null)
			throw new SandeshaException ("Stored Key not present in the retransmittable message");

		MessageContext messageContext = storageManager.retrieveMessageContext(storedKey,configContext);
		RMMsgContext rmMsgCtx = MsgInitializer.initializeMessage(messageContext);
		
		String internalSequenceID = retransmitterBean.getInternalSequenceID();
		String sequenceID = retransmitterBean.getSequenceID();
		
		//operation is the lowest level Sandesha2 could be attached.
		SandeshaPropertyBean propertyBean = SandeshaUtil.getPropertyBean(messageContext.getAxisOperation());
		
		retransmitterBean.setSentCount(retransmitterBean.getSentCount() + 1);
		adjustNextRetransmissionTime(retransmitterBean, propertyBean);

		int maxRetransmissionAttempts = propertyBean.getMaximumRetransmissionCount();
		
		boolean timeOutSequence = false;
		if (maxRetransmissionAttempts>=0 && retransmitterBean.getSentCount() > maxRetransmissionAttempts)
			timeOutSequence = true;		
		
		boolean sequenceTimedOut = SequenceManager.hasSequenceTimedOut(internalSequenceID, rmMsgCtx);
		if (sequenceTimedOut)
			timeOutSequence = true;
	
		boolean continueSending = true;
		if (timeOutSequence) {
			stopRetransmission(retransmitterBean);
			
			//Only messages of outgoing sequences get retransmitted. So named following method according to that.
			finalizeTimedOutSequence (internalSequenceID,sequenceID, messageContext);
			continueSending = false;
		}
		
		return continueSending;
	}

	/**
	 * This sets the next time the message has to be retransmitted. This uses the base retransmission interval
	 * and exponentialBackoff properties to calculate the correct time.
	 * 
	 * @param retransmitterBean
	 * @param policyBean
	 * @return
	 */
	private SenderBean adjustNextRetransmissionTime(
			SenderBean retransmitterBean, SandeshaPropertyBean propertyBean) {

//		long lastSentTime = retransmitterBean.getTimeToSend();

		int count = retransmitterBean.getSentCount();

		long baseInterval = propertyBean.getRetransmissionInterval();

		long newInterval = baseInterval;
		if (propertyBean.isExponentialBackoff()) {
			newInterval = generateNextExponentialBackedoffDifference(count,
					baseInterval);
		}

		long newTimeToSend = 0;
		//newTimeToSend = lastSentTime + newInterval;
		
		long timeNow = System.currentTimeMillis();
		newTimeToSend = timeNow + newInterval;
		
		retransmitterBean.setTimeToSend(newTimeToSend);

		return retransmitterBean;
	}

	private void stopRetransmission(SenderBean bean) {
		bean.setSend(false);
	}

	private long generateNextExponentialBackedoffDifference(int count,
			long initialInterval) {
		long interval = initialInterval;
		for (int i = 1; i < count; i++) {
			interval = interval * 2;
		}

		return interval;
	}
	
	private void finalizeTimedOutSequence (String internalSequenceID, String sequenceID ,MessageContext messageContext) throws SandeshaException {
		ConfigurationContext configurationContext = messageContext.getConfigurationContext();
		SequenceReport report = SandeshaClient.getOutgoingSequenceReport(internalSequenceID ,configurationContext);
		TerminateManager.timeOutSendingSideSequence(configurationContext,internalSequenceID, false);
		
		SandeshaListener listener = (SandeshaListener) messageContext.getProperty(SandeshaClientConstants.SANDESHA_LISTENER);
		if (listener!=null) {
			listener.onTimeOut(report);
		}
	}

}