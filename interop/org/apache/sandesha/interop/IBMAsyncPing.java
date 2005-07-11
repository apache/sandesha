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
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * Test client for Ping scenario for IBM.
 *
 * @author Jaliya Ekanyake
 */
public class IBMAsyncPing {

    private static String targetURL = "http://127.0.0.1:8080/wsrm/services/rmDemos";
    private static String sourceHost = "192.248.18.51"; //Change this to your public IP address
    private static String sourcePort = "9070"; //Change this according to the listening port of the TCPMonitor in the
    //client side.

    public static void main(String[] args) {
        System.out.println("Client started...... Asynchronous Ping - IBM");
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext();

            ctx.setToURL("http://wsi.alphaworks.ibm.com:8080/wsrm/services/rmDemos");
            ctx.setFaultToURL("http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setAcksToURL("http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");
            ctx.setFromURL("http://" + sourceHost + ":" + sourcePort + "/axis/services/RMService");

            ctx.initCall(call, targetURL, "urn:wsrm:Ping", Constants.ClientProperties.IN_ONLY);

            call.setOperationName(new QName("http://tempuri.org/", "Ping"));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

            call.invoke(new Object[]{"Ping Message Number One"});
            call.invoke(new Object[]{"Ping Message Number Two"});
            ctx.setLastMessage(call);
            call.invoke(new Object[]{"Ping Message Number Three"});

            ctx.endSequence();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
