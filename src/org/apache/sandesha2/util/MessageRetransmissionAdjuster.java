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
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beans.SenderBean;

/**
 * This is used to adjust retransmission infoamation after each time the message is sent.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class MessageRetransmissionAdjuster {

	Log log = LogFactory.getLog( getClass());
	
	public SenderBean adjustRetransmittion(
			SenderBean retransmitterBean,ConfigurationContext configContext) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		
		String storedKey = (String) retransmitterBean.getMessageContextRefKey();

		if (storedKey == null)
			return retransmitterBean;

		MessageContext messageContext = storageManager.retrieveMessageContext(storedKey,configContext);

		if (messageContext.getConfigurationContext() == null)
			return retransmitterBean;

//		RMPolicyBean policyBean = (RMPolicyBean) messageContext
//				.getProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN);
//		if (policyBean == null) {
//			//loading default policies.
//			policyBean = PropertyManager.getInstance().getRMPolicyBean();
//		}

		Parameter parameter =  messageContext.getParameter(Sandesha2Constants.SANDESHA2_POLICY_BEAN);
		if (parameter==null) {
			//log.error ("Cant adjust retransmission since, Parameter bean is not set");
			return retransmitterBean;
		}
		
		SandeshaPropertyBean propertyBean = (SandeshaPropertyBean) parameter.getValue();
		
		retransmitterBean.setSentCount(retransmitterBean.getSentCount() + 1);
		adjustNextRetransmissionTime(retransmitterBean, propertyBean);

		if (retransmitterBean.getSentCount() >= Sandesha2Constants.MAXIMUM_RETRANSMISSION_ATTEMPTS)
			stopRetransmission(retransmitterBean);

		return retransmitterBean;
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

		long lastSentTime = retransmitterBean.getTimeToSend();

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
		bean.setReSend(false);
	}

	private long generateNextExponentialBackedoffDifference(int count,
			long initialInterval) {
		long interval = initialInterval;
		for (int i = 1; i < count; i++) {
			interval = interval * 2;
		}

		return interval;
	}

}