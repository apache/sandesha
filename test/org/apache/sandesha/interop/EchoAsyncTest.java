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
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
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
public class EchoAsyncTest extends TestCase {
	
    private static String defaultServerPort = "8070";
    private static String defaultClientPort = "9070";
    
    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";
	
	public void testPingSync(){
        System.out.println("Client started...... Synchronous ");
        try {

            //A separate listner will be started if the value of the input parameter for the mehthod
            // initClient is "false". If the service is of type request/response the parameter value shoule be "false"
            RMInitiator.initClient(false);

            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
            String str = uuidGen.nextUUID();


            Service service = new Service();
            Call call = (Call) service.createCall();

            //To obtain the
            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:echoString");

            //These two are additional
            call.setProperty(Constants.ClientProperties.ACKS_TO,"http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.REPLY_TO,"http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
            //<wsrm:Offer> is also an configurable option.
            call.setProperty(Constants.ClientProperties.SEND_OFFER,new Boolean(true));

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org/", "echoString"));
            call.setTransport(new RMTransport(targetURL, ""));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
            String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", str});
            System.out.println("The Response for First Messsage is  :" + ret);

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 2", str});
            System.out.println("The Response for Second Messsage is  :" + ret);

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
            call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true)); //For last message.
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 3", str});
            System.out.println("The Response for Third Messsage is  :" + ret);

            RMStatus status =  RMInitiator.stopClient();
            RMReport report = status.getReport();
            
            assertEquals(report.isAllAcked(),true);
            assertEquals(report.getNumberOfReturnMessages(),3);

            

        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
	}
	
	public void setUp(){
		
	}
	
}
