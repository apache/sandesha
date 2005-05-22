package org.apache.sandesha.samples.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class IBMSyncPing {

    private static String targetURL = "http://127.0.0.1:8080/wsrm/services/rmDemos";

    public static void main(String[] args) {
        System.out.println("Client started...... Synchronous Ping - IBM");
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:Ping",
                    Constants.ClientProperties.IN_ONLY);
            ctx.setSynchronous(call);
            ctx.setToUrl(call, "http://wsi.alphaworks.ibm.com:8080/wsrm/services/rmDemos");
            ctx.setAcksToUrl(call, Constants.WSA.NS_ADDRESSING_ANONYMOUS);

            call.setOperationName(new QName("http://tempuri.org/", "Ping"));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

            call.invoke(new Object[]{"Ping Message Number One"});
            call.invoke(new Object[]{"Ping Message Number Two"});
            ctx.setLastMessage(call);
            call.invoke(new Object[]{"Ping Message Number Three"});

            ctx.endSequence(call);

        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
