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

package org.apache.sandesha2.scenarios;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SequenceReport;

import junit.framework.TestCase;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class AddressablePingTest extends TestCase {

	SimpleHTTPServer httpServer = null;
	
	private final String applicationNamespaceName = "http://tempuri.org/"; 
	private final String Ping = "Ping";
	private final String Text = "Text";
	
	public void setUp () throws AxisFault {
		String repoPath = "target" + File.separator + "repos" + File.separator + "server";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "server" + File.separator + "server_axis2.xml";
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);
		
		configContext.setProperty("test","test");
		
		httpServer = new SimpleHTTPServer (configContext,8060);
		
		httpServer.start();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			throw new SandeshaException ("sleep interupted");
		}
	}
	
	public void tearDown () throws SandeshaException {
		if (httpServer!=null)
			httpServer.stop();
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			throw new SandeshaException ("sleep interupted");
		}
	}
	
	public void testAsyncPing () throws AxisFault,IOException {
		
		String to = "http://127.0.0.1:8060/axis2/services/RMSampleService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMSampleService";
		String acksToEPR = "http://127.0.0.1:6060/axis2/services/__ANONYMOUS_SERVICE__";
		
		String repoPath = "target" + File.separator + "repos" + File.separator + "client";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "client" + File.separator + "client_axis2.xml";
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);

		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Options clientOptions = new Options ();
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		String sequenceKey = "sequence2";
		clientOptions.setProperty(SandeshaClientConstants.SEQUENCE_KEY,sequenceKey);
		clientOptions.setProperty(SandeshaClientConstants.AcksTo,acksToEPR);
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.
		
		serviceClient.setOptions(clientOptions);
		
		serviceClient.fireAndForget(getPingOMBlock("ping1"));
		
		clientOptions.setProperty(SandeshaClientConstants.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getPingOMBlock("ping2"));
		
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		serviceClient.finalizeInvoke();
		
		SequenceReport sequenceReport = SandeshaClient.getOutgoingSequenceReport(serviceClient);
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(1)));
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(2)));
		assertEquals(sequenceReport.getSequenceStatus(),SequenceReport.SEQUENCE_STATUS_TERMINATED);
		assertEquals(sequenceReport.getSequenceDirection(),SequenceReport.SEQUENCE_DIRECTION_OUT);
		
	}
	
	private OMElement getPingOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		OMElement pingElem = fac.createOMElement(Ping, namespace);
		OMElement textElem = fac.createOMElement(Text, namespace);
		
		textElem.setText(text);
		pingElem.addChild(textElem);

		return pingElem;
	}

}
