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
package org.apache.sandesha;

import org.apache.axis.Message;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPBody;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.*;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.sandesha.ws.rm.*;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeaderElement;
import java.util.Vector;

/**
 * This class creates the SOAPEnvelopes using the RMMessageContexts passed-in to various
 * methods. All the methods follow a simple process of creating SOAPEnvelopes using the
 * WS-Addressing and, WS-RM headers and Body Elements.
 *
 * @author Jaliya Ekanayake
 * @auther Chamikara Jayalath
 */
public class EnvelopeCreator {
    private static final Log log = LogFactory.getLog(EnvelopeCreator.class.getName());
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    public static SOAPEnvelope createCreateSequenceResponseEnvelope(String seqId,
                                                                    RMMessageContext rmMessageContext,
                                                                    boolean hasOffer,
                                                                    boolean offerAccepted)
            throws Exception {

        AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
        SOAPEnvelope envelope = createBasicEnvelop();

        AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(envelope);
        Action action = new Action(new URI(Constants.WSRM.ACTION_CREATE_SEQUENCE_RESPONSE));

        outGoingAddressingHaders.setAction(action);

        MessageID messageId = new MessageID(new URI(Constants.UUID + uuidGen.nextUUID()));
        outGoingAddressingHaders.setMessageID(messageId);

        To incommingTo = addressingHeaders.getTo();
        URI fromAddressURI = new URI(incommingTo.toString());

        Address fromAddress = new Address(fromAddressURI);
        From from = new From(fromAddress);
        outGoingAddressingHaders.setFrom(from);

        //RelatesTo
        MessageID incommingMessageId = addressingHeaders.getMessageID();
        outGoingAddressingHaders.addRelatesTo(incommingMessageId.toString(), new QName(
                org.apache.axis.message.addressing.Constants.NS_PREFIX_ADDRESSING,
                org.apache.axis.message.addressing.Constants.NS_URI_ADDRESSING_DEFAULT));

        //SettingTo
        AttributedURI incommingAddress = null;
        if (!rmMessageContext.getSync()) {
            if (addressingHeaders.getReplyTo() != null) {
                ReplyTo incommingReplyTo = (ReplyTo) addressingHeaders.getReplyTo();
                incommingAddress = incommingReplyTo.getAddress();
            }
            //            else if (rmHeaders.getCreateSequence().getAcksTo() != null) {
            //                AcksTo incommingAcksTo = (AcksTo) rmHeaders.getCreateSequence().getAcksTo();
            //                incommingAddress = incommingAcksTo.getAddress();
            //            }

            To to = new To(new URI(incommingAddress.toString()));
            outGoingAddressingHaders.setTo(to);
        }
        outGoingAddressingHaders.toEnvelope(envelope, null);

        CreateSequenceResponse response = new CreateSequenceResponse();
        Identifier id = new Identifier();
        id.setIdentifier(seqId);
        response.setIdentifier(id);

        if (hasOffer && offerAccepted) {
            Accept accept = new Accept();
            AcksTo acksTo = new AcksTo();
            acksTo.setAddress(new Address(addressingHeaders.getTo().toString()));
            accept.setAcksTo(acksTo);
            response.setAccept(accept);
        }

        response.toSoapEnvelop(envelope);
        return envelope;
    }


    public static SOAPEnvelope createCreateSequenceEnvelope(RMMessageContext rmMsgCtx)
            throws Exception {

        AddressingHeaders addressingHeaders = rmMsgCtx.getAddressingHeaders();
        SOAPEnvelope envelope = createBasicEnvelop();
        addressingHeaders.toEnvelope(envelope);
        CreateSequence crSeq = rmMsgCtx.getRMHeaders().getCreateSequence();

        crSeq.toSoapEnvelop(envelope);
        return envelope;
    }

    public static SOAPEnvelope createBasicEnvelop() throws Exception {

        SOAPEnvelope soapEnv = new SOAPEnvelope();
        addNamespaceDeclarations(soapEnv);
        return soapEnv;
    }

    public static void addNamespaceDeclarations(SOAPEnvelope soapEnv) throws Exception {

        soapEnv.addNamespaceDeclaration(Constants.WSRM.NS_PREFIX_RM, Constants.WSRM.NS_URI_RM);
        soapEnv.addNamespaceDeclaration(
                org.apache.axis.message.addressing.Constants.NS_PREFIX_ADDRESSING,
                org.apache.axis.message.addressing.Constants.NS_URI_ADDRESSING_DEFAULT);
        soapEnv.addNamespaceDeclaration(Constants.WSRM.NS_PREFIX_RM, Constants.WSRM.NS_URI_RM);
    }

    public static SOAPEnvelope createAcknowledgementEnvelope(RMMessageContext rmMessageContext,
                                                             String toAddress,
                                                             Vector ackRangeVector)
            throws Exception {

        AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
        SOAPEnvelope envelope = createBasicEnvelop();

        AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(envelope);
        Action action = new Action(new URI(Constants.WSRM.SEQUENCE_ACKNOWLEDGEMENT_ACTION));

        SOAPHeaderElement actionElement = action.toSOAPHeaderElement(envelope, null);
        outGoingAddressingHaders.setAction(action);

        MessageID messageId = new MessageID(new URI(Constants.UUID + uuidGen.nextUUID()));
        outGoingAddressingHaders.setMessageID(messageId);

        To incommingTo = addressingHeaders.getTo();
        URI fromAddressURI = new URI(incommingTo.toString());

        Address fromAddress = new Address(fromAddressURI);
        From from = new From(fromAddress);
        outGoingAddressingHaders.setFrom(from);

        To to = new To(new URI(toAddress));
        outGoingAddressingHaders.setTo(to);
        outGoingAddressingHaders.toEnvelope(envelope, null);

        SequenceAcknowledgement seqAck = new SequenceAcknowledgement();
        seqAck.setAckRanges(ackRangeVector);
        seqAck.setIdentifier(rmMessageContext.getRMHeaders().getSequence().getIdentifier());
        seqAck.toSoapEnvelop(envelope);

        return envelope;
    }


    public static SOAPEnvelope createServiceResponseEnvelope(RMMessageContext rmMessageContext)
            throws Exception {
        AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
        SOAPEnvelope responseEnvelope = createBasicEnvelop();

        AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(responseEnvelope);

        Identifier seqId = new Identifier();
        seqId.setIdentifier(rmMessageContext.getSequenceID());
        Sequence seq = new Sequence();
        seq.setIdentifier(seqId);

        MessageNumber msgNumber = new MessageNumber();
        msgNumber.setMessageNumber(rmMessageContext.getMsgNumber());

        seq.setMessageNumber(msgNumber);
        if (rmMessageContext.isLastMessage()) {
            seq.setLastMessage(new LastMessage());
        }


        seq.toSoapEnvelop(responseEnvelope);

        if (rmMessageContext.getReTransmissionCount() > 0) {
            AckRequested ackReq = new AckRequested();
            ackReq.setIdentifier(seqId);
            ackReq.toSoapEnvelop(responseEnvelope);
        }


        if (rmMessageContext.getOldSequenceID() != null)
            outGoingAddressingHaders.addRelatesTo(rmMessageContext.getMessageID(), null);

        //Set the messageID
        MessageID messageId = new MessageID(new URI(Constants.UUID + uuidGen.nextUUID()));
        outGoingAddressingHaders.setMessageID(messageId);

        //Set the <wsa:From> address from the incoming <wsa:To>
        To incommingTo = addressingHeaders.getTo();
        URI fromAddressURI = new URI(incommingTo.toString());

        Address fromAddress = new Address(fromAddressURI);
        From from = new From(fromAddress);
        outGoingAddressingHaders.setFrom(from);

        if (addressingHeaders.getReplyTo() == null)
            throw new Exception("ReplyTo is required to send Responses");
        AttributedURI inReplyTo = addressingHeaders.getReplyTo().getAddress();
        To to = new To(new URI(inReplyTo.toString()));
        outGoingAddressingHaders.setTo(to);

        if (addressingHeaders.getAction() != null) {
            Action action = new Action(new URI(addressingHeaders.getAction().toString()));
            outGoingAddressingHaders.setAction(action);
        }

        outGoingAddressingHaders.toEnvelope(responseEnvelope, null);
        responseEnvelope.setBody((SOAPBody) rmMessageContext.getMsgContext().getResponseMessage()
                .getSOAPEnvelope().getBody());

        return responseEnvelope;
    }


    public static SOAPEnvelope createServiceRequestEnvelope(RMMessageContext rmMessageContext)
            throws Exception {
        SOAPEnvelope requestEnvelope = null;
        AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
        RMHeaders rmHeaders = new RMHeaders();

        Sequence seq = new Sequence();
        Identifier id = new Identifier();
        id.setIdentifier(rmMessageContext.getSequenceID());
        seq.setIdentifier(id);

        if (rmMessageContext.getReTransmissionCount() > 0) {
            AckRequested ackReq = new AckRequested();
            ackReq.setIdentifier(id);
            rmHeaders.setAckRequest(ackReq);
        }

        if (rmMessageContext.isLastMessage()) {
            seq.setLastMessage(new LastMessage());
        }

        MessageNumber msgNumber = new MessageNumber();
        msgNumber.setMessageNumber(rmMessageContext.getMsgNumber());
        seq.setMessageNumber(msgNumber);

        rmHeaders.setSequence(seq);
        rmMessageContext.setRMHeaders(rmHeaders);

        String str = rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
        requestEnvelope = new Message(str).getSOAPEnvelope();

        rmMessageContext.getRMHeaders().toSoapEnvelop(requestEnvelope);
        AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(requestEnvelope);

        MessageID messageId = new MessageID(new URI(rmMessageContext.getMessageID()));
        outGoingAddressingHaders.setMessageID(messageId);

        outGoingAddressingHaders.setFrom(addressingHeaders.getFrom());
        outGoingAddressingHaders.setTo(addressingHeaders.getTo());

        if (addressingHeaders.getReplyTo() != null)
            outGoingAddressingHaders.setReplyTo(addressingHeaders.getReplyTo());

        if (addressingHeaders.getFaultTo() != null)
            outGoingAddressingHaders.setFaultTo(addressingHeaders.getFaultTo());
        try {
            Action action = new Action(new URI(rmMessageContext.getAction()));
            outGoingAddressingHaders.setAction(action);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rmMessageContext.getFaultTo() != null)
            outGoingAddressingHaders.setFaultTo(
                    new FaultTo(new Address(rmMessageContext.getFaultTo())));

        outGoingAddressingHaders.toEnvelope(requestEnvelope, null);

        return requestEnvelope;
    }


    public static SOAPEnvelope createTerminatSeqMessage(RMMessageContext rmMessageContext)
            throws Exception {
        AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
        SOAPEnvelope terSeqEnv = createBasicEnvelop();

        AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(terSeqEnv);
        Identifier seqId = new Identifier();
        seqId.setIdentifier(rmMessageContext.getSequenceID());

        Action terSeqAction = new Action(new URI(Constants.WSRM.ACTION_TERMINATE_SEQUENCE));
        outGoingAddressingHaders.setAction(terSeqAction);

        MessageID messageId = new MessageID(new URI(Constants.UUID + uuidGen.nextUUID()));
        outGoingAddressingHaders.setMessageID(messageId);

        outGoingAddressingHaders.setFrom(addressingHeaders.getFrom());
        outGoingAddressingHaders.setTo(addressingHeaders.getTo());

        if (addressingHeaders.getReplyTo() != null)
            outGoingAddressingHaders.setReplyTo(addressingHeaders.getReplyTo());
        if (addressingHeaders.getFaultTo() != null)
            outGoingAddressingHaders.setFaultTo(addressingHeaders.getFaultTo());

        outGoingAddressingHaders.toEnvelope(terSeqEnv, null);

        TerminateSequence terSeq = new TerminateSequence();
        terSeq.setIdentifier(seqId);
        terSeq.toSoapEnvelop(terSeqEnv);

        return terSeqEnv;
    }

}