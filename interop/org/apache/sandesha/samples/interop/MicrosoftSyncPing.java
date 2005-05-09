package org.apache.sandesha.samples.interop;

import org.apache.axis.Message;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMTransport;

public class MicrosoftSyncPing {

    private static String targetURL = "http://127.0.0.1:8080/SecureReliableMessaging/ReliableOneWay.svc";

    public static void main(String[] args) {
        System.out.println("Client started...... Synchronous - Microsoft ");
        try {

            RMInitiator.initClient(true);

            Service service = new Service();
            Call call = (Call) service.createCall();

            //This is only bacause we need to send ReplyTo in all the messages for the Microsoft endpoint.
            //If the invocation is sync then this property should be "true" however due to this bug in Microsoft
            //Have to make it "false" to send ReplyTo.
            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:Ping");
            call.setProperty(Constants.ClientProperties.TO, "http://131.107.153.195/SecureReliableMessaging/ReliableOneWay.svc");

            call.setProperty(Constants.ClientProperties.FROM, Constants.WSA.NS_ADDRESSING_ANONYMOUS);
            call.setProperty(Constants.ClientProperties.ACKS_TO,Constants.WSA.NS_ADDRESSING_ANONYMOUS);
            call.setProperty(Constants.ClientProperties.FAULT_TO, Constants.WSA.NS_ADDRESSING_ANONYMOUS);
            call.setProperty(Constants.ClientProperties.REPLY_TO, Constants.WSA.NS_ADDRESSING_ANONYMOUS);

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName("Ping");
            call.setTransport(new RMTransport(targetURL, ""));

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
            call.invoke(new Message(getSOAPEnvelope(1)));

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
            call.invoke(new Message(getSOAPEnvelope(2)));

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
            call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
            call.invoke(new Message(getSOAPEnvelope(3)));


            RMInitiator.stopClient();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSOAPEnvelope(int i) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "   <soapenv:Header>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <Ping xmlns=\"http://tempuri.org/\">\n" +
                "         <Text>Ping Message Number " + i + "</Text>\n" +
                "      </Ping>\n" +
                "   </soapenv:Body></soapenv:Envelope>";
    }
}
