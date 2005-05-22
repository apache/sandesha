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
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.sandesha.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class EchoClientSyncAck {

    private static String defaultServerPort = "8070";
    private static String defaultClientPort = "9070";
    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";

    public static void main(String[] args) {

        System.out.println("EchoClientSyncAck Started ........");

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

           call.setOperationName(new QName("http://tempuri.org/", "echoString"));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

                            String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", str});
            System.out.println("The Response for First Messsage is  :" + ret);

            ret = (String) call.invoke(new Object[]{"Sandesha Echo 2", str});
            System.out.println("The Response for Second Messsage is  :" + ret);

            //For last message.
         ctx.setLastMessage(call);
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 3", str});
            System.out.println("The Response for Third Messsage is  :" + ret);

            RMReport report = ctx.endSequence(call);


                       if (report != null) {
                           System.out.println("\n***********Printing RM Report***********");
                           System.out.println("Were all messages acked     - " + report.isAllAcked());
                           System.out.println(
                                   "No of response messages   - " + report.getNumberOfReturnMessages());
                           System.out.println("****************************************\n");
                       }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
