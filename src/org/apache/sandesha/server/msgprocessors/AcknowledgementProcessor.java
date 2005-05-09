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
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.ws.rm.AcknowledgementRange;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;

import javax.xml.soap.SOAPEnvelope;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author
 */
public final class AcknowledgementProcessor implements IRMMessageProcessor {
    private IStorageManager storageManager = null;
    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());

    public AcknowledgementProcessor(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public final boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {
        SequenceAcknowledgement seqAcknowledgement = rmMessageContext.getRMHeaders().getSequenceAcknowledgement();
        String seqID = seqAcknowledgement.getIdentifier().getIdentifier();
        List ackRanges = seqAcknowledgement.getAckRanges();
        Iterator ite = ackRanges.iterator();

        while (ite.hasNext()) {
            AcknowledgementRange ackRange = (AcknowledgementRange) ite.next();
            long msgNumber = ackRange.getMinValue();
            while (ackRange.getMaxValue() >= msgNumber) {
                if (!storageManager.isSentMsg(seqID, msgNumber)) {
                    throw new AxisFault(new javax.xml.namespace.QName(Constants.FaultCodes.WSRM_FAULT_INVALID_ACKNOWLEDGEMENT), Constants.FaultMessages.INVALID_ACKNOWLEDGEMENT, null, null);
                }
                storageManager.setAckReceived(seqID, msgNumber);
                storageManager.setAcknowledged(seqID, msgNumber);
                msgNumber++;
            }
        }
        //At the moment this return is not used.
        return false;
    }


    public boolean sendAcknowledgement(RMMessageContext rmMessageContext) throws AxisFault {
        //EnvelopCreater createAcknowledgement.  If async then add message to the queue
        //else set the response env of the messageContext.

        String seqID = rmMessageContext.getSequenceID();

        long messageNumber = rmMessageContext.getRMHeaders().getSequence().getMessageNumber().getMessageNumber();
        Map listOfMsgNumbers = storageManager.getListOfMessageNumbers(seqID);

        Vector ackRangeVector = null;
        if (listOfMsgNumbers != null) {
            ackRangeVector = getAckRangesVector(listOfMsgNumbers);
        } else {
            ackRangeVector = new Vector();
            AcknowledgementRange ackRange = new AcknowledgementRange();
            ackRange.setMaxValue(messageNumber);
            ackRange.setMinValue(messageNumber);
            ackRangeVector.add(ackRange);
        }
        RMMessageContext rmMsgContext = getAckRMMsgCtx(rmMessageContext, ackRangeVector);

        if (true == (storageManager.getAcksTo(seqID).equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS))) {
            try {
                String soapMsg = rmMsgContext.getMsgContext().getResponseMessage().getSOAPEnvelope().toString();
                rmMessageContext.getMsgContext().setResponseMessage(new Message(soapMsg));
            } catch (AxisFault af) {
                af.setFaultCodeAsString(Constants.FaultCodes.WSRM_SERVER_INTERNAL_ERROR);
                throw af;
            }
            return true;
        } else {
            //Store the asynchronize ack in the queue. The name for this queue is not yet fixed.
            storageManager.addAcknowledgement(rmMsgContext);
            return false;
        }
    }

    private  RMMessageContext getAckRMMsgCtx(RMMessageContext rmMessageContext, Vector ackRangeVector) {
        RMMessageContext rmMsgContext = new RMMessageContext();
        try {

            String to=storageManager.getAcksTo(rmMessageContext.getRMHeaders().getSequence().getIdentifier().getIdentifier());

            SOAPEnvelope ackEnvelope = EnvelopeCreator.createAcknowledgementEnvelope(rmMessageContext,to,ackRangeVector);
            //Create a new message using the ackEnvelope
            Message resMsg = new Message(ackEnvelope);
            //Create a new message context to store the ack message.
            MessageContext msgContext = new MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
            //Copy the contents of the rmMessageContext to the rmMsgContext.
            rmMessageContext.copyContents(rmMsgContext);
            //Set the response message using the Ack message
            msgContext.setResponseMessage(resMsg);
            //Set the msgContext to the rmMsgContext
            rmMsgContext.setMsgContext(msgContext);

            //Get the from address to send the Ack. Doesn't matter whether we have Sync or ASync messages.
            //If we have Sync them this property is not used.
            rmMsgContext.setOutGoingAddress(to);
            //Set the messsage type
            rmMsgContext.setMessageType(Constants.MSG_TYPE_ACKNOWLEDGEMENT);
        } catch (Exception e) {
            log.error(e);
        }
        return rmMsgContext;
    }

    /**
     * This method will split the input map with messages numbers to respectable
     * message ranges and will return a vector of AcknowledgementRange.
     *
     * @param listOfMsgNumbers
     * @return
     */
    private Vector getAckRangesVector(Map listOfMsgNumbers) {
        long min;
        long max;
        long size = listOfMsgNumbers.size();
        Vector vec = new Vector();
        boolean found = false;

        min = ((Long) listOfMsgNumbers.get(new Long(1))).longValue();
        max = min;

        if (size > 1) {
            for (long i = 1; i <= size; i++) {

                if (i + 1 > size) {
                    found = true;
                    max = ((Long) listOfMsgNumbers.get(new Long(i)))
                            .longValue();
                } else {

                    if (1 == (((Long) listOfMsgNumbers.get(new Long(i + 1)))
                            .longValue() - ((Long) listOfMsgNumbers
                            .get(new Long(i))).longValue())) {
                        max = ((Long) listOfMsgNumbers.get(new Long(i + 1)))
                                .longValue();
                        found = true;
                    } else {
                        found = false;
                        max = ((Long) listOfMsgNumbers.get(new Long(i)))
                                .longValue();
                        AcknowledgementRange ackRange = new AcknowledgementRange();
                        ackRange.setMaxValue(max);
                        ackRange.setMinValue(min);
                        vec.add(ackRange);

                        min = ((Long) listOfMsgNumbers.get(new Long(i + 1)))
                                .longValue();
                    }

                }
            }
            if (found) {
                AcknowledgementRange ackRange = new AcknowledgementRange();
                ackRange.setMaxValue(max);
                ackRange.setMinValue(min);
                vec.add(ackRange);
            }
        } else {
            AcknowledgementRange ackRange = new AcknowledgementRange();
            ackRange.setMaxValue(max);
            ackRange.setMinValue(min);
            vec.add(ackRange);
        }
        return vec;
    }
}