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

import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.policy.PolicyEngineData;
import org.apache.sandesha2.policy.RMPolicyBean;
import org.apache.sandesha2.policy.RMPolicyProcessor;
import org.apache.sandesha2.policy.RMProcessorContext;
import org.apache.ws.policy.Policy;


/**
 * Loads properties from sandesha2.properties file (from Sandesha2Constants if
 * this is not available).
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class PropertyManager {

	public static PropertyManager instance = null;

	private final byte PROPERTY_FILE = 1;

	private final byte MODULE_DESC = 2;

	private byte LOAD_SOURCE = MODULE_DESC;

	Log log = LogFactory.getLog(getClass());

	private SandeshaPropertyBean propertyBean = null;

	private PropertyManager() {
		propertyBean = new SandeshaPropertyBean();
		loadlPropertiesFromDefaultValues();
	}

	public void loadlPropertiesFromDefaultValues() {
		propertyBean
				.setAcknowledgementInterval(Sandesha2Constants.Properties.DefaultValues.AcknowledgementInterval);
		propertyBean
				.setExponentialBackoff(Sandesha2Constants.Properties.DefaultValues.ExponentialBackoff);
		propertyBean
				.setInactiveTimeoutInterval(
						Sandesha2Constants.Properties.DefaultValues.InactivityTimeout,
						Sandesha2Constants.Properties.DefaultValues.InactivityTimeoutMeasure);
		propertyBean
				.setInOrder(Sandesha2Constants.Properties.DefaultValues.InvokeInOrder);
		propertyBean.setMsgTypesToDrop(null);
		propertyBean
				.setRetransmissionInterval(Sandesha2Constants.Properties.DefaultValues.RetransmissionInterval);
		propertyBean
				.setStorageManagerClass(Sandesha2Constants.Properties.DefaultValues.StorageManager);
	}

	public void loadPropertiesFromPropertyFile(InputStream in) {
		try {
			if (in == null)
				in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(Sandesha2Constants.PROPERTY_FILE);

			Properties properties = new Properties();
			if (in != null) {
				properties.load(in);
			}

			String expoBackoffStr = properties
					.getProperty(Sandesha2Constants.Properties.ExponentialBackoff);
			loadExponentialBackoff(expoBackoffStr);

			String retransmissionIntStr = properties
					.getProperty(Sandesha2Constants.Properties.RetransmissionInterval);
			loadRetransmissionInterval(retransmissionIntStr);

			String acknowledgementIntStr = properties
					.getProperty(Sandesha2Constants.Properties.AcknowledgementInterval);
			loadAcknowledgementInterval(acknowledgementIntStr);

			String inactivityTimeoutStr = properties
					.getProperty(Sandesha2Constants.Properties.InactivityTimeout);
			String inactivityTimeoutMeasure = properties
					.getProperty(Sandesha2Constants.Properties.InactivityTimeoutMeasure);
			loadInactivityTimeout(inactivityTimeoutStr,
					inactivityTimeoutMeasure);

			String storageMgrClassStr = properties
					.getProperty(Sandesha2Constants.Properties.StorageManager);
			loadStoragemanagerClass(storageMgrClassStr);

			String inOrderInvocation = properties
					.getProperty(Sandesha2Constants.Properties.InOrderInvocation);
			loadInOrderInvocation(inOrderInvocation);

			String messageTypesToDrop = properties
					.getProperty(Sandesha2Constants.Properties.MessageTypesToDrop);
			loadMessageTypesToDrop(messageTypesToDrop);

			// loadPropertiesToBean (properties);
		} catch (IOException e) {
			log
					.info("A valid property file was not found. Using default values...");
		}
	}

	public void loadPropertiesFromModuleDesc (AxisModule desc) {
		
		Parameter expoBackoffParam = desc.getParameter (Sandesha2Constants.Properties.ExponentialBackoff);
		String expoBackoffStr = (String) expoBackoffParam.getValue();
		loadExponentialBackoff(expoBackoffStr);
		
		Parameter retransmissionIntParam = desc.getParameter (Sandesha2Constants.Properties.RetransmissionInterval);
		String retransmissionIntStr = (String) retransmissionIntParam.getValue();
		loadRetransmissionInterval(retransmissionIntStr);
		
		Parameter acknowledgementIntParam = desc.getParameter(Sandesha2Constants.Properties.AcknowledgementInterval);
		String acknowledgementIntStr = (String) acknowledgementIntParam.getValue();
		loadAcknowledgementInterval(acknowledgementIntStr);		
		
		Parameter inactivityTimeoutParam = desc.getParameter(Sandesha2Constants.Properties.InactivityTimeout);
		String inactivityTimeoutStr = (String) inactivityTimeoutParam.getValue();
		Parameter inactivityTimeoutMeasureParam = desc.getParameter(Sandesha2Constants.Properties.InactivityTimeoutMeasure);
		String inactivityTimeoutMeasure = (String) inactivityTimeoutMeasureParam.getValue();
		loadInactivityTimeout(inactivityTimeoutStr,inactivityTimeoutMeasure);
		
		Parameter storageMgrClassParam = desc.getParameter(Sandesha2Constants.Properties.StorageManager);
		String storageMgrClassStr = (String) storageMgrClassParam.getValue();
		loadStoragemanagerClass(storageMgrClassStr);
		
		Parameter inOrderInvocationParam = desc.getParameter(Sandesha2Constants.Properties.InOrderInvocation);
		String inOrderInvocation = (String) inOrderInvocationParam.getValue();
		loadInOrderInvocation (inOrderInvocation);
		
		Parameter messageTypesToDropParam = desc.getParameter(Sandesha2Constants.Properties.MessageTypesToDrop); 
		String messageTypesToDrop = (String) messageTypesToDropParam.getValue();
		loadMessageTypesToDrop (messageTypesToDrop);
	}
	
	public void loadPropertiesFromModuleDescPolicy(AxisModule desc)
			throws SandeshaException {
		Policy policy = desc.getPolicyInclude().getEffectivePolicy();

		if (policy == null) {
			throw new SandeshaException(
					"No configuration policy is found in module.xml");

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
		propertyBean.setStorageManagerClass(data.getInmemoryStorageManager());
	}

	public static PropertyManager getInstance() {
		if (instance == null) {
			instance = new PropertyManager();
		}

		return instance;
	}

	public void reloadFromPropertyFile(InputStream stream) {
		loadPropertiesFromPropertyFile(stream);
	}

	/**
	 * Loads wsp:exponentianbackoff.
	 * 
	 * @param properties
	 */
	private void loadExponentialBackoff(String expoBackoffStr) {

		boolean loaded = false;

		if (expoBackoffStr != null) {
			expoBackoffStr = expoBackoffStr.trim();
			if (expoBackoffStr.equals("true")) {
				propertyBean.setExponentialBackoff(true);
				loaded = true;
			} else if (expoBackoffStr.equals("false")) {
				propertyBean.setExponentialBackoff(false);
				loaded = true;
			}
		}

		if (!loaded)
			propertyBean
					.setExponentialBackoff(Sandesha2Constants.Properties.DefaultValues.ExponentialBackoff);
	}

	/**
	 * Loads wsp:retransmissionInterval.
	 * 
	 * @param properties
	 */
	private void loadRetransmissionInterval(String retransmissionIntStr) {

		boolean loaded = false;

		if (retransmissionIntStr != null) {
			try {
				retransmissionIntStr = retransmissionIntStr.trim();
				int retransmissionInterval = Integer
						.parseInt(retransmissionIntStr);
				if (retransmissionInterval > 0) {
					propertyBean
							.setRetransmissionInterval(retransmissionInterval);
					loaded = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				loaded = false;
			}
		}

		if (!loaded)
			propertyBean
					.setRetransmissionInterval(Sandesha2Constants.Properties.DefaultValues.RetransmissionInterval);
	}

	/**
	 * Loads wsp:acknowldgementInterval.
	 * 
	 * @param properties
	 */
	private void loadAcknowledgementInterval(String acknowledgementIntStr) {

		boolean loaded = false;

		if (acknowledgementIntStr != null) {
			try {
				acknowledgementIntStr = acknowledgementIntStr.trim();
				int acknowledgementInt = Integer
						.parseInt(acknowledgementIntStr);
				if (acknowledgementInt > 0) {
					propertyBean.setAcknowledgementInterval(acknowledgementInt);
					loaded = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				loaded = false;
			}
		}

		if (!loaded)
			propertyBean
					.setAcknowledgementInterval(Sandesha2Constants.Properties.DefaultValues.AcknowledgementInterval);
	}

	/**
	 * Loads wsp:inactivityInterval.
	 * 
	 * @param properties
	 */
	private void loadInactivityTimeout(String inactivityTimeoutStr,
			String inactivityTimeoutMeasure) {

		boolean loaded = false;

		if (inactivityTimeoutStr != null && inactivityTimeoutMeasure != null) {
			try {
				inactivityTimeoutStr = inactivityTimeoutStr.trim();
				inactivityTimeoutMeasure = inactivityTimeoutMeasure.trim();

				int inactivityTimeoutVal = Integer
						.parseInt(inactivityTimeoutStr);
				if (inactivityTimeoutVal > 0) {
					propertyBean.setInactiveTimeoutInterval(
							inactivityTimeoutVal, inactivityTimeoutMeasure);
					loaded = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				loaded = false;
			}
		}

		if (!loaded)
			propertyBean
					.setInactiveTimeoutInterval(
							Sandesha2Constants.Properties.DefaultValues.InactivityTimeout,
							Sandesha2Constants.Properties.DefaultValues.InactivityTimeoutMeasure);
	}

	/**
	 * Loads the StorageManager class name.
	 * 
	 * @param properties
	 */
	private void loadStoragemanagerClass(String storageMgrClassStr) {

		boolean loaded = false;

		if (storageMgrClassStr != null) {
			storageMgrClassStr = storageMgrClassStr.trim();
			propertyBean.setStorageManagerClass(storageMgrClassStr);
			loaded = true;
		}

		if (!loaded)
			propertyBean
					.setStorageManagerClass(Sandesha2Constants.Properties.DefaultValues.StorageManager);
	}

	private void loadInOrderInvocation(String inOrderInvocation) {

		boolean loaded = false;

		if (inOrderInvocation != null) {
			inOrderInvocation = inOrderInvocation.trim();
			if (inOrderInvocation.equalsIgnoreCase("true")) {
				propertyBean.setInOrder(true);
				loaded = true;
			} else if (inOrderInvocation.equalsIgnoreCase("false")) {
				propertyBean.setInOrder(false);
				loaded = true;
			}
		}

		if (!loaded)
			propertyBean
					.setInOrder(Sandesha2Constants.Properties.DefaultValues.InvokeInOrder);

	}

	private void loadMessageTypesToDrop(String messageTypesToDrop) {

		boolean loaded = false;

		try {
			if (messageTypesToDrop != null
					&& !Sandesha2Constants.VALUE_NONE
							.equals(messageTypesToDrop)) {
				messageTypesToDrop = messageTypesToDrop.trim();
				messageTypesToDrop = "[" + messageTypesToDrop + "]";
				ArrayList messageTypesLst = SandeshaUtil
						.getArrayListFromString(messageTypesToDrop);

				Iterator itr = messageTypesLst.iterator();
				while (itr.hasNext()) {
					String typeStr = (String) itr.next();
					Integer typeNo = new Integer(typeStr);
					propertyBean.addMsgTypeToDrop(typeNo);
				}
			}

		} catch (SandeshaException e) {
			log.error(e.getMessage());
		} catch (NumberFormatException e) {
			String message = "Property '"
					+ Sandesha2Constants.Properties.MessageTypesToDrop
					+ "' contains an invalid value.";
			log.error(message);
			log.error(e.getMessage());
		}

	}

	public boolean isExponentialBackoff() {

		return propertyBean.isExponentialBackoff();
	}

	public long getRetransmissionInterval() {
		return propertyBean.getRetransmissionInterval();
	}

	public long getAcknowledgementInterval() {
		return propertyBean.getAcknowledgementInaterval();
	}

	public long getInactivityTimeout() {
		return propertyBean.getInactiveTimeoutInterval();
	}

	public String getStorageManagerClass() {
		return propertyBean.getStorageManagerClass();
	}

	public RMPolicyBean getRMPolicyBean() {
		return propertyBean.getPolicyBean();
	}

	public boolean isInOrderInvocation() {
		return propertyBean.isInOrder();
	}

	public ArrayList getMessagesNotToSend() {
		return propertyBean.getMsgTypesToDrop();
	}

	public SandeshaPropertyBean getPropertyBean() {
		return propertyBean;
	}

}
