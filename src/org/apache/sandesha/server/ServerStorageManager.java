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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.dao.IServerDAO;
import org.apache.sandesha.server.dao.ServerDAOFactory;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class ServerStorageManager implements IStorageManager {

    protected static Log log = LogFactory.getLog(ServerStorageManager.class
            .getName());

    private String tempSeqId = null; // used by getNextMessageToProcess();

    /**
     * A very important method. Makes life easy for the thread or thread pool
     * that is using this. Every thread just have to create an instance of
     * ServerStorageManager and keep calling getNextMessageToProcess() and
     * processing messages. The method will try to give the messages from the
     * same sequence id. But if that doesnt hv processable messages it will go 4
     * a new sequence.
     *  
     */
    public RMMessageContext getNextMessageToProcess() {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        if (tempSeqId == null)
            tempSeqId = accessor.getRandomSeqIdToProcess();

        if (tempSeqId == null)
            return null;

        RMMessageContext nextMsg = accessor
                .getNextMsgContextToProcess(tempSeqId);

        if (nextMsg == null) {
            tempSeqId = accessor.getRandomSeqIdToProcess();
            nextMsg = accessor.getNextMsgContextToProcess(tempSeqId);
        }

        return nextMsg;
    }

    /**
     * This is used to set messages in the OutQueue. Various processors will use
     * this.
     */
    public void setReponse(RMMessageContext rmMessageContext) {
        //addMessageToOutQueue(rmMessageContext);

        //TODO decide this in implementing the ServerSender.
    }

    public void setAcknowledged(String seqID, long msgNumber) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        //TODO decide this in implementing the ServerSender.

        accessor.moveResponseMessageToBin(seqID, new Long(msgNumber));
    }

    public void init() {
        //:TODO Complete
    }

    /**
     * This will insert the message to the sequence in the InqQueue identified
     * by sequenceId. If sequence is not present will create a new one.
     */
    public void insertRequestMessage(RMMessageContext rmMessageContext) {

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        //No need to use this property
        //RMHeaders rmHeaders =
        //	(RMHeaders) rmMessageContext.getMsgContext().getProperty(
        //		org.apache.sandesha.Constants.ENV_RM_REQUEST_HEADERS);

        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();

        String sequenceId = rmHeaders.getSequence().getIdentifier()
                .getIdentifier();

        boolean exists = accessor.isSequenceExists(sequenceId);

        if (!exists)
            addSequence(sequenceId); //Creating new sequence

        //TODO: add getRmHeaders method to MessageContext
        long messageNumber = rmHeaders.getSequence().getMessageNumber()
                .getMessageNumber();

        if (messageNumber <= 0)
            return; //TODO: throw some exception

        Long msgNo = new Long(messageNumber);
        accessor.addMessageToSequence(sequenceId, msgNo, rmMessageContext);
    }

    public void insertResponseMessage(RMMessageContext msg) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        String sequenceId = msg.getSequenceID();
        //RMHeaders rmHeaders =msg.getRMHeaders();
        //String sequenceId =
        // rmHeaders.getSequence().getIdentifier().getIdentifier();

        boolean exists = accessor.isResponseSequenceExists(sequenceId);
        if (!exists)
            accessor.addResponseSequence(sequenceId);

        accessor.addMessageToResponseSequence(sequenceId, msg);

    }

    /**
     * Used to find out weather the sequence with this id has already been
     * created.
     */
    public boolean isSequenceExist(String sequenceID) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        return accessor.isSequenceExists(sequenceID);
    }

    public boolean isResponseSequenceExist(String sequenceID) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        return accessor.isResponseSequenceExists(sequenceID);
    }

    /**
     * This is used to get a random message from the out queue Basically server
     * sender will use this.
     */
    public RMMessageContext getNextMessageToSend() {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        RMMessageContext msg;

        msg = accessor.getNextPriorityMessageContextToSend();

        if (msg == null)
            msg = accessor.getNextResponseMsgContextToSend();

        return msg;
    }

    /**
     * This is used to add a new message to the out queue. Will be used by
     * various processors.
     */
    /*
     * public void addMessageToOutQueue(RMMessageContext rmMessageContext) {
     * IServerDAO accessor = ServerDAOFactory.getStorageAccessor(
     * Constants.SERVER_QUEUE_ACCESSOR); boolean result =
     * accessor.addOutQueueMessage(rmMessageContext);
     * 
     * if(!result) log.error("Message was not added to the out queue"); }
     */

    /**
     * Will be used to add a new Sequence Hash to the In Queue.
     */
    public void addSequence(String sequenceId) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        boolean result = accessor.addSequence(sequenceId);

        if (!result)
            log.error("Sequence was not created correcly in the in queue");
    }

    /**
     * This gives a sorted(by keys) map of messageIds present for a sequence.
     * This will be used to send Acks.
     */
    public Map getListOfMessageNumbers(String sequenceID) {

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        Set st = accessor.getAllReceivedMsgNumsOfSeq(sequenceID);

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
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        return accessor.isMessageExists(sequenceID, new Long(messageNumber));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#addCreateSequenceResponse(org.apache.sandesha.RMMessageContext)
     */
    public void addCreateSequenceResponse(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);
    }

    public void addCreateSequenceRequest(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#addAcknowledgement(org.apache.sandesha.RMMessageContext)
     */
    public void addAcknowledgement(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);
    }

    private void addPriorityMessage(RMMessageContext msg) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        accessor.addPriorityMessage(msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#getNextResponseMessageToSend()
     */
    /*
     * public RMMessageContext getNextResponseMessageToSend() { // TODO
     * Auto-generated method stub return null; }
     */

    //Simple method to sort an object array.
    /*
     * private Object[] sortObjArray(Object[] objs){
     * 
     * Object temp; for(int i=0;i <objs.length;i++){ for(int j=(i+1);j
     * <objs.length;j++){ long l1 = ((Long) objs[i]).longValue(); long l2 =
     * ((Long) objs[j]).longValue();
     * 
     * if(l1>l2){ //swaping temp=objs[i]; objs[i]=objs[j]; objs[j]=temp; } } }
     * 
     * return objs; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#setOutSequence(java.lang.String,
     *      java.lang.String)
     */
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        accessor.setOutSequence(sequenceId, outSequenceId);
        accessor.setOutSequenceApproved(sequenceId, false);
    }

    public boolean setApprovedOutSequence(String oldOutsequenceId,
            String newOutSequenceId) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        boolean done = false;
        String sequenceID = accessor.getSequenceOfOutSequence(oldOutsequenceId);

        if (sequenceID == null)
            return false;

        accessor.setOutSequence(sequenceID, newOutSequenceId);
        accessor.setOutSequenceApproved(sequenceID, true);

        //Deleting create sequence message from the priority queue.
        accessor.removeCreateSequenceMsg(oldOutsequenceId);
        return true;
    }
}