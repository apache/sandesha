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
import org.apache.sandesha.storage.queue.IncomingSequence;

import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaQueueDAO implements ISandeshaDAO {

    protected static Log log = LogFactory
            .getLog(SandeshaQueueDAO.class.getName());

    public boolean addIncomingSequence(String sequenceId) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            sq.createNewIncomingSequence(sequenceId);
            result = true;
        } catch (QueueException e) {
            log.error(e);
        }

        return result;
    }

    public boolean addPriorityMessage(RMMessageContext msg) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            sq.addPriorityMessage(msg);
        } catch (QueueException e) {
            log.error(e);
        }

        return result;
    }

    public RMMessageContext getNextPriorityMessageContextToSend() {
        RMMessageContext msg = null;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            msg = sq.nextPriorityMessageToSend();
        } catch (QueueException e) {
            log.error(e);
            e.printStackTrace();
        }

        return msg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addMessageToSequence(java.lang.String,
     *      java.lang.Long, org.apache.sandesha.RMMessageContext)
     */
    public boolean addMessageToIncomingSequence(String sequenceId, Long msgNo,
                                                RMMessageContext rmMessageContext) {

        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            sq.addMessageToIncomingSequence(sequenceId, msgNo,
                    rmMessageContext);
            result = true;
        } catch (QueueException e) {
            log.error(e);
            e.printStackTrace();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isSequenceExists(java.lang.String)
     */
    public boolean isIncomingSequenceExists(String sequenceId) {

        SandeshaQueue sq = SandeshaQueue.getInstance();
        boolean exists = sq.isIncomingSequenceExists(sequenceId);

        return exists;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextMsgContextToProcess(java.lang.String)
     */
    public RMMessageContext getNextMsgContextToProcess(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();

        RMMessageContext msg = null;
        try {
            msg = sq.nextIncomingMessageToProcess(sequenceId);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }

        return msg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#hasNewMessages()
     */
    public boolean hasNewMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getRandomSeqIdToProcess()
     */
    public String getRandomSeqIdToProcess() {
        // TODO Auto-generated method stub
        SandeshaQueue sq = SandeshaQueue.getInstance();
        Vector ids = sq.nextAllSeqIdsToProcess();

        int size = ids.size();

        if (size <= 0)
            return null;

        Random r = new Random();
        int number = r.nextInt(size);
        String id = (String) ids.get(number);

        sq.setSequenceLock(id, true); //Locks the sequence temporarily to tell
        // that it is locked.
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getAllReceivedMsgNumsOfSeq(java.lang.String)
     */
    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {

        SandeshaQueue sq = SandeshaQueue.getInstance();
        return sq.getAllReceivedMsgNumsOfIncomingSeq(sequenceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isMessageExists(java.lang.String,
     *      java.lang.String)
     */
    public boolean isIncomingMessageExists(String sequenceId, Long msgNo) {

        SandeshaQueue sq = SandeshaQueue.getInstance();
        return sq.isIncomingMessageExists(sequenceId, msgNo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addMessageToResponseSequence(java.lang.String,
     *      org.apache.sandesha.RMMessageContext)
     */
    public boolean addMessageToOutgoingSequence(String sequenceId,
                                                RMMessageContext rmMessageContext) {

        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            sq.addMessageToOutgoingSequence(sequenceId, rmMessageContext);
            result = true;
        } catch (QueueException e) {
            log.error(e);
            e.printStackTrace();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addResponseSequence(java.lang.String)
     */
    public boolean addOutgoingSequence(String sequenceId) {
        boolean result = false;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            sq.createNewOutgoingSequence(sequenceId);
            result = true;
        } catch (QueueException e) {
            log.error(e);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isResponseSequenceExists(java.lang.String)
     */
    public boolean isOutgoingSequenceExists(String sequenceId) {

        SandeshaQueue sq = SandeshaQueue.getInstance();
        boolean exists = sq.isOutgoingSequenceExists(sequenceId);

        return exists;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextResponseMsgContextToProcess(java.lang.String)
     */
    /*
     * public RMMessageContext getNextResponseMsgContextToSend(String
     * sequenceId) {
     * 
     * SandeshaQueue sq = SandeshaQueue.getInstance();
     * 
     * RMMessageContext msg = null; try{ msg =
     * sq.nextResponseMessageToSend(sequenceId); }catch(Exception e){
     * log.error(e); e.printStackTrace(); }
     * 
     * return msg; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getAllReceivedMsgNumsOfResponseSeq(java.lang.String)
     */
    public Set getAllReceivedMsgNumsOfOutgoingSeq(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        return sq.getAllReceivedMsgNumsOfOutgoingSeq(sequenceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getRandomResponseSeqIdToSend()
     */
    /*
     * public String getRandomResponseSeqIdToSend() { // TODO Auto-generated
     * method stub SandeshaQueue sq = SandeshaQueue.getInstance(); Vector ids =
     * sq.nextAllResponseSeqIdsToSend();
     * 
     * int size = ids.size();
     * 
     * if(size <=0) return null;
     * 
     * Random r = new Random(); int number = r.nextInt(size); String id =
     * (String) ids.get(number);
     * 
     * sq.setSequenceLock(id,true); //Locks the sequence temporarily to tell
     * that it is locked. return id; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextResponseMsgContextToSend()
     */
    public RMMessageContext getNextOutgoingMsgContextToSend() {
        RMMessageContext msg = null;
        try {
            SandeshaQueue sq = SandeshaQueue.getInstance();
            msg = sq.nextOutgoingMessageToSend();
        } catch (QueueException e) {
            log.error(e);
        }
        return msg;
    }

    public void setOutSequence(String seqId, String outSeqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.setOutSequence(seqId, outSeqId);

    }

    public void setOutSequenceApproved(String seqId, boolean approved) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.setOutSequenceApproved(seqId, approved);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getSequenceOfOutSequence(java.lang.String)
     */
    public String getSequenceOfOutSequence(String outsequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        return sq.getSequenceOfOutSequence(outsequenceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#moveResponseMessageToBin(java.lang.String,
     *      java.lang.Long)
     */
    public void moveOutgoingMessageToBin(String sequenceId, Long msgNo) {
        // TODO Auto-generated method stub
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.moveOutgoingMsgToBin(sequenceId, msgNo);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#removeCreateSequenceMsg(java.lang.String)
     */
    public void removeCreateSequenceMsg(String messageId) {
        // TODO Auto-generated method stub
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.movePriorityMsgToBin(messageId);

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextOutgoingMessageNumber(java.lang.String)
     */
    public long getNextOutgoingMessageNumber(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        return sq.getNextOutgoingMessageNumber(sequenceId);
    }

    //Only for client.
    public RMMessageContext checkForResponseMessage(String requestId, String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        RMMessageContext msg = sq.checkForResponseMessage(requestId, seqId);
        return msg;
    }

    public boolean isRequestMessagePresent(String sequenceId, String msgId) {
        // TODO Auto-generated method stub
        SandeshaQueue sq = SandeshaQueue.getInstance();
        boolean p = sq.isRequestMsgPresent(sequenceId, msgId);
        return p;

    }

    public String searchForSequenceId(String messageId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        String seqId = sq.searchForSequenceId(messageId);
        return seqId;
    }


    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#markOutgoingMessageToDelete(java.lang.String, java.lang.Long)
     */
    public void markOutgoingMessageToDelete(String seqId, Long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.markOutgoingMessageToDelete(seqId, msgNo);
    }


    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isAckComplete(java.lang.String)
     */
    public boolean compareAcksWithSequence(String sequenceId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        Vector acks = sq.getAllAckedMsgNumbers(sequenceId);
        Vector outGoingMsgs = sq.getAllOutgoingMsgNumbers(sequenceId);

        if (acks.size() < outGoingMsgs.size()) //Size must be equal (number of msgs=number of acks)
            return false;

        boolean result = true;
        for (int i = 0; i < outGoingMsgs.size(); i++) {
            if (!acks.contains(outGoingMsgs.get(i))) {

                //System.out.println("result false "+outGoingMsgs.get(i));
                result = false;
                break;
            }
        }

        return result;
    }

    public void setResponseReceived(RMMessageContext msg) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.setResponseReceived(msg);
    }


    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addLowPriorityMessage(org.apache.sandesha.RMMessageContext)
     */
    public void addLowPriorityMessage(RMMessageContext msg) {
        SandeshaQueue sq = SandeshaQueue.getInstance();

        try {
            sq.addLowPriorityMessage(msg);
        } catch (QueueException e) {
            log.error(e);
        }

    }


    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextLowPriorityMessageContextToSend()
     */
    public RMMessageContext getNextLowPriorityMessageContextToSend() {

        SandeshaQueue sq = SandeshaQueue.getInstance();
        try {
            return sq.getLowPriorityMessageIfAcked();
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

    public void setAckReceived(String seqId, long msgNo) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.setAckReceived(seqId, msgNo);
    }
    
    public void addSendMsgNo(String seqId,long msgNo){
    	SandeshaQueue sq = SandeshaQueue.getInstance();
    	sq.addSendMsgNo(seqId,msgNo);
    }
    
    public boolean isSentMsg(String seqId,long msgNo){
    	SandeshaQueue sq = SandeshaQueue.getInstance();
    	return sq.isSentMsg(seqId,msgNo);
    }
    
    public boolean hasLastMsgReceived(String seqId){
    	SandeshaQueue sq = SandeshaQueue.getInstance();
    	return sq.hasLastMsgReceived(seqId);   
    }

    public long getLastMsgNo(String seqId){
    	SandeshaQueue sq = SandeshaQueue.getInstance();
    	return sq.getLastMsgNo(seqId);
    }

    public void addRequestedSequence(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.addRequestedSequence(seqId);
    }

    public boolean isRequestedSeqPresent(String seqId) {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        return sq.isRequestedSeqPresent(seqId);
    }
}
