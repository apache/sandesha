/*
 * Created on Apr 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.samples.interop.testclient;


import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.message.addressing.util.AddressingUtils;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMTransport;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class InteropStub {
    public void runPing(InteropBean bean) {

        String target = bean.getTarget();
        String from = bean.getFrom();
        String replyTo = bean.getReplyto();
        String acksTo = bean.getAcksTo();
        String acks = bean.getAcks();
        String terminate = bean.getTerminate();
        String operation = bean.getOperation();
        int messages = bean.getNoOfMsgs();

        if (replyTo != null && replyTo.equalsIgnoreCase("anonymous"))
            replyTo = AddressingUtils.getAnonymousRoleURI();

        if (from != null && from.equalsIgnoreCase("anonymous"))
            from = AddressingUtils.getAnonymousRoleURI();

        if (acksTo != null && acksTo.equalsIgnoreCase("anonymous"))
            acksTo = AddressingUtils.getAnonymousRoleURI();

        try {
            boolean sync = false;
            if (acksTo.equals(AddressingUtils.getAnonymousRoleURI())) {
                sync = true;
            }
            System.out.println("=========== RUNNING THE \"Ping\" INTEROP TEST ==========");

            RMInitiator.initClient(sync);

            Service service = new Service();
            Call call = (Call) service.createCall();

            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(sync));
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:Ping");
            call.setProperty(Constants.ClientProperties.ACKS_TO, acksTo);

            if (from != null && from != "")
                call.setProperty(Constants.ClientProperties.FROM, from);

            if (replyTo != null && replyTo != "")
                call.setProperty(Constants.ClientProperties.REPLY_TO, replyTo);

            call.setTargetEndpointAddress(target);
            call.setOperationName(new QName("http://tempuri.org", "Ping"));
            call.setTransport(new RMTransport(target, ""));

            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);

            for (int i = 1; i <= messages; i++) {
                call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
                if (i == messages) {
                    call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
                }
                String msg = "Sandesha Ping Message Number " + i;
                call.invoke(new Object[]{msg});
            }

            RMInitiator.stopClient();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runEcho(InteropBean bean) {

        String target = bean.getTarget();
        String from = bean.getFrom();
        String replyTo = bean.getReplyto();
        String acksTo = bean.getAcksTo();
        String acks = bean.getAcks();
        String terminate = bean.getTerminate();
        String operation = bean.getOperation();
        boolean sendOffer = bean.isSendOffer();

        int messages = bean.getNoOfMsgs();

        if (replyTo != null && replyTo.equalsIgnoreCase("anonymous"))
            replyTo = AddressingUtils.getAnonymousRoleURI();

        if (from != null && from.equalsIgnoreCase("anonymous"))
            from = AddressingUtils.getAnonymousRoleURI();

        if (acksTo != null && acksTo.equalsIgnoreCase("anonymous"))
            acksTo = AddressingUtils.getAnonymousRoleURI();

        String seq = new Long(System.currentTimeMillis()).toString();

        try {
            boolean sync = false;
            if (acksTo.equalsIgnoreCase("anonymous"))
                sync = true;

            System.out.println("=========== RUNNING THE \"echoString\" INTEROP TEST ==========");

            //We start the listener to be in the safe side.
            //User may specify some external(not in sandesha endpoint) replyTo address, then
            //he/she will not be able to retrieve the responses to this client, yet they can verify
            //the reliablility of the sent messages.
            RMInitiator.initClient(false);

            Service service = new Service();
            Call call = (Call) service.createCall();

            System.out.println("Echo sync:" + sync);
            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(sync));
            call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:echoString");
            call.setProperty(Constants.ClientProperties.ACKS_TO, acksTo);

            if (from != null && from != "")
                call.setProperty(Constants.ClientProperties.FROM, from);

            if (replyTo != null && replyTo != "")
                call.setProperty(Constants.ClientProperties.REPLY_TO, replyTo);

            if (sendOffer) {
                call.setProperty(Constants.ClientProperties.SEND_OFFER, new Boolean(true));
            }

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

            RMInitiator.stopClient();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
