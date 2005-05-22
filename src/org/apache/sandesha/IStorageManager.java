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

import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.queue.IncomingSequence;

/**
 * @author
 */
public interface IStorageManager {
    
    public void init();

    /**
     * Checks weather a entry for the given outgoing sequence exists in
     * the storage.
     */
    public boolean isSequenceExist(String sequenceID);

    /**
     * This checks weather there is a entry for the given incoming sequence in 
     * the storage.
     */
    public boolean isResponseSequenceExist(String sequenceID);

    /**
     * This will be used by a message processing thread to get those
     * messages.
     */
    public RMMessageContext getNextMessageToProcess();

    /**
     * This will be used  to set the acks.
     */
    public void setAcknowledged(String seqID, long msgNumber);

    /**
     * This adds a entry in the storage for holding the messages of the given outgoing 
     * sequence.
     */
    public void addSequence(String sequenceID);

    /**
     * This will be used to set the create sequence responses.
     */
    public void addCreateSequenceResponse(RMMessageContext rmMessageContext);

    /**
     * This will be used to add the create sequence request.
     */
    public void addCreateSequenceRequest(RMMessageContext rmMessageContext);

    /**
     * This will be used to add acks to the storage.
     */
    public void addAcknowledgement(RMMessageContext rmMessageContext);

    /**
     * Check the existance of a message.
     */
    public boolean isMessageExist(String sequenceID, long messageNumber);

    /**
     * This will give a Map containing message numbers of all the messages of the given sequence,
     * that are currently in the storage.
     */
    public Map getListOfMessageNumbers(String sequenceID);

    /**
     * This should be the method used by a message sender to get messages from storage.
     * This actually does not give any odering to the messages (e.g. create sequence messages hv no priority).
     * The odering should be included in the implementation.
     */
    public RMMessageContext getNextMessageToSend();

    /**
     * The function of this method is the set the outSequence value (sequence id) of a perticular sequence 
     * to a temporary value. This is necessary because the sequence id may not be present iy the time the messages
     * get added to the storage (e.g. sequnce id will only be available after a create. seq response.).  
     */
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId);

    /**
     * When the actual outSequnce id becomes available it can be set using this.
     * Notice that oldOutSequnceId should be the temporary out sequence set by the method 'setTemporaryOutSequence'
     */
    public boolean setApprovedOutSequence(String oldOutsequenceId,
                                          String newOutSequenceId);

    /**
     * This could be called to get the next message number expected by a perticular sequence.
     * This will not be very useful in the cliend side since the user may himself set the 
     * message no. But in the server side this will be very useful when setting the message no.
     */
    public long getNextMessageNumber(String sequenceID);

    /**
     * This adds the given outgoing message to the correct sequence mentioned in its property.
     */
    public void insertOutgoingMessage(RMMessageContext rmMessageContext);

    /**
     * This adds the given incoming message to the correct sequence mentioned in its property.
     */
    public void insertIncomingMessage(RMMessageContext rmMessageContext);

    /**
     * When the messageId of the request message (e.g. from relates to) and the sequence id
     * is given this will give the response message.
     */
    public RMMessageContext checkForResponseMessage(String sequenceId, String requestMsgId);

    /**
     * Checks weathe a ack has been received for the given message.
     */
    public boolean checkForAcknowledgement(String sequenceId, String requestMsgId);

    /**
     * Checks weather all the messages of all the sequences that have been started hv 
     * been acked.
     */
    public boolean isAckComplete(String sequenceID);

    /**
     * Adds a terminate sequence message to the storage.
     */
    public void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage);

    /**
     * Checks weather all the work finished.
     * If this is true we can wind up.
     */
    public boolean isAllSequenceComplete();

    
    public boolean isResponseComplete(String sequenceID);

    
    public void terminateSequence(String sequenceID);

    /**
     * Tells to the storage that the given message of given sequence was acked.
     */
    public void setAckReceived(String seqId, long msgNo);
    
    public void insertFault(RMMessageContext rmMsgCtx);

    /**
     * The message will be added to a sent list (the list holds the 
     * messages that were sent at least once)
     */
    public void addSendMsgNo(String seqId, long msgNo);

    /**
     * Checks weather the message has been sent at least once.
     */
    public boolean isSentMsg(String seqId, long msgNo);

    /**
     * Asks weather the storage has got the last message of the outgoing sequence.
     */
    public boolean hasLastOutgoingMsgReceived(String seqId);

    /**
     * Gets the message number of the last outgoing message of the given sequence.
     */
    public long getLastOutgoingMsgNo(String seqId);

    /**
     * Asks weather the storage has got the last message of the incoming sequence.
     */
    public boolean hasLastIncomingMsgReceived(String seqId);

    /**
     * Gets the message number of the last incoming message of the given sequence.
     */
    public long getLastIncomingMsgNo(String seqId);

    /**
     * This adds a entry in the storage for holding the messages of the given outgoing 
     * sequence.
     */
    public void addOutgoingSequence(String seqId);

    /**
     * This adds a entry in the storage for holding the messages of the given incoming 
     * sequence.
     */
    public void addIncomingSequence(String seqId);

    /**
     * Tries to give the sequence id of the outgoing message with the given message id
     */
    public String getOutgoingSeqOfMsg(String msgId);

    /**
     * When a server or client sends a create sequence, it will be marked from this method.
     * But the actual resources will be allocated only when the first message arrives.
     */
    public void addRequestedSequence(String seqId);

    /**
     * Checks weather this sequence id has been marked by the method 'addRequestedSequence'
     */
    public boolean isRequestedSeqPresent(String seqId);

    /**
     *This tries to give the outgoing sequence id relating to a given incoming message.
     *e.g. seqience which contains the message given by 'relatesTo'
     */
    public String getOutgoingSeqenceIdOfIncomingMsg(RMMessageContext msg);

    /**
     * Tells that the terminate sequence has been sent for a perticular outgoing sequence.
     */
    public void setTerminateSend(String seqId);

    /**
     * Tells that the terminate sequence message has been received for a perticular incoming sequence.
     */
    public void setTerminateReceived(String seqId);

    /**
     *This asks for the key used to hold messages of a perticular sequence 
     *given the outgoing sequece id. Remember that sequence id may not be present when the messages
     *first get added. So these should be a seperate key other than the sequence id.
     */
    public String getKeyFromOutgoingSeqId(String seqId);
    
    /**
     * Sets the acksTo property of a perticular sequence.
     */
    public void setAcksTo(String seqId,String acksTo);
    
    /**
     * Gets the acksTo property of a perticular sequence.
     */
    public String getAcksTo(String seqId);

    /**
     * This callback will report when messages get added to the storage.
     */
	public void setCallback(Callback callBack);
	
	/**
	 * Removes the callback added by 'setCallback' (if present)
	 */
	public void removeCallback();

	/**
	 * Sets the offer property of a perticular sequence.
	 */
    void addOffer(String msgID, String offerID);

	/**
	 * Gets the offer property of a perticular sequence.
	 */
    String getOffer(String msgID);
    
    /**
     * Clears the complete storage.
     */
    void clearStorage();
}