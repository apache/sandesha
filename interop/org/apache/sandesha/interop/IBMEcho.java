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

package org.apache.sandesha.interop;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMTransport;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * Test client for echoString scenario for IBM.
 *
 * @auther Jaliya Ekanyake
 */
public class IBMEcho {
    private static String sourceHost = "192.248.18.51"; //Change this to your public IP address
    private static String targetURL = "http://127.0.0.1:8080/wsrm/services/rmDemos";
    private static String sourcePort = "9070"; //Change this according to the listening port of the TCPMonitor in the
    //client side.

    public static void main(String[] args) {

        System.out.println("Client started...... Asynchronous EchoString - IBM");

          UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
            String str = uuidGen.nextUUID();

        try {
              Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();
            ctx.addNewSequeceContext(call, targetURL, "urn:wsrm:echoString",Constants.ClientProperties.IN_OUT);

            ctx.setToUrl(call, "http://wsi.alphaworks.ibm.com:8080/wsrm/services/rmDemos");
            ctx.setFaultToUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setAcksToUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setFromUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setReplyToUrl(call,"http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");

            call.setOperationName(new QName("http://tempuri.org/", "echoString"));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("Sequence", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", str});
            System.out.println("The Response for First Messsage is  :" + ret);

            ret = (String) call.invoke(new Object[]{"Sandesha Echo 2", str});
            System.out.println("The Response for Second Messsage is  :" + ret);

            ctx.setLastMessage(call);
            ret = (String) call.invoke(new Object[]{"Sandesha Echo 3", str});
            System.out.println("The Response for Third Messsage is  :" + ret);

             ctx.endSequence(call);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
