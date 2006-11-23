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

package org.apache.sandesha2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.modules.Module;
import org.apache.axis2.modules.ModulePolicyExtension;
import org.apache.axis2.modules.PolicyExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.policy.RMPolicyExtension;
import org.apache.sandesha2.policy.SandeshaPolicyBean;
import org.apache.sandesha2.security.SecurityManager;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * The Module class of Sandesha2.
 */

public class SandeshaModule implements Module, ModulePolicyExtension {
    
    private Log log = LogFactory.getLog(SandeshaModule.class);
    
	// initialize the module
	public void init(ConfigurationContext configContext,
			AxisModule module) throws AxisFault {

		//storing the Sadesha module as a property.
		configContext.setProperty(Sandesha2Constants.MODULE_CLASS_LOADER,module.getModuleClassLoader());

		//init the i18n messages
		SandeshaMessageHelper.innit();
		
		// continueUncompletedSequences (storageManager,configCtx);

		SandeshaPolicyBean constantPropertyBean = PropertyManager.loadPropertiesFromDefaultValues();
		SandeshaPolicyBean propertyBean = PropertyManager.loadPropertiesFromModuleDescPolicy(module,constantPropertyBean);
		
		if (propertyBean==null) {
			String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.couldNotLoadModulePolicies);
			log.error (message);
		} else {
			if (log.isDebugEnabled()) {
				String message = SandeshaMessageHelper.getMessage(SandeshaMessageKeys.modulePoliciesLoaded);
				log.info (message);
			}
		}

		if (propertyBean==null) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.cannotFindModulePolicies);
			log.debug(message);
			
			propertyBean = PropertyManager.loadPropertiesFromDefaultValues();
		}
		
		Parameter parameter = new Parameter ();
		parameter.setName(Sandesha2Constants.SANDESHA_PROPERTY_BEAN);
		parameter.setValue(propertyBean);
		configContext.getAxisConfiguration().addParameter(parameter);;
		
		configContext.setProperty(Sandesha2Constants.INMEMORY_STORAGE_MANAGER,null);   // this must be resetted by the module settings.
		configContext.setProperty(Sandesha2Constants.PERMANENT_STORAGE_MANAGER,null);
		
		try {
			StorageManager inMemorytorageManager = SandeshaUtil.getInMemoryStorageManager(configContext);
			inMemorytorageManager.initStorage(module);
		} catch (SandeshaStorageException e) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.cannotInitInMemoryStorageManager,
					e.toString());
			log.debug(message,e);
		}
		
		try {
			StorageManager permanentStorageManager = SandeshaUtil.getPermanentStorageManager(configContext);
			permanentStorageManager.initStorage(module);
		} catch (SandeshaStorageException e) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.cannotInitPersistentStorageManager,
					e.toString());
			log.debug(message,e);
		}
		
		configContext.setProperty(Sandesha2Constants.SECURITY_MANAGER,null);
		SecurityManager util = SandeshaUtil.getSecurityManager(configContext);
		util.initSecurity(module);

	}

	public void engageNotify(AxisDescription axisDescription) throws AxisFault {
		
		SandeshaPolicyBean parentPropertyBean = SandeshaUtil.getPropertyBean(axisDescription);
		if (parentPropertyBean==null) 
			throw new AxisFault (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.defaultPropertyBeanNotSet));
		
		SandeshaPolicyBean axisDescPropertyBean = PropertyManager.loadPropertiesFromAxisDescription(axisDescription,parentPropertyBean);
		
		if (axisDescPropertyBean!=null) {
			Parameter parameter = new Parameter ();
			parameter.setName(Sandesha2Constants.SANDESHA_PROPERTY_BEAN);
			parameter.setValue(axisDescPropertyBean);
			axisDescription.addParameter(parameter);
		}
	}

	private void continueUncompletedSequences(StorageManager storageManager,
			ConfigurationContext configCtx) {
		// server side continues
		// SandeshaUtil.startInvokerIfStopped(configCtx);

		// server side re-injections

		// reinject everything that has been acked within the in-handler but
		// have not been invoked.

		// client side continues
		// SandeshaUtil.startSenderIfStopped(configCtx);

		// client side re-injections

	}
	
	public PolicyExtension getPolicyExtension() {
		return new RMPolicyExtension();
	}

	public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
		//removing the threads started by Sandesha2.
		SandeshaUtil.stopSender (configurationContext);
		SandeshaUtil.stopInvoker(configurationContext);
	}

	// Removing data of uncontinuuable sequences so that the sandesha2 system
	// will not be confused
	private void cleanStorage(StorageManager storageManager) throws AxisFault {

		

		// server side cleaning

		// cleaning NextMsgData
		// Cleaning InvokerData

		// client side cleaning

		// cleaning RetransmitterData
		// cleaning CreateSequenceData

		// cleaning sequence properties

	}

    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {
        // TODO 
        
    }

    public boolean canSupportAssertion(Assertion assertion) {
        // TODO 
        return true;
    }
    
    

}
