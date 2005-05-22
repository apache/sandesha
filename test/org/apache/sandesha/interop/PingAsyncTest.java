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
import org.apache.sandesha.*;

import junit.framework.TestCase;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PingAsyncTest extends TestCase {
    private static String defaultServerPort = "8080";
    private static String defaultClientPort = "9090";
    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";
	
	public void testPingSync(){
        System.out.println("Client started...... Asynchronous ");
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:ping",
                    Constants.ClientProperties.IN_ONLY);
            ctx.setAcksToUrl(call,
                    "http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");

            call.setOperationName(new QName("http://tempuri.org", "Ping"));
            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

            call.invoke(new Object[]{"Ping Message Number One"});
            call.invoke(new Object[]{"Ping Message Number Two"});
            ctx.setLastMessage(call);
            call.invoke(new Object[]{"Ping Message Number Three"});

            RMReport report = ctx.endSequence(call);
            
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
