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
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.*;
import org.apache.axis.types.URI;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.client.ClientPropertyValidator;

public class RMMessageCreator {

    public static RMMessageContext createCreateSeqMsg(RMMessageContext rmMsgCtx) throws Exception {
        //String toAddress = (String)
        // msgContext.getProperty(MessageContext.TRANS_URL);
        AddressingHeaders addrHeaders = getAddressingHeaders(rmMsgCtx);
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        String uuid = uuidGen.nextUUID();

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

        //Set the outgoing address these need to be corrected.
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
        MessageContext newMsgContext = cloneMsgContext(msgCtx);

        RMMessageContext requestMesssageContext = new RMMessageContext();
        //Get the message information from the client.
        Call call = (Call) newMsgContext.getProperty(MessageContext.CALL);
        //If the property specified by the client is not valid an AxisFault will be sent at this point.
        requestMesssageContext = ClientPropertyValidator.validate(call);
        requestMesssageContext.setOutGoingAddress((String) msgCtx.getProperty(MessageContext.TRANS_URL));
        requestMesssageContext.setMsgContext(newMsgContext);

        // long nextMsgNumber = reqRMMsgContext.getMsgNumber();
        AddressingHeaders addrHeaders = getAddressingHeaders(requestMesssageContext);

        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
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

        // MessageContext msgContext= rmMsgContext.getMsgContext();
        //Variable to hold the status of the asynchronous or synchronous state.
        boolean sync = rmMsgContext.getSync();
        AddressingHeaders addrHeaders = new AddressingHeaders();
        From from = null;
        ReplyTo replyTo = null;
        //String fromURL = rmMsgContext.getFrom();
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
            from = new From(new Address(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
            addrHeaders.setFrom(from);
            if (rmMsgContext.isHasResponse()) {
                replyTo = new ReplyTo(new Address(replyToURL));
                addrHeaders.setReplyTo(replyTo);
            }

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