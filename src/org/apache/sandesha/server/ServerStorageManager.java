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
package org.apache.sandesha.server;

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.dao.ISandeshaDAO;
import org.apache.sandesha.storage.dao.SandeshaDAOFactory;
import org.apache.sandesha.ws.rm.RMHeaders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * ServerStorageManager is the access point for the SandeshaQueue from server side.
 *
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class ServerStorageManager implements IStorageManager {

    public void setTerminateSend(String seqId) {

    }

    public void setTerminateReceived(String seqId) {

    }

    protected static Log log = LogFactory.getLog(ServerStorageManager.class.getName());
    private ISandeshaDAO accessor;

    public ServerStorageManager() {
        accessor =
                SandeshaDAOFactory.getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR,
                        Constants.SERVER);
    }


    /**
     * A very important method. Makes life easy for the thread or thread pool
     * that is using this. Every thread just have to create an instance of
     * ServerStorageManager and keep calling getNextMessageToProcess() and
     * processing messages. The method will try to give the messages from the
     * same sequence id. But if that doesnt hv processable messages it will go for
     * a new sequence.
     */
    public RMMessageContext getNextMessageToProcess(Object seq) {

        if (seq == null)
            return null;

        RMMessageContext nextMsg = accessor.getNextMsgContextToProcess(seq);
        return nextMsg;
    }

    public void setAcknowledged(String seqID, long msgNumber) {
        accessor.markOutgoingMessageToDelete(seqID, new Long(msgNumber));
    }

    public void init() {
    }

    /**
     * Used to find out weather the sequence with this id has already been
     * created.
     */
    public boolean isSequenceExist(String sequenceID) {
        return accessor.isIncomingSequenceExists(sequenceID);
    }

    public boolean isResponseSequenceExist(String sequenceID) {
        return accessor.isOutgoingSequenceExists(sequenceID);
    }

    public Object getNextSeqToProcess() {
        return accessor.getRandomSeqToProcess();
    }


    /**
     * This is used to get a random message from the out queue Basically server
     * sender will use this.
     */
    public synchronized RMMessageContext getNextMessageToSend() {
        RMMessageContext msg;
        msg = accessor.getNextPriorityMessageContextToSend();
        if (msg == null)
            msg = accessor.getNextOutgoingMsgContextToSend();
        if (msg == null)
            msg = accessor.getNextLowPriorityMessageContextToSend();

        if (msg != null && !msg.isLocked()) {
            msg.setLocked(true);
            return msg;
        } else {
            return null;
        }

    }

    /**
     * Will be used to add a new Sequence Hash to the In Queue.
     */
    public void addSequence(String sequenceId) {
        boolean result = accessor.addIncomingSequence(sequenceId);
        if (!result)
            ServerStorageManager.log.error(Constants.ErrorMessages.SEQ_IS_NOT_CREATED);
    }

    /**
     * This gives a sorted(by keys) map of messageIds present for a sequence.
     * This will be used to send Acks.
     */
    public Map getListOfMessageNumbers(String sequenceID) {
        Set st = accessor.getAllReceivedMsgNumsOfIncomingSeq(sequenceID);
        Iterator it = st.iterator();
        //To find the largest id present
        long largest = 0;
        while (it.hasNext()) {
            Long key = (Long) it.next();
            if (key == null)
                continue;

            long l = key.longValue();
            if (l > largest)
                largest = l;
        }

        HashMap results = new HashMap();
        //Add Keys to the results in order.
        long currentPosition = 1;
        for (long l = 1; l <= largest; l++) {
            boolean present = st.contains(new Long(l));
            if (present) {
                results.put(new Long(currentPosition), new Long(l));
                currentPosition++;
            }
        }
        return results;
    }

    public boolean isMessageExist(String sequenceID, long messageNumber) {
        synchronized (accessor) {
            return accessor.isIncomingMessageExists(sequenceID, new Long(messageNumber));
        }
    }


    public void addCreateSequenceResponse(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);
    }

    public void addCreateSequenceRequest(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);
    }

    public void addAcknowledgement(RMMessageContext rmMessageContext) {
        String sequenceID = rmMessageContext.getSequenceID();
        if (sequenceID != null)
            accessor.removeAllAcks(sequenceID);

        addPriorityMessage(rmMessageContext);
    }

    private void addPriorityMessage(RMMessageContext msg) {
        accessor.addPriorityMessage(msg);
    }

    public void setTemporaryOutSequence(String sequenceId, String outSequenceId) {
        accessor.setOutSequence(sequenceId, outSequenceId);
        accessor.setOutSequenceApproved(sequenceId, false);
    }

    public boolean setApprovedOutSequence(String createSeqId, String newOutSequenceId) {

        // String oldOutsequenceId = accessor.getFirstCreateSequenceMsgId(createSeqId);
        String oldOutsequenceId1 = createSeqId;

        String tempOutSeq = oldOutsequenceId1;
        if (tempOutSeq == null)
            tempOutSeq = createSeqId;
        String sequenceID = accessor.getSequenceOfOutSequence(tempOutSeq);

        if (sequenceID == null) {
            ServerStorageManager.log.error(Constants.ErrorMessages.SET_APPROVED_OUT_SEQ);
            return false;
        }
        accessor.setOutSequence(sequenceID, newOutSequenceId);
        accessor.setOutSequenceApproved(sequenceID, true);
        accessor.removeCreateSequenceMsg(tempOutSeq);
        return true;
    }

    public long getNextMessageNumber(String sequenceID) {
        long l = accessor.getNextOutgoingMessageNumber(sequenceID);
        return l;
    }

    public void insertOutgoingMessage(RMMessageContext msg) {
        String sequenceId = msg.getSequenceID();

        boolean exists = accessor.isOutgoingSequenceExists(sequenceId);
        if (!exists)
            accessor.addOutgoingSequence(sequenceId);
        accessor.addMessageToOutgoingSequence(sequenceId, msg);

    }

    public void insertIncomingMessage(RMMessageContext rmMessageContext) {
        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
        String sequenceId = rmHeaders.getSequence().getIdentifier().getIdentifier();
        boolean exists = accessor.isIncomingSequenceExists(sequenceId);
        if (!exists)
            addSequence(sequenceId); //Creating new sequence

        //TODO: add getRmHeaders method to MessageContext
        long messageNumber = rmHeaders.getSequence().getMessageNumber().getMessageNumber();

        if (messageNumber <= 0)
            return;

        Long msgNo = new Long(messageNumber);
        accessor.addMessageToIncomingSequence(sequenceId, msgNo, rmMessageContext);
        accessor.updateFinalMessageArrivedTime(sequenceId);

    }

    public RMMessageContext checkForResponseMessage(String sequenceId, String requestMsgId) {
        return null;
    }

    public void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage) {
        accessor.addLowPriorityMessage(terminateSeqMessage);
    }

    public void setAckReceived(String seqId, long msgNo) {
        accessor.setAckReceived(seqId, msgNo);

    }

    public void insertFault(RMMessageContext rmMsgCtx) {
    }


    public void addSendMsgNo(String seqId, long msgNo) {
        accessor.addSendMsgNo(accessor.getSequenceOfOutSequence(seqId), msgNo);
    }

    public boolean isSentMsg(String seqId, long msgNo) {
        return accessor.isSentMsg(accessor.getSequenceOfOutSequence(seqId), msgNo);
    }


    public void addOutgoingSequence(String sequenceId) {
        accessor.addOutgoingSequence(sequenceId);
    }

    public void addIncomingSequence(String sequenceId) {
        accessor.addIncomingSequence(sequenceId);
    }

    public String getOutgoingSeqOfMsg(String msgId) {
        return null;
    }

    public void addRequestedSequence(String seqId) {
        accessor.addRequestedSequence(seqId);
    }

    public boolean isRequestedSeqPresent(String seqId) {
        return accessor.isRequestedSeqPresent(seqId);
    }

    public String getOutgoingSeqenceIdOfIncomingMsg(RMMessageContext msg) {

        return msg.getSequenceID();
    }

    public long getLastIncomingMsgNo(String seqId) {
        return accessor.getLastIncomingMsgNo(seqId);
    }

    public boolean hasLastIncomingMsgReceived(String seqId) {
        return accessor.hasLastIncomingMsgReceived(seqId);
    }

    public String getKeyFromOutgoingSeqId(String seqId) {
        return null;
    }

    public void setAcksTo(String seqId, String acksTo) {
        accessor.setAcksTo(seqId, acksTo);
    }

    public String getAcksTo(String seqId) {
        return accessor.getAcksTo(seqId);
    }

    public void setCallback(Callback cb) {
    }

    public void removeCallback() {
    }

    public void addOffer(String msgID, String offerID) {

    }

    public String getOffer(String msgID) {
        return null;
    }

    public void clearStorage() {
        accessor.clear();
    }

    public boolean isSequenceComplete(String seqId) {
        return false;
    }

    public void sendAck(String sequenceId) {
        accessor.sendAck(sequenceId);
    }
}