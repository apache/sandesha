package org.apache.sandesha.samples.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMTransport;

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
            call.setProperty(Constants.ClientProperties.TO, "http://wsi.alphaworks.ibm.com:8080/wsrm/services/rmDemos");

            call.setProperty(Constants.ClientProperties.ACKS_TO, "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.FAULT_TO, "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.FROM, "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.REPLY_TO, "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org/", "echoString"));
            call.setTransport(new RMTransport(targetURL, ""));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("Sequence", XMLType.XSD_STRING, ParameterMode.IN);
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

            RMInitiator.stopClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
