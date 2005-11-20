/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 *  
 */

package org.apache.sandesha2.handlers;

import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.msgprocessors.MsgProcessor;
import org.apache.sandesha2.msgprocessors.MsgProcessorFactory;
import org.apache.sandesha2.util.FaultManager;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaInHandler extends AbstractHandler {

	protected Log log = LogFactory.getLog(SandeshaInHandler.class.getName());

	public QName getName() {
		return new QName(Constants.IN_HANDLER_NAME);
	}

	public void invoke(MessageContext msgCtx) throws AxisFault {

		//try {

		ConfigurationContext context = msgCtx.getSystemContext();
		if (context == null)
			throw new AxisFault("ConfigurationContext is null");

		String DONE = (String) msgCtx
				.getProperty(Constants.APPLICATION_PROCESSING_DONE);
		if (null != DONE && "true".equals(DONE))
			return;

		FaultManager faultManager = new FaultManager();
		RMMsgContext faultMessageContext = faultManager
				.checkForPossibleFaults(msgCtx);
		if (faultMessageContext != null) {
			AxisEngine engine = new AxisEngine(context);
			engine.send(faultMessageContext.getMessageContext());
			return;
		}

		AxisService axisService = msgCtx.getAxisService();
		if (axisService == null)
			throw new AxisFault("AxisService is null");

		Parameter keyParam = axisService.getParameter(Constants.RM_ENABLE_KEY);
		Object keyValue = null;
		if (keyParam != null)
			keyValue = keyParam.getValue();

		if (keyValue == null || !keyValue.equals("true")) {
			//RM is not enabled for the service. Quiting SandeshaInHandler
			return;
		}

		RMMsgContext rmMsgCtx = null;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		ServiceContext serviceContext = msgCtx.getServiceContext();
		Object debug = null;
		if (serviceContext != null) {
			debug = serviceContext.getProperty(Constants.SANDESHA_DEBUG_MODE);
			if (debug != null && "on".equals(debug)) {
				System.out.println("DEBUG: SandeshaInHandler got a '"
						+ SandeshaUtil.getMessageTypeString(rmMsgCtx
								.getMessageType()) + "' message.");
			}
		}

		MsgProcessor msgProcessor = MsgProcessorFactory
				.getMessageProcessor(rmMsgCtx.getMessageType());

		if (msgProcessor == null)
			throw new AxisFault("Cant find a suitable message processor");

		try {
			msgProcessor.processMessage(rmMsgCtx);
		} catch (SandeshaException se) {
			se.printStackTrace();
			throw new AxisFault("Error in processing the message");
		}

		//		}catch (Exception e) {
		//			e.getStackTrace();
		//			throw new AxisFault ("Sandesha got an exception. See logs for
		// details");
		//		}

	}

}