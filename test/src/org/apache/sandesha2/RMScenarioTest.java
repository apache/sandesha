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
import org.apache.axis2.client.MessageSender;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.transport.http.SimpleHTTPServer;

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
		
		MessageSender sender = new MessageSender ("target\\repos\\client");
		sender.engageModule(new QName ("Sandesha2-0.9"));
		Options clientOptions = new Options ();
		sender.setClientOptions(clientOptions);
		
		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
		String to = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		
		clientOptions.setTo(new EndpointReference(to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,"sequence1");
		sender.send("ping",getPingOMBlock("ping1"));
		sender.send("ping",getPingOMBlock("ping2"));
		clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
		sender.send("ping",getPingOMBlock("ping3"));
		
		try {
			//waiting till the messages exchange finishes.
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			throw new SandeshaException ("sleep interupted");
		}
		
	    RMReport rmReport = Sandesha2ClientAPI.getRMReport(to,"sequence1",sender.getServiceContext().getConfigurationContext());
	    assertTrue(rmReport.isSequenceCompleted());
	    assertEquals(rmReport.getAckedMessageCount(),3);
	}
	
	public void testAsyncPing () throws AxisFault{
		
	}
	
	public void testSyncEcho () throws AxisFault {
		
	}
	
	public void testAsyncEcho () throws AxisFault {
		
	}
	
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
}
