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
package org.apache.sandesha.client;

import java.util.Map;

import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.addressing.RelatesTo;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.dao.IServerDAO;
import org.apache.sandesha.server.dao.ServerDAOFactory;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author Jaliya
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ClientStorageManager implements IStorageManager {

    protected static Log log = LogFactory.getLog(ClientStorageManager.class
            .getName());

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#init()
     */
    public void init() {
        // TODO Auto-generated method stub

    }

    public boolean isSequenceExist(String sequenceID) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        return accessor.isOutgoingSequenceExists(sequenceID);
    }

    public boolean isResponseSequenceExist(String sequenceID) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        return accessor.isIncomingSequenceExists(sequenceID);
    }

    /**
     * This will be used to inform the client about the presence of the response
     * message. But will be impemented later.
     */
    public RMMessageContext getNextMessageToProcess() {
        return null;
    }

    /**
     * This will be used both by the Sender and the SimpleAxisServer to set the
     * acks.
     */
    public void setAcknowledged(String seqID, long msgNumber) {
        //seqId is just a dummy since the client will hv only a one seq.
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        //No hard checking. User may insert the real sequence.

        String sequenceId = seqID;
        //accessor.moveOutgoingMessageToBin(sequenceId, new Long(msgNumber));

        accessor.markOutgoingMessageToDelete(sequenceId, new Long(msgNumber));

    }

    public void addSequence(String sequenceID) {

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        //boolean result = accessor.addIncomingSequence(sequenceID);
        boolean result = accessor.addOutgoingSequence(sequenceID);

        if (!result)
            log.error("Sequence was not created correcly in the in queue");

    }

    /**
     * This will be used both by the Sender and the SimpleAxisServer to set the
     * create sequence responses.
     */
    public void addCreateSequenceResponse(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);

    }

    /**
     * This will be used by the RMSender to add the create sequence request.
     */
    public void addCreateSequenceRequest(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);

    }

    /**
     * SimpleAxisServer will use this method to add acks for the application
     * responses received from the server side.
     */
    public void addAcknowledgement(RMMessageContext rmMessageContext) {
        addPriorityMessage(rmMessageContext);

    }

    //private method
    private void addPriorityMessage(RMMessageContext msg) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        accessor.addPriorityMessage(msg);
    }

    /**
     * Check the existance of a message.
     */
    public boolean isMessageExist(String sequenceID, long messageNumber) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        return accessor.isIncomingMessageExists(sequenceID, new Long(
                messageNumber));
    }

    /**
     * Get a Map of messages.
     */
    public Map getListOfMessageNumbers(String sequenceID) {
        return null;
        /*
         * IServerDAO accessor = ServerDAOFactory.getStorageAccessor(
         * Constants.SERVER_QUEUE_ACCESSOR);
         * 
         * Set st = accessor.getAllReceivedMsgNumsOfIncomingSeq(sequenceID);
         * 
         * Iterator it = st.iterator();
         * 
         * //To find the largest id present long largest=0; while(it.hasNext()){
         * Long key = (Long) it.next(); if(key==null) continue;
         * 
         * long l = key.longValue(); if(l>largest) largest = l; }
         * 
         * 
         * HashMap results = new HashMap(); //Add Keys to the results in order.
         * long currentPosition=1; for(long l=1;l <=largest;l++){ boolean
         * present = st.contains(new Long(l)); if(present){ results.put(new
         * Long(currentPosition),new Long(l)); currentPosition++; } } return
         * results;
         */
    }

    /**
     * This will be used by the sender.
     */
    public RMMessageContext getNextMessageToSend() {
        //System.out.println("getNextMessageToSend() is called");
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        RMMessageContext msg;

        msg = accessor.getNextPriorityMessageContextToSend();

        if (msg == null)
            msg = accessor.getNextOutgoingMsgContextToSend();

        return msg;
    }

    /**
     * This will be used by the RMSender when adding messages to the Queue.
     * RMSender will also add a createSequenceRequest message to the prioriy
     * queue using this temporary ID as the messageID.
     */
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId) {

        /*if (!sequenceId.equals(Constants.CLIENT_DEFAULD_SEQUENCE_ID)) {
            System.out.println("Error: Wrong sequence id for client");
            return;
        }*/

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        accessor.setOutSequence(sequenceId, outSequenceId);
        accessor.setOutSequenceApproved(sequenceId, false);

    }

    /**
     * This will be used by the SimpleAxisServer and the Sender to set the
     * proper sequenceID
     */
    public boolean setApprovedOutSequence(String oldOutsequenceId,
            String newOutSequenceId) {

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        boolean done = false;
        String sequenceID = accessor.getSequenceOfOutSequence(oldOutsequenceId);
       
        if (sequenceID == null) {
            System.out.println("ERROR: setApprovedOutSequence()");
            return false;
        }
        //sequenceid should be the default one.
        /* if (!sequenceID.equals(Constants.CLIENT_DEFAULD_SEQUENCE_ID)) {
            System.out.println("Error: Wrong sequence id for client");
            return false;
        }
        */
        accessor.setOutSequence(sequenceID, newOutSequenceId);
        accessor.setOutSequenceApproved(sequenceID, true);

        //Deleting create sequence message from the priority queue.
        //System.out.println("OLD OUT SEQ IS "+oldOutsequenceId);
        accessor.removeCreateSequenceMsg(oldOutsequenceId);

        return true;
    }

    /**
     * This will be used by the RMSender when adding messages. Initially it
     * should return 1.
     */
    public long getNextMessageNumber(String sequenceID) {

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
        long msgNo = accessor.getNextOutgoingMessageNumber(sequenceID);
        return msgNo;

    }

    /**
     * This sholud be called by the RMSender when adding request messages.
     *  
     */
    /*private void insertClientRequestMessage(RMMessageContext msg) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        //This is the seuqnceid used to create the map entry.
        // (not the actual seq id of the msg).
        String sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;

        boolean exists = accessor.isOutgoingSequenceExists(sequenceId);
        if (!exists)
            accessor.addOutgoingSequence(sequenceId);

        accessor.addMessageToOutgoingSequence(sequenceId, msg);
    }*/

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#insertOutgoingMessage(org.apache.sandesha.RMMessageContext)
     */
    public void insertOutgoingMessage(RMMessageContext msg) {
        //System.out.println("RESPONSE MESSAGE IS RECEIVED..");

        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        //System.out.println("Client StorageManager is called");

        //This is the seuqnceid used to create the map entry.
        // (not the actual seq id of the msg).
        String sequenceId = msg.getSequenceID();  
		if(sequenceId==null)
		    sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;

        boolean exists = accessor.isOutgoingSequenceExists(sequenceId);
        if (!exists)
            accessor.addOutgoingSequence(sequenceId);

        accessor.addMessageToOutgoingSequence(sequenceId, msg);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#insertIncomingMessage(org.apache.sandesha.RMMessageContext)
     */

    //IN THE CLIENT RESPONSE HASH HAS THE ID OF RESPONSE MESSAGES.
    public void insertIncomingMessage(RMMessageContext rmMessageContext) {
        IServerDAO accessor = ServerDAOFactory
                .getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);

        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
    	
        RelatesTo relatesTo = (RelatesTo) rmMessageContext
        .getAddressingHeaders().getRelatesTo().get(0);
        String messageId = relatesTo.getURI().toString();
        //CHANGE THIS. SEARCH FOR THE SEQ USING MESID
        //String sequenceId = rmMessageContext.getSequenceID();
        System.out.println("******** SEARCH FOR THIS MSG ID "+messageId);
        
        String sequenceId = null;
        sequenceId = accessor.searchForSequenceId(messageId);
        
        System.out.println("******** SEARCH OBTAINED SEQ ID IS : "+sequenceId);
        boolean exists = accessor.isIncomingSequenceExists(sequenceId);

        if (!exists) {
            accessor.addIncomingSequence(sequenceId);
        }

        //TODO: add getRmHeaders method to MessageContext
        long messageNumber = rmHeaders.getSequence().getMessageNumber()
                .getMessageNumber();

        if (messageNumber <= 0)
            return; //TODO: throw some exception

        Long msgNo = new Long(messageNumber);
        accessor.addMessageToIncomingSequence(sequenceId, msgNo,
                rmMessageContext);

    }

    public RMMessageContext checkForResponseMessage(String sequenceId,String requestMsgId){
        
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		
		if(sequenceId==null)
		    sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;
		
		RMMessageContext response = accessor.checkForResponseMessage(requestMsgId,sequenceId);
        return response; 
        
    }
    
    public boolean checkForAcknowledgement(String sequenceId,String requestMsgId){
        
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		
		//Request message will be present in the queue only if the ack has not been
		//receive. It will be deleted by the AckProcessor when an ack get received.
		if(sequenceId==null)
		    sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;
		
		boolean requestPresent = accessor.isRequestMessagePresent(sequenceId,requestMsgId);
		return !requestPresent;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#isAckComplete(java.lang.String)
     */
    public boolean isAckComplete(String sequenceID) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#insertTerminateSeqMessage(org.apache.sandesha.RMMessageContext)
     */
    public void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#isAllSequenceComplete()
     */
    public boolean isAllSequenceComplete() {
        // TODO Auto-generated method stub
        return false;
    }
    

    


}