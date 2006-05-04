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

import org.apache.axis2.description.AxisDescription;
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

	private static final Log log = LogFactory.getLog(PropertyManager.class);

	public static SandeshaPropertyBean loadPropertiesFromDefaultValues() throws SandeshaException{
		SandeshaPropertyBean propertyBean = new SandeshaPropertyBean ();
		propertyBean.setAcknowledgementInterval(Sandesha2Constants.Properties.DefaultValues.AcknowledgementInterval);
		propertyBean.setExponentialBackoff(Sandesha2Constants.Properties.DefaultValues.ExponentialBackoff);
		propertyBean.setInactiveTimeoutInterval(
				        Sandesha2Constants.Properties.DefaultValues.InactivityTimeout,
						Sandesha2Constants.Properties.DefaultValues.InactivityTimeoutMeasure);
		
		propertyBean.setInOrder(Sandesha2Constants.Properties.DefaultValues.InvokeInOrder);
		propertyBean.setMsgTypesToDrop(null);
		propertyBean.setRetransmissionInterval(Sandesha2Constants.Properties.DefaultValues.RetransmissionInterval);
		propertyBean.setStorageManagerClass(Sandesha2Constants.Properties.DefaultValues.StorageManager);
		propertyBean.setMaximumRetransmissionCount(Sandesha2Constants.Properties.DefaultValues.MaximumRetransmissionCount);
		
		String msgTypesToDrop = Sandesha2Constants.Properties.DefaultValues.MessageTypesToDrop;
		loadMessageTypesToDrop(msgTypesToDrop,propertyBean);
		
		return propertyBean;
	}

	public static SandeshaPropertyBean loadPropertiesFromPropertyFile(InputStream in) throws SandeshaException{
		SandeshaPropertyBean propertyBean = new SandeshaPropertyBean ();
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
			loadExponentialBackoff(expoBackoffStr,propertyBean);

			String retransmissionIntStr = properties
					.getProperty(Sandesha2Constants.Properties.RetransmissionInterval);
			loadRetransmissionInterval(retransmissionIntStr,propertyBean);

			String acknowledgementIntStr = properties
					.getProperty(Sandesha2Constants.Properties.AcknowledgementInterval);
			loadAcknowledgementInterval(acknowledgementIntStr,propertyBean);

			String inactivityTimeoutStr = properties
					.getProperty(Sandesha2Constants.Properties.InactivityTimeout);
			String inactivityTimeoutMeasure = properties
					.getProperty(Sandesha2Constants.Properties.InactivityTimeoutMeasure);
			loadInactivityTimeout(inactivityTimeoutStr,
					inactivityTimeoutMeasure,propertyBean);

			String storageMgrClassStr = properties
					.getProperty(Sandesha2Constants.Properties.StorageManager);
			loadStoragemanagerClass(storageMgrClassStr,propertyBean);

			String inOrderInvocation = properties
					.getProperty(Sandesha2Constants.Properties.InOrderInvocation);
			loadInOrderInvocation(inOrderInvocation,propertyBean);

			String messageTypesToDrop = properties
					.getProperty(Sandesha2Constants.Properties.MessageTypesToDrop);
			loadMessageTypesToDrop(messageTypesToDrop,propertyBean);

		} catch (IOException e) {
			throw new SandeshaException (e);
		}
		
		return propertyBean;
	}

	public static SandeshaPropertyBean loadPropertiesFromModuleDesc (AxisModule desc) throws SandeshaException{
		SandeshaPropertyBean propertyBean = new SandeshaPropertyBean ();
		
		Parameter expoBackoffParam = desc.getParameter (Sandesha2Constants.Properties.ExponentialBackoff);
		String expoBackoffStr = (String) expoBackoffParam.getValue();
		loadExponentialBackoff(expoBackoffStr,propertyBean);
		
		Parameter retransmissionIntParam = desc.getParameter (Sandesha2Constants.Properties.RetransmissionInterval);
		String retransmissionIntStr = (String) retransmissionIntParam.getValue();
		loadRetransmissionInterval(retransmissionIntStr,propertyBean);
		
		Parameter acknowledgementIntParam = desc.getParameter(Sandesha2Constants.Properties.AcknowledgementInterval);
		String acknowledgementIntStr = (String) acknowledgementIntParam.getValue();
		loadAcknowledgementInterval(acknowledgementIntStr,propertyBean);		
		
		Parameter inactivityTimeoutParam = desc.getParameter(Sandesha2Constants.Properties.InactivityTimeout);
		String inactivityTimeoutStr = (String) inactivityTimeoutParam.getValue();
		Parameter inactivityTimeoutMeasureParam = desc.getParameter(Sandesha2Constants.Properties.InactivityTimeoutMeasure);
		String inactivityTimeoutMeasure = (String) inactivityTimeoutMeasureParam.getValue();
		loadInactivityTimeout(inactivityTimeoutStr,inactivityTimeoutMeasure,propertyBean);
		
		Parameter storageMgrClassParam = desc.getParameter(Sandesha2Constants.Properties.StorageManager);
		String storageMgrClassStr = (String) storageMgrClassParam.getValue();
		loadStoragemanagerClass(storageMgrClassStr,propertyBean);
		
		Parameter inOrderInvocationParam = desc.getParameter(Sandesha2Constants.Properties.InOrderInvocation);
		String inOrderInvocation = (String) inOrderInvocationParam.getValue();
		loadInOrderInvocation (inOrderInvocation,propertyBean);
		
		Parameter messageTypesToDropParam = desc.getParameter(Sandesha2Constants.Properties.MessageTypesToDrop); 
		String messageTypesToDrop = (String) messageTypesToDropParam.getValue();
		loadMessageTypesToDrop (messageTypesToDrop,propertyBean);
		
		return propertyBean;
	}
	
	public static SandeshaPropertyBean loadPropertiesFromModuleDescPolicy(AxisModule desc, SandeshaPropertyBean parentPropertyBean) throws SandeshaException {
		SandeshaPropertyBean propertyBean = new SandeshaPropertyBean ();
		
		Policy policy = desc.getPolicyInclude().getEffectivePolicy();

		if (policy == null) {
			return null; //no pilicy is available in the module description
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

		
		if (data.isAcknowledgementIntervalSet()) 
			propertyBean.setAcknowledgementInterval(data.getAcknowledgementInterval());
		else
			propertyBean.setAcknowledgementInterval(parentPropertyBean.getAcknowledgementInaterval());
		
		if (data.isExponentialBackoffSet())
			propertyBean.setExponentialBackoff(data.isExponentialBackoff());
		else
			propertyBean.setExponentialBackoff(parentPropertyBean.isExponentialBackoff());
		
		//Inactivity timeout given in the policy will affect only if it gives both the measure and the value.
		//Otherwise value will be taken from the parent.
		if (data.isInactivityTimeoutSet() && data.isInactivityTimeoutMeassureSet() )
			propertyBean.setInactiveTimeoutInterval(data.getInactivityTimeout(),data.getInactivityTimeoutMeassure());
		else 
			propertyBean.setInactiveTimeoutInterval(parentPropertyBean.getInactiveTimeoutInterval());
		
		if (data.isInvokeInOrderSet()) 
			propertyBean.setInOrder(data.isInvokeInOrder());
		else
			propertyBean.setInOrder(parentPropertyBean.isInOrder());
		
		if (data.isMaximumRetransmissionCountSet())
			propertyBean.setMaximumRetransmissionCount(data.getMaximumRetransmissionCount());
		else 
			propertyBean.setMaximumRetransmissionCount(parentPropertyBean.getMaximumRetransmissionCount());
		
		if (data.isRetransmissionIntervalSet()) 
			propertyBean.setRetransmissionInterval(data.getRetransmissionInterval());
		else
			propertyBean.setRetransmissionInterval(parentPropertyBean.getRetransmissionInterval());
			
		if (data.isStorageManagerSet())
			propertyBean.setStorageManagerClass(data.getStorageManager());
		else
			propertyBean.setStorageManagerClass(data.getStorageManager());
		
		if (data.isMessageTypesToDropSet()) 
			loadMessageTypesToDrop(data.getMessageTypesToDrop(),propertyBean);
		else
			propertyBean.setMsgTypesToDrop(parentPropertyBean.getMsgTypesToDrop());
		
		return propertyBean;
	}

	public static SandeshaPropertyBean loadPropertiesFromAxisDescription(AxisDescription desc, SandeshaPropertyBean parentPropertyBean)
			throws SandeshaException {
		SandeshaPropertyBean propertyBean = new SandeshaPropertyBean ();
		Policy policy = desc.getPolicyInclude().getEffectivePolicy();

		if (policy == null) {
			return null; //no policy is available in this axis description
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

		if (data.isAcknowledgementIntervalSet()) 
			propertyBean.setAcknowledgementInterval(data.getAcknowledgementInterval());
		else
			propertyBean.setAcknowledgementInterval(parentPropertyBean.getAcknowledgementInaterval());
		
		if (data.isExponentialBackoffSet())
			propertyBean.setExponentialBackoff(data.isExponentialBackoff());
		else
			propertyBean.setExponentialBackoff(parentPropertyBean.isExponentialBackoff());
		
		//Inactivity timeout given in the policy will affect only if it gives both the measure and the value.
		//Otherwise value will be taken from the parent.
		if (data.isInactivityTimeoutSet() && data.isInactivityTimeoutMeassureSet() )
			propertyBean.setInactiveTimeoutInterval(data.getInactivityTimeout(),data.getInactivityTimeoutMeassure());
		else 
			propertyBean.setInactiveTimeoutInterval(parentPropertyBean.getInactiveTimeoutInterval());
		
		if (data.isInvokeInOrderSet()) 
			propertyBean.setInOrder(data.isInvokeInOrder());
		else
			propertyBean.setInOrder(parentPropertyBean.isInOrder());
		
		if (data.isMaximumRetransmissionCountSet())
			propertyBean.setMaximumRetransmissionCount(data.getMaximumRetransmissionCount());
		else 
			propertyBean.setMaximumRetransmissionCount(parentPropertyBean.getMaximumRetransmissionCount());
		
		if (data.isRetransmissionIntervalSet()) 
			propertyBean.setRetransmissionInterval(data.getRetransmissionInterval());
		else
			propertyBean.setRetransmissionInterval(parentPropertyBean.getRetransmissionInterval());
			
		if (data.isStorageManagerSet())
			propertyBean.setStorageManagerClass(data.getStorageManager());
		else
			propertyBean.setStorageManagerClass(data.getStorageManager());
		
		if (data.isMessageTypesToDropSet()) 
			loadMessageTypesToDrop(data.getMessageTypesToDrop(),propertyBean);
		else
			propertyBean.setMsgTypesToDrop(parentPropertyBean.getMsgTypesToDrop());
		
		return propertyBean;
	}

	public static void reloadFromPropertyFile(InputStream stream) throws SandeshaException {
		loadPropertiesFromPropertyFile(stream);
	}

	/**
	 * Loads wsp:exponentianbackoff.
	 * 
	 * @param properties
	 */
	private static void loadExponentialBackoff(String expoBackoffStr, SandeshaPropertyBean propertyBean) throws SandeshaException {

		if (expoBackoffStr != null) {
			expoBackoffStr = expoBackoffStr.trim();
			if (expoBackoffStr.equals("true")) {
				propertyBean.setExponentialBackoff(true);
			} else if (expoBackoffStr.equals("false")) {
				propertyBean.setExponentialBackoff(false);
			}
		}
	}

	/**
	 * Loads wsp:retransmissionInterval.
	 * 
	 * @param properties
	 */
	private static void loadRetransmissionInterval(String retransmissionIntStr, SandeshaPropertyBean propertyBean) throws SandeshaException  {

		if (retransmissionIntStr != null) {
			try {
				retransmissionIntStr = retransmissionIntStr.trim();
				int retransmissionInterval = Integer
						.parseInt(retransmissionIntStr);
				if (retransmissionInterval > 0) {
					propertyBean.setRetransmissionInterval(retransmissionInterval);
				}
			} catch (NumberFormatException e) {
				String message = "Cannot derive the Acknowledgement Interval from the passed string";
				throw new SandeshaException (message,e);
			}
		}
	}

	/**
	 * Loads wsp:acknowldgementInterval.
	 * 
	 * @param properties
	 */
	private static void loadAcknowledgementInterval(String acknowledgementIntStr, SandeshaPropertyBean propertyBean) throws SandeshaException  {

		if (acknowledgementIntStr != null) {
			try {
				acknowledgementIntStr = acknowledgementIntStr.trim();
				int acknowledgementInt = Integer
						.parseInt(acknowledgementIntStr);
				if (acknowledgementInt > 0) {
					propertyBean.setAcknowledgementInterval(acknowledgementInt);
				}
			} catch (NumberFormatException e) {
				String message = "Cannot derive the Acknowledgement Interval from the passed string";
				throw new SandeshaException(message,e);
			}
		}
	}

	/**
	 * Loads wsp:inactivityInterval.
	 * 
	 * @param properties
	 */
	private static void loadInactivityTimeout(String inactivityTimeoutStr,
			String inactivityTimeoutMeasure, SandeshaPropertyBean propertyBean) throws SandeshaException  {

		if (inactivityTimeoutStr != null && inactivityTimeoutMeasure != null) {
			try {
				inactivityTimeoutStr = inactivityTimeoutStr.trim();
				inactivityTimeoutMeasure = inactivityTimeoutMeasure.trim();

				int inactivityTimeoutVal = Integer
						.parseInt(inactivityTimeoutStr);
				if (inactivityTimeoutVal > 0) {
					propertyBean.setInactiveTimeoutInterval(
							inactivityTimeoutVal, inactivityTimeoutMeasure);
				}
			} catch (NumberFormatException e) {
				String message = "Cannot derive the Inactivity Timeout from the passed string";
				throw new SandeshaException(message,e);
			}
		}
	}

	/**
	 * Loads the StorageManager class name.
	 * 
	 * @param properties
	 */
	private static void loadStoragemanagerClass(String storageMgrClassStr, SandeshaPropertyBean propertyBean) throws SandeshaException  {
		if (storageMgrClassStr != null) {
			storageMgrClassStr = storageMgrClassStr.trim();
			propertyBean.setStorageManagerClass(storageMgrClassStr);
		}
	}

	private static void loadInOrderInvocation(String inOrderInvocation, SandeshaPropertyBean propertyBean) throws SandeshaException  {

		if (inOrderInvocation != null) {
			inOrderInvocation = inOrderInvocation.trim();
			if (inOrderInvocation.equalsIgnoreCase("true")) {
				propertyBean.setInOrder(true);
			} else if (inOrderInvocation.equalsIgnoreCase("false")) {
				propertyBean.setInOrder(false);
			}
		}
	}

	private static void loadMessageTypesToDrop(String messageTypesToDrop, SandeshaPropertyBean propertyBean) throws SandeshaException  {

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
		} catch (NumberFormatException e) {
			String message = "Property '"
					+ Sandesha2Constants.Properties.MessageTypesToDrop
					+ "' contains an invalid value.";
			throw new SandeshaException (message,e);
		}
	}

}
