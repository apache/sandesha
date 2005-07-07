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
 */

public interface ISandeshaDAO {

    /**
     * This adds a new entry in the storage to to hold messages of a perticular
     * sequence that come in to the sandesha server/client
     */
    boolean addIncomingSequence(String sequenceId);


    /**
     * This adds a entry in the storage for holding the messages of the given outgoing
     * sequence.
     */
    boolean addOutgoingSequence(String sequenceId);


    /**
     * Adds a priority message (e.g. create seq) to the queue. These will be sent
     * before other messages like application requests.
     */
    boolean addPriorityMessage(RMMessageContext message);


    /**
     * This checks the priority messages to see weather there is any one to
     * be sent (either for the first time or a retransmission)
     */
    RMMessageContext getNextPriorityMessageContextToSend();


    /**
     * This adds a incoming message to a area belonging to the given sequence in the
     * queue.
     */
    boolean addMessageToIncomingSequence(String sequenceId, Long msgNo,
                                         RMMessageContext rmMessageContext);


    /**
     * This adds the given message to the given sequence.
     */
    boolean addMessageToOutgoingSequence(String sequenceId,
                                         RMMessageContext rmMessageContext);


    /**
     * This checks weather there is a entry for the given incoming sequence in
     * the queue.
     */
    boolean isIncomingSequenceExists(String sequenceId);


    /**
     * Checks weather a entry for the given outgoing sequence exists in
     * the queue.
     */
    boolean isOutgoingSequenceExists(String sequenceId);


    /**
     * This checks in the given incoming sequence to see weather a message of
     * the given message no exists
     */
    boolean isIncomingMessageExists(String sequenceId, Long msgNo);


    /**
     * This tries to get the next message to be sent from the given outgoing sequence
     * If these is no message to be sent in the given sequence, null will be returned.
     */
    RMMessageContext getNextMsgContextToProcess(Object seq);


    /**
     * Gets the next possible message to be sent from the queue.
     */
    RMMessageContext getNextOutgoingMsgContextToSend();

    public Object getRandomSeqToProcess();

   
    /**
     * This reutns a set of message numbers with all the message numbers of
     * incoming sequence.
     */
    Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId);


    /**
     * This sets the outgoing sequence. Here seqId is the entry in the queue
     * that we hope to hold the messages of a perticular sequence. outseqid is the
     * actual sequence id (i.e. uuid). That will be set within the wsrm:sequence field of
     * this message. (remember that we may not have received this actual outgoing sequence id
     * by the time we start to store outgoing messages).
     */
    void setOutSequence(String sequenceId, String outSequenceId);


    /**
     * This sets a flag in the queue to indicate that the outSequence of the perticular message set
     * (stored with the id seqId) has been set correctly. The value in outSequence may be wrong
     * before the sender gets the create seq. response. After getting this and after setting the
     * out sequence correctly using the previous method, this flag will be set to true. Only then these
     * messages thould be send to the sender.
     */
    void setOutSequenceApproved(String sequenceID, boolean approved);


    /**
     * this gives the seqId which is used to hold the messages of which the
     * outsequence entry has been set to the value outsequenceId.
     */
    String getSequenceOfOutSequence(String outsequenceId);


    /**
     * This checks the priority queue for a message if given messageid and
     * moves it to the bin
     */
    void removeCreateSequenceMsg(String messageId);

    /**
     * This gives the next message number, outgoing storage of the given sequence
     * esxpects. Actually this will be used by storage managers to obtain the message
     * number that should be put to the next application message.
     */
    long getNextOutgoingMessageNumber(String sequenceId);

    /**
     * When the messageId of therequest message (e.g. from relates to) and the sequence id
     * is given this will give the response message.
     */
    public RMMessageContext checkForResponseMessage(String requestId, String seqId);

    /**
     * Tries to give the sequence id of the outgoing message with the given message id
     */
    public String searchForSequenceId(String messageId);

    /**
     * This outgoing message will be marked as deleted.
     * i.e. it will not be re-transmitted
     */
    public void markOutgoingMessageToDelete(String sequenceId, Long msgNumber);


    /**
     * Tells to the rtorage that the given message of given sequence was acked.
     */
    public void setAckReceived(String seqId, long msgNo);

    /**
     * Adds a low priority message to the storage (e.g. terminate sequence).
     * These messages are axpected to be sent after sending all the other messages
     * of the given sequence (but it is not a must)
     */
    public void addLowPriorityMessage(RMMessageContext msg);

    /**
     * Asks the storage for the next low priority message (if there is any)
     */
    public RMMessageContext getNextLowPriorityMessageContextToSend();

    /**
     * The message will be added to a sent list (the list holds the
     * messages that were sent at least once)
     */
    public void addSendMsgNo(String seqId, long msgNo);

    /**
     * Asks from the storage weather the given message has been sent at
     * least once
     */
    public boolean isSentMsg(String seqId, long msgNo);


    /**
     * Can be used to ckeck weather the last message has been received in the
     * incoming sequence.
     */
    public boolean hasLastIncomingMsgReceived(String seqId);

    /**
     * Asks for the last message of the incoming sequence (if it has been received)
     */
    public long getLastIncomingMsgNo(String seqId);

    /**
     * When a server or client sends a create sequence, it will be marked from this method.
     * But the actual resources will be allocated only when the first message arrives.
     */
    public void addRequestedSequence(String seqId);

    /**
     * Check weather the given sequence id is one of the requested once (see 'addRequestedSequence')
     */
    public boolean isRequestedSeqPresent(String seqId);

    /**
     * The client side will not be able to have sequenceId as a key for storing request messages.
     * Since it may be not known when the user adds first message.
     * This asks for that key, giving sequence id of incoming messages.
     */
    public String getKeyFromIncomingSequenceId(String incomingSeqID);

    /**
     * This asks for the above key (the key used to hold messages) given the outgoing sequece id.
     */
    public String getKeyFromOutgoingSequenceId(String outgoingSeqID);

    /**
     * Sets this after sending the terminate sequence message.
     */
    public void setTerminateSend(String seqId);

    /**
     * Sets a incoming sequence of messages as terminate received.
     */
    public void setTerminateReceived(String seqId);

    /**
     * Checks weather the terminate message of all out going sequences have been sent.
     */
    public boolean isAllOutgoingTerminateSent();

    /**
     * Use this to check weather the terminate message of all sequences (incoming) has
     * been received.
     */
    public boolean isAllIncommingTerminateReceived();

    /**
     * Set the acks to of the given sequence.
     */
    public void setAcksTo(String seqId, String acksTo);

    /**
     * gets the acksTo value
     */
    public String getAcksTo(String seqId);

    /**
     * Sets the offered incoming sequence, of a outgoing sequence.
     */
    void addOffer(String msgID, String offerID);

    /**
     * Gets the offered incoming sequence, of outgoing sequence.
     */
    public String getOffer(String msgID);

    /**
     * clears the storage.
     */
    void clear();

    public boolean isOutgoingTerminateSent(String seqId);

    public boolean isIncommingTerminateReceived(String seqId);

    public void updateFinalMessageArrivedTime(String sequenceID);

    public void sendAck(String sequenceId);

    public void removeAllAcks(String sequenceID);
}