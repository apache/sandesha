package org.apache.sandesha2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SequenceReport;

public class MessageContextPropertyTest extends SandeshaTestCase {



	private final String TEST_OPERATION_NAME = "testOperation";
	SimpleHTTPServer httpServer = null;
	private final String applicationNamespaceName = "http://tempuri.org/"; 
	private final String ping = "ping";
	private final String Text = "Text";
	
	private Log log = LogFactory.getLog(getClass());
	int serverPort = DEFAULT_SERVER_TEST_PORT;

	public MessageContextPropertyTest() {
		super("MessageContextPropertyTest");
	}

	public void setUp() throws AxisFault {

		String repoPath = "target" + File.separator + "repos" + File.separator + "server";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "server" + File.separator + "server_axis2.xml";

		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
				repoPath, axis2_xml);

		AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
		AxisService axisService = axisConfiguration.getService("RMSampleService");
		AxisOperation operation = AxisOperationFactory.getAxisOperation(WSDL20_2004Constants.MEP_CONSTANT_IN_ONLY);
		operation.setMessageReceiver(new TestMessageReceiver());
		operation.setName(new QName(TEST_OPERATION_NAME));
		axisService.addOperation(operation);

		AxisOperation pingOperation = axisService.getOperation(new QName("ping"));
		if (pingOperation == null)
			throw new AxisFault("Cant find the ping operation");

		// setting the operation specific phase chain
		operation.setRemainingPhasesInFlow(pingOperation.getRemainingPhasesInFlow());

		httpServer = new SimpleHTTPServer(configContext, serverPort);
		httpServer.start();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			throw new SandeshaException("sleep interupted");
		}
	}

	public void tearDown() throws SandeshaException {
		if (httpServer != null)
			httpServer.stop();

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			throw new SandeshaException("sleep interupted");
		}
	}


	public void testParameters () throws AxisFault,InterruptedException  {
		
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
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		//serviceClient.

		serviceClient.setOptions(clientOptions);
		
		serviceClient.fireAndForget(getTestOperationOMBlock("ping1"));
		
		clientOptions.setProperty(SandeshaClientConstants.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getTestOperationOMBlock("ping2"));
		
		Thread.sleep(10000);
				
		SequenceReport sequenceReport = SandeshaClient.getOutgoingSequenceReport(serviceClient);
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(2)));
		assertEquals(sequenceReport.getSequenceStatus(),SequenceReport.SEQUENCE_STATUS_TERMINATED);
		assertEquals(sequenceReport.getSequenceDirection(),SequenceReport.SEQUENCE_DIRECTION_OUT);
	
		serviceClient.finalizeInvoke();
	}
	
	private OMElement getTestOperationOMBlock(String text) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		OMElement pingElem = fac.createOMElement(TEST_OPERATION_NAME, namespace);
		OMElement textElem = fac.createOMElement(Text, namespace);
		
		textElem.setText(text);
		pingElem.addChild(textElem);

		return pingElem;
	}



	private class TestMessageReceiver extends RawXMLINOnlyMessageReceiver {

		Long lastReceivedMessage = null;
		String sequenceId = null;
		
		public void invokeBusinessLogic(MessageContext msgContext) throws AxisFault {
			Long msgNo = (Long) msgContext.getProperty(Sandesha2Constants.MessageContextProperties.MESSAGE_NUMBER);
			String sequenceId = (String) msgContext.getProperty(Sandesha2Constants.MessageContextProperties.SEQUENCE_ID);
			
			assertNotNull(msgNo);
			assertNotNull(sequenceId);
			
			if (lastReceivedMessage==null)
				assertEquals(msgNo,new Long (1));
			else 
				assertEquals(msgNo, new Long (2));
			
			if (this.sequenceId!=null)
				assertEquals(this.sequenceId,sequenceId);
			
			this.sequenceId = sequenceId;
			lastReceivedMessage = msgNo;
		}

	}



}