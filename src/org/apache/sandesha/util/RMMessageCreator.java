package org.apache.sandesha.util;

import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.types.URI;
import org.apache.axis.message.addressing.*;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.client.Call;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.client.ClientPropertyValidator;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Feb 24, 2005
 * Time: 3:16:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class RMMessageCreator {

    public static RMMessageContext createCreateSeqMsg(RMMessageContext rmMsgCtx) throws Exception {
         //String toAddress = (String)
        // msgContext.getProperty(MessageContext.TRANS_URL);
        AddressingHeaders addrHeaders= getAddressingHeaders(rmMsgCtx);
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        String uuid = uuidGen.nextUUID();

             //Create the RMMessageContext to hold the create Sequence Request.
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();
        createSeqRMMsgContext.setMessageID(Constants.UUID+uuid);

        MessageContext msgContext = rmMsgCtx.getMsgContext();
        String toAddress = rmMsgCtx.getOutGoingAddress();

        //Set the action
        Action action = new Action(new URI(Constants.ACTION_CREATE_SEQUENCE));
        addrHeaders.setAction(action);


        createSeqRMMsgContext.setAddressingHeaders(addrHeaders);
        createSeqRMMsgContext.setSync(rmMsgCtx.getSync());

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

    public static RMMessageContext createAcknowledgementMsg(RMMessageContext rmMsgCtx) throws Exception {
        return new RMMessageContext();
    }

    public static RMMessageContext createServiceResponseMsg(RMMessageContext rmMsgCtx) throws Exception {
        return new RMMessageContext();
    }

//    public static RMMessageContext createServiceRequestMsg(RMMessageContext rmMsgCtx) throws Exception {
//        long nextMsgNumber = rmMsgCtx.getMsgNumber();
//        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
//        reqRMMsgContext.setAddressingHeaders(addrHeaders);
//        reqRMMsgContext.setOutGoingAddress(addrHeaders.getTo().toString());
//        reqRMMsgContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
//        reqRMMsgContext.setMessageID(Constants.UUID + uuidGen.nextUUID());
//        reqRMMsgContext.setMsgNumber(nextMsgNumber);
//        //Set the processing state of the RMMessageContext
//        reqRMMsgContext.setSync(sync);
//    }


       public  static RMMessageContext createServiceRequestMsg(MessageContext msgCtx) throws Exception {
         MessageContext newMsgContext = cloneMsgContext(msgCtx);

        RMMessageContext requestMesssageContext = new RMMessageContext();
        //Get the message information from the client.
        Call call = (Call) newMsgContext.getProperty(MessageContext.CALL);
        //If the property specified by the client is not valid
        //an AxisFault will be sent at this point.
        requestMesssageContext = ClientPropertyValidator.validate(call);
        requestMesssageContext.setOutGoingAddress((String) newMsgContext.getProperty(MessageContext.TRANS_URL));
        requestMesssageContext.setMsgContext(newMsgContext);

        // long nextMsgNumber = reqRMMsgContext.getMsgNumber();
        AddressingHeaders addrHeaders = getAddressingHeaders(requestMesssageContext);

        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        requestMesssageContext.setAddressingHeaders(addrHeaders);
        requestMesssageContext.setOutGoingAddress(addrHeaders.getTo().toString());
        requestMesssageContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
        requestMesssageContext.setMessageID(Constants.UUID + uuidGen.nextUUID());
        //requestMesssageContext.setMsgNumber(nextMsgNumber);
        //Set the processing state of the RMMessageContext
        //requestMesssageContext.setSync(sync);


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


}