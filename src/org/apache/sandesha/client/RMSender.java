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
import org.apache.sandesha.*;
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
    private static Thread senderThread = null;

    public void invoke(MessageContext msgContext) throws AxisFault {

        //TODO This we need to check.
        MessageContext newMsgContext = cloneMsgContext(msgContext);

        RMMessageContext requestMesssageContext = getRMMessageContext(newMsgContext);
        //Initialize the storage manager. We are in the client side
        //So initialize the client Storage Manager.
        storageManager = new ClientStorageManager();
        RMInitiator.initClient(requestMesssageContext.getSync());
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

            if (requestMesssageContext.isLastMessage()) {
                storageManager.insertTerminateSeqMessage(getTerminateSeqMessage(requestMesssageContext));
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
            } else {
                boolean gotAck = false;
                while (!gotAck) {
                    System.out.println("");
                    gotAck = checkTheQueueForAck(requestMesssageContext.getSequenceID(),
                            requestMesssageContext.getMessageID());
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }

                msgContext.setResponseMessage(null);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * @return
     */
    private RMMessageContext getTerminateSeqMessage(RMMessageContext rmMessageContext) {
        RMMessageContext terSeqRMMsgContext = new RMMessageContext();
        MessageContext terSeqMsgContext = new MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
        terSeqRMMsgContext.setSequenceID(rmMessageContext.getSequenceID());
        terSeqRMMsgContext.setAddressingHeaders(rmMessageContext.getAddressingHeaders());
        terSeqRMMsgContext.setOutGoingAddress(rmMessageContext.getOutGoingAddress());
        terSeqRMMsgContext.setMsgContext(terSeqMsgContext);
        terSeqRMMsgContext.setMessageType(Constants.MSG_TYPE_TERMINATE_SEQUENCE);
        // TODO Auto-generated method stub
        return terSeqRMMsgContext;
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


    private boolean checkTheQueueForAck(String sequenceId, String reqMessageID) {
        return storageManager.checkForAcknowledgement(sequenceId, reqMessageID);
    }

    private RMMessageContext checkTheQueueForResponse(String sequenceId, String reqMessageID) {
        return storageManager.checkForResponseMessage(sequenceId, reqMessageID);
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



