/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha.client;

import java.util.Map;

import org.apache.axis.components.logger.LogFactory;
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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClientStorageManager implements IStorageManager{

	protected static Log log =
		 LogFactory.getLog(ClientStorageManager.class.getName());
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
    /*private void insertRequestMessage(RMMessageContext msg) {
		
        System.out.println("RESPONSE MESSAGE IS RECEIVED..");
        
        IServerDAO accessor =
		ServerDAOFactory.getStorageAccessor(
			Constants.SERVER_QUEUE_ACCESSOR);
		
        System.out.println("Client StorageManager is called");
		
        //This is the seuqnceid used to create the map entry.
        // (not the actual seq id of the msg).
		String sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;
		    
		boolean exists = accessor.isOutgoingSequenceExists(sequenceId);
		if(!exists)
		   accessor.addOutgoingSequence(sequenceId);
		
		accessor.addMessageToOutgoingSequence(sequenceId,msg);	       
    }*/

    /**
     * This can only be called by the SimpleAxisServer in the client side.
     */
    /*private void insertResponseMessage(RMMessageContext rmMessageContext) {
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);

		
		RMHeaders rmHeaders =rmMessageContext.getRMHeaders();
	
		String sequenceId = rmHeaders.getSequence().getIdentifier().getIdentifier();
     
		//String sequenceId = rmMessageContext.getSequenceID();
		boolean exists = accessor.isIncomingSequenceExists(sequenceId);

		if (!exists)
			addSequence(sequenceId);  //Creating new sequence
			

		//TODO: add getRmHeaders method to  MessageContext
		long messageNumber = rmHeaders.getSequence().getMessageNumber().getMessageNumber();
	
		if(messageNumber<=0)
			return;  //TODO: throw some exception
	        
		 Long msgNo = new Long(messageNumber);
		 accessor.addMessageToIncomingSequence(sequenceId,msgNo,rmMessageContext);  
        
    }*/

  
    public boolean isSequenceExist(String sequenceID) {
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		return accessor.isOutgoingSequenceExists(sequenceID);
    }

    
    public boolean isResponseSequenceExist(String sequenceID) {
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		return accessor.isIncomingSequenceExists(sequenceID);
    }

 /**
  * This will be used to inform the client about the presence of the response message.
  * But will be impemented later.
  */
    public RMMessageContext getNextMessageToProcess() {
        return null;
    }



  /**
   * This will be used both by the Sender and the SimpleAxisServer
   * to set the acks.
   */
    public void setAcknowledged(String seqID, long msgNumber) {
    	//seqId is just a dummy since the client will hv only a one seq.
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		
		
		//No hard checking. User may insert the real sequence. 
		
		/*if(!seqID.equals(Constants.CLIENT_DEFAULD_SEQUENCE_ID)){
			System.out.println("Error: Wrong sequence id for client");
			return;
		}*/
		
		//String sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;
		String sequenceId = seqID;
		accessor.moveOutgoingMessageToBin(sequenceId,new Long(msgNumber));
        
    }


    public void addSequence(String sequenceID) {
    			
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		//boolean result = accessor.addIncomingSequence(sequenceID);
		boolean result = accessor.addOutgoingSequence(sequenceID);
		
		if(!result)
		   log.error("Sequence was not created correcly in the in queue");
        
    }

    /**
     * This will be used both by the Sender and the SimpleAxisServer
     * to set the create sequence responses.
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
     * SimpleAxisServer will use this method to add acks for the 
     * application responses received from the server side. 
     */
    public void addAcknowledgement(RMMessageContext rmMessageContext) {
		addPriorityMessage(rmMessageContext);
        
    }
    
    //private method
	private void addPriorityMessage(RMMessageContext msg){
		IServerDAO accessor = ServerDAOFactory.getStorageAccessor(
		Constants.SERVER_QUEUE_ACCESSOR);
		
		accessor.addPriorityMessage(msg);		
	}
	

    /**
     * Check the existance of a message.
     */
    public boolean isMessageExist(String sequenceID, long messageNumber) {
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
		return accessor.isIncomingMessageExists(sequenceID,new Long(messageNumber));
    }

    /**
     * Get a Map of messages.
     */
    public Map getListOfMessageNumbers(String sequenceID) {
    	return null;
    	/*
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);
				
		Set st = accessor.getAllReceivedMsgNumsOfIncomingSeq(sequenceID);

		Iterator it = st.iterator();
		
		//To find the largest id present
		long largest=0;
		while(it.hasNext()){
			Long key = (Long) it.next();
			if(key==null)
				continue;
			    
			long l = key.longValue();
			if(l>largest)
				largest = l;
		}
		
		
		HashMap results = new HashMap();
		//Add Keys to the results in order.
		long currentPosition=1;
		for(long l=1;l<=largest;l++){
			boolean present = st.contains(new Long(l));
			if(present){
				results.put(new Long(currentPosition),new Long(l));
				currentPosition++;
			}
		}	
		return results;	
		*/
    }

    /**
     * This will be used by the sender.
     */
    public RMMessageContext getNextMessageToSend() {
        //System.out.println("getNextMessageToSend() is called");
		IServerDAO accessor =
					 ServerDAOFactory.getStorageAccessor(
						 Constants.SERVER_QUEUE_ACCESSOR);	
		RMMessageContext msg;
       
		msg = accessor.getNextPriorityMessageContextToSend();
       
		if(msg==null)
			msg = accessor.getNextOutgoingMsgContextToSend();
		
		 return msg;
    }

    /**
     * This will be used by the RMSender when adding messages to the 
     * Queue. RMSender will also add a createSequenceRequest message to the
     * prioriy queue using this temporary ID as the messageID.
     */
    public void setTemporaryOutSequence(String sequenceId, String outSequenceId) {
		
		if(!sequenceId.equals(Constants.CLIENT_DEFAULD_SEQUENCE_ID)){
			System.out.println("Error: Wrong sequence id for client");
			return;
		}
		
		IServerDAO accessor = ServerDAOFactory.getStorageAccessor(
		Constants.SERVER_QUEUE_ACCESSOR);	
		
		accessor.setOutSequence(sequenceId,outSequenceId);
		accessor.setOutSequenceApproved(sequenceId,false);
        
    }

  /**
   * This will be used by the SimpleAxisServer and the Sender to set the 
   * proper sequenceID
   */
    public boolean setApprovedOutSequence(String oldOutsequenceId, String newOutSequenceId) {
        
        //System.out.println("IN THE START OF SET APPROVED00 "+ oldOutsequenceId +"   "+newOutSequenceId);
        
		IServerDAO accessor = ServerDAOFactory.getStorageAccessor(
		Constants.SERVER_QUEUE_ACCESSOR);	
		
		boolean done = false;
		String sequenceID = accessor.getSequenceOfOutSequence(oldOutsequenceId);
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//System.out.println(sequenceID);
		
		if(sequenceID==null){
		    System.out.println("ERROR: setApprovedOutSequence()");
			return false;
		}
		//sequenceid should be the default one.	
		if(!sequenceID.equals(Constants.CLIENT_DEFAULD_SEQUENCE_ID)){
			System.out.println("Error: Wrong sequence id for client");
			return false;
		}	
		    
		accessor.setOutSequence(sequenceID,newOutSequenceId);
		accessor.setOutSequenceApproved(sequenceID,true);  
		
		//Deleting create sequence message from the priority queue.  
		//System.out.println("OLD OUT SEQ IS "+oldOutsequenceId);
		accessor.removeCreateSequenceMsg(oldOutsequenceId);
		
		return true; 
    }

    /**
     * This will be used by the RMSender when adding messages.
     * Initially it should return 1.
     */
    public long getNextMessageNumber(String sequenceID) {
	
		IServerDAO accessor = ServerDAOFactory.getStorageAccessor(Constants.SERVER_QUEUE_ACCESSOR);	
		long msgNo = accessor.getNextOutgoingMessageNumber (sequenceID);
		return msgNo;
        
    }
    
    
    /**
     * This sholud be called by the RMSender when adding request messages.
     * 
     */
     private void insertClientRequestMessage(RMMessageContext msg) {
 		IServerDAO accessor =
 		ServerDAOFactory.getStorageAccessor(
 			Constants.SERVER_QUEUE_ACCESSOR);
 		
         //System.out.println("Client StorageManager is called");
 		
         //This is the seuqnceid used to create the map entry.
         // (not the actual seq id of the msg).
 		String sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;
 		    
 		boolean exists = accessor.isOutgoingSequenceExists(sequenceId);
 		if(!exists)
 		   accessor.addOutgoingSequence(sequenceId);
 		
 		accessor.addMessageToOutgoingSequence(sequenceId,msg);	        
     }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#insertOutgoingMessage(org.apache.sandesha.RMMessageContext)
     */
    public void insertOutgoingMessage(RMMessageContext msg) {
        //System.out.println("RESPONSE MESSAGE IS RECEIVED..");
        
        IServerDAO accessor =
		ServerDAOFactory.getStorageAccessor(
			Constants.SERVER_QUEUE_ACCESSOR);
		
        //System.out.println("Client StorageManager is called");
		
        //This is the seuqnceid used to create the map entry.
        // (not the actual seq id of the msg).
		String sequenceId = Constants.CLIENT_DEFAULD_SEQUENCE_ID;
		    
		boolean exists = accessor.isOutgoingSequenceExists(sequenceId);
		if(!exists)
		   accessor.addOutgoingSequence(sequenceId);
		
		accessor.addMessageToOutgoingSequence(sequenceId,msg);	
        
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.IStorageManager#insertIncomingMessage(org.apache.sandesha.RMMessageContext)
     */
    public void insertIncomingMessage(RMMessageContext rmMessageContext) {
		IServerDAO accessor =
			ServerDAOFactory.getStorageAccessor(
				Constants.SERVER_QUEUE_ACCESSOR);

		
		RMHeaders rmHeaders =rmMessageContext.getRMHeaders();
	
		String sequenceId = rmHeaders.getSequence().getIdentifier().getIdentifier();
     
		//String sequenceId = rmMessageContext.getSequenceID();
		boolean exists = accessor.isIncomingSequenceExists(sequenceId);

		if (!exists)
			addSequence(sequenceId);  //Creating new sequence
			

		//TODO: add getRmHeaders method to  MessageContext
		long messageNumber = rmHeaders.getSequence().getMessageNumber().getMessageNumber();
	
		if(messageNumber<=0)
			return;  //TODO: throw some exception
	        
		 Long msgNo = new Long(messageNumber);
		 accessor.addMessageToIncomingSequence(sequenceId,msgNo,rmMessageContext);  
        
    }
    

}
