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

import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.addressing.RelatesTo;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.dao.ISandeshaDAO;
import org.apache.sandesha.storage.dao.SandeshaDAOFactory;
import org.apache.sandesha.ws.rm.RMHeaders;

import java.util.Map;

/**
 * @author Jaliya
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ClientStorageManager implements IStorageManager {

    protected static Log log = LogFactory.getLog(ClientStorageManager.class
            .getName());

    private ISandeshaDAO accessor;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.IStorageManager#init()
     */
    public void init() {
        // TODO Auto-generated method stub

    }

    public ClientStorageManager() {
        accessor = SandeshaDAOFactory.getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);
    }

    public boolean isSequenceExist(String sequenceID) {
        return accessor.isOutgoingSequenceExists(sequenceID);
    }

    public boolean isResponseSequenceExist(String sequenceID) {
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
        //No hard checking. User may insert the real sequence.

        String sequenceId = seqID;
        //accessor.moveOutgoingMessageToBin(sequenceId, new Long(msgNumber));

        accessor.markOutgoingMessageToDelete(sequenceId, new Long(msgNumber));

    }

    public void addSequence(String sequenceID) {
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
        accessor.addPriorityMessage(msg);
    }

    /**
     * Check the existance of a message.
     */
    public boolean isMessageExist(String sequenceID, long messageNumber) {
        return accessor.isIncomingMessageExists(sequenceID, new Long(messageNumber));
    }

    /**
     * Get a Map of messages.
     */
    public Map getListOfMessageNumbers(String sequenceID) {
        return null;
        /*
         * ISandeshaDAO accessor = SandeshaDAOFactory.getStorageAccessor(
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
        RMMessageContext msg;

        msg = accessor.getNextPriorityMessageContextToSend();

        if (msg == null)
            msg = accessor.getNextOutgoingMsgContextToSend();

        if (msg == null) {
            msg = accessor.getNextLowPriorityMessageContextToSend();   // checks whether all the request messages hv been acked
        }
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

        accessor.setOutSequence(sequenceId, outSequenceId);
        accessor.setOutSequenceApproved(sequenceId, false);

    }

    /**
     * This will be used by the SimpleAxisServer and the Sender to set the
     * proper sequenceID
     */
    public boolean setApprovedOutSequence(String oldOutsequenceId,
                                          String newOutSequenceId) {

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
        long msgNo = accessor.getNextOutgoingMessageNumber(sequenceID);
        return msgNo;

    }

    /**
     * This sholud be called by the RMSender when adding request messages.
     */
    /*private void insertClientRequestMessage(RMMessageContext msg) {
        ISandeshaDAO accessor = SandeshaDAOFactory
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

        //System.out.println("Client StorageManager is called");

        //This is the seuqnceid used to create the map entry.
        // (not the actual seq id of the msg).
        String sequenceId = msg.getSequenceID();
        //if(sequenceId==null)
        //    sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;

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

        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();

        RelatesTo relatesTo = (RelatesTo) rmMessageContext
                .getAddressingHeaders().getRelatesTo().get(0);
        String messageId = relatesTo.getURI().toString();
        //CHANGE THIS. SEARCH FOR THE SEQ USING MESID
        //String sequenceId = rmMessageContext.getSequenceID();

        String sequenceId = null;
        sequenceId = accessor.searchForSequenceId(messageId);

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


    //Sets the property responseReceived of the request message
    //corresponding to this response message.
    private void setResponseReceived(RMMessageContext responseMsg) {
        accessor.setResponseReceived(responseMsg);
    }

    public RMMessageContext checkForResponseMessage(String sequenceId, String requestMsgId) {
        if (sequenceId == null)
            sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;

        RMMessageContext response = accessor.checkForResponseMessage(requestMsgId, sequenceId);
        return response;

    }

    public boolean checkForAcknowledgement(String sequenceId, String requestMsgId) {

        //Request message will be present in the queue only if the ack has not been
        //receive. It will be deleted by the AckProcessor when an ack get received.
        if (sequenceId == null)
            sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;

        boolean requestPresent = accessor.isRequestMessagePresent(sequenceId, requestMsgId);
        return !requestPresent;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#isAckComplete(java.lang.String)
     */
    //For client sequenceId should be outgoing sequence id.
    public boolean isAckComplete(String sequenceID) {
        boolean result = accessor.compareAcksWithSequence(sequenceID);  //For client
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#insertTerminateSeqMessage(org.apache.sandesha.RMMessageContext)
     */
    public void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage) {
        accessor.addLowPriorityMessage(terminateSeqMessage);

    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#isAllSequenceComplete()
     */
    public boolean isAllSequenceComplete() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#isResponseComplete(java.lang.String)
     */
    public boolean isResponseComplete(String sequenceID) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#terminateSequence(java.lang.String)
     */
    public void terminateSequence(String sequenceID) {
        // TODO Auto-generated method stub

    }

    public void setAckReceived(String seqId, long msgNo) {
        accessor.setAckReceived(seqId, msgNo);

    }

    public void insertFault(RMMessageContext rmMsgCtx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    
    
	/* (non-Javadoc)
	 * @see org.apache.sandesha.IStorageManager#addSentMsgNo(java.lang.String, long)
	 */

	/* (non-Javadoc)
	 * @see org.apache.sandesha.IStorageManager#addSendMsgNo(java.lang.String, long)
	 */
	public void addSendMsgNo(String seqId, long msgNo) {
		accessor.addSendMsgNo(seqId,msgNo);

	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha.IStorageManager#getLastMsgNo(java.lang.String)
	 */
	public long getLastMsgNo(String seqId) {
		return accessor.getLastMsgNo(seqId);
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha.IStorageManager#hasLastMsgReceived(java.lang.String)
	 */
	public boolean hasLastMsgReceived(String seqId) {
		return accessor.hasLastMsgReceived(seqId);
	}
	/* (non-Javadoc)
	 * @see org.apache.sandesha.IStorageManager#isSentMsg(java.lang.String, long)
	 */
	public boolean isSentMsg(String seqId, long msgNo) {
		return accessor.isSentMsg(seqId,msgNo);
	}
}