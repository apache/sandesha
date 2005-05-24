package org.apache.sandesha.samples.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMTransport;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Apr 21, 2005
 * Time: 5:09:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class IBMEcho {
    private static String sourceHost = "192.248.18.51"; //Change this to your public IP address
    private static String targetURL = "http://127.0.0.1:8080/wsrm/services/rmDemos";
    private static String sourcePort = "9070"; //Change this according to the listening port of the TCPMonitor in the
    //client side.

    public static void main(String[] args) {

        System.out.println("Client started...... Asynchronous EchoString - IBM");

          UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
            String str = uuidGen.nextUUID();

        try {
              Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:echoString",Constants.ClientProperties.IN_OUT);

            ctx.setToUrl(call, "http://wsi.alphaworks.ibm.com:8080/wsrm/services/rmDemos");
            ctx.setFaultToUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setAcksToUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setFromUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setReplyToUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");

            call.setOperationName(new QName("http://tempuri.org/", "echoString"));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("Sequence", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", str});
            System.out.println("The Response for First Messsage is  :" + ret);

            ret = (String) call.invoke(new Object[]{"Sandesha Echo 2", str});
            System.out.println("The Response for Second Messsage is  :" + ret);

            ctx.setLastMessage(call);
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 3", str});
            System.out.println("The Response for Third Messsage is  :" + ret);

             ctx.endSequence(call);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
