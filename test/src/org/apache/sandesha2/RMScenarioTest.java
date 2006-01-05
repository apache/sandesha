/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sandesha2;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.sandesha2.client.Sandesha2ClientAPI;


/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class RMScenarioTest extends TestCase {
	
	SimpleHTTPServer httpServer = null;
	
	public void setUp () throws AxisFault {
		httpServer = new SimpleHTTPServer ("target\\repos\\server",8060);
		httpServer.start();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			throw new SandeshaException ("sleep interupted");
		}
	}
	
	public void tearDown () throws AxisFault {
		if (httpServer!=null)
			httpServer.stop();
	}
	
	public void testSyncPing () throws AxisFault {
		
		String to = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		
		ConfigurationContext configContext = new ConfigurationContextFactory().createConfigurationContextFromFileSystem("target\\repos\\client");

		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Options clientOptions = new Options ();
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
	//	clientOptions.setr\
		clientOptions.setProperty(Options.COPY_PROPERTIES, new Boolean (true));
		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,"sequence1");
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.
		
		serviceClient.engageModule(new QName ("Sandesha2-0.9"));
		serviceClient.setOptions(clientOptions);
		
		serviceClient.fireAndForget(getPingOMBlock("ping1"));
		serviceClient.fireAndForget(getPingOMBlock("ping2"));
		
		clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getPingOMBlock("ping3"));
		
		try {
			//waiting till the messages exchange finishes.
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new SandeshaException ("sleep interupted");
		}
		
//	    RMReport rmReport = Sandesha2ClientAPI.getRMReport(to,"sequence1",sender.getServiceContext().getConfigurationContext());
//	    assertTrue(rmReport.isSequenceCompleted());
//	    assertEquals(rmReport.getAckedMessageCount(),3);
	}
	
	public void testAsyncPing () throws AxisFault{
		
		String to = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String acksToEPR = "http://127.0.0.1:6060/axis2/services/AnonymousService/echoString";
		
		ConfigurationContext configContext = new ConfigurationContextFactory().createConfigurationContextFromFileSystem("target\\repos\\client");

		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Options clientOptions = new Options ();
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
	//	clientOptions.setr\
		clientOptions.setProperty(Options.COPY_PROPERTIES, new Boolean (true));
		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,"sequence1");
		clientOptions.setProperty(Sandesha2ClientAPI.AcksTo,acksToEPR);
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.
		
		serviceClient.engageModule(new QName ("Sandesha2-0.9"));
		serviceClient.setOptions(clientOptions);
		
		serviceClient.fireAndForget(getPingOMBlock("ping1"));
		serviceClient.fireAndForget(getPingOMBlock("ping2"));
		
		clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getPingOMBlock("ping3"));
		
		try {
			//waiting till the messages exchange finishes.
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			throw new SandeshaException ("sleep interupted");
		}
		
//	    RMReport rmReport = Sandesha2ClientAPI.getRMReport(to,"sequence2",sender.getServiceContext().getConfigurationContext());
//	    assertTrue(rmReport.isSequenceCompleted());
//	    assertEquals(rmReport.getAckedMessageCount(),3);
	}
	
	public void testSyncEcho () throws AxisFault {
		
	}
	
	/*
	public void testAsyncEcho () throws AxisFault {
		
		String to = "http://127.0.0.1:8070/axis2/services/RMInteropService";
		String transportTo = "http://127.0.0.1:8070/axis2/services/RMInteropService";
		String acksTo = "http://127.0.0.1:8070/axis2/services/AnonymousService/echoString";
		
		Call call = new Call("target\\repos\\client");
		call.engageModule(new QName("Sandesha2-0.9"));
		Options clientOptions = new Options ();
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		call.setClientOptions(clientOptions);
		
		//You must set the following two properties in the request-reply case.
		clientOptions.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
		clientOptions.setUseSeparateListener(true);
		
		clientOptions.setProperty(Sandesha2ClientAPI.AcksTo,acksTo);
		clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		clientOptions.setTo(new EndpointReference(to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,"sequence3");  //Optional
		clientOptions.setSoapAction("test:soap:action");
		
		String offeredSequenceID = SandeshaUtil.getUUID();
		clientOptions.setProperty(Sandesha2ClientAPI.OFFERED_SEQUENCE_ID,offeredSequenceID);  //Optional
		Callback callback1 = new TestCallback ("Callback 1");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo1"),callback1);
		Callback callback2 = new TestCallback ("Callback 2");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo2"),callback2);
		clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
		Callback callback3 = new TestCallback ("Callback 3");
		call.invokeNonBlocking("echoString", getEchoOMBlock("echo3"),callback3);
		
        try {
			while (!callback3.isComplete()) {
			    Thread.sleep(1000);
			}
			
			Thread.sleep(5000);   //waiting till the terminate finishes

		} catch (InterruptedException e) {
			throw new SandeshaException (e.getMessage());
		}        
		
        ConfigurationContext configurationContext = call.getServiceContext().getConfigurationContext();
        RMReport report1 = Sandesha2ClientAPI.getRMReport(to,"sequence3",configurationContext);
        RMReport report2 = Sandesha2ClientAPI.getIncomingSequenceReport(offeredSequenceID,configurationContext);
        
	} */
	
	private OMElement getPingOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("http://tempuri.apache.org",
				"ns1");
		OMElement pingElement = fac.createOMElement("ping", ns);
		OMElement paramElement = fac.createOMElement("param1", ns);
		pingElement.addChild(paramElement);
		paramElement.setText(text);

		return pingElement;
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
		boolean completed = false;
		boolean faultReported = false;
		
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
			
			completed = true;
			
		}

		public void onError (Exception e) {
			// TODO Auto-generated method stub
			faultReported = true;
			System.out.println("Error reported for test call back");
			e.printStackTrace();
		}
		
		
		public boolean isCompleted() {
			return completed;
		}
		
		public void setCompleted(boolean completed) {
			this.completed = completed;
		}
		
		public boolean isFaultReported() {
			return faultReported;
		}
		
		public void setFaultReported(boolean faultReported) {
			this.faultReported = faultReported;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
	}
}
