/*
 * Created on May 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha.interop;

import junit.framework.TestCase;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMReport;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * @author Administrator
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class EchoSyncTest extends TestCase {

    private static String defaultServerPort = "8080";
    private static String defaultClientPort = "9090";

    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";

    public void testPingSync() {
        System.out.println("Client started...... Synchronous ");
        try {

            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
            String str = uuidGen.nextUUID();

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:echoString",
                    Constants.ClientProperties.INOUT);
            ctx.setAcksToUrl(call, Constants.WSA.NS_ADDRESSING_ANONYMOUS);
            ctx.setReplyToUrl(call,
                    "http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
            ctx.setSendOffer(call, true);

            call.setOperationName(new QName("http://tempuri.org/", "echoString"));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", str});
            System.out.println("The Response for First Messsage is  :" + ret);

            ret = (String) call.invoke(new Object[]{"Sandesha Echo 2", str});
            System.out.println("The Response for Second Messsage is  :" + ret);

            ctx.setLastMessage(call);
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 3", str});
            System.out.println("The Response for Third Messsage is  :" + ret);

            RMReport report = ctx.endSequence(call);

            assertEquals(report.isAllAcked(), true);
            assertEquals(report.getNumberOfReturnMessages(), 3);


        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
    }

    public void setUp() {

    }

}
