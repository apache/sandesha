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
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.*;
import org.apache.axis.types.URI;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.*;

public class RMMessageCreator {
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    public static RMMessageContext createCreateSeqMsg(RMMessageContext rmMsgCtx, byte endPoint, String msgID, String offer) throws Exception {
        RMMessageContext createSeqRMMsgContext = new RMMessageContext();
        rmMsgCtx.copyContents(createSeqRMMsgContext);

        RMHeaders rmHeaders = new RMHeaders();

        CreateSequence createSeq = new CreateSequence();
        AcksTo acksTo = getAcksTo(rmMsgCtx, endPoint);
        createSeq.setAcksTo(acksTo);

        if (offer != null) {
             SequenceOffer seqOffer = new SequenceOffer();
            Identifier id = new Identifier();
            id.setIdentifier(offer);

            seqOffer.setIdentifier(id);
            createSeq.setOffer(seqOffer);
        }

        rmHeaders.setCreateSequence(createSeq);
        createSeqRMMsgContext.setRMHeaders(rmHeaders);

        AddressingHeaders csAddrHeaders = getAddressingHeaedersForCreateSequenceRequest(rmMsgCtx, endPoint, msgID);

        csAddrHeaders.setAction(new Action(new URI(Constants.WSRM.ACTION_CREATE_SEQUENCE)));
        createSeqRMMsgContext.setAddressingHeaders(csAddrHeaders);

        createSeqRMMsgContext.setMessageID(msgID);
        createSeqRMMsgContext.addToMsgIdList(msgID);
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

    private static AddressingHeaders getAddressingHeaedersForCreateSequenceRequest(RMMessageContext rmMsgCtx, byte endPoint, String msgID) throws Exception {
        AddressingHeaders csAddrHeaders = new AddressingHeaders();
        csAddrHeaders.setMessageID(new MessageID(new URI(msgID)));
        if (endPoint == Constants.SERVER) {
            AddressingHeaders ah = rmMsgCtx.getAddressingHeaders();
            csAddrHeaders.setFrom(new EndpointReference(ah.getTo().toString()));
            csAddrHeaders.setReplyTo(new EndpointReference(ah.getTo().toString()));
            csAddrHeaders.setFaultTo(new EndpointReference(ah.getTo().toString()));
            csAddrHeaders.setTo(ah.getReplyTo().getAddress());
        } else {
            csAddrHeaders.setTo(new To(rmMsgCtx.getTo()));
            if (rmMsgCtx.getSync()) {
                csAddrHeaders.setFrom(new EndpointReference(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
                csAddrHeaders.setFaultTo(new EndpointReference(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
                csAddrHeaders.setReplyTo(new EndpointReference(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
            } else {
                String sourceURL = rmMsgCtx.getSourceURL();
                if (rmMsgCtx.getFrom() != null)
                    csAddrHeaders.setFrom(new EndpointReference(rmMsgCtx.getFrom()));
                else
                    csAddrHeaders.setFrom(new EndpointReference(sourceURL));
                if (rmMsgCtx.getReplyTo() != null)
                    csAddrHeaders.setReplyTo(new EndpointReference(rmMsgCtx.getReplyTo()));
                else
                    csAddrHeaders.setReplyTo(new EndpointReference(sourceURL));
                if (rmMsgCtx.getFaultTo() != null)
                    csAddrHeaders.setFaultTo(new EndpointReference(rmMsgCtx.getFaultTo()));
                else
                    csAddrHeaders.setFaultTo(new EndpointReference(sourceURL));
            }
        }
        return csAddrHeaders;
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

    public static RMMessageContext createTerminateSeqMsg(RMMessageContext rmMsgCtx, byte endPoint) throws Exception {
        RMMessageContext terSeqRMMsgContext = new RMMessageContext();
        MessageContext terSeqMsgContext = new MessageContext(rmMsgCtx.getMsgContext().getAxisEngine());
        terSeqRMMsgContext.setSequenceID(rmMsgCtx.getSequenceID());

        AddressingHeaders addHeaders = getAddressingHeadersForTerminateSequence(rmMsgCtx, endPoint);
        SOAPEnvelope soe = new SOAPEnvelope();
        addHeaders.toEnvelope(soe);
        AddressingHeaders terSqAddrHeaders = new AddressingHeaders(soe);
        terSqAddrHeaders.setAction(new Action(new URI(Constants.WSRM.ACTION_TERMINATE_SEQUENCE)));

        terSeqRMMsgContext.setAddressingHeaders(terSqAddrHeaders);
        terSeqRMMsgContext.setOutGoingAddress(rmMsgCtx.getOutGoingAddress());
        terSeqRMMsgContext.setMsgContext(terSeqMsgContext);
        terSeqRMMsgContext.setMessageType(Constants.MSG_TYPE_TERMINATE_SEQUENCE);
        return terSeqRMMsgContext;
    }

    private static AddressingHeaders getAddressingHeadersForTerminateSequence(RMMessageContext rmMsgCtx, byte endPoint) throws Exception {
        AddressingHeaders csAddrHeaders = new AddressingHeaders();
        if (endPoint == Constants.SERVER) {
            AddressingHeaders ah = rmMsgCtx.getAddressingHeaders();
            csAddrHeaders.setTo(ah.getReplyTo().getAddress());
            csAddrHeaders.setFrom(new EndpointReference(ah.getTo().toString()));
        } else {
            csAddrHeaders.setTo(new To(rmMsgCtx.getTo()));
            String sourceURL = rmMsgCtx.getSourceURL();
            if (rmMsgCtx.getFrom() != null)
                csAddrHeaders.setFrom(new EndpointReference(rmMsgCtx.getFrom()));
            else
                csAddrHeaders.setFrom(new EndpointReference(sourceURL));
            if (rmMsgCtx.getFaultTo() != null)
                csAddrHeaders.setFaultTo(new EndpointReference(rmMsgCtx.getFaultTo()));
            else
                csAddrHeaders.setFaultTo(new EndpointReference(sourceURL));
        }
        return csAddrHeaders;
    }

    public static RMMessageContext createAcknowledgementMsg(RMMessageContext rmMessageContext) throws Exception {
        return new RMMessageContext();
    }

    public static RMMessageContext createServiceResponseMessage(RMMessageContext rmMsgCtx) throws Exception {
        return new RMMessageContext();
    }

    public static RMMessageContext createServiceRequestMessage(RMMessageContext rmMsgCtx) throws Exception {
        AddressingHeaders addrHeaders = getAddressingHeaedersForServiceRequest(rmMsgCtx);
        if (rmMsgCtx.getAction() != null)
            addrHeaders.setAction(new Action(new URI(rmMsgCtx.getAction())));
        rmMsgCtx.setAddressingHeaders(addrHeaders);
        rmMsgCtx.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
        rmMsgCtx.setMessageID(Constants.UUID + uuidGen.nextUUID());
        return rmMsgCtx;

    }

    private static AddressingHeaders getAddressingHeaedersForServiceRequest(RMMessageContext rmMsgCtx) throws Exception {
        AddressingHeaders csAddrHeaders = new AddressingHeaders();
        csAddrHeaders.setTo(new To(rmMsgCtx.getTo()));
        if (rmMsgCtx.getSync()) {
            csAddrHeaders.setFrom(new EndpointReference(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
            csAddrHeaders.setFaultTo(new EndpointReference(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
            if (rmMsgCtx.isHasResponse())
                csAddrHeaders.setReplyTo(new EndpointReference(Constants.WSA.NS_ADDRESSING_ANONYMOUS));
        } else {
            String sourceURL = rmMsgCtx.getSourceURL();
            if (rmMsgCtx.getFrom() != null)
                csAddrHeaders.setFrom(new EndpointReference(rmMsgCtx.getFrom()));
            else
                csAddrHeaders.setFrom(new EndpointReference(sourceURL));
            if (rmMsgCtx.isHasResponse()) {
                if (rmMsgCtx.getReplyTo() != null)
                    csAddrHeaders.setReplyTo(new EndpointReference(rmMsgCtx.getReplyTo()));
                else
                    csAddrHeaders.setReplyTo(new EndpointReference(sourceURL));
            }
            if (rmMsgCtx.getFaultTo() != null)
                csAddrHeaders.setFaultTo(new EndpointReference(rmMsgCtx.getFaultTo()));
            else
                csAddrHeaders.setFaultTo(new EndpointReference(sourceURL));
        }
        return csAddrHeaders;
    }

    public static MessageContext cloneMsgContext(MessageContext msgContext) throws AxisFault {
        MessageContext clone = new MessageContext(msgContext.getAxisEngine());
        String str = msgContext.getRequestMessage().getSOAPPartAsString();
        Message msg = new Message(str);
        clone.setRequestMessage(msg);
        RMMessageContext.copyMessageContext(msgContext, clone);
        return clone;
    }

}