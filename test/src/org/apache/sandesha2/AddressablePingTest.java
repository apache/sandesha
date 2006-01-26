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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
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
import org.apache.sandesha2.client.SequenceReport;

import junit.framework.TestCase;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class AddressablePingTest extends TestCase {

	SimpleHTTPServer httpServer = null;
	
	public void setUp () throws AxisFault {
		httpServer = new SimpleHTTPServer ("target\\repos\\server","target\\repos\\server\\axis2.xml",8060);
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
	
	public void testAsyncPing () throws AxisFault{
		
		String to = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String acksToEPR = "http://127.0.0.1:6060/axis2/services/__ANONYMOUS_SERVICE__";
		
		ConfigurationContext configContext = new ConfigurationContextFactory().createConfigurationContextFromFileSystem("target\\repos\\client","target\\repos\\server\\axis2.xml");

		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Options clientOptions = new Options ();
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
	//	clientOptions.setr\
		clientOptions.setProperty(Options.COPY_PROPERTIES, new Boolean (true));
		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		String sequenceKey = "sequence2";
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,sequenceKey);
		clientOptions.setProperty(Sandesha2ClientAPI.AcksTo,acksToEPR);
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.
		
		serviceClient.engageModule(new QName ("sandesha2"));
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
		
		SequenceReport sequenceReport = Sandesha2ClientAPI.getOutgoingSequenceReport(to,sequenceKey,configContext);
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(1)));
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(2)));
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(3)));
		assertEquals(sequenceReport.getSequenceStatus(),SequenceReport.SEQUENCE_STATUS_COMPLETED);
		assertEquals(sequenceReport.getSequenceDirection(),SequenceReport.SEQUENCE_DIRECTION_OUT);
		
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
