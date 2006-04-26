package org.apache.sandesha2.versions;

import java.io.File;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.SandeshaTestCase;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SequenceReport;

public class RMVersionTest extends SandeshaTestCase {


	SimpleHTTPServer httpServer = null;
	private final String applicationNamespaceName = "http://tempuri.org/"; 
	private final String ping = "ping";
	private final String Text = "Text";
	int serverPort = DEFAULT_SERVER_TEST_PORT;
	private Log log = LogFactory.getLog(getClass());
	
	public RMVersionTest () {
		super ("RMVersionTest");
	}
	
	public void setUp () throws AxisFault {
		
		String repoPath = "target" + File.separator + "repos" + File.separator + "server";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "server" + File.separator + "server_axis2.xml";


		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);
		String serverPortStr = getTestProperty("test.server.port");
		if (serverPortStr!=null) {
		
			try {
				serverPort = Integer.parseInt(serverPortStr);
			} catch (NumberFormatException e) {
				log.error(e);
			}
		}
		
		httpServer = new SimpleHTTPServer (configContext,serverPort);
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
	
	public void testRMSubmission () throws AxisFault,InterruptedException  {
		
		String to = "http://127.0.0.1:" + serverPort + "/axis2/services/RMSampleService";
		String transportTo = "http://127.0.0.1:" + serverPort + "/axis2/services/RMSampleService";
		
		String repoPath = "target" + File.separator + "repos" + File.separator + "client";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "client" + File.separator + "client_axis2.xml";

		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);

		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Options clientOptions = new Options ();
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		String sequenceKey = "sequence1";
		clientOptions.setProperty(SandeshaClientConstants.SEQUENCE_KEY,sequenceKey);
		
		//setting the addressing version as submission
		clientOptions.setProperty(SandeshaClientConstants.RM_SPEC_VERSION,Sandesha2Constants.SPEC_VERSIONS.WSRM);

		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.
		
		serviceClient.setOptions(clientOptions);
		
		clientOptions.setProperty(SandeshaClientConstants.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getPingOMBlock("ping3"));

		Thread.sleep(5000);
				
		SequenceReport sequenceReport = SandeshaClient.getOutgoingSequenceReport(serviceClient);
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(1)));
		assertEquals(sequenceReport.getSequenceStatus(),SequenceReport.SEQUENCE_STATUS_TERMINATED);
		assertEquals(sequenceReport.getSequenceDirection(),SequenceReport.SEQUENCE_DIRECTION_OUT);
	
		serviceClient.finalizeInvoke();
	}
	
	public void testRMOASIS () throws AxisFault,InterruptedException  {
		
		String to = "http://127.0.0.1:" + serverPort + "/axis2/services/RMSampleService";
		String transportTo = "http://127.0.0.1:" + serverPort + "/axis2/services/RMSampleService";
		
		String repoPath = "target" + File.separator + "repos" + File.separator + "client";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "client" + File.separator + "client_axis2.xml";

		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);

		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		Options clientOptions = new Options ();
		clientOptions.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		
		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		String sequenceKey = "sequence1";
		clientOptions.setProperty(SandeshaClientConstants.SEQUENCE_KEY,sequenceKey);
		
		//setting the RM version as OASIS.
		clientOptions.setProperty(SandeshaClientConstants.RM_SPEC_VERSION,Sandesha2Constants.SPEC_VERSIONS.WSRX);
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.
		
		serviceClient.setOptions(clientOptions);
		
		clientOptions.setProperty(SandeshaClientConstants.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getPingOMBlock("ping3"));

		Thread.sleep(5000);
				
		SequenceReport sequenceReport = SandeshaClient.getOutgoingSequenceReport(serviceClient);
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(1)));
		assertEquals(sequenceReport.getSequenceStatus(),SequenceReport.SEQUENCE_STATUS_TERMINATED);
		assertEquals(sequenceReport.getSequenceDirection(),SequenceReport.SEQUENCE_DIRECTION_OUT);
	
		serviceClient.finalizeInvoke();
	}
	
	private OMElement getPingOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		OMElement pingElem = fac.createOMElement(ping, namespace);
		OMElement textElem = fac.createOMElement(Text, namespace);
		
		textElem.setText(text);
		pingElem.addChild(textElem);

		return pingElem;
	}
}
