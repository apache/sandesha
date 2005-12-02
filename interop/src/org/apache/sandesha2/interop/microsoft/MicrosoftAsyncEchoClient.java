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
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.sandesha2.Sandesha2Constants.ClientAPI;
import org.apache.sandesha2.util.SandeshaUtil;

public class MicrosoftAsyncEchoClient {
	private static String to = "http://131.107.72.15/ReliableMessaging_Service_Indigo/ReliableRequestReplyDual.svc"; // IP: 131.107.153.195  port:80

	private static String transportTo = "http://127.0.0.1:8070/ReliableMessaging_Service_Indigo/ReliableRequestReplyDual.svc";
	
	//private static String replyTo = "http://localhost:9080/axis/services/RMSampleService";
	
	private static String SANDESHA_HOME = "<SANDESHA2_HOME>"; //Change this to ur path.
	
	private static String AXIS2_CLIENT_PATH = SANDESHA_HOME + "\\target\\client\\";   //this will be available after a maven build
	
	public static void main(String[] args) throws AxisFault {
		new MicrosoftAsyncEchoClient ().run();
	}
	
	private void run () throws AxisFault {
		Call call = new Call(AXIS2_CLIENT_PATH);
		Options clientOptions = new Options ();
		call.setClientOptions(clientOptions);
		
		call.engageModule(new QName("sandesha"));
		clientOptions.setProperty(ClientAPI.AcksTo,"http://www-lk.wso2.com:9080/axis2/services/AnonymousService/echoString"); //Optional
		
		clientOptions.setTo(new EndpointReference(to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		clientOptions.setProperty(ClientAPI.OFFERED_SEQUENCE_ID,SandeshaUtil.getUUID());  //Optional
		clientOptions.setAction("urn:wsrm:EchoString");
		clientOptions.setSoapAction("urn:wsrm:EchoString");
		clientOptions.setProperty(ClientAPI.LAST_MESSAGE, "true");
		Callback callback3 = new TestCallback ("Callback 3");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo3"),callback3);
	}


	private static OMElement getEchoOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("http://tempuri.org/","ns1");
		OMElement echoElement = fac.createOMElement("echoString", ns);
		OMElement param1Element = fac.createOMElement("Text", ns);
		OMElement param2Element = fac.createOMElement("Sequence", ns);
		echoElement.addChild(param1Element);
		echoElement.addChild(param2Element);
		param1Element.setText(text);
		param2Element.setText("sequenceid");
		return echoElement;
	}

	class TestCallback extends Callback {

		String name = null;
		
		public TestCallback (String name) {
			this.name = name;
		}
		
		public void onComplete(AsyncResult result) {
			//System.out.println("On Complete Called for " + text);
			OMElement responseElement = result.getResponseEnvelope().getBody().getFirstElement();
			if (responseElement==null) {
				System.out.println("Response element is null");
				return;
			}
			
			String tempText = responseElement.getText();
			if (tempText==null || "".equals(tempText)){
				OMElement child = responseElement.getFirstElement();
				if (child!=null)
					tempText = child.getText();
			}
			
			
			tempText = (tempText==null)?"":tempText;
			
			System.out.println("Callback '" + name +  "' got result:" + tempText);
			
		}

		public void reportError(Exception e) {
			// TODO Auto-generated method stub
			System.out.println("Error reported for test call back");
		}
	}

	
}
