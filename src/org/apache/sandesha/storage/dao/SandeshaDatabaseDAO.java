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

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.queue.IncomingSequence;

import java.util.Set;
import java.util.Iterator;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaDatabaseDAO implements ISandeshaDAO {

    private static final Log log = LogFactory.getLog(SandeshaDatabaseDAO.class.getName());
    
    public void setAckReceived(String seqId, long msgNo) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#setResponseReceived(org.apache.sandesha.RMMessageContext)
     */
    public void setResponseReceived(RMMessageContext msg) {
        // TODO Auto-generated method stub
    }

    public void addRequestedSequence(String seqId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSeqPresent(String seqId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#markOutgoingMessageToDelete(java.lang.String, java.lang.Long)
     */
    public void markOutgoingMessageToDelete(String seqId, Long msgNo) {
        // TODO Auto-generated method stub
    }

    public boolean addIncomingSequence(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.server.ISandeshaDAO#addOutQueueMessage()
     */
    public boolean addPriorityMessage(RMMessageContext msg) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.server.ISandeshaDAO#getNextMessageToSend()
     */
    public RMMessageContext getNextMessageToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addMessageToSequence(java.lang.String,
     *      java.lang.Long, org.apache.sandesha.RMMessageContext)
     */
    public boolean addMessageToIncomingSequence(String sequenceId, Long msgNo,
                                                RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isSequenceExists(java.lang.String)
     */
    public boolean isIncomingSequenceExists(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextMsgContextToProcess(java.lang.String)
     */
    public RMMessageContext getNextMsgContextToProcess(String sequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#hasNewMessages()
     */
    public boolean hasNewMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getRandomSeqIdToProcess()
     */
    public String getRandomSeqIdToProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getAllReceivedMsgNumsOfSeq(java.lang.String)
     */
    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isMessageExists(java.lang.String,
     *      java.lang.String)
     */
    public boolean isIncomingMessageExists(String sequenceId, Long msgNo) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addMessageToResponseSequence(java.lang.String,
     *      org.apache.sandesha.RMMessageContext)
     */
    public boolean addMessageToOutgoingSequence(String sequenceId,
                                                RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addResponseSequence(java.lang.String)
     */
    public boolean addOutgoingSequence(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getAllReceivedMsgNumsOfResponseSeq(java.lang.String)
     */
    public Set getAllReceivedMsgNumsOfOutgoingSeq(String sequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextPriorityMessageContextToSend()
     */
    public RMMessageContext getNextPriorityMessageContextToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextResponseMsgContextToSend()
     */
    public RMMessageContext getNextOutgoingMsgContextToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isResponseSequenceExists(java.lang.String)
     */
    public boolean isOutgoingSequenceExists(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#setOutSequence(java.lang.String,
     *      java.lang.String)
     */
    public void setOutSequence(String sequenceId, String outSequenceId) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#setOutSequenceApproved(java.lang.String,
     *      boolean)
     */
    public void setOutSequenceApproved(String sequenceID, boolean approved) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getSequenceOfOutSequence(java.lang.String)
     */
    public String getSequenceOfOutSequence(String outsequenceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#moveResponseMessageToBin(java.lang.String,
     *      java.lang.Long)
     */
    public void moveOutgoingMessageToBin(String sequenceId, Long msgNo) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#removeCreateSequenceMsg(java.lang.String)
     */
    public void removeCreateSequenceMsg(String messageId) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextOutgoingMessageNumber(java.lang.String)
     */
    public long getNextOutgoingMessageNumber(String sequenceId) {
        // TODO Auto-generated method stub
        return 0;
    }


    public RMMessageContext checkForResponseMessage(String requestId, String SeqId) {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean isRequestMessagePresent(String sequenceId, String msgId) {
        // TODO Auto-generated method stub
        return false;
    }

    public String searchForSequenceId(String messageId) {
        return null;
    }


    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#compareAcksWithSequence(java.lang.String)
     */
    public boolean compareAcksWithSequence(String sequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addLowPriorityMessage(org.apache.sandesha.RMMessageContext)
     */
    public void addLowPriorityMessage(RMMessageContext msg) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getNextLowPriorityMessageContextToSend()
     */
    public RMMessageContext getNextLowPriorityMessageContextToSend() {
        // TODO Auto-generated method stub
        return null;
    }
    
    
	/* (non-Javadoc)
	 * @see org.apache.sandesha.storage.dao.ISandeshaDAO#addSendMsgNo(java.lang.String, long)
	 */
	public void addSendMsgNo(String seqId, long msgNo) {
		// TODO Auto-generated method stub

	}

	

    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getLastIncomingMsgNo(java.lang.String)
     */
    public long getLastIncomingMsgNo(String seqId) {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getLastOutgoingMsgNo(java.lang.String)
     */
    public long getLastOutgoingMsgNo(String seqId) {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#hasLastIncomingMsgReceived(java.lang.String)
     */
    public boolean hasLastIncomingMsgReceived(String seqId) {
        // TODO Auto-generated method stub
        return false;
    }
    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#hasLastOutgoingMsgReceived(java.lang.String)
     */
    public boolean hasLastOutgoingMsgReceived(String seqId) {
        // TODO Auto-generated method stub
        return false;
    }
	/* (non-Javadoc)
	 * @see org.apache.sandesha.storage.dao.ISandeshaDAO#isSentMsg(java.lang.String, long)
	 */
	public boolean isSentMsg(String seqId, long msgNo) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getKeyFromIncomingSequenceId(java.lang.String)
     */
    public String getKeyFromIncomingSequenceId(String seqID) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.sandesha.storage.dao.ISandeshaDAO#getKeyFromOutgoingSequenceId(java.lang.String)
     */
    public String getKeyFromOutgoingSequenceId(String seqID) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getAllOutgoingSequences() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}