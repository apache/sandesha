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
import org.apache.axis.Message;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.SandeshaContext;
import org.apache.sandesha.client.ClientHandlerUtil;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.util.PropertyLoader;

import java.util.ArrayList;

/**
 * This is class has the client for the interop testing. interop.jsp
 * simply calls the runPing and runEcho methods.
 *
 * @author Chamikara Jayalath
 */


public class InteropStub {

    private InteropStub() {
    }

    private static Sender sender = null;
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

    private void configureContext(SandeshaContext ctx, InteropBean bean) {
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
        if (log.isDebugEnabled()) {
            log.debug("=========== RUNNING THE \"Ping\" INTEROP TEST ==========");
        }
        String target = bean.getTarget();
        int msgs = bean.getNoOfMsgs();
        try {

            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext(true);
            ctx.setSourceURL(bean.getSourceURL());
            configureContext(ctx, bean);
            ctx.initCall(call, target, "urn:wsrm:Ping", Constants.ClientProperties.IN_ONLY);

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
            log.error(e);
        }
    }


    private static String getPingSOAPEnvelope(int i) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "   <soapenv:Header>\n" + "   </soapenv:Header>\n" + "   <soapenv:Body>\n" + "      <Ping xmlns=\"http://tempuri.org/\">\n" +
                "         <Text>Sandesha Ping Message " + i + "</Text>\n" + "      </Ping>\n" + "   </soapenv:Body></soapenv:Envelope>";

    }

    private static String getEchoSOAPEnvelope(int i, String seq) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "<soapenv:Header>\n" +
                "</soapenv:Header>\n" +
                "<soapenv:Body>\n" +
                " <echoString xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns=\"http://tempuri.org/\">\n" +
                "   <Text>Sandesha Echo Message " + i + "</Text>\n" +
                "   <Sequence>" + seq + "</Sequence>\n" +
                "  </echoString>\n" +
                "</soapenv:Body></soapenv:Envelope>";

    }

    public synchronized void runEcho(InteropBean bean) {

        String target = bean.getTarget();
        int messages = bean.getNoOfMsgs();
        String seq = new Long(System.currentTimeMillis()).toString();

        try {
            if (log.isDebugEnabled()) {
                log.debug("=========== RUNNING THE \"echoString\" INTEROP TEST ==========");
            }

            //We start the listener to be in the safe side.
            //User may specify some external(not in sandesha endpoint) replyTo address, then
            //he/she will not be able to retrieve the responses to this client, yet they can verify
            //the reliablility of the sent messages.


            Service service = new Service();
            Call call = (Call) service.createCall();

            SandeshaContext ctx = new SandeshaContext(true);
            ctx.setSourceURL(bean.getSourceURL());

            configureContext(ctx, bean);
            ctx.initCall(call, target, "urn:wsrm:echoString", Constants.ClientProperties.IN_OUT);

            for (int i = 1; i <= messages; i++) {
                if (i == messages) {
                    ctx.setLastMessage(call);
                }

                SOAPEnvelope env = call.invoke(new Message(getEchoSOAPEnvelope(i, seq)));
                if (log.isDebugEnabled()) {
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
        if (log.isDebugEnabled()) {
            log.debug("STARTING SENDER FOR THE CLIENT .......");
        }
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
        sender.startSender();
    }


    public static void stopClientByForce() throws AxisFault {
        sender.stop();
        throw new AxisFault("Inactivity Timeout Reached, No Response from the Server");
    }

    private static SimpleChain getRequestChain() {
        ArrayList arr = PropertyLoader.getRequestHandlerNames();
        return ClientHandlerUtil.getHandlerChain(arr);
    }


    private static SimpleChain getResponseChain() {

        ArrayList arr = PropertyLoader.getResponseHandlerNames();
        return ClientHandlerUtil.getHandlerChain(arr);
    }

}
