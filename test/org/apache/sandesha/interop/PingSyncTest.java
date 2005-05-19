/*
 * Created on May 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha.interop;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMReport;
import org.apache.sandesha.RMStatus;
import org.apache.sandesha.RMTransport;

import junit.framework.TestCase;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PingSyncTest extends TestCase {

    private static String defaultServerPort = "8080";

    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";
	
	public void testPingSync(){
        System.out.println("Client started...... Synchronous ");
        try {

            RMInitiator.initClient(true);

            Service service = new Service();
            Call call = (Call) service.createCall();

            //By setting the SYNC property as true and false, the user can select between
            //the synchronous version and asynchronous version of invocations.
            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(true));

            //Sandesha uses action to differentitiate services initially (when there are no
            //sequence identifiers are available. This is a REQUIRED option for Sandesha.
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:Ping");

            //This line is used to set the transport for Sandesha
            call.setTransport(new RMTransport(targetURL, ""));

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org/", "Ping"));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);

            //First Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
            call.invoke(new Object[]{"Ping Message Number One"});

            //Second Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
            call.invoke(new Object[]{"Ping Message Number Two"});

            //Third Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
            //For last message.
            call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
            call.invoke(new Object[]{"Ping Message Number Three"});

            RMStatus status =  RMInitiator.stopClient();
            RMReport report = status.getReport();
            
            assertEquals(report.isAllAcked(),true);
            assertEquals(report.getNumberOfReturnMessages(),0);

        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
	}
	
	public void setUp(){
		
	}
}
