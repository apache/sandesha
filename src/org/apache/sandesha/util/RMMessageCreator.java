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

package org.apache.sandesha.util;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.*;
import org.apache.axis.types.URI;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.client.ClientPropertyValidator;
import org.apache.sandesha.ws.rm.AcksTo;
import org.apache.sandesha.ws.rm.CreateSequence;
import org.apache.sandesha.ws.rm.RMHeaders;

public class RMMessageCreator {
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    /*public static RMMessageContext createCreateSeqMsg(RMMessageContext rmMsgCtx) throws Exception {
        AddressingHeaders addrHeaders = getAddressingHeaders(rmMsgCtx);
        String uuid = uuidGen.nextUUID(); String uuid = uuidGen.nextUUID();

        //Create the RMMessageContext to hold the create Sequence Request.
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();

        String msgId = Constants.UUID + uuid;
        createSeqRMMsgContext.setMessageID(msgId);
        createSeqRMMsgContext.addToMsgIdList(msgId);
        MessageContext msgContext = rmMsgCtx.getMsgContext();

        String toAddress = rmMsgCtx.getOutGoingAddress();
        //Set the action
        Action action = new Action(new URI(Constants.WSRM.ACTION_CREATE_SEQUENCE));
        addrHeaders.setAction(action);
        createSeqRMMsgContext.setAddressingHeaders(addrHeaders);
        createSeqRMMsgContext.setSync(rmMsgCtx.getSync());
        createSeqRMMsgContext.setAcksTo(rmMsgCtx.getAcksTo());

        createSeqRMMsgContext.setOutGoingAddress(toAddress);
        SOAPEnvelope resEnvelope = EnvelopeCreator.createCreateSequenceEnvelope(uuid, createSeqRMMsgContext, Constants.CLIENT);
        MessageContext createSeqMsgContext = new MessageContext(msgContext.getAxisEngine());
        //This should be a clone operation.
        RMMessageContext.copyMessageContext(msgContext, createSeqMsgContext);
        createSeqMsgContext.setRequestMessage(new Message(resEnvelope));
        createSeqRMMsgContext.setMsgContext(createSeqMsgContext);
        //Set the message type
        createSeqRMMsgContext.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        return createSeqRMMsgContext;
    }*/


    public static RMMessageContext createCreateSeqMsg(RMMessageContext rmMsgCtx, byte endPoint) throws Exception {
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();
        rmMsgCtx.copyContents(createSeqRMMsgContext);
        AddressingHeaders addrHeaders = getAddressingHeaders(rmMsgCtx);

        RMHeaders rmHeaders = new RMHeaders();

        CreateSequence createSeq = new CreateSequence();
        AcksTo acksTo = getAcksTo(rmMsgCtx, endPoint);
        createSeq.setAcksTo(acksTo);
        rmHeaders.setCreateSequence(createSeq);
        createSeqRMMsgContext.setRMHeaders(rmHeaders);

        AddressingHeaders csAddrHeaders = new AddressingHeaders();
        if (endPoint == Constants.CLIENT) {
            csAddrHeaders.setTo(addrHeaders.getTo());
            csAddrHeaders.setFaultTo(addrHeaders.getFaultTo());
        } else {
            csAddrHeaders.setTo(new To(addrHeaders.getReplyTo().getAddress().toString()));
            csAddrHeaders.setFaultTo(new FaultTo(new Address(rmMsgCtx.getAddressingHeaders().getTo().toString())));
        }
        csAddrHeaders.setAction(new Action(new URI(Constants.WSRM.ACTION_CREATE_SEQUENCE)));

        createSeqRMMsgContext.setAddressingHeaders(csAddrHeaders);

        String uuid = uuidGen.nextUUID();
        String msgId = Constants.UUID + uuid;
        createSeqRMMsgContext.setMessageID(msgId);
        createSeqRMMsgContext.addToMsgIdList(msgId);
        createSeqRMMsgContext.setSync(rmMsgCtx.getSync());
        createSeqRMMsgContext.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);

        String toAddress = rmMsgCtx.getOutGoingAddress();

        createSeqRMMsgContext.setOutGoingAddress(toAddress);
        MessageContext msgContext = rmMsgCtx.getMsgContext();
        MessageContext createSeqMsgContext = new MessageContext(msgContext.getAxisEngine());
        RMMessageContext.copyMessageContext(msgContext, createSeqMsgContext);
        createSeqRMMsgContext.setMsgContext(createSeqMsgContext);

        return createSeqRMMsgContext;
    }


    private static AcksTo getAcksTo(RMMessageContext rmMsgCtx, byte endPoint) throws Exception {
        AcksTo acksTo = null;

        if (endPoint == Constants.CLIENT) {
            if (rmMsgCtx.getSync()) {
                acksTo = new AcksTo(new Address(new URI(Constants.WSA.NS_ADDRESSING_ANONYMOUS)));
            } else {
                if (rmMsgCtx.getAcksTo() != null)
                    acksTo = new AcksTo(new Address(new URI(rmMsgCtx.getAcksTo())));
                else if (rmMsgCtx.getReplyTo() != null)
                    acksTo = new AcksTo(new Address(new URI(rmMsgCtx.getReplyTo())));
                else if (rmMsgCtx.getSourceURL() != null)
                    acksTo = new AcksTo(new Address(new URI(rmMsgCtx.getSourceURL())));
            }
        } else if (endPoint == Constants.SERVER) {
            AddressingHeaders addrHeaders = rmMsgCtx.getAddressingHeaders();

            To incommingTo = addrHeaders.getTo();
            acksTo = new AcksTo(new Address(incommingTo.toString()));
        }
        return acksTo;
    }

    public static RMMessageContext createCreateSeqResponseMsg(RMMessageContext rmMsgCtx) throws Exception {
        return new RMMessageContext();
    }

    public static RMMessageContext createTerminateSeqMsg(RMMessageContext rmMsgCtx) throws Exception {
        RMMessageContext terSeqRMMsgContext = new RMMessageContext();
        MessageContext terSeqMsgContext = new MessageContext(rmMsgCtx.getMsgContext().getAxisEngine());
        terSeqRMMsgContext.setSequenceID(rmMsgCtx.getSequenceID());
        terSeqRMMsgContext.setAddressingHeaders(rmMsgCtx.getAddressingHeaders());
        terSeqRMMsgContext.setOutGoingAddress(rmMsgCtx.getOutGoingAddress());
        terSeqRMMsgContext.setMsgContext(terSeqMsgContext);
        terSeqRMMsgContext.setMessageType(Constants.MSG_TYPE_TERMINATE_SEQUENCE);
        return terSeqRMMsgContext;
    }

    public static RMMessageContext createAcknowledgementMsg(RMMessageContext rmMessageContext) throws Exception {
        return new RMMessageContext();
    }

    public static RMMessageContext createServiceResponseMsg(RMMessageContext rmMsgCtx) throws Exception {
        return new RMMessageContext();
    }

    public static RMMessageContext createServiceRequestMsg(MessageContext msgCtx) throws Exception {
        //Get a copy of the MessageContext. This is required when sending multiple messages from
        //one call object
        MessageContext newMsgContext = cloneMsgContext(msgCtx);
        RMMessageContext requestMesssageContext = new RMMessageContext();
        //Get the message information from the client.
        Call call = (Call) newMsgContext.getProperty(MessageContext.CALL);
        //If the property specified by the client is not valid an AxisFault will be sent at this point.
        requestMesssageContext = ClientPropertyValidator.validate(call);
        requestMesssageContext.setOutGoingAddress((String) msgCtx.getProperty(MessageContext.TRANS_URL));
        requestMesssageContext.setMsgContext(newMsgContext);
        AddressingHeaders addrHeaders = getAddressingHeaders(requestMesssageContext);
        if (requestMesssageContext.getAction() != null)
            addrHeaders.setAction(new Action(new URI(requestMesssageContext.getAction())));
        requestMesssageContext.setAddressingHeaders(addrHeaders);
        requestMesssageContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
        requestMesssageContext.setMessageID(Constants.UUID + uuidGen.nextUUID());
        return requestMesssageContext;

    }

    private static MessageContext cloneMsgContext(MessageContext msgContext) throws AxisFault {
        MessageContext clone = new MessageContext(msgContext.getAxisEngine());
        String str = msgContext.getRequestMessage().getSOAPPartAsString();
        Message msg = new Message(str);
        clone.setRequestMessage(msg);
        RMMessageContext.copyMessageContext(msgContext, clone);
        return clone;
    }

    private static AddressingHeaders getAddressingHeaders(RMMessageContext rmMsgContext)
            throws URI.MalformedURIException {

        if (rmMsgContext.getAddressingHeaders() != null) {
            return rmMsgContext.getAddressingHeaders();
        } else {

            // MessageContext msgContext= rmMsgContext.getMsgContext();
            //Variable to hold the status of the asynchronous or synchronous state.
            boolean sync = rmMsgContext.getSync();
            AddressingHeaders addrHeaders = new AddressingHeaders();
            From from = null;
            ReplyTo replyTo = null;
            FaultTo faultTo = null;
            String replyToURL = rmMsgContext.getReplyTo();
            String fromURL = rmMsgContext.getFrom();
            String faultToURL = rmMsgContext.getFaultTo();

            //Need to use the anonymous_URI if the client is synchronous.
            if (!sync) {
                if (fromURL != null) {
                    from = new From(new Address(fromURL));
                } else {
                    from = new From(new Address(rmMsgContext.getSourceURL()));
                }
                addrHeaders.setFrom(from);
                if (replyToURL != null) {
                    replyTo = new ReplyTo(new Address(replyToURL));
                    addrHeaders.setReplyTo(replyTo);
                } else {
                    replyTo = new ReplyTo(new Address(rmMsgContext.getSourceURL()));
                    addrHeaders.setReplyTo(replyTo);
                }

            } else {
                if (fromURL != null) {
                    from = new From(new Address(fromURL));
                } else {
                    from = new From(new Address(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
                }
                addrHeaders.setFrom(from);
                if (rmMsgContext.isHasResponse()) {
                    replyTo = new ReplyTo(new Address(replyToURL));
                    addrHeaders.setReplyTo(replyTo);
                }
            }

            if (faultToURL != null) {
                faultTo = new FaultTo(new Address(faultToURL));
                addrHeaders.setFaultTo(faultTo);
            } else {
                faultTo = new FaultTo(new Address(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
                addrHeaders.setFaultTo(faultTo);
            }
            //Set the target endpoint URL
            if (rmMsgContext.getTo() != null) {
                To to = new To(new Address(rmMsgContext.getTo()));
                addrHeaders.setTo(to);
            } else {
                To to = new To(new Address(rmMsgContext.getOutGoingAddress()));
                addrHeaders.setTo(to);
            }

            return addrHeaders;
        }
    }
}