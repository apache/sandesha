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

package org.apache.sandesha2.samples;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.clientapi.InOnlyMEPClient;
import org.apache.axis2.clientapi.MessageSender;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.bcel.generic.GETFIELD;
import org.apache.sandesha2.Constants;

public class SampleClient {

	private static String sandesha1TO = "http://localhost:8070/axis/services/RMSampleService";

	private static String replyTo = "http://localhost:9070/axis/services/RMSampleService";
	
	private static String sandesha2TO = "http://localhost:8070/axis2/services/InteropService";

	public static void main(String[] args) throws AxisFault {
		SampleClient client = new SampleClient ();
		client.run();

	}
	
	private void run () throws AxisFault {
		/*
		 * MessageSender ms = new MessageSender ("E:\\wso2\\sandesha\\sandesha
		 * 2\\code\\checkouts\\Aug_25_2005\\target\\dist\\client");
		 * ms.engageModule(new QName ("sandesha")); ms.engageModule(new QName
		 * ("addressing")); ms.setTo(new EndpointReference (endPoint));
		 * ms.send("ping1",getPingOMBlock());
		 */
		
		testEcho();

		int i = 1;
	}

	public void testEcho () throws AxisFault {
		
		Call call = new Call("E:\\wso2\\sandesha\\sandesha 2\\code\\checkouts\\Aug_25_2005\\target\\dist\\client");
		call.engageModule(new QName("sandesha"));

		call.setTo(new EndpointReference(sandesha2TO));
		call.set(Constants.SEQUENCE_KEY,"sequence1");
		call.setTransportInfo(org.apache.axis2.Constants.TRANSPORT_HTTP,org.apache.axis2.Constants.TRANSPORT_HTTP,true);
		Callback callback1 = new TestCallback ("Callback 1");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo1"),callback1);
		Callback callback2 = new TestCallback ("Callback 2");
		call.set(Constants.SEQUENCE_KEY,"sequence1");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo2"),callback2);
		call.set(Constants.LAST_MESSAGE, "true");
		Callback callback3 = new TestCallback ("Callback 3");
		call.set(Constants.SEQUENCE_KEY,"sequence1");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo3"),callback3);
	}
	
	private static OMElement getPingOMBlock() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("http://tempuri.apache.org",
				"ns1");
		OMElement pingElement = fac.createOMElement("ping", ns);
		OMElement paramElement = fac.createOMElement("param1", ns);
		pingElement.addChild(paramElement);
		paramElement.setText("ping text");

		return pingElement;
	}

	private static OMElement getEchoOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace defaultNS = fac.createOMNamespace("",null);
		OMElement echoElement = fac.createOMElement("echoString", defaultNS);
		OMElement paramElement = fac.createOMElement("text", defaultNS);
		echoElement.addChild(paramElement);
		paramElement.setText(text);

		return echoElement;
	}

	private class TestCallback extends Callback {

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