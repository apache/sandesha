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
import org.apache.sandesha.storage.queue.SandeshaQueue;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaDatabaseDAO implements ISandeshaDAO {
       private byte endPoint;
    SandeshaDatabaseDAO(byte endPoint){
        this.endPoint= endPoint;
    }

    public String getAcksTo(String seqId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addOffer(String msgID, String offerID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setOffer(String seqID, String Offer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getOffer(String seqID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAcksTo(String seqId, String acksTo) {
        // TODO Auto-generated method stub

    }

    public String getFirstCreateSequenceMsgId(String createSeqId) {
        // TODO Auto-generated method stub
        return null;
    }
    private static final Log log = LogFactory.getLog(SandeshaDatabaseDAO.class.getName());

    public void setAckReceived(String seqId, long msgNo) {
        // TODO Auto-generated method stub
    }

    public void setResponseReceived(RMMessageContext msg) {
        // TODO Auto-generated method stub
    }

    public boolean isOutgoingTerminateSent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isIncommingTerminateReceived() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addRequestedSequence(String seqId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSeqPresent(String seqId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void markOutgoingMessageToDelete(String seqId, Long msgNo) {
        // TODO Auto-generated method stub
    }

    public boolean addIncomingSequence(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addPriorityMessage(RMMessageContext msg) {
        // TODO Auto-generated method stub
        return false;
    }

    public RMMessageContext getNextMessageToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean addMessageToIncomingSequence(String sequenceId, Long msgNo,
                                                RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isIncomingSequenceExists(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    public RMMessageContext getNextMsgContextToProcess(String sequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasNewMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getRandomSeqIdToProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isIncomingMessageExists(String sequenceId, Long msgNo) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addMessageToOutgoingSequence(String sequenceId,
                                                RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addOutgoingSequence(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    public Set getAllReceivedMsgNumsOfOutgoingSeq(String sequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    public RMMessageContext getNextPriorityMessageContextToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    public RMMessageContext getNextOutgoingMsgContextToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isOutgoingSequenceExists(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setOutSequence(String sequenceId, String outSequenceId) {
        // TODO Auto-generated method stub

    }

    public void setOutSequenceApproved(String sequenceID, boolean approved) {
        // TODO Auto-generated method stub

    }

    public String getSequenceOfOutSequence(String outsequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void moveOutgoingMessageToBin(String sequenceId, Long msgNo) {
        // TODO Auto-generated method stub

    }

    public void removeCreateSequenceMsg(String messageId) {
        // TODO Auto-generated method stub

    }

    public long getNextOutgoingMessageNumber(String sequenceId) {
        // TODO Auto-generated method stub
        return 0;
    }


    public RMMessageContext checkForResponseMessage(String requestId, String SeqId) {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean isRequestMessagePresent(String sequenceId, String msgId) {
        // TODO Auto-generated method stub
        return false;
    }

    public String searchForSequenceId(String messageId) {
        return null;
    }

    public boolean compareAcksWithSequence(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    public void addLowPriorityMessage(RMMessageContext msg) {
        // TODO Auto-generated method stub

    }

    public RMMessageContext getNextLowPriorityMessageContextToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    public void addSendMsgNo(String seqId, long msgNo) {
        // TODO Auto-generated method stub

    }

    public long getLastIncomingMsgNo(String seqId) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getLastOutgoingMsgNo(String seqId) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean hasLastIncomingMsgReceived(String seqId) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasLastOutgoingMsgReceived(String seqId) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSentMsg(String seqId, long msgNo) {
        // TODO Auto-generated method stub
        return false;
    }

    public String getKeyFromIncomingSequenceId(String seqID) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getKeyFromOutgoingSequenceId(String seqID) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getAllOutgoingSequences() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setTerminateSend(String seqId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setTerminateReceived(String seqId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAllOutgoingTerminateSent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAllIncommingTerminateReceived() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clear(){

    }

    public boolean isOutgoingTerminateSent(String seqId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isIncommingTerminateReceived(String seqId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateFinalMessageArrivedTime(String sequenceID) {
    }
    
    public void sendAck(String sequenceId) {
    }
    
    public void removeAllAcks(String sequenceID){
    }
}