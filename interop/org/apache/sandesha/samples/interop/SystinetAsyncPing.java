package org.apache.sandesha.samples.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMTransport;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;


public class SystinetAsyncPing {

    private static String targetURL = "http://127.0.0.1:6064/Service";
    private static String sourceHost="192.248.18.51"; //Change this to your public IP address
    private static String sourcePort="9070"; //Change this according to the listening port of the TCPMonitor in the
                                             //client side.

    public static void main(String[] args) {
        System.out.println("Client started...... Synchronous ");
        try {

            RMInitiator.initClient(false);

            Service service = new Service();
            Call call = (Call) service.createCall();

            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:Ping");
            call.setProperty(Constants.ClientProperties.TO, "http://soap.systinet.net:6064/Service");

            //We need to specify these since, we need to pass all the messages thorugh TCPMonitor. Else
            //it will pick up the source host as the machine's IP and the source port as 9090 (default)
            call.setProperty(Constants.ClientProperties.ACKS_TO, "http://"+sourceHost+":"+sourcePort+"/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.FAULT_TO,"http://"+sourceHost+":"+sourcePort+"/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.FROM,"http://"+sourceHost+":"+sourcePort+"/axis/services/RMService");

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("urn:wsrm:Ping", "Ping"));
            call.setTransport(new RMTransport(targetURL, ""));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);

            //First Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
            call.invoke(new Object[]{"Ping Message Number One"});

            //Second Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
            call.invoke(new Object[]{"Ping Message Number Two"});

            //Third Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
            call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true)); //For last message.
            call.invoke(new Object[]{"Ping Message Number Three"});

            RMInitiator.stopClient();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}