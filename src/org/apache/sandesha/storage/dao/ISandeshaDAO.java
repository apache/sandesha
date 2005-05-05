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
import org.apache.sandesha.storage.queue.IncomingSequence;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public interface ISandeshaDAO {
    boolean addIncomingSequence(String sequenceId);

    boolean addOutgoingSequence(String sequenceId);

    boolean addPriorityMessage(RMMessageContext message);

    RMMessageContext getNextPriorityMessageContextToSend();

    boolean addMessageToIncomingSequence(String sequenceId, Long msgNo,
                                         RMMessageContext rmMessageContext);

    boolean addMessageToOutgoingSequence(String sequenceId,
                                         RMMessageContext rmMessageContext);

    boolean isIncomingSequenceExists(String sequenceId);

    boolean isOutgoingSequenceExists(String sequenceId);

    boolean isIncomingMessageExists(String sequenceId, Long msgNo);

    RMMessageContext getNextMsgContextToProcess(String sequenceId);

    RMMessageContext getNextOutgoingMsgContextToSend();

    String getRandomSeqIdToProcess();

    Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId);

    Set getAllReceivedMsgNumsOfOutgoingSeq(String sequenceId);

    void setOutSequence(String sequenceId, String outSequenceId);

    void setOutSequenceApproved(String sequenceID, boolean approved);

    String getSequenceOfOutSequence(String outsequenceId);

    void moveOutgoingMessageToBin(String sequenceId, Long msgNo);

    void removeCreateSequenceMsg(String messageId);

    long getNextOutgoingMessageNumber(String sequenceId);

    public RMMessageContext checkForResponseMessage(String requestId, String seqId);

    public boolean isRequestMessagePresent(String sequenceId, String msgId);

    public String searchForSequenceId(String messageId);

    public void markOutgoingMessageToDelete(String sequenceId, Long msgNumber);

    public boolean compareAcksWithSequence(String sequenceId);

    public void setResponseReceived(RMMessageContext msg);

    public void setAckReceived(String seqId, long msgNo);

    public void addLowPriorityMessage(RMMessageContext msg);

    public RMMessageContext getNextLowPriorityMessageContextToSend();

    public void addSendMsgNo(String seqId, long msgNo);

    public boolean isSentMsg(String seqId, long msgNo);

    public boolean hasLastOutgoingMsgReceived(String seqId);

    public long getLastOutgoingMsgNo(String seqId);

    public boolean hasLastIncomingMsgReceived(String seqId);

    public long getLastIncomingMsgNo(String seqId);

    public void addRequestedSequence(String seqId);

    public boolean isRequestedSeqPresent(String seqId);

    //The client side will not be able to have sequenceId as a key for storing request messages.
    //Since it may be not known when the user adds first message.
    //Two methods below will be used to get this key from the actual sequenceid.
    public String getKeyFromIncomingSequenceId(String incomingSeqID);

    public String getKeyFromOutgoingSequenceId(String outgoingSeqID);

    public Iterator getAllOutgoingSequences();

    public void setTerminateSend(String seqId);

    public void setTerminateReceived(String seqId);

    public boolean isAllOutgoingTerminateSent();

    public boolean isAllIncommingTerminateReceived();
    
    public String getFirstCreateSequenceMsgId(String createSeqId);

    public void setAcksTo(String seqId,String acksTo);
    
    public String getAcksTo(String seqId);
}