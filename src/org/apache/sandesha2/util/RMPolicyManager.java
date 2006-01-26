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

import java.util.ArrayList;

import org.apache.axis2.description.AxisDescription;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.PolicyEngineData;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.policy.RMPolicyProcessor;
import org.apache.sandesha2.policy.RMProcessorContext;
import org.apache.ws.policy.Policy;

/**
 * This is used to manage RM Policies.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class RMPolicyManager {

	public static RMPolicyBean getPolicyBean(RMMsgContext msgContext) {
		//TODO extract policies from the msgCtx.
		
		RMPolicyBean policyBean = PropertyManager.getInstance().getRMPolicyBean();
		return policyBean;
	}
	
	public static SandeshaPropertyBean loadPoliciesFromAxisDescription (AxisDescription desc) throws SandeshaException{
		
		SandeshaPropertyBean propertyBean = new SandeshaPropertyBean ();
		
		Policy policy = desc.getPolicyInclude().getEffectivePolicy();

		if (policy == null) {
			//no policy found
			return null;
		}

		RMPolicyProcessor processor = new RMPolicyProcessor();

		try {
			processor.setup();
		} catch (NoSuchMethodException e) {
			throw new SandeshaException(e.getMessage());
		}
		
		processor.processPolicy(policy);

		RMProcessorContext ctx = processor.getContext();
		PolicyEngineData data = ctx.readCurrentPolicyEngineData();

		propertyBean.setAcknowledgementInterval(data
				.getAcknowledgementInterval());
		propertyBean.setExponentialBackoff(data.isExponentialBackoff());
		propertyBean.setInactiveTimeoutInterval((int) data
				.getInactivityTimeout(), data.getInactivityTimeoutMeassure());
		propertyBean.setInOrder(data.isInvokeInOrder());

		// CHECKME
		ArrayList msgTypesToDrop = new ArrayList();
		msgTypesToDrop.add(data.getMessageTypesToDrop());
		propertyBean.setMsgTypesToDrop(msgTypesToDrop);

		propertyBean
				.setRetransmissionInterval(data.getRetransmissionInterval());

		// CHECKME
		propertyBean.setStorageManagerClass(data.getStorageManager());
		
		return propertyBean;
	}
}