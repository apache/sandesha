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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.RMPolicyBean;

/**
 * Loads properties from sandesha2.properties file (from Sandesha2Constants if this is not available).
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class PropertyManager {

	public static PropertyManager instance = null;
	
	Log log = LogFactory.getLog(getClass());
	
	private SandeshaPropertyBean propertyBean = null;
	 
	private PropertyManager () {
		propertyBean = new SandeshaPropertyBean ();
		loadProperties(null);
	}
	
	private void loadProperties (InputStream in) {
		try {
			if (in==null)
				in = Thread.currentThread().getContextClassLoader().getResourceAsStream(Sandesha2Constants.PROPERTY_FILE);
			
			Properties properties = new Properties ();
			if (in!=null) {
				properties.load(in);
			}
			
			loadPropertiesToBean (properties);
		} catch (IOException e) {
			log.info ("A valid property file was not found. Using default values...");
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
		loadInOrderInvocation (properties);
		loadMessageTypesToDrop (properties);
	}
	
	/**
	 * Loads wsp:exponentianbackoff.
	 * 
	 * @param properties
	 */
	private void loadExponentialBackoff (Properties properties) {
		
		String expoBackoffStr = properties.getProperty(Sandesha2Constants.Properties.ExponentialBackoff);
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
			propertyBean.setExponentialBackoff(Sandesha2Constants.Properties.DefaultValues.ExponentialBackoff);
	}
	
	
	/**
	 * Loads wsp:retransmissionInterval.
	 * 
	 * @param properties
	 */
	private void loadRetransmissionInterval (Properties properties) {
		
		String retransmissionIntStr = properties.getProperty(Sandesha2Constants.Properties.RetransmissionInterval);
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
			propertyBean.setRetransmissionInterval(Sandesha2Constants.Properties.DefaultValues.RetransmissionInterval);
	}
	
	/**
	 * Loads wsp:acknowldgementInterval.
	 * 
	 * @param properties
	 */
	private void loadAcknowledgementInterval (Properties properties) {
		
		String acknowledgementIntStr = properties.getProperty(Sandesha2Constants.Properties.AcknowledgementInterval);
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
			propertyBean.setAcknowledgementInterval(Sandesha2Constants.Properties.DefaultValues.AcknowledgementInterval);
	}
	
	/**
	 * Loads wsp:inactivityInterval.
	 * 
	 * @param properties
	 */
	private void loadInactivityTimeout (Properties properties) {
		
		String inactivityTimeoutStr = properties.getProperty(Sandesha2Constants.Properties.InactivityTimeout);
		String inactivityTimeoutMeasure = properties.getProperty(Sandesha2Constants.Properties.InactivityTimeoutMeasure);
		
		
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
			propertyBean.setInactiveTimeoutInterval(Sandesha2Constants.Properties.DefaultValues.InactivityTimeout,Sandesha2Constants.Properties.DefaultValues.InactivityTimeoutMeasure);
	}
	
	/**
	 * Loads the StorageManager class name.
	 * 
	 * @param properties
	 */
	private void loadStoragemanagerClass (Properties properties) {
		String storageMgrClassStr = properties.getProperty(Sandesha2Constants.Properties.StorageManager);
		boolean loaded = false;
		
		if (storageMgrClassStr!=null) {
			storageMgrClassStr = storageMgrClassStr.trim();
			propertyBean.setStorageManagerClass(storageMgrClassStr);
			loaded = true;
		}
		
		if (!loaded)
			propertyBean.setStorageManagerClass(Sandesha2Constants.Properties.DefaultValues.StorageManager);
	}
	
	private void loadInOrderInvocation (Properties properties) {
		String inOrderInvocation = properties.getProperty(Sandesha2Constants.Properties.InOrderInvocation);
		boolean loaded = false;
		
		if (inOrderInvocation!=null) {
			inOrderInvocation = inOrderInvocation.trim();
			if (inOrderInvocation.equalsIgnoreCase("true")) {
				propertyBean.setInOrder(true);
				loaded = true;
			}else if (inOrderInvocation.equalsIgnoreCase("false")) {
				propertyBean.setInOrder(false);
				loaded = true;
			}
		}
		
		if (!loaded)
			propertyBean.setInOrder(Sandesha2Constants.Properties.DefaultValues.InvokeInOrder);
		
	}
	
	private void loadMessageTypesToDrop (Properties properties) {
		String messageTypesToDrop = properties.getProperty(Sandesha2Constants.Properties.MessageTypesToDrop);
		boolean loaded=false;
		
		try {
			if (messageTypesToDrop!=null && !Sandesha2Constants.VALUE_NONE.equals(messageTypesToDrop)) {
				messageTypesToDrop = messageTypesToDrop.trim();
				messageTypesToDrop = "[" + messageTypesToDrop + "]";
				ArrayList messageTypesLst =  SandeshaUtil.getArrayListFromString(messageTypesToDrop);
				
				Iterator itr = messageTypesLst.iterator();
				while (itr.hasNext()) {
					String typeStr = (String) itr.next();
					Integer typeNo = new Integer (typeStr);
					propertyBean.addMsgTypeToDrop(typeNo);
				}
			}
			
		} catch (SandeshaException e) {
			log.error(e.getMessage());
		} catch (NumberFormatException e) {
			String message = "Property '" + Sandesha2Constants.Properties.MessageTypesToDrop + "' contains an invalid value.";
			log.error(message);
			log.error(e.getMessage());
		}
		
		
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
	
	public boolean isInOrderInvocation () {
		return propertyBean.isInOrder();
	}
	
	public ArrayList getMessagesNotToSend () {
		return propertyBean.getMsgTypesToDrop();
	}
	
}
