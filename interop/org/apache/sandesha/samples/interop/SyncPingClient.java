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
import org.apache.sandesha.client.RMSender;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class SyncPingClient {

    private static String defaultServerPort = "8070";

    private static String targetURL = "http://127.0.0.1:" + defaultServerPort +
            "/axis/services/RMInteropService?wsdl";

    public static void main(String[] args) {
        System.out.println("Client started...... Synchronous ");
        try {

            RMInitiator.initClient(true);

            Service service = new Service();
            Call call = (Call) service.createCall();

            //By setting the SYNC property as true and false, the user can select between
            //the synchronous version and asynchronous version of invocations.
            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(true));

            //Sandesha uses action to differentitiate services initially (when there are no
            //sequence identifiers are available. This is a REQUIRED option for Sandesha.
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:Ping");

            //This line is used to set the transport for Sandesha
            call.setTransport(new RMTransport(targetURL, ""));

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org/", "Ping"));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);

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
