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
package org.apache.sandesha.client;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.From;
import org.apache.axis.message.addressing.ReplyTo;
import org.apache.axis.message.addressing.To;
import org.apache.axis.message.addressing.handler.AddressingHandler;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.server.ServerStorageManager;
import org.apache.sandesha.server.queue.ServerQueue;
import org.apache.sandesha.ws.rm.MessageNumber;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.Sequence;
import org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler;
import org.apache.sandesha.ws.rm.providers.RMProvider;
import org.apache.sandesha.ws.utility.Identifier;

import com.sun.jndi.url.rmi.rmiURLContext;

public class RMSender extends BasicHandler {

    /**
     * Initialize the StorageManager Add the messsag to the queue and just
     * return Create SimpleAxisServer
     */

    private static boolean senderStarted = false;

    private static boolean serverStarted = false;

    private IStorageManager storageManager;

    private SimpleAxisServer sas = null;

    private Sender sender = null;

    public void invoke(MessageContext msgContext) throws AxisFault {

        //Get the URL to send the message.

        storageManager = new ClientStorageManager();
        initializeRMSender(msgContext, storageManager);

        //Check whether we have messages or not in the queue.
        //If yes, just add
        //If no, need to add a priority message.
        //return.

        //Start the sender
        //Start the SimpleAxisServer
        //Initiate the StorageManager
        //Insert the messae
        //Return null ; Later return for callback.

        try {
            //This should be changed so the inital sequence is sent by the
            // client.
            //This way we can verify that we are shifting from one set of
            // messages to the other.
            //Also helps when we introduce the callback mechanism.
            //we need the following sequence of methods to support this

            AddressingHeaders addrHeaders = getAddressingHeaders(msgContext);

            long nextMsgNumber = storageManager
                    .getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID);

            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

            if (nextMsgNumber == 1) {
                //This is the first message..
                //add a create sequence message
                //add the message with the temp seqID
                //System.out.println("First Message");

                //Set the tempUUID
                String tempUUID = uuidGen.nextUUID();
                RMMessageContext createSeqRMMsgContext = getCreateSeqRMContext(
                        msgContext, addrHeaders, tempUUID);
                createSeqRMMsgContext.setMessageID("uuid:" + tempUUID);
                //Create a sequence first.
                storageManager
                        .addSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
                storageManager.setTemporaryOutSequence(
                        Constants.CLIENT_DEFAULD_SEQUENCE_ID, "uuid:"
                                + tempUUID);
                storageManager.addCreateSequenceRequest(createSeqRMMsgContext);

                //RMMessageContext reqRMMsgContext =
                // getReqRMContext(msgContext,
                //        addrHeaders, tempUUID, nextMsgNumber);

                RMMessageContext reqRMMsgContext = new RMMessageContext();
                reqRMMsgContext.setAddressingHeaders(addrHeaders);
                reqRMMsgContext.setOutGoingAddress(addrHeaders.getTo()
                        .toString());
                reqRMMsgContext.setMsgContext(msgContext);
                reqRMMsgContext.setMsgNumber(nextMsgNumber);
                reqRMMsgContext
                        .setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
                reqRMMsgContext.setMessageID("uuid:" + uuidGen.nextUUID());
                storageManager.insertOutgoingMessage(reqRMMsgContext);

            } else {
                RMMessageContext reqRMMsgContext = new RMMessageContext();
                reqRMMsgContext.setMsgContext(msgContext);
                reqRMMsgContext.setAddressingHeaders(addrHeaders);
                reqRMMsgContext.setOutGoingAddress(addrHeaders.getTo()
                        .toString());
                //reqRMMsgContext.setMsgNumber(storageManager
                //        .getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID));
                reqRMMsgContext.setMsgNumber(nextMsgNumber);

                reqRMMsgContext
                        .setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
                reqRMMsgContext.setMessageID("uuid:" + uuidGen.nextUUID());
                storageManager.insertOutgoingMessage(reqRMMsgContext);
                //System.out.println("This is NOT the first
                // message..........................");
            }

            /*
             * RMMessageContext rmMessageContext= new RMMessageContext();
             * 
             * rmMessageContext.setMsgContext(msgContext);
             * rmMessageContext.setSequenceID("abc");
             * storageManager.insertRequestMessage(rmMessageContext);
             * 
             * storageManager.setTemporaryOutSequence("abc","def");
             * storageManager.setApprovedOutSequence("def","pqr");
             *  
             */

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //ServerQueue sq= ServerQueue.getInstance();
        // sq.displayIncomingMap();
        // sq.displayOutgoingMap();
        // sq.displayPriorityQueue();

        // RMSender will hang at this point.
        //while(storageManager.getResponseMessage(MessageID)!=null){
        //Thread.sleap(1000);
        //}
        //
        //msgContext.setResponseMessage(storageManager.getResponseMessage(MessageID));
        msgContext.setResponseMessage(null);

    }

    /**
     * @param msgContext
     * @param addrHeaders
     * @return
     */
    private RMMessageContext getReqRMContext(MessageContext msgContext,
            AddressingHeaders addrHeaders, String uuid, long msgNo) {

        //Get the URL to send the message.
        String toAddress = (String) msgContext
                .getProperty(MessageContext.TRANS_URL);

        // Create the RMMessageContext to hold the Request message.
        RMMessageContext reqRMMsgContext = new RMMessageContext();
        MessageContext messageContext = new MessageContext(msgContext
                .getAxisEngine());
        RMMessageContext.copyMessageContext(msgContext, messageContext);

        reqRMMsgContext.setOutGoingAddress(addrHeaders.getTo().toString());

        //RMHeaders for the message.
        RMHeaders rmHeaders = new RMHeaders();

        //Sequence for the new message.
        Sequence seq = new Sequence();
        Identifier id = new Identifier();
        id.setIdentifier("uuid:" + uuid);
        seq.setIdentifier(id);

        //Message Number for the new message.
        MessageNumber msgNumber = new MessageNumber();
        msgNumber.setMessageNumber(msgNo);
        seq.setMessageNumber(msgNumber);

        rmHeaders.setSequence(seq);

        reqRMMsgContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
        //Set the RMheaders to the RMMessageContext.
        reqRMMsgContext.setRMHeaders(rmHeaders);
        //Set the addrssing headers to RMMessageContext.
        reqRMMsgContext.setAddressingHeaders(addrHeaders);

        reqRMMsgContext.setOutGoingAddress(toAddress);
        //SOAPEnvelope resEnvelope = EnvelopeCreator
        //        .createServiceRequestEnvelope(uuid, reqRMMsgContext,
        //                Constants.CLIENT);
        //Add the message to the request message.
        //messageContext.setRequestMessage(new Message(resEnvelope));
        reqRMMsgContext.setMsgContext(messageContext);
        return reqRMMsgContext;
    }

    private RMMessageContext getCreateSeqRMContext(MessageContext msgContext,
            AddressingHeaders addrHeaders, String uuid)
            throws MalformedURIException {

        String toAddress = (String) msgContext
                .getProperty(MessageContext.TRANS_URL);
        //Set the action
        Action action = new Action(new URI(Constants.ACTION_CREATE_SEQUENCE));
        addrHeaders.setAction(action);

        //Create the RMMessageContext to hold the create Sequence Request.
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();
        createSeqRMMsgContext.setAddressingHeaders(addrHeaders);

        //Set the outgoing address these need to be corrected.
        createSeqRMMsgContext.setOutGoingAddress(toAddress);

        SOAPEnvelope resEnvelope = EnvelopeCreator
                .createCreateSequenceEnvelope(uuid, createSeqRMMsgContext,
                        Constants.CLIENT);

        MessageContext createSeqMsgContext = new MessageContext(msgContext
                .getAxisEngine());

        RMMessageContext.copyMessageContext(msgContext, createSeqMsgContext);
        createSeqMsgContext.setRequestMessage(new Message(resEnvelope));
        createSeqRMMsgContext.setMsgContext(createSeqMsgContext);

        //Set the message type
        createSeqRMMsgContext
                .setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        return createSeqRMMsgContext;
    }

    private void initializeRMSender(MessageContext msgContext,
            IStorageManager storageManager) {

        if (!senderStarted) {
            //Pass the storageManager to the Sender.
            sender = new Sender(storageManager);
            Thread senderThread = new Thread(sender);
            //senderThread.setDaemon(true);
            senderThread.start();
        }

        if (!serverStarted) {
            sas = new SimpleAxisServer();
            serverStarted = true;
            try {
                SimpleProvider sp = new SimpleProvider();
                sas.setMyConfig(sp);
                //SOAPService myService = new SOAPService(new RPCProvider());

                Handler addrHanlder = new AddressingHandler();
                Handler rmHandler = new RMServerRequestHandler();

                SimpleChain shc = new SimpleChain();
                shc.addHandler(addrHanlder);
                shc.addHandler(rmHandler);

                //Need to use the RMProvider at the client side to handle the
                //Asynchronous responses.
                RMProvider rmProvider = new RMProvider();
                //This is the switch used to inform the RMProvider about the
                // side that it operates.
                rmProvider.setClient(true);

                SOAPService myService = new SOAPService(shc, rmProvider, null);

                JavaServiceDesc desc = new JavaServiceDesc();
                myService.setOption("className",
                        "samples.userguide.example3.MyService");
                myService.setOption("allowedMethods", "*");

                //Add Handlers ; Addressing and ws-rm before the service.
                desc.setName("MyService");
                myService.setServiceDescription(desc);

                //deploy the service to server
                sp.deployService("MyService", myService);
                //finally start the server
                //Start the simple axis server in port 8090
                sas.setServerSocket(new ServerSocket(8090));

                Thread serverThread = new Thread(sas);
                //serverThread.setDaemon(true);
                serverThread.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    private AddressingHeaders getAddressingHeaders(MessageContext msgContext)
            throws MalformedURIException {
        String toAddress = (String) msgContext
                .getProperty(MessageContext.TRANS_URL);//"http://127.0.0.1:9070/axis/services/EchoStringService?wsdl";
        Call call = (Call) msgContext.getProperty(MessageContext.CALL);
        //Variable to hold the status of the asynchronous or synchronous state.
        boolean isAsync = false;
        if ((String) call.getProperty("isAsync") == "true")
            isAsync = true;

        //Get the host address of the source machine.
        String sourceHost = (String) call.getProperty("sourceAddress");

        AddressingHeaders addrHeaders = new AddressingHeaders();

        From from = null;

        //Need to use the anonymous_URI if the client is synchronous.
        if (isAsync == true) {
            from = new From(new Address("http://" + sourceHost
                    + ":8080/axis/services/MyService"));
            addrHeaders.setFrom(from);
            ReplyTo replyTo = new ReplyTo(new Address("http://" + sourceHost
                    + ":8080/axis/services/MyService"));
            addrHeaders.setReplyTo(replyTo);
        } else {
            from = new From(new Address(Constants.ANONYMOUS_URI));
        }

        //Set the target endpoint URL
        To to = new To(new Address(toAddress));
        addrHeaders.setTo(to);

        return addrHeaders;
    }

}

