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

package sandesha2.samples.interop;

import javax.xml.namespace.QName;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.sandesha2.client.RMReport;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.util.SandeshaUtil;

public class AsyncEchoClient {
	
	private String toIP = "127.0.0.1";
	
	private String toPort = "8070";
	
	private String ackIP = "127.0.0.1";
	
	private String ackPort = "9070";
	
	private String toEPR = "http://" + toIP +  ":" + toPort + "/axis2/services/RMInteropService";

	private String acksToEPR = "http://" + ackIP +  ":" + ackPort + "/axis2/services/" + "__ANONYMOUS_SERVICE__";
	
	private static String SANDESHA2_HOME = "<SANDESHA2_HOME>"; //Change this to ur path.
	
	private static String AXIS2_CLIENT_PATH = SANDESHA2_HOME + "\\target\\repos\\client\\";   //this will be available after a maven build
	
	public static void main(String[] args) throws Exception {
		
		
		String axisClientRepo = null;
		if (args!=null && args.length>0)
			axisClientRepo = args[0];
		
		if (axisClientRepo!=null && !"".equals(axisClientRepo)) {
			AXIS2_CLIENT_PATH = axisClientRepo;
			SANDESHA2_HOME = "";
		}
		
		new AsyncEchoClient ().run();
	}
	
	private void run () throws Exception {
		
		if ("<SANDESHA2_HOME>".equals(SANDESHA2_HOME)){
			System.out.println("ERROR: Please set the directory you unzipped Sandesha2 as the first option.");
			return;
		}
		
		String axis2_xml = AXIS2_CLIENT_PATH + "axis2.xml";
     
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_CLIENT_PATH,axis2_xml);

		ServiceClient serviceClient = new ServiceClient (configContext,null);
		
		
		Options clientOptions = new Options ();
		
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		clientOptions.setTo(new EndpointReference (toEPR));
		clientOptions.setProperty(Sandesha2ClientAPI.AcksTo,acksToEPR);
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,"sequence1");

		//You must set the following two properties in the request-reply case.
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		clientOptions.setUseSeparateListener(true);
		
		serviceClient.setOptions(clientOptions);
		serviceClient.engageModule(new QName ("Sandesha2-0.9"));
		//clientOptions.setProperty(MessageContextConstants.CHUNKED,Constants.VALUE_FALSE);
		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		String offeredSequenceID = SandeshaUtil.getUUID();
		clientOptions.setProperty(Sandesha2ClientAPI.OFFERED_SEQUENCE_ID,offeredSequenceID);  //Optional
		
		Callback callback1 = new TestCallback ("Callback 1");
		serviceClient.sendReceiveNonblocking(getEchoOMBlock("echo1"),callback1);
		Callback callback2 = new TestCallback ("Callback 2");
		serviceClient.sendReceiveNonblocking(getEchoOMBlock("echo2"),callback2);
		
		Callback callback3 = new TestCallback ("Callback 3");
		serviceClient.sendReceiveNonblocking(getEchoOMBlock("echo3"),callback3);
		Callback callback4 = new TestCallback ("Callback 4");
		serviceClient.sendReceiveNonblocking(getEchoOMBlock("echo4"),callback4);
		
		clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
		Callback callback5 = new TestCallback ("Callback 5");
		serviceClient.sendReceiveNonblocking(getEchoOMBlock("echo5"),callback5);
		
        while (!callback5.isComplete()) {
            Thread.sleep(1000);
        }
       
	}

	private static OMElement getEchoOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace defaultNS = fac.createOMNamespace("http://tempuri.apache.org","ns1");
		OMElement echoElement = fac.createOMElement("echoString", null);
		OMElement paramElement = fac.createOMElement("text", null);
		echoElement.addChild(paramElement);
		paramElement.setText(text);

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

		public void onError (Exception e) {
			// TODO Auto-generated method stub
			System.out.println("Error reported for test call back");
			e.printStackTrace();
		}
	}

	
}
