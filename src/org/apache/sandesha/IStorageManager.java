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

import org.apache.sandesha.storage.Callback;

import java.util.Map;

/**
 * Interface for StorageManager. This is the interface for RMSource and RMDestination for the
 * Storage. Any implementation of this interface can be used as the storage for Sandesha
 */
public interface IStorageManager {
    void init();

    boolean isSequenceExist(String sequenceID);

    boolean isResponseSequenceExist(String sequenceID);

    Object getNextSeqToProcess();

    RMMessageContext getNextMessageToProcess(Object seq);

    void setAcknowledged(String seqID, long msgNumber);

    void addSequence(String sequenceID);

    void addCreateSequenceResponse(RMMessageContext rmMessageContext);

    void addCreateSequenceRequest(RMMessageContext rmMessageContext);

    void addAcknowledgement(RMMessageContext rmMessageContext);

    boolean isMessageExist(String sequenceID, long messageNumber);

    Map getListOfMessageNumbers(String sequenceID);

    RMMessageContext getNextMessageToSend();

    void setTemporaryOutSequence(String sequenceId, String outSequenceId);

    boolean setApprovedOutSequence(String oldOutsequenceId, String newOutSequenceId);

    long getNextMessageNumber(String sequenceID);

    void insertOutgoingMessage(RMMessageContext rmMessageContext);

    void insertIncomingMessage(RMMessageContext rmMessageContext);

    RMMessageContext checkForResponseMessage(String sequenceId, String requestMsgId);

    void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage);

    void setAckReceived(String seqId, long msgNo);

    void insertFault(RMMessageContext rmMsgCtx);

    void addSendMsgNo(String seqId, long msgNo);

    boolean isSentMsg(String seqId, long msgNo);

    boolean hasLastIncomingMsgReceived(String seqId);

    long getLastIncomingMsgNo(String seqId);

    void addOutgoingSequence(String seqId);

    void addIncomingSequence(String seqId);

    String getOutgoingSeqOfMsg(String msgId);

    void addRequestedSequence(String seqId);

    boolean isRequestedSeqPresent(String seqId);

    String getOutgoingSeqenceIdOfIncomingMsg(RMMessageContext msg);

    void setTerminateSend(String seqId);

    void setTerminateReceived(String seqId);

    String getKeyFromOutgoingSeqId(String seqId);

    void setAcksTo(String seqId, String acksTo);

    String getAcksTo(String seqId);

    void setCallback(Callback callBack);

    void removeCallback();

    void addOffer(String msgID, String offerID);

    String getOffer(String msgID);

    void clearStorage();

    boolean isSequenceComplete(String seqId);

    void sendAck(String sequenceId);
}