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

import javax.wsdl.extensions.soap.SOAPFault;
import javax.xml.namespace.QName;

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
import org.omg.CORBA.portable.RemarshalException;

import com.sun.jndi.url.rmi.rmiURLContext;

public class RMSender extends BasicHandler {

    private static boolean senderStarted = false;
    private static boolean serverStarted = false;
    private IStorageManager storageManager;
    private static SimpleAxisServer sas = null;
    private static Sender sender = null;
    private static Thread senderThread=null;
    public void invoke(MessageContext msgContext) throws AxisFault {

        //TODO This we need to check.
        MessageContext newMsgContext = cloneMsgContext(msgContext);

        RMMessageContext requestMesssageContext = getRMMessageContext(newMsgContext);
        //Initialize the storage manager. We are in the client side
        //So initialize the client Storage Manager.
        storageManager = new ClientStorageManager();
        initializeRMSender(storageManager, requestMesssageContext.getSync());
        try {
            String sequenceID = requestMesssageContext.getSequenceID();
            AddressingHeaders addrHeaders = getAddressingHeaders(requestMesssageContext);

            if (requestMesssageContext.getMsgNumber() == 1) {
                requestMesssageContext = processFirstMessage(requestMesssageContext, addrHeaders,
                        requestMesssageContext.getSync());
            } else {
                requestMesssageContext = processNonFirstMessage(requestMesssageContext,
                        addrHeaders, requestMesssageContext.getSync());
            }

            if (requestMesssageContext.isHasResponse() && !requestMesssageContext.getSync()) {
                RMMessageContext responseMessageContext = null;
                while (responseMessageContext == null) {
                    //TODO Need to check for errors in the queue.
                    //If the queue has an error message, then need to report it
                    // to client.
                    responseMessageContext = checkTheQueueForResponse(sequenceID,
                            requestMesssageContext.getMessageID());
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }
                msgContext.setResponseMessage(responseMessageContext.getMsgContext()
                        .getRequestMessage());
                //SEND TERMINATE SEQ
            } else {
                boolean gotAck = false;
                while (!gotAck) {
                    gotAck = checkTheQueueForAck(requestMesssageContext.getSequenceID(),
                            requestMesssageContext.getMessageID());
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }
            
                msgContext.setResponseMessage(null);
                //SEND TERMINATE SEQ
            }
            
            if(requestMesssageContext.isLastMessage()){
                while(!storageManager.isAckComplete(requestMesssageContext.getSequenceID())){
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }
                if(requestMesssageContext.getSync()){
                    while(!storageManager.isResponseComplete(requestMesssageContext.getSequenceID())){
                        Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                    }   
                               }
               storageManager.insertTerminateSeqMessage(getTerminateSeqMessage()); 	
               
               if(storageManager.isAllSequenceComplete()){
                   senderThread.stop();
                   sas.stop(); 
               }
            }
            
            

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * @return
     */
    private RMMessageContext getTerminateSeqMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param msgContext
     * @return @throws
     *         AxisFault
     */
    private MessageContext cloneMsgContext(MessageContext msgContext) throws AxisFault {

        MessageContext clone = new MessageContext(msgContext.getAxisEngine());
        String str = msgContext.getRequestMessage().getSOAPPartAsString();
        Message msg = new Message(str);
        clone.setRequestMessage(msg);
        RMMessageContext.copyMessageContext(msgContext, clone);
        return clone;
    }

    /**
     * @param msgContext   
     * @param addrHeaders
     * @return
     */
    private RMMessageContext getReqRMContext(MessageContext msgContext,
            AddressingHeaders addrHeaders, String uuid, long msgNo) {
        //Get the URL to send the message.
        String toAddress = (String) msgContext.getProperty(MessageContext.TRANS_URL);
        // Create the RMMessageContext to hold the Request message.
        RMMessageContext reqRMMsgContext = new RMMessageContext();
        MessageContext messageContext = new MessageContext(msgContext.getAxisEngine());
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
        reqRMMsgContext.setMsgContext(messageContext);
        return reqRMMsgContext;
    }

    private RMMessageContext getCreateSeqRMContext(RMMessageContext rmMsgContext,
            AddressingHeaders addrHeaders, String uuid) throws MalformedURIException {

        //String toAddress = (String)
        // msgContext.getProperty(MessageContext.TRANS_URL);
        MessageContext msgContext = rmMsgContext.getMsgContext();
        String toAddress = rmMsgContext.getOutGoingAddress();

        //Set the action
        Action action = new Action(new URI(Constants.ACTION_CREATE_SEQUENCE));
        addrHeaders.setAction(action);

        //Create the RMMessageContext to hold the create Sequence Request.
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();
        createSeqRMMsgContext.setAddressingHeaders(addrHeaders);
        createSeqRMMsgContext.setSync(rmMsgContext.getSync());

        //Set the outgoing address these need to be corrected.
        createSeqRMMsgContext.setOutGoingAddress(toAddress);
        SOAPEnvelope resEnvelope = EnvelopeCreator.createCreateSequenceEnvelope(uuid,
                createSeqRMMsgContext, Constants.CLIENT);

        MessageContext createSeqMsgContext = new MessageContext(msgContext.getAxisEngine());

        //This should be a clone operation.
        RMMessageContext.copyMessageContext(msgContext, createSeqMsgContext);
        createSeqMsgContext.setRequestMessage(new Message(resEnvelope));
        createSeqRMMsgContext.setMsgContext(createSeqMsgContext);

        //Set the message type
        createSeqRMMsgContext.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        return createSeqRMMsgContext;
    }

    private void initializeRMSender(IStorageManager storageManager, boolean sync) {

        if (!senderStarted) {
            //Pass the storageManager to the Sender.
            sender = new Sender(storageManager);
            senderThread = new Thread(sender);
            //senderThread.setDaemon(true);
            senderThread.start();
        }

        if (!sync && !serverStarted) {
            sas = new SimpleAxisServer();
            serverStarted = true;
            try {
                SimpleProvider sp = new SimpleProvider();
                sas.setMyConfig(sp);

                SimpleChain shc = new SimpleChain();
                //We need these two handlers in the request path to the client.
                //Actually the response messages coming asynchronously should
                //come through the SimpleAxisServer instance.
                //We need to load the response handlers specified by the users
                // in addtion to the the above two.
                Handler addrHanlder = new AddressingHandler();
                Handler rmHandler = new RMServerRequestHandler();
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
                myService.setOption("className", "samples.userguide.example3.MyService");
                myService.setOption("allowedMethods", "*");

                //Add Handlers ; Addressing and ws-rm before the service.
                desc.setName("MyService");
                myService.setServiceDescription(desc);

                //deploy the service to server
                sp.deployService("MyService", myService);
                //finally start the server
                //Start the simple axis server in port 8090
                sas.setServerSocket(new ServerSocket(Constants.SOURCE_LISTEN_PORT));

                Thread serverThread = new Thread(sas);
                //serverThread.setDaemon(true);
                serverThread.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    private AddressingHeaders getAddressingHeaders(RMMessageContext rmMsgContext)
            throws MalformedURIException {

        // MessageContext msgContext= rmMsgContext.getMsgContext();
        //Variable to hold the status of the asynchronous or synchronous state.
        boolean sync = rmMsgContext.getSync();
        AddressingHeaders addrHeaders = new AddressingHeaders();
        From from = null;
        ReplyTo replyTo = null;
        String fromURL = rmMsgContext.getFrom();
        String replyToURL = rmMsgContext.getReplyTo();

        //Need to use the anonymous_URI if the client is synchronous.
        if (!sync) {
            from = new From(new Address(rmMsgContext.getSourceURL()));
            addrHeaders.setFrom(from);

            if (replyToURL != null) {
                replyTo = new ReplyTo(new Address(replyToURL));
                addrHeaders.setReplyTo(replyTo);
            } else {
                replyTo = new ReplyTo(new Address(rmMsgContext.getSourceURL()));
                addrHeaders.setReplyTo(replyTo);
            }

        } else {
            from = new From(new Address(Constants.ANONYMOUS_URI));
            addrHeaders.setFrom(from);
            if (rmMsgContext.isHasResponse()) {
                replyTo = new ReplyTo(new Address(replyToURL));
                addrHeaders.setReplyTo(replyTo);
            }

        }
        //Set the target endpoint URL
        To to = new To(new Address(rmMsgContext.getOutGoingAddress()));
        addrHeaders.setTo(to);
        return addrHeaders;
    }

    private RMMessageContext processFirstMessage(RMMessageContext reqRMMsgContext,
            AddressingHeaders addrHeaders, boolean sync) throws Exception {
        long nextMsgNumber = reqRMMsgContext.getMsgNumber();
        System.out.println(nextMsgNumber);
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        //Set the tempUUID
        String tempUUID = uuidGen.nextUUID();
        RMMessageContext createSeqRMMsgContext = getCreateSeqRMContext(reqRMMsgContext,
                addrHeaders, tempUUID);
        createSeqRMMsgContext.setMessageID("uuid:" + tempUUID);
        //Create a sequence first.
        //storageManager.addSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        //storageManager.setTemporaryOutSequence(
        //        Constants.CLIENT_DEFAULD_SEQUENCE_ID, "uuid:" + tempUUID);

        storageManager.addSequence(reqRMMsgContext.getSequenceID());
        storageManager.setTemporaryOutSequence(reqRMMsgContext.getSequenceID(), "uuid:" + tempUUID);

        //Set the processing state to the RMMessageContext
        createSeqRMMsgContext.setSync(sync);

        storageManager.addCreateSequenceRequest(createSeqRMMsgContext);
        reqRMMsgContext.setAddressingHeaders(addrHeaders);
        reqRMMsgContext.setOutGoingAddress(addrHeaders.getTo().toString());
        reqRMMsgContext.setMsgNumber(nextMsgNumber);
        reqRMMsgContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
        reqRMMsgContext.setMessageID("uuid:" + uuidGen.nextUUID());
        storageManager.insertOutgoingMessage(reqRMMsgContext);
        return reqRMMsgContext;
    }

    private RMMessageContext processNonFirstMessage(RMMessageContext reqRMMsgContext,
            AddressingHeaders addrHeaders, boolean sync) throws Exception {
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        reqRMMsgContext.setAddressingHeaders(addrHeaders);
        reqRMMsgContext.setOutGoingAddress(addrHeaders.getTo().toString());
        reqRMMsgContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
        reqRMMsgContext.setMessageID("uuid:" + uuidGen.nextUUID());
        //Set the processing state of the RMMessageContext
        reqRMMsgContext.setSync(sync);
        storageManager.insertOutgoingMessage(reqRMMsgContext);
        return reqRMMsgContext;
    }

    private boolean getAckExpected(String synchronous, String hasResponse, String sourceURI) {
        boolean ackExpected = false;

        if (synchronous.equals("true") && hasResponse.equals("false") && sourceURI.equals(""))
            ackExpected = true;

        if (synchronous.equals("true") && hasResponse.equals("false") && !sourceURI.equals(""))
            ackExpected = true;
        return ackExpected;
    }

    private boolean getResponseExpected(String synchronous, String hasResponse, String sourceURI) {
        boolean responseExpeceted = false;

        if (synchronous.equals("true") && hasResponse.equals("true"))
            responseExpeceted = true;

        if (synchronous.equals("false") && hasResponse.equals("true"))
            responseExpeceted = true;

        return responseExpeceted;
    }

    private boolean checkTheQueueForAck(String sequenceId, String reqMessageID) {
        return storageManager.checkForAcknowledgement(sequenceId, reqMessageID);
    }

    private RMMessageContext checkTheQueueForResponse(String sequenceId, String reqMessageID) {
        return storageManager.checkForResponseMessage(sequenceId, reqMessageID);
    }

    private void setProcessingState(RMMessageContext rmMessageContext, boolean sync) {
    }

    private RMMessageContext getRMMessageContext(MessageContext msgContext) throws AxisFault {
        RMMessageContext requestMesssageContext = new RMMessageContext();
        //Get the message information from the client.
        Call call = (Call) msgContext.getProperty(MessageContext.CALL);
        //If the property specified by the client is not valid
        //an AxisFault will be sent at this point.
        requestMesssageContext = ClientPropertyValidator.validate(call);
        requestMesssageContext.setOutGoingAddress((String) msgContext
                .getProperty(MessageContext.TRANS_URL));
        requestMesssageContext.setMsgContext(msgContext);
        return requestMesssageContext;

    }

}

