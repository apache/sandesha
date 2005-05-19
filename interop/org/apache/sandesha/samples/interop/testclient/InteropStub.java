/*
 * Created on Apr 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.samples.interop.testclient;


import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.message.addressing.util.AddressingUtils;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMTransport;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.sandesha.util.PropertyLoader;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InteropStub {

    private InteropStub() {
    }

    private static Sender sender = null;
    private static Thread thSender = null;
    private static ClientStorageManager storageManager = new ClientStorageManager();

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


    private Call getCall(InteropBean bean) throws ServiceException {
        String target = bean.getTarget();
        String from = bean.getFrom();
        String replyTo = bean.getReplyto();
        String acksTo = bean.getAcksTo();
        String faultTo = bean.getFaultto();

        boolean sendOffer = false;
        if (bean.getOffer().equalsIgnoreCase("yes"))
            sendOffer = true;

        Service service = new Service();
        Call call = (Call) service.createCall();

        if (replyTo != null && replyTo.equalsIgnoreCase("anonymous")) {
            call.setProperty(Constants.ClientProperties.REPLY_TO,
                    AddressingUtils.getAnonymousRoleURI());
        } else if (replyTo != null) {
            call.setProperty(Constants.ClientProperties.REPLY_TO, bean.getReplyto());
        }

        if (from != null && from.equalsIgnoreCase("anonymous")) {
            from = AddressingUtils.getAnonymousRoleURI();
            call.setProperty(Constants.ClientProperties.FROM,
                    AddressingUtils.getAnonymousRoleURI());
        } else if (from != null) {
            call.setProperty(Constants.ClientProperties.FROM, from);
        }

        if (acksTo != null && acksTo.equalsIgnoreCase("anonymous")) {
            acksTo = AddressingUtils.getAnonymousRoleURI();
            call.setProperty(Constants.ClientProperties.ACKS_TO,
                    AddressingUtils.getAnonymousRoleURI());
        } else if (acksTo != null) {
            call.setProperty(Constants.ClientProperties.ACKS_TO, acksTo);
        }

        if (faultTo != null && faultTo.equalsIgnoreCase("anonymous")) {
            faultTo = AddressingUtils.getAnonymousRoleURI();
            call.setProperty(Constants.ClientProperties.FAULT_TO,
                    AddressingUtils.getAnonymousRoleURI());
        } else if (faultTo != null) {
            call.setProperty(Constants.ClientProperties.FAULT_TO, bean.getFaultto());
        }


        if (sendOffer)
            call.setProperty(Constants.ClientProperties.SEND_OFFER, new Boolean(true));

        call.setTargetEndpointAddress(target);
        call.setProperty(Constants.ClientProperties.SOURCE_URL, bean.getSourceURL());

        return call;
    }

    public synchronized void runPing(InteropBean bean) {

        System.out.println("=========== RUNNING THE \"Ping\" INTEROP TEST ==========");
        String target = bean.getTarget();
        int msgs = bean.getNoOfMsgs();
        try {
            InteropStub.initClient();
            Call call = getCall(bean);

            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:Ping");

            call.setOperationName(new QName("http://tempuri.org", "Ping"));
            call.setTransport(new RMTransport(target, ""));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

            for (int i = 1; i <= msgs; i++) {
                call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
                if (i == msgs) {
                    call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
                }
                String msg = "Sandesha Ping Message Number " + i;
                call.invoke(new Object[]{msg});
            }

            InteropStub.stopClient();

        } catch (Exception e) {
            if (callback != null)
                callback.onError(e);
            e.printStackTrace();
        }
    }

    public synchronized void runEcho(InteropBean bean) {

        String target = bean.getTarget();
        int messages = bean.getNoOfMsgs();
        String seq = new Long(System.currentTimeMillis()).toString();

        try {
            System.out.println("=========== RUNNING THE \"echoString\" INTEROP TEST ==========");

            //We start the listener to be in the safe side.
            //User may specify some external(not in sandesha endpoint) replyTo address, then
            //he/she will not be able to retrieve the responses to this client, yet they can verify
            //the reliablility of the sent messages.
            InteropStub.initClient();

            Call call = getCall(bean);
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:echoString");

            call.setTargetEndpointAddress(target);
            call.setOperationName(new QName("http://tempuri.org/", "echoString"));
            call.setTransport(new RMTransport(target, ""));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("Sequence", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            for (int i = 1; i <= messages; i++) {
                call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
                String msg = "Sandesha Echo String " + i;

                if (i == messages) {
                    call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
                }

                String ret = (String) call.invoke(new Object[]{msg, seq});
                System.out.println("Got response from server " + ret);
            }

            InteropStub.stopClient();

        } catch (Exception e) {
            if (callback != null)
                callback.onError(e);
            e.printStackTrace();
        }
    }


    public static void initClient() {
        System.out.println("STARTING SENDER FOR THE CLIENT .......");
        sender = new Sender(storageManager);

        SimpleChain reqChain = getRequestChain();
        SimpleChain resChain = getResponseChain();
        if (reqChain != null)
            sender.setRequestChain(reqChain);
        if (resChain != null)
            sender.setResponseChain(resChain);

        thSender = new Thread(sender);
        thSender.setDaemon(false);
        thSender.start();
    }

    public static void stopClient() throws AxisFault {
        //This should check whether we have received all the acks or reponses if any
        storageManager.isAllSequenceComplete();
        long startingTime = System.currentTimeMillis();
        long inactivityTimeOut = PolicyLoader.getInstance().getInactivityTimeout();
        while (!storageManager.isAllSequenceComplete()) {
            try {
                System.out.println(Constants.InfomationMessage.WAITING_TO_STOP_CLIENT);
                Thread.sleep(Constants.CLIENT_WAIT_PERIOD_FOR_COMPLETE);
                if ((System.currentTimeMillis() - startingTime) >= inactivityTimeOut) {
                    stopClientByForce();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.setRunning(false);
        storageManager.clearStorage();


    }

    public static void stopClientByForce() throws AxisFault {

        sender.setRunning(false);
        throw new AxisFault("Inactivity Timeout Reached, No Response from the Server");
    }

    private static SimpleChain getRequestChain() {
        ArrayList arr = PropertyLoader.getRequestHandlerNames();
        return getHandlerChain(arr);
    }


    private static SimpleChain getResponseChain() {

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
