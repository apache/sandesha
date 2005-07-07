/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.sandesha.intergratedtest;

import junit.framework.TestCase;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMReport;
import org.apache.sandesha.SandeshaContext;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ParameterMode;
import java.io.File;
import java.net.ServerSocket;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: May 20, 2005
 * Time: 4:47:43 PM
 */
public class TCScenarioTest extends TestCase {
    private static SimpleAxisServer sas = null;

    private static String defaultServerPort = "5555";
    private static String defaultClientPort = "9090";
    private static boolean serverStarted = false;
    private static int testCount = 5;

    private static String targetURL = "http://127.0.0.1:" +defaultServerPort +
            "/axis/services/RMTestService";


    public void setUp() throws Exception {
        if (!serverStarted) {
            sas = new SimpleAxisServer();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File("test-resources/server-config.wsdd"));
            WSDDDocument wsdddoc = new WSDDDocument(doc);
            WSDDDeployment wsdddep = wsdddoc.getDeployment();
            sas.setMyConfig(wsdddep);

            sas.setServerSocket(new ServerSocket((new Integer(defaultServerPort)).intValue()));
            sas.start();
            serverStarted = true;
        }
    }

    public void tearDown() throws InterruptedException {
        if (testCount == 0) {
            Thread.sleep(5000);
            sas.stop();
        }
    }

    /**
     * This test will test the Ping interop scenario. 3 One-way messages are sent with
     * <wsrm:AckTo> set to ANONYMOUS URI and acknowledgements are received.
     *
     * @throws Exception
     */
    public void testPingSync() throws Exception {
        System.out.println("          Synchronous Ping Test Started");

        Service service = new Service();
        Call call = (Call) service.createCall();

        SandeshaContext ctx = new SandeshaContext(Constants.SYNCHRONIZED);
        ctx.initCall(call, targetURL, "urn:wsrm:Ping", Constants.ClientProperties.IN_ONLY);

        call.setOperationName(new QName("http://tempuri.org/", "Ping"));
        call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);


        call.invoke(new Object[]{"Ping One"});
        ctx.setLastMessage(call);
        call.invoke(new Object[]{"Ping Two"});

        RMReport report = ctx.endSequence();

        assertEquals(report.isAllAcked(), true);
        assertEquals(report.getNumberOfReturnMessages(), 0);
        testCount--;
        System.out.println("          Synchronous Ping Test Finished");
    }

    /**
     * This test will test the Ping interop scenario. 3 One-way messages are sent with
     * <wsrm:AckTo> set to asynchronous client URI and acknowledgements are received.
     *
     * @throws Exception
     */
    public void testPingAsync() throws Exception {
        System.out.println("          Asynchronous Ping Test Started");

        Service service = new Service();
        Call call = (Call) service.createCall();

        SandeshaContext ctx = new SandeshaContext();

        ctx.setAcksToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.setReplyToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.initCall(call, targetURL, "urn:wsrm:ping", Constants.ClientProperties.IN_ONLY);

        call.setOperationName(new QName("http://tempuri.org", "Ping"));
        call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

        call.invoke(new Object[]{"Ping One"});
        ctx.setLastMessage(call);
        call.invoke(new Object[]{"Ping Two"});

        RMReport report = ctx.endSequence();

        assertEquals(report.isAllAcked(), true);
        assertEquals(report.getNumberOfReturnMessages(), 0);
        testCount--;
        System.out.println("          Asynchronous Ping Test Finished");

    }

    /**
     * This test will test the echoString interop scenario. 3 echo messages are sent with
     * <wsrm:AckTo> set to ANONYMOUS URI. Acknowledgements relating to the scenario is received
     * using the same HTTP connection used in the request message while the responses are
     * received using the asynchronous client side endpoint.
     *
     * @throws Exception
     */

    public void testEchoSyncAck() throws Exception {
        System.out.println("          Echo(Sync Ack) Test Started");

        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
        String str = uuidGen.nextUUID();

        Service service = new Service();
        Call call = (Call) service.createCall();

        SandeshaContext ctx = new SandeshaContext();

        ctx.setAcksToURL(Constants.WSA.NS_ADDRESSING_ANONYMOUS);
        ctx.setReplyToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.setSendOffer(true);

        ctx.initCall(call, targetURL, "urn:wsrm:echoString", Constants.ClientProperties.IN_OUT);

        call.setOperationName(new QName("http://tempuri.org/", "echoString"));

        call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
        call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

        String ret = (String) call.invoke(new Object[]{" Echo 1 ", str});
        System.out.println("          The Response for First Messsage is  :" + ret);

        ctx.setLastMessage(call);
        ret = (String) call.invoke(new Object[]{" Echo 2 ", str});
        System.out.println("          The Response for Second Messsage is  :" + ret);

        RMReport report = ctx.endSequence();

        assertEquals(report.isAllAcked(), true);
        assertEquals(report.getNumberOfReturnMessages(), 2);
        testCount--;
        System.out.println("          Echo(Sync Ack) Test Finished");
    }

    /**
     * This test will test the echoString interop scenario. 3 echo messages are sent with
     * <wsrm:AckTo> set to asynchronous client side endpoint. Acknowledgements and responses
     * are both received using the asynchronous client side endpoint.
     *
     * @throws Exception
     */
    public void testEchoAsyncAck() throws Exception {
        System.out.println("          Echo(Aync Ack) Test Started");

        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
        String str = uuidGen.nextUUID();

        Service service = new Service();
        Call call = (Call) service.createCall();

        SandeshaContext ctx = new SandeshaContext();

        ctx.setAcksToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.setReplyToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.setSendOffer(true);

        ctx.initCall(call, targetURL, "urn:wsrm:echoString", Constants.ClientProperties.IN_OUT);

        call.setOperationName(new QName("http://tempuri.org/", "echoString"));

        call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
        call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

        String ret = (String) call.invoke(new Object[]{" Echo 1 ", str});
        System.out.println("          The Response for First Messsage is  :" + ret);

        ctx.setLastMessage(call);
        ret = (String) call.invoke(new Object[]{" Echo 2 ", str});
        System.out.println("          The Response for Second Messsage is  :" + ret);

        RMReport report = ctx.endSequence();

        assertEquals(report.isAllAcked(), true);
        assertEquals(report.getNumberOfReturnMessages(), 2);
        testCount--;
        System.out.println("          Echo(Async Ack) Test Finished");
    }

    /**
     * This test will test the echoString interop scenario and Ping scenario together.
     * Response of each echoString request is used to invoke a Ping service.  This test tests the
     * capability of Sandesha Client side endpoint to handle multiple web service requests
     * at the same time.
     *
     * @throws Exception
     */
    public void testEchoPing() throws Exception {
        System.out.println("          Echo and Ping Combined Test Started");
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
        String str = uuidGen.nextUUID();

        Service service = new Service();
        Call echoCall = (Call) service.createCall();

        SandeshaContext ctx = new SandeshaContext();
        //------------------------ECHO--------------------------------------------

        ctx.setAcksToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.setReplyToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        ctx.setSendOffer(true);
        ctx.initCall(echoCall, targetURL, "urn:wsrm:echoString", Constants.ClientProperties.IN_OUT);

        echoCall.setOperationName(new QName("http://tempuri.org/", "echoString"));

        echoCall.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
        echoCall.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
        echoCall.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);
        //----------------------ECHO------------------------------------------------

        //------------------------PING--------------------------------------------
        Service pingService = new Service();
        Call pingCall = (Call) pingService.createCall();
        SandeshaContext pingCtx = new SandeshaContext();
        pingCtx.setAcksToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");
        pingCtx.setReplyToURL("http://127.0.0.1:" + defaultClientPort + "/axis/services/RMService");

        pingCtx.initCall(pingCall, targetURL, "urn:wsrm:Ping", Constants.ClientProperties.IN_ONLY);

        pingCall.setOperationName(new QName("http://tempuri.org/", "ping"));
        pingCall.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
        //----------------------PING------------------------------------------------


        String ret = (String) echoCall.invoke(new Object[]{" Echo 1 ", str});
        System.out.println("          The Response for First Messsage is  :" + ret);
        pingCall.invoke(new Object[]{ret});

        ctx.setLastMessage(echoCall);
        ret = (String) echoCall.invoke(new Object[]{" Echo 2 ", str});
        System.out.println("          The Response for Second Messsage is  :" + ret);
        pingCall.invoke(new Object[]{ret});

        pingCtx.setLastMessage(pingCall);
        pingCall.invoke(new Object[]{ret});

        RMReport echoReport = ctx.endSequence();
         RMReport pingReport = pingCtx.endSequence();

        assertEquals(echoReport.isAllAcked(), true);
        assertEquals(echoReport.getNumberOfReturnMessages(), 2);

        assertEquals(pingReport.isAllAcked(), true);
        assertEquals(pingReport.getNumberOfReturnMessages(), 0);
        testCount--;
        System.out.println("          Echo and Ping Combined Test Finished");

    }

}
