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

package org.apache.sandesha2.interop.microsoft;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.MessageSender;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.sandesha2.Sandesha2Constants.ClientAPI;

/**
 * @author chamikara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class MicrosoftAsyncPingClient {

	private static String to = "http://131.107.72.15/ReliableMessaging_Service_Indigo/ReliableOneWayDual.svc";   //IP : 131.107.153.195  Port:80

	private static String transportTo = "http://127.0.0.1:8070/ReliableMessaging_Service_Indigo/ReliableOneWayDual.svc";
	
	private static String SANDESHA2_HOME = "<SANDESHA2_HOME>"; //Change this to ur path.
	
	private static String AXIS2_CLIENT_PATH = SANDESHA2_HOME + "\\target\\client\\";   //this will be available after a maven build
	
	public static void main(String[] args) throws AxisFault {
		new MicrosoftAsyncPingClient ().run();
	}
	
	public void run () throws AxisFault {
		
		if ("<SANDESHA2_HOME>".equals(SANDESHA2_HOME)){
			System.out.println("ERROR: Please change <SANDESHA2_HOME> to your Sandesha2 installation directory.");
			return;
		}
		
		MessageSender sender = new MessageSender (AXIS2_CLIENT_PATH);
		Options clientOptions = new Options ();
		sender.setClientOptions(clientOptions);
		sender.engageModule(new QName ("sandesha"));
		clientOptions.setProperty(ClientAPI.SANDESHA_DEBUG_MODE,"on");   //Sets the debug on for sandesha.
		clientOptions.setTo(new EndpointReference(to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		clientOptions.setProperty(ClientAPI.AcksTo,"http://www-lk.wso2.com:9080/axis2/services/AnonymousService/echoString");
		clientOptions.setReplyTo(new EndpointReference ("http://www-lk.wso2.com:9080/axis2/services/AnonymousService/echoString"));
		clientOptions.setFaultTo(new EndpointReference ("http://www-lk.wso2.com:9080/axis2/services/AnonymousService/echoString"));
		clientOptions.setProperty(ClientAPI.SEQUENCE_KEY,"sequence1");
		clientOptions.setSoapAction("urn:wsrm:Ping");
		clientOptions.setAction("urn:wsrm:Ping");
  		sender.send("ping",getPingOMBlock("Microsoft-1"));
		sender.send("ping",getPingOMBlock("Microsoft-2"));
		clientOptions.setProperty(ClientAPI.LAST_MESSAGE, "true");
		sender.send("ping",getPingOMBlock("Microsoft-3"));
	}
	
	private static OMElement getPingOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("http://tempuri.org/",
				"ns1");
		OMNamespace defautNS = fac.createOMNamespace("",null);
		OMElement pingElement = fac.createOMElement("Ping", ns);
		OMElement paramElement = fac.createOMElement("Text", ns);
		pingElement.addChild(paramElement);
		paramElement.setText(text);

		return pingElement;
	}

}
