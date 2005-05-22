package org.apache.sandesha.samples.interop;

import org.apache.axis.Message;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;

public class MicrosoftAsyncPing {

    private static String sourceHost = "192.248.18.51"; //Change this to your public IP address
    private static String sourcePort = "9070"; //Change this according to the listening port of the TCPMonitor in the
    private static String targetURL = "http://127.0.0.1:8080/SecureReliableMessaging/ReliableOneWayDual.svc";

    public static void main(String[] args) {
        System.out.println("Client started...... Asynchronous - Microsoft");
        try {


            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:Ping",
                    Constants.ClientProperties.IN_ONLY);

            ctx.setToUrl(call,
                    "http://131.107.153.195/SecureReliableMessaging/ReliableOneWayDual.svc");
            ctx.setFromUrl(call,
                    "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setAcksToUrl(call,
                    "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setFaultToUrl(call,
                    "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setReplyToUrl(call,
                    "http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");

            call.setOperationName("Ping");
            call.invoke(new Message(getSOAPEnvelope(1)));
            call.invoke(new Message(getSOAPEnvelope(2)));
            ctx.setLastMessage(call);
            call.invoke(new Message(getSOAPEnvelope(3)));

            ctx.endSequence(call);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSOAPEnvelope(int i) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "   <soapenv:Header>\n" + "   </soapenv:Header>\n" + "   <soapenv:Body>\n" + "      <Ping xmlns=\"http://tempuri.org/\">\n" +
                "         <Text>Ping Message Number " + i + "</Text>\n" + "      </Ping>\n" + "   </soapenv:Body></soapenv:Envelope>";
    }
}
