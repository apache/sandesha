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
package org.apache.sandesha.server.msgprocessors;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.CreateSequence;
import org.apache.sandesha.ws.rm.Identifier;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.SequenceOffer;

/**
 * @author
 */
public class CreateSequenceProcessor implements IRMMessageProcessor {
    IStorageManager storageManager;

    public static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    public CreateSequenceProcessor(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {

        AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();

        if (rmHeaders.getSequenceAcknowledgement() != null) {
            processForAckIfAny(rmMessageContext);
        }

        //wsrm:CreateSequenceRefused
        if (rmHeaders.getCreateSequence() == null)
            throw new AxisFault();

        String seqId =Constants.UUID+ uuidGen.nextUUID();
        storageManager.addRequestedSequence(seqId);


        CreateSequence createSeq = rmMessageContext.getRMHeaders().getCreateSequence();
        //Support AcksTo
        if(createSeq.getAcksTo()!=null){
            storageManager.setAcksTo(seqId,createSeq.getAcksTo().getAddress().toString());
        }

         //Support offer
        SequenceOffer offer = createSeq.getOffer();
        boolean offerAccepted = false;
        boolean hasOffer=false;
        if (offer != null) {
            hasOffer=true;
            offerAccepted = acceptOrRejectOffer(offer);
            if (offerAccepted) {
                String responseSeqId = null;
                if (offer != null) {
                    Identifier id = offer.getIdentifier();
                    if (id != null)
                        responseSeqId = id.getIdentifier();
                }
                String incomingSeqId = seqId;
                if (responseSeqId != null) {
                    storageManager.addOutgoingSequence(incomingSeqId);
                    storageManager.setTemporaryOutSequence(incomingSeqId, responseSeqId);
                    storageManager.setApprovedOutSequence(responseSeqId, responseSeqId);
                    //Now it has a approved out sequence of responseSeqId
                }
            }
        }

        //END OFFER PROCESSING
        setOutGoingAddress(rmMessageContext, addrHeaders);

        SOAPEnvelope resEnvelope = null;
        try {
            resEnvelope = EnvelopeCreator.createCreateSequenceResponseEnvelope(seqId, rmMessageContext,hasOffer,offerAccepted);
        } catch (Exception e) {
            throw new AxisFault(org.apache.sandesha.Constants.FaultCodes.WSRM_SERVER_INTERNAL_ERROR);
        }
        rmMessageContext.setMessageType(org.apache.sandesha.Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE);

        if (rmMessageContext.getSync()) {
            rmMessageContext.getMsgContext().setResponseMessage(new Message(resEnvelope));
            return true;

        } else {
            MessageContext msgContext = new MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
            msgContext.setResponseMessage(new Message(resEnvelope));
            rmMessageContext.setMsgContext(msgContext);

            rmMessageContext.setSync(false);
            storageManager.addCreateSequenceResponse(rmMessageContext);
            return false;
        }

    }

    private void handleOffer(){

    }

    //TODO  Implent the strategy for accepting the offer or rejecting it.
    private boolean acceptOrRejectOffer(SequenceOffer offer) {
        return true;
    }

    private void processForAckIfAny(RMMessageContext rmMsgCtx) throws AxisFault {
        AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor(this.storageManager);
        ackProcessor.processMessage(rmMsgCtx);
    }

    private void setOutGoingAddress(RMMessageContext rmMsgCtx, AddressingHeaders addrHeaders) throws AxisFault{
        CreateSequence createSeq = rmMsgCtx.getRMHeaders().getCreateSequence();
//        if ((createSeq.getAcksTo() != null)) {
//            if ((createSeq.getAcksTo().getAddress().toString().equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS))) {
//                rmMsgCtx.setSync(true);
//            } else {
//                rmMsgCtx.setSync(false);
//                rmMsgCtx.setOutGoingAddress(createSeq.getAcksTo().getAddress().toString());
//            }
//        } else
         if (addrHeaders.getReplyTo() == null || addrHeaders.getReplyTo().getAddress().toString().equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS)) {
            rmMsgCtx.setSync(true);
        } else {
            rmMsgCtx.setSync(false);
            rmMsgCtx.setOutGoingAddress(addrHeaders.getReplyTo().getAddress().toString());
        }

    }
}