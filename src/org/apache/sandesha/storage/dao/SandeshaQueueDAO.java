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
package org.apache.sandesha.storage.dao;

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.queue.QueueException;
import org.apache.sandesha.storage.queue.SandeshaQueue;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaQueueDAO implements ISandeshaDAO {

    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());
    private byte endPoint;

    public SandeshaQueueDAO(byte endPoint) {
        super();
        this.endPoint = endPoint;
    }


    public boolean addIncomingSequence(String sequenceId) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            sq.createNewIncomingSequence(sequenceId);
            result = true;
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return result;
    }

    public boolean addPriorityMessage(RMMessageContext msg) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            sq.addPriorityMessage(msg);
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return result;
    }

    public RMMessageContext getNextPriorityMessageContextToSend() {
        RMMessageContext msg = null;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            msg = sq.nextPriorityMessageToSend();
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return msg;
    }

    public boolean addMessageToIncomingSequence(String sequenceId, Long msgNo,
                                                RMMessageContext rmMessageContext) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            sq.addMessageToIncomingSequence(sequenceId, msgNo, rmMessageContext);
            result = true;
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return result;
    }

    public boolean isIncomingSequenceExists(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        boolean exists = sq.isIncomingSequenceExists(sequenceId);
        return exists;
    }

    public RMMessageContext getNextMsgContextToProcess(Object sequence) {

        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        RMMessageContext msg = null;
        try {
            msg = sq.nextIncomingMessageToProcess(sequence);
        } catch (Exception e) {
            SandeshaQueueDAO.log.error(e);
        }
        return msg;
    }

    public Object getRandomSeqToProcess() {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        List seqs = sq.nextAllSeqsToProcess();
        int size = seqs.size();
        if (size <= 0)
            return null;
        Random r = new Random();
        int number = r.nextInt(size);

        return seqs.get(number);
    }


    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getAllReceivedMsgNumsOfIncomingSeq(sequenceId);
    }

    public boolean isIncomingMessageExists(String sequenceId, Long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isIncomingMessageExists(sequenceId, msgNo);
    }

    public boolean addMessageToOutgoingSequence(String sequenceId,
                                                RMMessageContext rmMessageContext) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            sq.addMessageToOutgoingSequence(sequenceId, rmMessageContext);
            result = true;
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return result;
    }

    public boolean addOutgoingSequence(String sequenceId) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            sq.createNewOutgoingSequence(sequenceId);
            result = true;
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return result;
    }

    public boolean isOutgoingSequenceExists(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        boolean exists = sq.isOutgoingSequenceExists(sequenceId);
        return exists;
    }

    public RMMessageContext getNextOutgoingMsgContextToSend() {
        RMMessageContext msg = null;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
            msg = sq.nextOutgoingMessageToSend();
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }
        return msg;
    }

    public void setOutSequence(String seqId, String outSeqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.setOutSequence(seqId, outSeqId);
    }

    public void setOutSequenceApproved(String seqId, boolean approved) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.setOutSequenceApproved(seqId, approved);

    }

    public String getSequenceOfOutSequence(String outsequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getSequenceOfOutSequence(outsequenceId);
    }

    public void removeCreateSequenceMsg(String messageId) {
        // TODO Auto-generated method stub
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.movePriorityMsgToBin(messageId);
    }

    public long getNextOutgoingMessageNumber(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getNextOutgoingMessageNumber(sequenceId);
    }

    //Only for client.
    public RMMessageContext checkForResponseMessage(String requestId, String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        RMMessageContext msg = sq.checkForResponseMessage(requestId, seqId);
        return msg;
    }

    public String searchForSequenceId(String messageId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        String seqId = sq.searchForSequenceId(messageId);
        return seqId;
    }


    /* 
     * 
     * 
     */
    public void markOutgoingMessageToDelete(String seqId, Long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.markOutgoingMessageToDelete(seqId, msgNo);
    }


    public void addLowPriorityMessage(RMMessageContext msg) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        try {
            sq.addLowPriorityMessage(msg);
        } catch (QueueException e) {
            SandeshaQueueDAO.log.error(e);
        }

    }

    public RMMessageContext getNextLowPriorityMessageContextToSend() {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        try {
            return sq.getLowPriorityMessageIfAcked();
        } catch (Exception e) {
            SandeshaQueueDAO.log.error(e);
        }

        return null;
    }

    public void setAckReceived(String seqId, long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.setAckReceived(seqId, msgNo);
    }

    public void addSendMsgNo(String seqId, long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.addSendMsgNo(seqId, msgNo);
    }

    public boolean isSentMsg(String seqId, long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isSentMsg(seqId, msgNo);
    }

    public boolean hasLastIncomingMsgReceived(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.hasLastIncomingMsgReceived(seqId);
    }

    public long getLastIncomingMsgNo(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getLastIncomingMsgNo(seqId);
    }

    public void addRequestedSequence(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.addRequestedSequence(seqId);
    }

    public boolean isRequestedSeqPresent(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isRequestedSeqPresent(seqId);
    }

    public String getKeyFromIncomingSequenceId(String seqID) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getKeyFromIncomingSequenceId(seqID);
    }

    public String getKeyFromOutgoingSequenceId(String seqID) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getKeyFromOutgoingSequenceId(seqID);
    }

    public boolean isAllOutgoingTerminateSent() {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isAllOutgoingTerminateSent();
    }

    public boolean isAllIncommingTerminateReceived() {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isAllIncommingTerminateReceived();
    }

    public void setTerminateSend(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.setTerminateSend(seqId);
    }

    public void setTerminateReceived(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.setTerminateReceived(seqId);
    }


    public void setAcksTo(String seqId, String acksTo) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.setAcksTo(seqId, acksTo);
    }

    public String getAcksTo(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getAcksTo(seqId);
    }

    public void addOffer(String msgID, String offerID) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.addOffer(msgID, offerID);
    }

    public String getOffer(String msgID) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.getOffer(msgID);
    }

    public void clear() {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.clear(true);
    }

    public boolean isOutgoingTerminateSent(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isOutgoingTerminateSent(seqId);
    }

    public boolean isIncommingTerminateReceived(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        return sq.isIncommingTerminateReceived(seqId);
    }

    public void updateFinalMessageArrivedTime(String sequenceID) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.updateFinalMessageArrivedTime(sequenceID);
    }

    public void sendAck(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.sendAck(sequenceId);
    }

    public void removeAllAcks(String sequenceID) {
        SandeshaQueue sq = SandeshaQueue.getInstance(endPoint);
        sq.removeAllAcks(sequenceID);
    }
}
