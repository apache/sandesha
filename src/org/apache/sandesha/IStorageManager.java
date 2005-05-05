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

import java.util.Iterator;
import java.util.Map;

import org.apache.sandesha.storage.queue.IncomingSequence;

/**
 * @author
 */
public interface IStorageManager {
    public void init();

    //This will be used by the processors to insert a request message
    //Here a decision need to be made whether we use the sequence already
    // created or need to
    //create a new sequence as there are no sequence request by the client.

    //public void insertRequestMessage(RMMessageContext rmMessageContext);

    //This will be used by the Invoker but at the moment we don't need this
    // too.

    //public void insertResponseMessage(RMMessageContext rmMessageContext);

    //This is used by the processors.
    public boolean isSequenceExist(String sequenceID);

    public boolean isResponseSequenceExist(String sequenceID);

    //This for the invoker and need to be implemented.
    public RMMessageContext getNextMessageToProcess();

    //Remove this
    //public void setAcknowledged();

    //Use this instead
    public void setAcknowledged(String seqID, long msgNumber);

    //No need to change.
    public void addSequence(String sequenceID);

    //This is used to add create sequence responses
    //This is dealing with the new list of messages that we have discussed.
    //These messages may not need to be in order.
    public void addCreateSequenceResponse(RMMessageContext rmMessageContext);

    public void addCreateSequenceRequest(RMMessageContext rmMessageContext);

    //This is used to add addAcknowledgement
    //This is dealing with the new list of messages that we have discussed.
    //These messages may not need to be in order.
    //This method and the method above will do the same task at the moment.
    public void addAcknowledgement(RMMessageContext rmMessageContext);

    //Need to implement this
    public boolean isMessageExist(String sequenceID, long messageNumber);

    //Try to send me a sorted map as we disucssed.
    public Map getListOfMessageNumbers(String sequenceID);

    //This is used by the Sender to get the response messages from the response
    // queue;
    //public RMMessageContext getNextResponseMessageToSend();

    //This is used by the Sender to get the non-application response type
    // messages.
    //e.g. CreateSequence, CreateSequenceResponse, AsynchronousAcks
    public RMMessageContext getNextMessageToSend();

    //These two methods were added to deal with the out sequence.
    //First Server sender generates and sets a temporari out sequence to a
    // responseSequence.
    //Then it sends a CreateSequenceMessage setting this as the Message ID.
    //When the RMProvider receives is sets the correct outSequence ID.
    // (Using RelatesTo).
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId);

    public boolean setApprovedOutSequence(String oldOutsequenceId,
                                          String newOutSequenceId);

    //In the server side we will get the appropriate message number
    //In the client side this will start from 1
    //But the sequenceID is neglected.
    public long getNextMessageNumber(String sequenceID);

    //This is used by the client when storing the request messages.

    //public void insertClientRequestMessage(RMMessageContext
    // rmMessageContext);

    public void insertOutgoingMessage(RMMessageContext rmMessageContext);

    public void insertIncomingMessage(RMMessageContext rmMessageContext);

    public RMMessageContext checkForResponseMessage(String sequenceId, String requestMsgId);

    public boolean checkForAcknowledgement(String sequenceId, String requestMsgId);


    public boolean isAckComplete(String sequenceID);

    public void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage);

    public boolean isAllSequenceComplete();

    public boolean isResponseComplete(String sequenceID);

    public void terminateSequence(String sequenceID);

    public void setAckReceived(String seqId, long msgNo);

    public void insertFault(RMMessageContext rmMsgCtx);

    public void addSendMsgNo(String seqId, long msgNo);

    public boolean isSentMsg(String seqId, long msgNo);

    public boolean hasLastOutgoingMsgReceived(String seqId);

    public long getLastOutgoingMsgNo(String seqId);

    public boolean hasLastIncomingMsgReceived(String seqId);

    public long getLastIncomingMsgNo(String seqId);

    public void addOutgoingSequence(String seqId);

    public void addIncomingSequence(String seqId);

    public String getOutgoingSeqOfMsg(String msgId);

    public void addRequestedSequence(String seqId);

    public boolean isRequestedSeqPresent(String seqId);

    public String getOutgoingSeqenceIdOfIncomingMsg(RMMessageContext msg);

    public void setTerminateSend(String seqId);

    public void setTerminateReceived(String seqId);

    public String getKeyFromOutgoingSeqId(String seqId);
    
    public void setAcksTo(String seqId,String acksTo);
    
    public String getAcksTo(String seqId);

}