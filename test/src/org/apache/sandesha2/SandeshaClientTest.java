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

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SequenceReport;
import org.apache.sandesha2.util.SandeshaUtil;

import junit.framework.TestCase;

public class SandeshaClientTest extends TestCase {

	SimpleHTTPServer httpServer = null;
	
	public void setUp () throws AxisFault {
		String repoPath = "target" + File.separator + "repos" + File.separator + "server";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "server" + File.separator + "server_axis2.xml";
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);
		
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
	
	public void testCreateSequenceWithOffer () throws AxisFault,InterruptedException {
		
		String to = "http://127.0.0.1:8060/axis2/services/RMSampleService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMSampleService";
		String acksToEPR = "http://127.0.0.1:6060/axis2/services/__ANONYMOUS_SERVICE__";
		
		String repoPath = "target" + File.separator + "repos" + File.separator + "client";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "client" + File.separator + "client_axis2.xml";
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);

		Options clientOptions = new Options ();

		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		String sequenceKey = SandeshaUtil.getUUID();
		clientOptions.setProperty(SandeshaClientConstants.SEQUENCE_KEY,sequenceKey);
		clientOptions.setProperty(SandeshaClientConstants.AcksTo,acksToEPR);
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		
		String offeredSequenceID = SandeshaUtil.getUUID();
		clientOptions.setProperty(SandeshaClientConstants.OFFERED_SEQUENCE_ID,offeredSequenceID);
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		serviceClient.setOptions(clientOptions);
		//serviceClient.
		
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		clientOptions.setUseSeparateListener(true);
		
		serviceClient.setOptions(clientOptions);
		
		SandeshaClient.createSequence(serviceClient,true);
		
		Thread.sleep(3000);
		
		SequenceReport sequenceReport = SandeshaClient.getOutgoingSequenceReport(serviceClient);
		
		assertNotNull(sequenceReport.getSequenceID());
	}
	
//	public void testCreateSequenceWithoutOffer () {
////		SandeshaClient.createSequence(serviceClient,true);
//		
//		
//	}
	
//	public void testCreateSequenceWithSequenceKey () {
//		
//	}
//	
//	public void testTerminateSequence () {
//		
//	}
//	
//	public void testCloseSequence () {
//		
//	}
//	
//	public void testAckRequest () {
//		
//	}
//	
//	public void getSequenceIDTest () {
//		
//	}
	
	
}
