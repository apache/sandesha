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
import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.CallbackData;
import org.apache.sandesha.storage.dao.ISandeshaDAO;
import org.apache.sandesha.storage.dao.SandeshaDAOFactory;
import org.apache.sandesha.storage.queue.SandeshaQueue;
import org.apache.sandesha.ws.rm.RMHeaders;

import java.util.Map;

public class ClientStorageManager implements IStorageManager {

    protected static Log log = LogFactory.getLog(ClientStorageManager.class.getName());

    private ISandeshaDAO accessor;
    private static Callback callBack = null;

    public void init() {
    }

    public ClientStorageManager() {
        accessor = SandeshaDAOFactory.getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR,Constants.CLIENT);
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
        accessor.markOutgoingMessageToDelete(sequenceId, new Long(msgNumber));

    }

    public void addSequence(String sequenceID) {
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
    }

    /**
     * This will be used by the sender.
     */
    public RMMessageContext getNextMessageToSend() {
        RMMessageContext msg;
        msg = accessor.getNextPriorityMessageContextToSend();
        if (null == msg)
            msg = accessor.getNextOutgoingMsgContextToSend();

        if (msg == null) {
            msg = accessor.getNextLowPriorityMessageContextToSend();

            // checks whether all the request messages hv been acked
        }
        if (callBack != null && msg != null)
            informOutgoingMessage(msg);
        return msg;
    }

    /**
     * This will be used by the RMSender when adding messages to the Queue.
     * RMSender will also add a createSequenceRequest message to the prioriy
     * queue using this temporary ID as the messageID.
     */
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId) {
        accessor.setOutSequence(sequenceId, outSequenceId);
        accessor.setOutSequenceApproved(sequenceId, false);
    }

    /**
     * This will be used by the SimpleAxisServer and the Sender to set the
     * proper sequenceID
     */
    public boolean setApprovedOutSequence(String createSeqId, String newOutSequenceId) {

        boolean done = false;
        // String oldOutsequenceId = accessor.getFirstCreateSequenceMsgId(createSeqId);
        String oldOutsequenceId = createSeqId;
        if (null == oldOutsequenceId) {
            return false;
        }

        String sequenceID = accessor.getSequenceOfOutSequence(oldOutsequenceId);

        if (sequenceID == null) {
            log.error(Constants.ErrorMessages.SET_APPROVED_OUT_SEQ);
            return false;
        }
        accessor.setOutSequence(sequenceID, newOutSequenceId);
        accessor.setOutSequenceApproved(sequenceID, true);
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.sandesha.IStorageManager#insertOutgoingMessage
     * (org.apache.sandesha.RMMessageContext)
     */
    public void insertOutgoingMessage(RMMessageContext msg) {
        String sequenceId = msg.getSequenceID();
        accessor.addMessageToOutgoingSequence(sequenceId, msg);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.sandesha.IStorageManager#insertIncomingMessage
     *(org.apache.sandesha.RMMessageContext)
     */

    public void insertIncomingMessage(RMMessageContext rmMessageContext) {
        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
        RelatesTo relatesTo = (RelatesTo) rmMessageContext.getAddressingHeaders().getRelatesTo()
                .get(0);
        String messageId = relatesTo.getURI().toString();
        String sequenceId = null;

        sequenceId = accessor.searchForSequenceId(messageId);

        boolean exists = accessor.isIncomingSequenceExists(sequenceId);

        if (!exists) {
            accessor.addIncomingSequence(sequenceId);
        }

        long messageNumber = rmHeaders.getSequence().getMessageNumber().getMessageNumber();
        if (0 >= messageNumber)
            return;
        Long msgNo = new Long(messageNumber);
        accessor.addMessageToIncomingSequence(sequenceId, msgNo, rmMessageContext);
    }


    //Sets the property responseReceived of the request message
    //corresponding to this response message.
    private void setResponseReceived(RMMessageContext responseMsg) {
        accessor.setResponseReceived(responseMsg);
    }

    public RMMessageContext checkForResponseMessage(String sequenceId, String requestMsgId) {
        RMMessageContext response = accessor.checkForResponseMessage(requestMsgId, sequenceId);
        return response;

    }

    public boolean checkForAcknowledgement(String sequenceId, String requestMsgId) {

        //Request message will be present in the queue only if the ack has not been
        //receive. It will be deleted by the AckProcessor when an ack get received.
    
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


    public void insertTerminateSeqMessage(RMMessageContext terminateSeqMessage) {
        accessor.addLowPriorityMessage(terminateSeqMessage);
    }


    public boolean isAllSequenceComplete() {
        boolean outTerminateSent = accessor.isAllOutgoingTerminateSent();
        boolean incomingTerminateReceived = accessor.isAllIncommingTerminateReceived();
        if (outTerminateSent && incomingTerminateReceived)
            return true;
        else
            return false;
    }


    public boolean isResponseComplete(String sequenceID) {
        return false;
    }


    public void terminateSequence(String sequenceID) {
    }

    public void setAckReceived(String seqId, long msgNo) {
        accessor.setAckReceived(seqId, msgNo);
    }

    public void insertFault(RMMessageContext rmMsgCtx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void addSendMsgNo(String seqId, long msgNo) {
        accessor.addSendMsgNo(accessor.getSequenceOfOutSequence(seqId), msgNo);
    }

    public void addOutgoingSequence(String sequenceId) {
        accessor.addOutgoingSequence(sequenceId);
    }

    public void addIncomingSequence(String sequenceId) {
        accessor.addIncomingSequence(sequenceId);
    }

    /* (non-Javadoc)
	 * @see org.apache.sandesha.IStorageManager#hasLastMsgReceived(java.lang.String)
	 */
    public boolean hasLastOutgoingMsgReceived(String seqId) {
        String key = accessor.getKeyFromOutgoingSequenceId(seqId);
        return accessor.hasLastOutgoingMsgReceived(key);
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#getLastMsgNo(java.lang.String)
     */
    public long getLastOutgoingMsgNo(String seqId) {
        String key = accessor.getKeyFromOutgoingSequenceId(seqId);
        return accessor.getLastOutgoingMsgNo(key);
    }

    public long getLastIncomingMsgNo(String seqId) {
        String key = accessor.getKeyFromIncomingSequenceId(seqId);
        return accessor.getLastIncomingMsgNo(key);
    }

    public boolean hasLastIncomingMsgReceived(String seqId) {
        String key = accessor.getKeyFromIncomingSequenceId(seqId);
        return accessor.hasLastIncomingMsgReceived(key);
    }

    public void addRequestedSequence(String seqId) {
        accessor.addRequestedSequence(seqId);
    }

    public boolean isRequestedSeqPresent(String seqId) {
        return accessor.isRequestedSeqPresent(seqId);
    }

    public boolean isSentMsg(String seqId, long msgNo) {
        return accessor.isSentMsg(accessor.getSequenceOfOutSequence(seqId), msgNo);
    }

    public String getOutgoingSeqOfMsg(String msgId) {
        return accessor.searchForSequenceId(msgId);
    }

    public String getOutgoingSeqenceIdOfIncomingMsg(RMMessageContext msg) {
        String msgId = msg.getMessageID();
        return accessor.searchForSequenceId(msgId);
    }

    public void setTerminateSend(String seqId) {
        accessor.setTerminateSend(seqId);
    }

    public void setTerminateReceived(String seqId) {
        accessor.setTerminateReceived(seqId);
    }

    public String getKeyFromOutgoingSeqId(String seqId) {
        return accessor.getKeyFromOutgoingSequenceId(seqId);
    }

    public void setAcksTo(String seqId, String acksTo) {
        accessor.setAcksTo(seqId, acksTo);
    }

    public String getAcksTo(String seqId) {
        return accessor.getAcksTo(seqId);
    }

    public void addOffer(String msgID, String offerID) {
        accessor.addOffer(msgID, offerID);
    }

    public String getOffer(String msgID) {
        return accessor.getOffer(msgID);
    }

    public void setCallback(Callback cb) {
        callBack = cb;
    }

    public void removeCallback() {
        callBack = null;
    }

    private void informOutgoingMessage(RMMessageContext rmMsgContext) {

        CallbackData cbData = new CallbackData();

        //  setting callback data;
        if (rmMsgContext != null) {
            cbData.setSequenceId(rmMsgContext.getSequenceID());
            cbData.setMessageId(rmMsgContext.getMessageID());
            cbData.setMessageType(rmMsgContext.getMessageType());

        }

        if (callBack != null)
            callBack.onOutgoingMessage(cbData);
    }
    
    public void clearStorage(){
        accessor.clear();
    }


}