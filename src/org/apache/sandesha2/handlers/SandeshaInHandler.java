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

package org.apache.sandesha2.handlers;

import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.msgprocessors.MsgProcessor;
import org.apache.sandesha2.msgprocessors.MsgProcessorFactory;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya
 */
public class SandeshaInHandler extends AbstractHandler {

	public QName getName() {
		return new QName(Constants.IN_HANDLER_NAME);
	}

	public void invoke(MessageContext msgCtx) throws AxisFault {
		System.out.println ("Sandesha in handler called");
		
		RMMsgContext rmMsgCtx = null;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

//		try {
//			System.out.println("SandeshaInHandler Got a message of type:" + rmMsgCtx.getMessageType());
//			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
//			SOAPEnvelope env123 = msgCtx.getEnvelope();
//			env123.serialize(writer);
//		} catch (XMLStreamException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FactoryConfigurationError e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
			


		MsgProcessor msgProcessor = MsgProcessorFactory
				.getMessageProcessor(rmMsgCtx.getMessageType());

		if (msgProcessor==null)
			throw new AxisFault ("Cant find a suitable message processor");
		
		try {
			msgProcessor.processMessage(rmMsgCtx);
		} catch (SandeshaException se) {
			se.printStackTrace();
			throw new AxisFault("Error in processing the message");
		}

	}

}