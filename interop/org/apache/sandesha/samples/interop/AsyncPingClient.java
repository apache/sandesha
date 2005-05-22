/*
* Copyright 1999-2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/

package org.apache.sandesha.samples.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMReport;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class AsyncPingClient {
    private static String defaultServerPort = "8070";
    private static String defaultClientPort = "9070";
    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";

    public static void main(String[] args) {
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


            if (report != null) {
                System.out.println("\n***********Printing RM Report***********");
                System.out.println("Were all messages acked     - " + report.isAllAcked());
                System.out.println(
                        "No of response messages   - " + report.getNumberOfReturnMessages());
                System.out.println("****************************************\n");
            }


        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
