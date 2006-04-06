package org.apache.sandesha2;

import java.io.File;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.client.RMClientAPI;
import org.apache.sandesha2.client.RMClientConstants;
import org.apache.sandesha2.client.RMReport;
import org.apache.sandesha2.client.SequenceReport;
import org.apache.sandesha2.util.SandeshaUtil;

import junit.framework.TestCase;

public class SquenceOfferTest extends TestCase {

	private static Log log = LogFactory.getLog(SquenceOfferTest.class);
	
	SimpleHTTPServer httpServer = null;
	
	private final static String applicationNamespaceName = "http://tempuri.org/"; 
	private final static String echoString = "echoString";
	private final static String Text = "Text";
	private final static String Sequence = "Sequence";
	private final static String echoStringResponse = "echoStringResponse";
	private final static String EchoStringReturn = "EchoStringReturn";
	
	public void setUp () throws AxisFault {
		String repoPath = "target" + File.separator + "repos" + File.separator + "server";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "server" + File.separator + "axis2.xml";
		
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
	
	public void testSequenceOffer () throws AxisFault, InterruptedException {
		
		String to = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String transportTo = "http://127.0.0.1:8060/axis2/services/RMInteropService";
		String acksToEPR = "http://127.0.0.1:9070/axis2/services/__ANONYMOUS_SERVICE__";
		
		String repoPath = "target" + File.separator + "repos" + File.separator + "client";
		String axis2_xml = "target" + File.separator + "repos" + File.separator + "client" + File.separator + "axis2.xml";
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2_xml);

		Options clientOptions = new Options ();

		clientOptions.setTo(new EndpointReference (to));
		clientOptions.setProperty(MessageContextConstants.TRANSPORT_URL,transportTo);
		
		String sequenceKey = SandeshaUtil.getUUID();
		clientOptions.setProperty(RMClientConstants.SEQUENCE_KEY,sequenceKey);
		clientOptions.setProperty(RMClientConstants.AcksTo,acksToEPR);
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		
		String offeredSequenceID = SandeshaUtil.getUUID();
		clientOptions.setProperty(RMClientConstants.OFFERED_SEQUENCE_ID,offeredSequenceID);
		
		ServiceClient serviceClient = new ServiceClient (configContext,null);
		serviceClient.setOptions(clientOptions);
		//serviceClient.
		
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		clientOptions.setUseSeparateListener(true);
		
		serviceClient.setOptions(clientOptions);
		serviceClient.engageModule(new QName ("sandesha2"));  //engaging the sandesha2 module.
		
		TestCallback callback1 = new TestCallback ("Callback 1");
		serviceClient.sendReceiveNonBlocking(getEchoOMBlock("echo1",sequenceKey),callback1);
		
		clientOptions.setProperty(RMClientConstants.LAST_MESSAGE, "true");
		TestCallback callback2 = new TestCallback ("Callback 2");
		serviceClient.sendReceiveNonBlocking (getEchoOMBlock("echo2",sequenceKey),callback2);

        
        Thread.sleep(12000);
		serviceClient.finalizeInvoke();
		
        //assertions for the out sequence.
		SequenceReport sequenceReport = RMClientAPI.getOutgoingSequenceReport(to,sequenceKey,configContext);
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(1)));
		assertTrue(sequenceReport.getCompletedMessages().contains(new Long(2)));
		assertEquals(sequenceReport.getSequenceStatus(),SequenceReport.SEQUENCE_STATUS_TERMINATED);
		assertEquals(sequenceReport.getSequenceDirection(),SequenceReport.SEQUENCE_DIRECTION_OUT);
		
		assertTrue(callback1.isComplete());
		assertTrue(callback2.isComplete());
		assertEquals(callback1.getResult(),"echo1");
		assertEquals(callback2.getResult(),"echo1echo2");
		
		//checking weather the incomingSequenceReport has the offered sequence ID
		RMReport rmReport = RMClientAPI.getRMReport(configContext);
		ArrayList incomingSeqList = rmReport.getIncomingSequenceList();
		assertEquals(incomingSeqList.size(),1);
		assertEquals(incomingSeqList.get(0),offeredSequenceID);	
	}
	
	private static OMElement getEchoOMBlock(String text, String sequenceKey) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace applicationNamespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		OMElement echoStringElement = fac.createOMElement(echoString, applicationNamespace);
		OMElement textElem = fac.createOMElement(Text,applicationNamespace);
		OMElement sequenceElem = fac.createOMElement(Sequence,applicationNamespace);
		
		textElem.setText(text);
		sequenceElem.setText(sequenceKey);
		echoStringElement.addChild(textElem);
		echoStringElement.addChild(sequenceElem);
		
		return echoStringElement;
	}
	
	class TestCallback extends Callback {

		String name = null;
		boolean completed = false;
		boolean errorRported = false;
		String resultStr;
		
		public boolean isCompleted() {
			return completed;
		}

		public boolean isErrorRported() {
			return errorRported;
		}

		public String getResult () {
			return resultStr;
		}
		
		public TestCallback (String name) {
			this.name = name;
		}
		
		public void onComplete(AsyncResult result) {

			SOAPBody body = result.getResponseEnvelope().getBody();
			
			OMElement echoStringResponseElem = body.getFirstChildWithName(new QName (applicationNamespaceName,echoStringResponse));
			if (echoStringResponseElem==null) { 
				log.error("Error: SOAPBody does not have a 'echoStringResponse' child");
				return;
			}
			
			OMElement echoStringReturnElem = echoStringResponseElem.getFirstChildWithName(new QName (applicationNamespaceName,EchoStringReturn));
			if (echoStringReturnElem==null) { 
				log.error("Error: 'echoStringResponse' element does not have a 'EchoStringReturn' child");
				return;
			}
			
			String resultStr = echoStringReturnElem.getText();
			this.resultStr = resultStr;
			completed = true;
		}

		public void onError (Exception e) {
			e.printStackTrace();
			errorRported = true;
		}
	}

}