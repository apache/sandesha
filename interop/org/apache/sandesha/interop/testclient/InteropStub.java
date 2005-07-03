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
package org.apache.sandesha.interop.testclient;


import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.Message;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.sandesha.util.PropertyLoader;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

/**
 * This is class has the client for the interop testing. interop.jsp
 * simply calls the runPing and runEcho methods.
 *
 * @auther Chamikara Jayalath
 */


public class InteropStub {

    private InteropStub() {
    }

    private static Sender sender = null;
    private static Thread thSender = null;
    private static ClientStorageManager storageManager = new ClientStorageManager();
    private static final Log log = LogFactory.getLog(InteropStub.class.getName());

    private static InteropStub stub = null;

    public static InteropStub getInstance() {

        if (stub != null) {
            return stub;
        } else {
            stub = new InteropStub();
            return stub;
        }
    }

    public static InteropCallback getCallback() {
        return callback;
    }

    public static void setCallback(InteropCallback callback) {
        InteropStub.callback = callback;
    }

    private static InteropCallback callback = null;

    private void configureContext(SandeshaContext ctx, Call call, InteropBean bean) {
        String from = bean.getFrom();
        String replyTo = bean.getReplyto();
        String acksTo = bean.getAcksTo();
        String faultTo = bean.getFaultto();

        boolean sendOffer = false;
        if (bean.getOffer().equalsIgnoreCase("yes"))
            sendOffer = true;

        if (replyTo != null && replyTo.equalsIgnoreCase("anonymous")) {
            ctx.setReplyToURL(Constants.WSA.NS_ADDRESSING_ANONYMOUS);
        } else if (replyTo != null) {
            ctx.setReplyToURL(bean.getReplyto());
        }

        if (from != null && from.equalsIgnoreCase("anonymous")) {
            ctx.setFromURL(Constants.WSA.NS_ADDRESSING_ANONYMOUS);
        } else if (from != null) {
            ctx.setFromURL(from);
        }

        if (acksTo != null && acksTo.equalsIgnoreCase("anonymous")) {
            ctx.setAcksToURL(Constants.WSA.NS_ADDRESSING_ANONYMOUS);
        } else if (acksTo != null) {
            ctx.setAcksToURL(acksTo);
        }

        if (faultTo != null && faultTo.equalsIgnoreCase("anonymous")) {
            ctx.setFaultToURL(Constants.WSA.NS_ADDRESSING_ANONYMOUS);
        } else if (faultTo != null) {
            ctx.setFaultToURL(bean.getFaultto());
        }


        if (sendOffer)
            ctx.setSendOffer(true);

    }

    public synchronized void runPing(InteropBean bean) {

        log.info("=========== RUNNING THE \"Ping\" INTEROP TEST ==========");
        String target = bean.getTarget();
        int msgs = bean.getNoOfMsgs();
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext(true);
            ctx.setSourceURL(bean.getSourceURL());
            configureContext(ctx, call, bean);
            ctx.initCall(call, target, "urn:wsrm:ping", Constants.ClientProperties.IN_ONLY);

            for (int i = 1; i <= msgs; i++) {
                if (i == msgs) {
                    ctx.setLastMessage(call);
                }
                call.invoke(new Message(getPingSOAPEnvelope(i)));
            }

            //InteropStub.stopClient();
            ctx.endSequence();

        } catch (Exception e) {
            if (callback != null)
                callback.onError(e);
            log.info(e);
        }
    }


    private static String getPingSOAPEnvelope(int i) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "   <soapenv:Header>\n" + "   </soapenv:Header>\n" + "   <soapenv:Body>\n" + "      <Ping xmlns=\"http://tempuri.org/\">\n" +
                "         <Text>Sandesha Ping Message " + i + "</Text>\n" + "      </Ping>\n" + "   </soapenv:Body></soapenv:Envelope>";

    }

    private static String getEchoSOAPEnvelope(int i,String seq) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "<soapenv:Header>\n" +
                "</soapenv:Header>\n" +
                "<soapenv:Body>\n" +
                " <echoString xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns=\"http://tempuri.org/\">\n" +
                "   <Text>Sandesha Echo Message "+i+"</Text>\n" +
                "   <Sequence>"+seq+"</Sequence>\n" +
                "  </echoString>\n"+
                "</soapenv:Body></soapenv:Envelope>";

    }

    public synchronized void runEcho(InteropBean bean) {

        String target = bean.getTarget();
        int messages = bean.getNoOfMsgs();
        String seq = new Long(System.currentTimeMillis()).toString();

        try {
            log.info("=========== RUNNING THE \"echoString\" INTEROP TEST ==========");

            //We start the listener to be in the safe side.
            //User may specify some external(not in sandesha endpoint) replyTo address, then
            //he/she will not be able to retrieve the responses to this client, yet they can verify
            //the reliablility of the sent messages.


            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext(true);
            ctx.setSourceURL(bean.getSourceURL());

            configureContext(ctx, call, bean);
            ctx.initCall(call, target, "urn:wsrm:echoString", Constants.ClientProperties.IN_OUT);

            for (int i = 1; i <= messages; i++) {
                 if (i == messages) {
                    ctx.setLastMessage(call);
                }

                SOAPEnvelope env = call.invoke(new Message(getEchoSOAPEnvelope(i,seq)));
                if(log.isDebugEnabled()){
                log.debug("Got response from server " + env.toString());
                }
            }

            ctx.endSequence();

        } catch (Exception e) {
            if (callback != null)
                callback.onError(e);
            log.error(e);
        }
    }


    public static void initClient() throws AxisFault {
        log.info("STARTING SENDER FOR THE CLIENT .......");
        sender = new Sender(storageManager);
        SimpleChain reqChain = null;
        SimpleChain resChain = null;
        try {
            reqChain = getRequestChain();
            resChain = getResponseChain();
        } catch (Exception e) {
            throw new AxisFault(e.getMessage());
        }
        if (reqChain != null)
            sender.setRequestChain(reqChain);
        if (resChain != null)
            sender.setResponseChain(resChain);

        //thSender = new Thread(sender);
        //thSender.setDaemon(false);
        //thSender.start();
        sender.startSender();
    }

    public static void stopClient() throws AxisFault {
        //This should check whether we have received all the acks or reponses if any
        storageManager.isAllSequenceComplete();
        long startingTime = System.currentTimeMillis();
        long inactivityTimeOut = PolicyLoader.getInstance().getInactivityTimeout();
        while (!storageManager.isAllSequenceComplete()) {
            try {
                log.info(Constants.InfomationMessage.WAITING_TO_STOP_CLIENT);
                Thread.sleep(Constants.CLIENT_WAIT_PERIOD_FOR_COMPLETE);
                if ((System.currentTimeMillis() - startingTime) >= inactivityTimeOut) {
                    stopClientByForce();
                }
            } catch (InterruptedException e) {
                log.error(e);
            }
        }

        sender.stop();
        storageManager.clearStorage();


    }

    public static void stopClientByForce() throws AxisFault {

        sender.stop();

        throw new AxisFault("Inactivity Timeout Reached, No Response from the Server");
    }

    private static SimpleChain getRequestChain() throws Exception {
        ArrayList arr = PropertyLoader.getRequestHandlerNames();
        return getHandlerChain(arr);
    }


    private static SimpleChain getResponseChain() throws Exception {

        ArrayList arr = PropertyLoader.getResponseHandlerNames();
        return getHandlerChain(arr);
    }

    public static SimpleChain getHandlerChain(List arr) {
        SimpleChain reqHandlers = new SimpleChain();
        Iterator it = arr.iterator();
        boolean hasReqHandlers = false;
        try {
            while (it.hasNext()) {
                hasReqHandlers = true;
                String strClass = (String) it.next();
                Class c = Class.forName(strClass);
                Handler h = (Handler) c.newInstance();
                reqHandlers.addHandler(h);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (hasReqHandlers)
            return reqHandlers;
        else
            return null;
    }

}
