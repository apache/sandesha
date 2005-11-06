/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */

package org.apache.sandesha2;

import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.sandesha2.policy.RMPolicyBean;


public class SandeshaDynamicProperties {

	private String storageManagerImpl = Constants.STORAGE_MANAGER_IMPL;
	private RMPolicyBean policyBean = null;
	//private String SOAPVersionURI = null;
	
	public SandeshaDynamicProperties () {
	
		loadPolicyBeanFromConstants ();
	}
	
	private void loadPolicyBeanFromConstants () {
		//loading default properties. these will be overriden later (most of the time).
		policyBean = new RMPolicyBean ();
		policyBean.setAcknowledgementInterval(Constants.WSP.ACKNOWLEDGEMENT_INTERVAL);
		policyBean.setRetransmissionInterval(Constants.WSP.RETRANSMISSION_INTERVAL);
		policyBean.setExponentialBackoff(Constants.WSP.EXPONENTION_BACKOFF);
		policyBean.setInactiveTimeoutInterval(Constants.WSP.INACTIVITY_TIMEOUT_INTERVAL);
		
//		if (Constants.SOAPVersion.DEFAULT==Constants.SOAPVersion.v1_1){
//			SOAPVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
//		}else {
//			SOAPVersionURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
//		}
		
		//default is SOAP 1.1
		//SOAPVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
	}
	
	
	
	
	
	public RMPolicyBean getPolicyBean() {
		return policyBean;
	}
	
	public void setPolicyBean(RMPolicyBean policyBean) {
		this.policyBean = policyBean;
	}
	
//	public String getSOAPVersionURI() {
//		return SOAPVersionURI;
//	}
//	
//	public void setSOAPVersionURI(String versionURI) {
//		SOAPVersionURI = versionURI;
//	}
	
	public String getStorageManagerImpl() {
		return storageManagerImpl;
	}
	
	public void setStorageManagerImpl(String storageManagerImpl) {
		this.storageManagerImpl = storageManagerImpl;
	}
}
