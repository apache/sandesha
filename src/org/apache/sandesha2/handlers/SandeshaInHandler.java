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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.msgprocessors.MsgProcessor;
import org.apache.sandesha2.msgprocessors.MsgProcessorFactory;
import org.apache.sandesha2.util.MsgInitializer;

/**
 * This is invoked in the inFlow of an RM endpoint. This is responsible for selecting an suitable
 * message processor and letting it process the message.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaInHandler extends AbstractHandler {

	protected Log log = LogFactory.getLog(SandeshaInHandler.class.getName());

	public QName getName() {
		return new QName(Sandesha2Constants.IN_HANDLER_NAME);
	}

	public void invoke(MessageContext msgCtx) throws AxisFault {
	
		
		
		ConfigurationContext context = msgCtx.getConfigurationContext();
		if (context == null) {
			String message = "ConfigurationContext is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		String DONE = (String) msgCtx
				.getProperty(Sandesha2Constants.APPLICATION_PROCESSING_DONE);
		if (null != DONE && "true".equals(DONE))
			return;

		AxisService axisService = msgCtx.getAxisService();
		if (axisService == null) {
			String message = "AxisService is null";
			log.debug(message);
			throw new AxisFault(message);
		}

		RMMsgContext rmMsgCtx = null;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		} catch (SandeshaException ex) {
			String message = "Cant initialize the message";
			log.debug(message);
			throw new AxisFault(message);
		}
		
		MsgProcessor msgProcessor = MsgProcessorFactory.getMessageProcessor (rmMsgCtx);

		try {
			if (msgProcessor!=null)
				msgProcessor.processInMessage(rmMsgCtx);
		} catch (SandeshaException se) {
			String message = "Error in processing the message";
			log.debug(message);
			throw new AxisFault(message,se);
		}

	}

}