/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha.client;

import java.util.Map;

import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;

/**
 * @author Jaliya
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClientStorageManager implements IStorageManager{

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#init()
     */
    public void init() {
        // TODO Auto-generated method stub
        
    }

   /**
    * This sholud be called by the RMSender when adding request messages.
    * 
    */
    public void insertRequestMessage(RMMessageContext rmMessageContext) {
         
    }

    /**
     * This can only be called by the SimpleAxisServer in the client side.
     */
    public void insertResponseMessage(RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        
    }

  
    public boolean isSequenceExist(String sequenceID) {
        // TODO Auto-generated method stub
        return false;
    }

    
    public boolean isResponseSequenceExist(String sequenceID) {
        // TODO Auto-generated method stub
        return false;
    }

 /**
  * This will be used to inform the client about the presence of the response message.
  * But will be imlemented later.
  */
    public RMMessageContext getNextMessageToProcess() {
        // TODO Auto-generated method stub
        return null;
    }



  /**
   * This will be used both by the Sender and the SimpleAxisServer
   * to set the acks.
   */
    public void setAcknowledged(String seqID, long msgNumber) {
        // TODO Auto-generated method stub
        
    }


    public void addSequence(String sequenceID) {
        // TODO Auto-generated method stub
        
    }

    /**
     * This will be used both by the Sender and the SimpleAxisServer
     * to set the create sequence responses.
     */
    public void addCreateSequenceResponse(RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        
    }

   /**
    * This will be used by the RMSender to add the create sequence request.
    */
    public void addCreateSequenceRequest(RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        
    }

    /**
     * SimpleAxisServer will use this method to add acks for the 
     * application responses received from the server side. 
     */
    public void addAcknowledgement(RMMessageContext rmMessageContext) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Check the existance of a message.
     */
    public boolean isMessageExist(String sequenceID, long messageNumber) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Get a Map of messages.
     */
    public Map getListOfMessageNumbers(String sequenceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This will be used by the sender.
     */
    public RMMessageContext getNextMessageToSend() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This will be used by the RMSender when adding messages to the 
     * Queue. RMSender will also add a createSequenceRequest message to the
     * prioriy queue using this temporary ID as the messageID.
     */
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId) {
        // TODO Auto-generated method stub
        
    }

  /**
   * This will be used by the SimpleAxisServer and the Sender to set the 
   * proper sequenceID
   */
    public boolean setApprovedOutSequence(String oldOutsequenceId, String newOutSequenceId) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * This will be used by the RMSender when adding messages.
     * Initially it should return 1.
     */
    public long getNextMessageNumber(String sequenceID) {
        // TODO Auto-generated method stub
        return 0;
    }
    

}
