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

import org.apache.sandesha.RMMessageContext;

import java.util.Set;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaDatabaseDAO implements ISandeshaDAO {
    public SandeshaDatabaseDAO(byte endPoint) {
    }

    public boolean addIncomingSequence(String sequenceId) {
        return false;
    }

    public boolean addOutgoingSequence(String sequenceId) {
        return false;
    }

    public boolean addPriorityMessage(RMMessageContext message) {
        return false;
    }

    public RMMessageContext getNextPriorityMessageContextToSend() {
        return null;
    }

    public boolean addMessageToIncomingSequence(String sequenceId, Long msgNo, RMMessageContext rmMessageContext) {
        return false;
    }

    public boolean addMessageToOutgoingSequence(String sequenceId, RMMessageContext rmMessageContext) {
        return false;
    }

    public boolean isIncomingSequenceExists(String sequenceId) {
        return false;
    }

    public boolean isOutgoingSequenceExists(String sequenceId) {
        return false;
    }

    public boolean isIncomingMessageExists(String sequenceId, Long msgNo) {
        return false;
    }

    public RMMessageContext getNextMsgContextToProcess(Object seq) {
        return null;
    }

    public RMMessageContext getNextOutgoingMsgContextToSend() {
        return null;
    }

    public Object getRandomSeqToProcess() {
        return null;
    }

    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        return null;
    }

    public void setOutSequence(String sequenceId, String outSequenceId) {

    }

    public void setOutSequenceApproved(String sequenceID, boolean approved) {

    }

    public String getSequenceOfOutSequence(String outsequenceId) {
        return null;
    }

    public void removeCreateSequenceMsg(String messageId) {

    }

    public long getNextOutgoingMessageNumber(String sequenceId) {
        return 0;
    }

    public RMMessageContext checkForResponseMessage(String requestId, String seqId) {
        return null;
    }

    public String searchForSequenceId(String messageId) {
        return null;
    }

    public void markOutgoingMessageToDelete(String sequenceId, Long msgNumber) {

    }

    public void setAckReceived(String seqId, long msgNo) {

    }

    public void addLowPriorityMessage(RMMessageContext msg) {

    }

    public RMMessageContext getNextLowPriorityMessageContextToSend() {
        return null;
    }

    public void addSendMsgNo(String seqId, long msgNo) {

    }

    public boolean isSentMsg(String seqId, long msgNo) {
        return false;
    }

    public boolean hasLastIncomingMsgReceived(String seqId) {
        return false;
    }

    public long getLastIncomingMsgNo(String seqId) {
        return 0;
    }

    public void addRequestedSequence(String seqId) {

    }

    public boolean isRequestedSeqPresent(String seqId) {
        return false;
    }

    public String getKeyFromIncomingSequenceId(String incomingSeqID) {
        return null;
    }

    public String getKeyFromOutgoingSequenceId(String outgoingSeqID) {
        return null;
    }

    public void setTerminateSend(String seqId) {

    }

    public void setTerminateReceived(String seqId) {

    }

    public boolean isAllOutgoingTerminateSent() {
        return false;
    }

    public boolean isAllIncommingTerminateReceived() {
        return false;
    }

    public void setAcksTo(String seqId, String acksTo) {

    }

    public String getAcksTo(String seqId) {
        return null;
    }

    public void addOffer(String msgID, String offerID) {

    }

    public String getOffer(String msgID) {
        return null;
    }

    public void clear() {

    }

    public boolean isOutgoingTerminateSent(String seqId) {
        return false;
    }

    public boolean isIncommingTerminateReceived(String seqId) {
        return false;
    }

    public void updateFinalMessageArrivedTime(String sequenceID) {

    }

    public void sendAck(String sequenceId) {

    }

    public void removeAllAcks(String sequenceID) {

    }
}