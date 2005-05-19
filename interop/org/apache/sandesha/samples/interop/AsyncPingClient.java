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
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMReport;
import org.apache.sandesha.RMStatus;
import org.apache.sandesha.RMTransport;

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

            RMInitiator.initClient();

            Service service = new Service();
            Call call = (Call) service.createCall();

            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:ping");

            //We need this since we need to see the results in the Monitor.
            //For the actual usagd AcksTo will be set by the RM endpoint.
            call.setProperty(Constants.ClientProperties.ACKS_TO,
                    "http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
            

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org", "Ping"));
            call.setTransport(new RMTransport(targetURL, ""));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

            //First Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
            call.invoke(new Object[]{"Ping Message Number One"});

            //Second Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
            call.invoke(new Object[]{"Ping Message Number Two"});

            //Third Message
            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
            //For last message.
            call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
            call.invoke(new Object[]{"Ping Message Number Three"});

            RMStatus status = RMInitiator.stopClient();

            if(status!=null){
            	RMReport report = status.getReport();
            	
            	if(report!=null){
            		System.out.println("\n***********Printing RM Report***********");
            		System.out.println("Were all messages add     - " + report.isAllAcked());
            		System.out.println("No of response messages   - " + report.getNumberOfReturnMessages());
            		System.out.println("****************************************\n");
            	}
            }

        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
