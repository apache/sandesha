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

import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.policy.RMPolicyBean;

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
}