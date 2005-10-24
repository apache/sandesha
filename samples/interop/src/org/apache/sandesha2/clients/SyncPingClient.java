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

package org.apache.sandesha2.clients;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.MessageSender;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.sandesha2.Constants;

public class SyncPingClient {

	private static String sandesha1TO = "http://localhost:8070/axis/services/RMSampleService";

	private static String replyTo = "http://localhost:9070/axis/services/RMSampleService";
	
	private static String sandesha2TO = "http://localhost:8070/axis2/services/InteropService";

	private static String SANDESHA_HOME = "E:\\sandesha\\sandesha 2\\code\\checkouts\\"; //Change this to ur path.
	
	private static String AXIS2_CLIENT_PATH = SANDESHA_HOME + "target\\client\\";   //this will be available after a maven build
	
	public static void main(String[] args) throws AxisFault {
		new SyncPingClient ().run();
	}
	
	public void run () throws AxisFault {
		MessageSender sender = new MessageSender (AXIS2_CLIENT_PATH);
		sender.engageModule(new QName ("sandesha"));
		sender.setTo(new EndpointReference(sandesha2TO));
		sender.set(Constants.SEQUENCE_KEY,"sequence1");
		sender.send("ping",getPingOMBlock("ping1"));
		sender.send("ping",getPingOMBlock("ping2"));
		sender.set(Constants.LAST_MESSAGE, "true");
		sender.send("ping",getPingOMBlock("ping3"));
	}
	
	private static OMElement getPingOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("http://tempuri.apache.org",
				"ns1");
		OMElement pingElement = fac.createOMElement("ping", ns);
		OMElement paramElement = fac.createOMElement("param1", ns);
		pingElement.addChild(paramElement);
		paramElement.setText(text);

		return pingElement;
	}
	
}
