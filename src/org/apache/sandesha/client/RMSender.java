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
import org.apache.sandesha.ws.rm.MessageNumber;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.Sequence;
import org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler;
import org.apache.sandesha.ws.utility.Identifier;

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
        //Check whether we have messages or not in the queue.
        //If yes, just add
        //If no, need to add a priority message.
        //return.

        //Start the sender
        //Start the SimpleAxisServer
        //Initiate the StorageManager
        //Insert the messae
        //Return null ; Later return for callback.

        storageManager = new ClientStorageManager();

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
            try{
            SimpleProvider sp = new SimpleProvider();
            sas.setMyConfig(sp);
            //SOAPService myService = new SOAPService(new RPCProvider());

            Handler addrHanlder = new AddressingHandler();
            Handler rmHandler = new RMServerRequestHandler();

            SimpleChain shc = new SimpleChain();
            shc.addHandler(addrHanlder);
            shc.addHandler(rmHandler);
            
            //Set the provider to the CRMProvider so that it will use the 
            //ClientStorageManger.. 
            //Need to revise this use of CRMProvider.
            
            //This needs to be corrected. To Client provider
            //TODO
            SOAPService myService = new SOAPService(shc,
                    new org.apache.sandesha.ws.rm.providers.RMProvider(), null);
            //			customize the webservice
            JavaServiceDesc desc = new JavaServiceDesc();
            myService.setOption("className",
                    "samples.userguide.example3.MyService");
            myService.setOption("allowedMethods", "*");

            //Add Handlers ; Addressing and ws-rm before the service.

            desc.setName("MyService");
            myService.setServiceDescription(desc);

            //			 deploy the service to server
            sp.deployService("MyService", myService);
            //			finally start the server
            sas.setServerSocket(new ServerSocket(8090));

            Thread serverThread = new Thread(sas);
            //serverThread.setDaemon(true);
            serverThread.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
            
            
        }
        try {

            //At this moment we don't know a sequence
            long nextMsgNumber = storageManager
                    .getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID);

            if (nextMsgNumber == 1) {
                //This is the first message..
                //add a create sequence message
                //add the message with the temp seqID
                System.out.println("First Message");
               
                //Addressing information currently hard-coded.
                //---------------------------------------------------------------
                AddressingHeaders addrHeaders = new AddressingHeaders();
                From from = new From(new Address(
                        "http://localhost:8090/axis/services/MyService"));
                addrHeaders.setFrom(from);

                To to = new To(
                        new Address(
                                "http://127.0.0.1:8080/axis/services/EchoStringService?wsdl"));
                addrHeaders.setTo(to);

                ReplyTo replyTo = new ReplyTo(new Address(
                        "http://localhost:8090/axis/services/MyService"));
                addrHeaders.setReplyTo(replyTo);
                //---------------------------------------------------------------

                //Set the tempUUID
                String tempUUID = "ABCDEFGH";
                RMMessageContext createSeqRMMsgContext = getCreateSeqRMContext(
                        msgContext, addrHeaders, tempUUID);
               
                //Create a sequence first.
                storageManager.addSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
                storageManager.setTemporaryOutSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID,"uuid:ABCDEFGH");
                storageManager.addCreateSequenceRequest(createSeqRMMsgContext);
                //RMMessageContext reqRMMsgContext = getReqRMContext(msgContext,
                //        addrHeaders, tempUUID, nextMsgNumber);
                //storageManager.insertRequestMessage(reqRMMsgContext);

            } else {
                //Add the message only.
                System.out.println("This is NOT the first message..........................");
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

        msgContext.setResponseMessage(null);

    }

    /**
     * @param msgContext
     * @param addrHeaders
     * @return
     */
    private RMMessageContext getReqRMContext(MessageContext msgContext,
            AddressingHeaders addrHeaders, String uuid, long msgNo) {
        // Create the RMMessageContext to hold the create Sequence Request.

        //RMHeaders for the message.
        RMHeaders rmHeaders = new RMHeaders();

        //Sequence for the new message.
        Sequence seq = new Sequence();
        Identifier id = new Identifier();
        id.setIdentifier(uuid);
        seq.setIdentifier(id);
        rmHeaders.setSequence(seq);

        //Message Number for the new message.
        MessageNumber msgNumber = new MessageNumber();
        msgNumber.setMessageNumber(msgNo);
        seq.setMessageNumber(msgNumber);

        RMMessageContext reqRMMsgContext = new RMMessageContext();
        //Set the RMheaders to the RMMessageContext.
        reqRMMsgContext.setRMHeaders(rmHeaders);
        //Set the addrssing headers to RMMessageContext.
        reqRMMsgContext.setAddressingHeaders(addrHeaders);
        reqRMMsgContext.setMsgContext(msgContext);
        reqRMMsgContext
                .setOutGoingAddress("http://127.0.0.1:8080/axis/services/EchoStringService?wsdl");
        SOAPEnvelope resEnvelope = EnvelopeCreator
                .createServiceRequestEnvelope(uuid, reqRMMsgContext,
                        Constants.CLIENT);

        return reqRMMsgContext;
    }

    private RMMessageContext getCreateSeqRMContext(MessageContext msgContext,
            AddressingHeaders addrHeaders, String uuid)
            throws MalformedURIException {
        //Set the action
        Action action = new Action(new URI(Constants.ACTION_CREATE_SEQUENCE));
        addrHeaders.setAction(action);

        //Create the RMMessageContext to hold the create Sequence Request.
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();
        createSeqRMMsgContext.setAddressingHeaders(addrHeaders);

        //Set the outgoing address these need to be corrected.
        createSeqRMMsgContext
                .setOutGoingAddress("http://127.0.0.1:8080/axis/services/EchoStringService?wsdl");

        SOAPEnvelope resEnvelope = EnvelopeCreator
                .createCreateSequenceEnvelope(uuid, createSeqRMMsgContext,
                        Constants.CLIENT);

        MessageContext createSeqMsgContext = new MessageContext(msgContext
                .getAxisEngine());
           
        RMMessageContext.copyMessageContext(msgContext,createSeqMsgContext);
        createSeqMsgContext.setRequestMessage(new Message(resEnvelope));
        createSeqRMMsgContext.setMsgContext(createSeqMsgContext);

        //Set the message type
        createSeqRMMsgContext
                .setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        return createSeqRMMsgContext;
    }

}

