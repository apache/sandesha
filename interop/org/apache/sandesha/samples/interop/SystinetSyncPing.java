package org.apache.sandesha.samples.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;


public class SystinetSyncPing {

    private static String targetURL = "http://127.0.0.1:6064/Service";

    public static void main(String[] args) {
        System.out.println("Client started...... Synchronous ");
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:Ping",
                    Constants.ClientProperties.IN_ONLY);
            ctx.setSynchronous(call);
            ctx.setToUrl(call, "http://soap.systinet.net:6064/Service");
            ctx.setAcksToUrl(call, Constants.WSA.NS_ADDRESSING_ANONYMOUS);

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org/", "Ping"));


            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);

            call.invoke(new Object[]{"Ping Message Number One"});
            call.invoke(new Object[]{"Ping Message Number Two"});
            ctx.setLastMessage(call);
            call.invoke(new Object[]{"Ping Message Number Three"});

            ctx.endSequence(call);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}