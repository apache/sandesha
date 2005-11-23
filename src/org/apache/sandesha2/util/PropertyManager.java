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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.axis2.InavalidModuleImpl;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.RMPolicyBean;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class PropertyManager {

	public static PropertyManager instance = null;
	
	private SandeshaPropertyBean propertyBean = null;
	 
	private PropertyManager () {
		propertyBean = new SandeshaPropertyBean ();
		loadProperties(null);
	}
	
	private void loadProperties (InputStream in) {
		try {
			if (in==null)
				in = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.PROPERTY_FILE);
			
			if (in==null)
				throw new IOException ("sandesha2 property file not found");
			
			Properties properties = new Properties ();
			properties.load(in);
			
			loadPropertiesToBean (properties);
		} catch (IOException e) {
			System.out.println("A valid property file was not found. Using default values...");
		}
	}
	
	public static PropertyManager getInstance () {
		if (instance==null) {
			instance = new PropertyManager ();	
		}
		
		return instance;
	}
	
	public void reload (InputStream stream) {
		loadProperties(stream);
	}
	
	
	private void loadPropertiesToBean (Properties properties) {
		
		loadExponentialBackoff(properties);
		loadRetransmissionInterval(properties);
		loadAcknowledgementInterval(properties);
		loadInactivityTimeout(properties);
		loadStoragemanagerClass(properties);
		
	}
	
	private void loadExponentialBackoff (Properties properties) {
		
		String expoBackoffStr = properties.getProperty(Constants.Properties.ExponentialBackoff);
		boolean loaded = false;
		
		if (expoBackoffStr!=null) {
			expoBackoffStr = expoBackoffStr.trim();
			if (expoBackoffStr.equals("true")) {
				propertyBean.setExponentialBackoff(true);
				loaded = true;
			}else if (expoBackoffStr.equals("false")){
				propertyBean.setExponentialBackoff(false);
				loaded = true;
			}
		}
		
		if (!loaded)
			propertyBean.setExponentialBackoff(Constants.Properties.DefaultValues.ExponentialBackoff);
	}
	
	private void loadRetransmissionInterval (Properties properties) {
		
		String retransmissionIntStr = properties.getProperty(Constants.Properties.RetransmissionInterval);
		boolean loaded = false;
		
		if (retransmissionIntStr!=null) {
			try {
				retransmissionIntStr = retransmissionIntStr.trim();
				int retransmissionInterval = Integer.parseInt(retransmissionIntStr);
				if (retransmissionInterval>0) {
					propertyBean.setRetransmissionInterval(retransmissionInterval);
					loaded = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				loaded = false;
			}
		}
		
		if (!loaded)
			propertyBean.setRetransmissionInterval(Constants.Properties.DefaultValues.RetransmissionInterval);
	}
	
	private void loadAcknowledgementInterval (Properties properties) {
		
		String acknowledgementIntStr = properties.getProperty(Constants.Properties.AcknowledgementInterval);
		boolean loaded = false;
		
		if (acknowledgementIntStr!=null) {
			try {
				acknowledgementIntStr = acknowledgementIntStr.trim();
				int acknowledgementInt = Integer.parseInt(acknowledgementIntStr);
				if (acknowledgementInt>0) {
					propertyBean.setAcknowledgementInterval(acknowledgementInt);
					loaded = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				loaded = false;
			}
		}
		
		if (!loaded)
			propertyBean.setAcknowledgementInterval(Constants.Properties.DefaultValues.AcknowledgementInterval);
	}
	
	private void loadInactivityTimeout (Properties properties) {
		
		String inactivityTimeoutStr = properties.getProperty(Constants.Properties.InactivityTimeout);
		String inactivityTimeoutMeasure = properties.getProperty(Constants.Properties.InactivityTimeoutMeasure);
		
		
		boolean loaded = false;
		
		if (inactivityTimeoutStr!=null && inactivityTimeoutMeasure!=null) {
			try {
				inactivityTimeoutStr = inactivityTimeoutStr.trim();
				inactivityTimeoutMeasure = inactivityTimeoutMeasure.trim();
				
				int inactivityTimeoutVal = Integer.parseInt(inactivityTimeoutStr);
				if (inactivityTimeoutVal>0) {
					propertyBean.setInactiveTimeoutInterval(inactivityTimeoutVal,inactivityTimeoutMeasure);
					loaded = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				loaded = false;
			}
		}
		
		if (!loaded)
			propertyBean.setInactiveTimeoutInterval(Constants.Properties.DefaultValues.InactivityTimeout,Constants.Properties.DefaultValues.InactivityTimeoutMeasure);
	}
	
	private void loadStoragemanagerClass (Properties properties) {
		String storageMgrClassStr = properties.getProperty(Constants.Properties.StorageManager);
		boolean loaded = false;
		
		if (storageMgrClassStr!=null) {
			storageMgrClassStr = storageMgrClassStr.trim();
			propertyBean.setStorageManagerClass(storageMgrClassStr);
			loaded = true;
		}
		
		if (!loaded)
			propertyBean.setStorageManagerClass(Constants.Properties.DefaultValues.StorageManager);
	}
	
	
	
	public boolean isExponentialBackoff () {
		
		return propertyBean.isExponentialBackoff();
	}
	
	public long getRetransmissionInterval () {
		return propertyBean.getRetransmissionInterval();
	}
	
	public long getAcknowledgementInterval () {
		return propertyBean.getAcknowledgementInaterval();
	}
	
	public long getInactivityTimeout () {
		return propertyBean.getInactiveTimeoutInterval();
	}
	
	public String getStorageManagerClass () {
		return propertyBean.getStorageManagerClass();
	}
	
	public RMPolicyBean getRMPolicyBean () {
		return propertyBean.getPolicyBean();
	}
	
}
