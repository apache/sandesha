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
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMReport;
import org.apache.sandesha.RMStatus;
import org.apache.sandesha.RMTransport;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class EchoClientAsyncAck {

    private static String defaultServerPort = "8070";
    private static String defaultClientPort = "9070";
    private static String targetURL = "http://127.0.0.1:" + defaultServerPort + "/axis/services/RMInteropService?wsdl";

    public static void main(String[] args) {

        System.out.println("EchoClientAsyncAck Started ........");

        try {
            //A separate listner will be started if the value of the input parameter for the mehthod
            // initClient is "false". If the service is of type request/response the parameter value shoule be "false"
            RMInitiator.initClient();

            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
            String str = uuidGen.nextUUID();


            Service service = new Service();
            Call call = (Call) service.createCall();

            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:echoString");

            //These two are additional
            call.setProperty(Constants.ClientProperties.ACKS_TO,"http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.REPLY_TO,"http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
            //<wsrm:Offer> is also an configurable option.
            call.setProperty(Constants.ClientProperties.SEND_OFFER,new Boolean(true));

            call.setTargetEndpointAddress(targetURL);
            call.setOperationName(new QName("http://tempuri.org/", "echoString"));
            call.setTransport(new RMTransport(targetURL, ""));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
            String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", str});
            System.out.println("The Response for First Messsage is  :" + ret);

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 2", str});
            System.out.println("The Response for Second Messsage is  :" + ret);

            call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
            call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true)); //For last message.
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 3", str});
            System.out.println("The Response for Third Messsage is  :" + ret);

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
            e.printStackTrace();
        }
    }
}
