package org.apache.sandesha.samples.interop;

import org.apache.axis.Message;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;

public class MicrosoftSyncPing {

    private static String targetURL = "http://127.0.0.1:8080/SecureReliableMessaging/ReliableOneWay.svc";

    public static void main(String[] args) {
        System.out.println("Client started...... Synchronous - Microsoft ");
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:Ping",
                    Constants.ClientProperties.IN_ONLY);
            ctx.setSynchronous(call);
            ctx.setToUrl(call, "http://131.107.153.195/SecureReliableMessaging/ReliableOneWay.svc");
             //We really do not want to send wsa:ReplyTo header for a synchronous operation.
            //But Microsoft endpoint expects it for all the messages. So let's set that manually.
            ctx.setReplyToUrl(call, Constants.WSA.NS_ADDRESSING_ANONYMOUS);

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
