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

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.MessageSender;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.client.Sandesha2ClientAPI;


public class AsyncPingClient {

	private String toIP = "127.0.0.1";
	
	private String toPort = "8070";
	
	private String ackIP = "127.0.0.1";
	
	private String ackPort = "9070";

	private String toEPR = "http://" + toIP +  ":" + toPort + "/axis2/services/RMInteropService";
	
	private String acksToEPR = "http://" + ackIP +  ":" + ackPort + "/axis2/services/" + "__ANONYMOUS_SERVICE__";
	
	private static String SANDESHA2_HOME = "<SANDESHA2_HOME>"; //Change this to ur path.
	
	private static String AXIS2_CLIENT_PATH = SANDESHA2_HOME + File.separator + "target" + File.separator +"repos" + File.separator + "client" + File.separator;   //this will be available after a maven build
	
	public static void main(String[] args) throws AxisFault {
		
		String axisClientRepo = null;
		if (args!=null && args.length>0)
			axisClientRepo = args[0];
		
		if (axisClientRepo!=null && !"".equals(axisClientRepo)) {
			AXIS2_CLIENT_PATH = axisClientRepo;
			SANDESHA2_HOME = "";
		}
		
		new AsyncPingClient().run();
	}
	
	public void run () throws AxisFault {
		if ("<SANDESHA2_HOME>".equals(SANDESHA2_HOME)){
			System.out.println("ERROR: Please change <SANDESHA2_HOME> to your Sandesha2 installation directory.");
			return;
		}
		
		String axis2_xml = AXIS2_CLIENT_PATH + "axis2.xml";
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_CLIENT_PATH,axis2_xml);

		ServiceClient serviceClient = new ServiceClient (configContext,null);
		
		Options clientOptions = new Options ();
		
		//clientOptions.setProperty(MessageContextConstants.CHUNKED,Constants.VALUE_FALSE);   //uncomment this to send messages without chunking.
		
		//clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);   //uncomment this to send messages in SOAP 1.2
		
		//clientOptions.setProperty(Sandesha2ClientAPI.RM_SPEC_VERSION,Sandesha2Constants.SPEC_VERSIONS.WSRX);  //uncomment this to send the messages according to the WSRX spec.
		
		clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
		clientOptions.setTo(new EndpointReference (toEPR));
		clientOptions.setProperty(Sandesha2ClientAPI.AcksTo,acksToEPR);
		clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		clientOptions.setProperty(Sandesha2ClientAPI.SEQUENCE_KEY,"sequence1");
		
		serviceClient.setOptions(clientOptions);
		serviceClient.engageModule(new QName ("sandesha2"));
		
		serviceClient.fireAndForget(getPingOMBlock("ping1"));
		serviceClient.fireAndForget(getPingOMBlock("ping2"));
		
		clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
		serviceClient.fireAndForget(getPingOMBlock("ping3"));
		
	}
	
	private static OMElement getPingOMBlock(String text) {
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
