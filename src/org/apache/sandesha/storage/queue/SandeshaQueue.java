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

package org.apache.sandesha.storage.queue;


import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.AcknowledgementRange;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;

import java.util.*;

/*
 * Created on Aug 4, 2004 at 4:49:49 PM
 */

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaQueue {

    private static SandeshaQueue queue = null;

    HashMap incomingMap; //In comming messages.

    HashMap outgoingMap; //Response messages

    ArrayList highPriorityQueue; // Acks and create seq. responses.

    HashMap queueBin; // Messaged processed from out queue will be moved

    ArrayList lowPriorityQueue;
    // to this.

    // to this.

    private SandeshaQueue() {
        incomingMap = new HashMap();
        outgoingMap = new HashMap();
        highPriorityQueue = new ArrayList();
        queueBin = new HashMap();
        lowPriorityQueue = new ArrayList();
    }

    public static SandeshaQueue getInstance() {
        if (queue == null) {
            queue = new SandeshaQueue();
        }
        return queue;
    }

    /**
     * This will not replace messages automatically.
     */
    public boolean addMessageToIncomingSequence(String seqId, Long messageNo, RMMessageContext msgCon) throws QueueException {
        boolean successful = false;

        if (seqId == null || msgCon == null)
            throw new QueueException("Error in adding message");

        if (isIncomingSequenceExists(seqId)) {
            SequenceHash seqHash = (SequenceHash) incomingMap.get(seqId);

            synchronized (seqHash) {

                if (seqHash == null)
                    throw new QueueException("Inconsistent queue");

                if (seqHash.hasMessage(messageNo))
                    throw new QueueException("Message already exists");
                //Messages will not be replaced automatically.

                seqHash.putNewMessage(messageNo, msgCon);
            }
        }

        return successful;
    }

    /**
     *  
     */
    public boolean addMessageToOutgoingSequence(String seqId,
                                                RMMessageContext msgCon) throws QueueException {
        boolean successful = false;

        if (seqId == null || msgCon == null)
            throw new QueueException("Error in adding message");

        if (isOutgoingSequenceExists(seqId)) {
            ResponseSequenceHash resSeqHash = (ResponseSequenceHash) outgoingMap
                    .get(seqId);

            synchronized (resSeqHash) {

                if (resSeqHash == null)
                    throw new QueueException("Inconsistent queue");
                resSeqHash.putNewMessage(msgCon);
                
                //if last message
                if(msgCon.isLastMessage())
                	resSeqHash.setLastMsg(msgCon.getMsgNumber());
                	
            }
        }

        return successful;
    }

    public boolean messagePresentInIncomingSequence(String sequenceId,
                                                    Long messageNo) throws QueueException {

        SequenceHash seqHash = (SequenceHash) incomingMap.get(sequenceId);

        if (seqHash == null)
            throw new QueueException("Sequence not present");

        synchronized (seqHash) {
            return seqHash.hasMessage(messageNo);
        }
    }

    public boolean isIncomingSequenceExists(String seqId) {

        synchronized (incomingMap) {

            return incomingMap.containsKey(seqId);
        }
    }

    public boolean isOutgoingSequenceExists(String resSeqId) {

        synchronized (outgoingMap) {

            return outgoingMap.containsKey(resSeqId);
        }
    }

    public String nextIncomingSequenceIdToProcess() {

        synchronized (incomingMap) {

            int count = incomingMap.size();
            Iterator it = incomingMap.keySet().iterator();
            SequenceHash sh = null;
            String seqId = null;

            whileLoop: while (it.hasNext()) {
                String tempSeqId = (String) it.next();
                sh = (SequenceHash) incomingMap.get(tempSeqId);
                if (sh.hasProcessableMessages()) {
                    seqId = tempSeqId;
                    break whileLoop;
                }
            }

            return seqId;
        }
    }

    public RMMessageContext nextIncomingMessageToProcess(String sequenceId)
            throws QueueException {

        if (sequenceId == null)
            return null;

        SequenceHash sh = (SequenceHash) incomingMap.get(sequenceId);

        synchronized (sh) {

            if (sh == null)
                throw new QueueException("Sequence id does not exist");

            if (!sh.hasProcessableMessages())
                return null;

            RMMessageContext msgCon = sh.getNextMessageToProcess();
            return msgCon;

        }
    }

    public RMMessageContext nextOutgoingMessageToSend() throws QueueException {

        RMMessageContext msg = null;

        synchronized (outgoingMap) {

            Iterator it = outgoingMap.keySet().iterator();

            whileLoop: while (it.hasNext()) {
                RMMessageContext tempMsg;
                String tempKey = (String) it.next();
                ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                        .get(tempKey);
                if (rsh.isOutSeqApproved()) {
                    tempMsg = rsh.getNextMessageToSend();
                    if (tempMsg != null) {
                        msg = tempMsg;
                        msg.setSequenceID(rsh.getOutSequenceId());
                        msg.setOldSequenceID(rsh.getSequenceId());
                        break whileLoop;
                    }
                }
            }
        }
        return msg;
    }

    public void createNewIncomingSequence(String sequenceId)
            throws QueueException {
        if (sequenceId == null)
            throw new QueueException("Sequence Id is null");

        synchronized (incomingMap) {

            SequenceHash sh = new SequenceHash(sequenceId);
            incomingMap.put(sequenceId, sh);

        }
    }

    public void createNewOutgoingSequence(String sequenceId)
            throws QueueException {
        if (sequenceId == null)
            throw new QueueException("Sequence Id is null");

        synchronized (outgoingMap) {

            ResponseSequenceHash rsh = new ResponseSequenceHash(sequenceId);
            outgoingMap.put(sequenceId, rsh);
        }

    }

    /**
     * Adds a new message to the responses queue.
     */
    public void addPriorityMessage(RMMessageContext msg) throws QueueException {

        synchronized (highPriorityQueue) {

            if (msg == null)
                throw new QueueException("Message is null");

            highPriorityQueue.add(msg);
        }
    }


    public void addLowPriorityMessage(RMMessageContext msg) throws QueueException {

        synchronized (lowPriorityQueue) {

            if (msg == null)
                throw new QueueException("Message is null");

            lowPriorityQueue.add(msg);
        }
    }


    public RMMessageContext nextPriorityMessageToSend() throws QueueException {

        synchronized (highPriorityQueue) {

            if (highPriorityQueue.size() <= 0)
                return null;

            //RMMessageContext msg = (RMMessageContext) highPriorityQueue.get(0);
            RMMessageContext msg = null;
            int size = highPriorityQueue.size();

            synchronized (highPriorityQueue) {

                forLoop: //Label
                for (int i = 0; i < size; i++) {
                    RMMessageContext tempMsg = (RMMessageContext) highPriorityQueue
                            .get(i);
                    if (tempMsg != null) {

                        switch (tempMsg.getMessageType()) {
                            //Create seq messages will not be removed.
                            case Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST:
                                long lastSentTime = tempMsg.getLastSentTime();
                                Date d = new Date();
                                long currentTime = d.getTime();
                                if (currentTime >= lastSentTime
                                        + Constants.RETRANSMISSION_INTERVAL) {
                                    tempMsg.setLastSentTime(currentTime);
                                    msg = tempMsg;
                                    break forLoop;
                                }
                                break;

                                //Other msgs will be removed.
                                //These include CreareSeqResponses and
                                // Acknowledgements.
                            default:
                                highPriorityQueue.remove(i);
                                queueBin.put(tempMsg.getMessageID(), tempMsg);
                                msg = tempMsg;
                                break forLoop;
                        }

                    }
                }
            }

            return msg;

        }
    }
    
    

    /*
     * public RMMessageContext getNextToProcessIfHasNew(String sequenceId){
     * SequenceHash sh = (SequenceHash) incomingMap.get(sequenceId);
     * if(sh==null) return null;
     * 
     * synchronized (sh) { if(!sh.hasNewMessages()) return null;
     * 
     * Long key = sh. } }
     */

    public Vector nextAllMessagesToProcess(String sequenceId)
            throws QueueException {
        SequenceHash sh = (SequenceHash) incomingMap.get(sequenceId);

        synchronized (sh) {
            Vector v = sh.getNextMessagesToProcess();
            return v;
        }
    }

    //Folowing func. may cause errors.
    /*
     * public Vector nextAllResponseMessagesToSend(String sequenceId) throws
     * QueueException{ ResponseSequenceHash rsh = (ResponseSequenceHash)
     * outgoingMap.get(sequenceId); Vector v = new Vector(); synchronized (rsh){
     * RMMessageContext msg = nextAllResponseMessagesToSend()
     * 
     * while(msg!=null){ v.add(msg); msg = rsh.getNextMessageToSend(); } return
     * v; } }
     */

    public Vector nextAllSeqIdsToProcess() {
        Vector ids = new Vector();

        synchronized (incomingMap) {
            Iterator it = incomingMap.keySet().iterator();

            while (it.hasNext()) {
                Object tempKey = it.next();
                SequenceHash sh = (SequenceHash) incomingMap.get(tempKey);
                if (sh.hasProcessableMessages() && !sh.isSequenceLocked())
                    ids.add(sh.getSequenceId());
            }
            return ids;
        }
    }

    /*
     * public Vector nextAllResponseSeqIdsToSend(){ Vector ids = new Vector();
     * 
     * synchronized (outgoingMap){ Iterator it =
     * outgoingMap.keySet().iterator();
     * 
     * while(it.hasNext()){ Object tempKey = it.next(); ResponseSequenceHash sh =
     * (ResponseSequenceHash) outgoingMap.get(tempKey);
     * if(sh.hasProcessableMessages()) ids.add(sh.getSequenceId()); } } return
     * ids; }
     */

    public void clear(boolean yes) {
        if (!yes)
            return;

        incomingMap.clear();
        highPriorityQueue.clear();
        outgoingMap.clear();
        queueBin.clear();
    }

    public void removeAllMsgsFromIncomingSeqence(String seqId, boolean yes) {
        if (!yes)
            return;

        SequenceHash sh = (SequenceHash) incomingMap.get(seqId);
        sh.clearSequence(yes);
    }

    public void removeAllMsgsFromOutgoingSeqence(String seqId, boolean yes) {
        if (!yes)
            return;

        ResponseSequenceHash sh = (ResponseSequenceHash) outgoingMap.get(seqId);
        sh.clearSequence(yes);
    }

    public void removeIncomingSequence(String sequenceId, boolean yes) {
        if (!yes)
            return;

        incomingMap.remove(sequenceId);
    }

    public void removeOutgoingSequence(String sequenceId, boolean yes) {
        if (!yes)
            return;

        synchronized (outgoingMap) {
            outgoingMap.remove(sequenceId);
        }
    }

    public void setSequenceLock(String sequenceId, boolean lock) {
        SequenceHash sh = (SequenceHash) incomingMap.get(sequenceId);
        sh.setProcessLock(lock);
    }

    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        Vector v = new Vector();
        SequenceHash sh = (SequenceHash) incomingMap.get(sequenceId);
        if (sh != null)
            return sh.getAllKeys();
        else
            return null;
    }

    public Set getAllReceivedMsgNumsOfOutgoingSeq(String sequenceId) {
        Vector v = new Vector();
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                .get(sequenceId);
        synchronized (rsh) {
            return rsh.getAllKeys();
        }
    }

    public boolean isIncomingMessageExists(String sequenceId, Long messageNo) {
        SequenceHash sh = (SequenceHash) incomingMap.get(sequenceId);
        //sh can be null if there are no messages at the initial point.
        if (sh != null)
            return sh.hasMessage(messageNo);
        else
            return false;
    }

    public void setOutSequence(String seqId, String outSeqId) {
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                .get(seqId);

        if (rsh == null) {
            System.out.println("ERROR: RESPONSE SEQ IS NULL");
            return;
        }

        synchronized (rsh) {
            rsh.setOutSequenceId(outSeqId);
        }
    }

    public void setOutSequenceApproved(String seqId, boolean approved) {
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                .get(seqId);

        if (rsh == null) {
            System.out.println("ERROR: RESPONSE SEQ IS NULL");
            return;
        }
        synchronized (rsh) {
            rsh.setOutSeqApproved(approved);
        }
    }

    public String getSequenceOfOutSequence(String outSequence) {

        if (outSequence == null) {
            return null;
        }

        //Client will always handle a single seq
        //if(outSequence==Constants.CLIENT_DEFAULD_SEQUENCE_ID)
        //    return outSequence;

        Iterator it = outgoingMap.keySet().iterator();
        synchronized (outgoingMap) {
            while (it.hasNext()) {

                String tempSeqId = (String) it.next();
                ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                        .get(tempSeqId);
                String tempOutSequence = rsh.getOutSequenceId();
                if (outSequence.equals(tempOutSequence))
                    return tempSeqId;

            }
        }
        return null;
    }

    public void displayOutgoingMap() {
        Iterator it = outgoingMap.keySet().iterator();
        System.out.println("------------------------------------");
        System.out.println("      DISPLAYING RESPONSE MAP");
        System.out.println("------------------------------------");
        while (it.hasNext()) {
            String s = (String) it.next();
            System.out.println("\n Sequence id - " + s);
            ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                    .get(s);

            Iterator it1 = rsh.getAllKeys().iterator();
            while (it1.hasNext()) {
                Long l = (Long) it1.next();
                String msgId = rsh.getMessageId(l);
                System.out.println("* key -" + l.longValue() + "- MessageID -"
                        + msgId + "-");
            }
        }
        System.out.println("\n");
    }

    public void displayIncomingMap() {
        Iterator it = incomingMap.keySet().iterator();
        System.out.println("------------------------------------");
        System.out.println("       DISPLAYING SEQUENCE MAP");
        System.out.println("------------------------------------");
        while (it.hasNext()) {
            String s = (String) it.next();
            System.out.println("\n Sequence id - " + s);
            SequenceHash sh = (SequenceHash) incomingMap.get(s);

            Iterator it1 = sh.getAllKeys().iterator();
            while (it1.hasNext()) {
                Long l = (Long) it1.next();
                String msgId = sh.getMessageId(l);
                System.out.println("* key -" + l.longValue() + "- MessageID -"
                        + msgId + "-");
            }
        }
        System.out.println("\n");
    }

    public void displayPriorityQueue() {

        System.out.println("------------------------------------");
        System.out.println("       DISPLAYING PRIORITY QUEUE");
        System.out.println("------------------------------------");

        Iterator it = highPriorityQueue.iterator();
        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) it.next();
            String id = msg.getMessageID();
            int type = msg.getMessageType();

            System.out.println("Message " + id + "  Type " + type);
        }
        System.out.println("\n");
    }

    public void moveOutgoingMsgToBin(String sequenceId, Long messageNo) {
        String sequence = getSequenceOfOutSequence(sequenceId);
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                .get(sequence);

        if (rsh == null) {
            System.out.println("ERROR: RESPONSE SEQ IS NULL " + sequence);
            return;
        }

        synchronized (rsh) {
            //Deleting retuns the deleted message.
            RMMessageContext msg = rsh.deleteMessage(messageNo);
            //If we jave already deleted then no message to return.
            if (msg != null) {

                String msgId = msg.getMessageID();
                System.out
                        .println("INFO: Moving out going messages to bin : msgId "
                        + msgId);
                //Add msg to bin if id isnt null.
                if (msgId != null)
                    queueBin.put(msgId, msg);
            }

        }
    }

    public void markOutgoingMessageToDelete(String sequenceId, Long messageNo) {
        String sequence = getSequenceOfOutSequence(sequenceId);
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap
                .get(sequence);

        if (rsh == null) {
            System.out.println("ERROR: RESPONSE SEQ IS NULL " + sequence);
            return;
        }

        synchronized (rsh) {
            //Deleting retuns the deleted message.
            rsh.markMessageDeleted(messageNo);
            //If we jave already deleted then no message to return.
        }

    }


    public void movePriorityMsgToBin(String messageId) {
        synchronized (highPriorityQueue) {
            int size = highPriorityQueue.size();
            for (int i = 0; i < size; i++) {
                RMMessageContext msg = (RMMessageContext) highPriorityQueue.get(i);

                if (msg.getMessageID().equals(messageId)) {

                    highPriorityQueue.remove(i);
                    queueBin.put(messageId, msg);
                    return;
                }
            }
        }
    }

    public long getNextOutgoingMessageNumber(String seq) {
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seq);

        if (rsh == null) { //saquence not created yet.
            try {
                createNewOutgoingSequence(seq);
            } catch (QueueException q) {
                System.out.println(q.getStackTrace());
            }
        }
        rsh = (ResponseSequenceHash) outgoingMap.get(seq);
        synchronized (rsh) {
            Iterator keys = rsh.getAllKeys().iterator();

            long msgNo = rsh.nextMessageNumber();

            /* while (keys.hasNext()) {
                 System.out.println("HAS KEYS");
                 long temp = ((Long) keys.next()).longValue();
                 System.out.println("TEMP IS "+temp);
                 if (temp > msgNo)
                     msgNo = temp;
             }

             msgNo++;*/
             
            System.out.println("RETURNING MSG NUMBER " + msgNo);
            return (msgNo);
        }
    }

    public RMMessageContext checkForResponseMessage(String requestId, String seqId) {
        SequenceHash sh = (SequenceHash) incomingMap.get(seqId);
        System.out.println("DEFAULT : " + requestId + " SEQ " + seqId);

        if (sh == null) {
            System.out.println("ERROR: SEQ IS NULL");
            return null;
        }

        synchronized (sh) {
            RMMessageContext msg = sh.getMessageRelatingTo(requestId);
            return msg;
        }
    }

    public boolean isRequestMsgPresent(String seqId, String messageId) {
        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seqId);

        if (rsh == null) {
            System.out.println("ERROR: SEQ IS NULL");
            return false;
        }

        boolean present = false;
        synchronized (rsh) {
            present = rsh.isMessagePresent(messageId);
        }

        return present;

    }

    public String searchForSequenceId(String messageId) {
        Iterator it = outgoingMap.keySet().iterator();

        String key = null;
        
        while (it.hasNext()) {
            key = (String) it.next();
            Object obj = outgoingMap.get(key);
            if (obj != null) {
                ResponseSequenceHash hash = (ResponseSequenceHash) obj;
                boolean hasMsg = hash.hasMessageWithId(messageId);
                if (!hasMsg)
                    key = null;
            }

        }

        return key;
    }

    public Vector getAllAckedMsgNumbers(String seqId) {
        Vector msgNumbers = new Vector();

        Iterator it = highPriorityQueue.iterator();
        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) it.next();
            if (msg.getMessageType() != Constants.MSG_TYPE_ACKNOWLEDGEMENT)
                continue;

            SequenceAcknowledgement seqAck = msg.getRMHeaders().getSequenceAcknowledgement();
            String sId = seqAck.getIdentifier().getIdentifier();
            if (seqId != sId)    //Sorry. Wrong sequence.
                continue;

            List ackList = seqAck.getAckRanges();

            Iterator ackIt = ackList.iterator();

            while (ackIt.hasNext()) {
                AcknowledgementRange ackRng = (AcknowledgementRange) ackIt.next();
                long min = ackRng.getMinValue();
                long temp = min;
                while (temp <= ackRng.getMaxValue()) {
                    Long lng = new Long(temp);
                    if (!msgNumbers.contains(lng))  //vector cant hv duplicate entries.
                        msgNumbers.add(new Long(temp));

                    temp++;
                }
            }
        }
        return msgNumbers;
    }

    public Vector getAllOutgoingMsgNumbers(String seqId) {
        Vector msgNumbers = new Vector();

        ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seqId);

        if (rsh == null) {
            System.out.println("ERROR: SEQ IS NULL " + seqId);
            return msgNumbers;
        }

        synchronized (rsh) {
            msgNumbers = rsh.getReceivedMsgNumbers();
        }

        return msgNumbers;
    }

    public void setResponseReceived(RMMessageContext responseMsg) {
        String requestMsgID = responseMsg.getAddressingHeaders().getRelatesTo().toString();

        Iterator it = outgoingMap.keySet().iterator();

        String key = null;
        while (it.hasNext()) {
            key = (String) it.next();
            Object obj = outgoingMap.get(key);
            if (obj != null) {
                ResponseSequenceHash hash = (ResponseSequenceHash) obj;
                boolean hasMsg = hash.hasMessageWithId(requestMsgID);
                if (!hasMsg)
                //set the property response received
                    hash.setResponseReceived(requestMsgID);
            }
        }

    }

    public void setAckReceived(String seqId, long msgNo) {

        Iterator it = outgoingMap.keySet().iterator();

        String key = null;
        while (it.hasNext()) {
            key = (String) it.next();
            Object obj = outgoingMap.get(key);


            if (obj != null) {
                ResponseSequenceHash hash = (ResponseSequenceHash) obj;
                //System.out.println("************** HASH SEQ IS " + hash.getSequenceId() + "      SEQ IS " + seqId + " OUT SEQ IS  " + hash.getOutSequenceId());
                if (hash.getOutSequenceId().equals(seqId)) {

                    hash.setAckReceived(msgNo);
                }
            }
        }

    }


    public Vector getAllIncommingMsgNumbers(String seqId) {
        Vector msgNumbers = new Vector();
        incomingMap.get(seqId);
        
        //Not implemented yet.
        return msgNumbers;
    }

    public RMMessageContext getLowPriorityMessageIfAcked() {

        int size = lowPriorityQueue.size();

        RMMessageContext terminateMsg = null;
        for (int i = 0; i < size; i++) {

            RMMessageContext temp;
            temp = (RMMessageContext) lowPriorityQueue.get(i);
            String seqId = temp.getSequenceID();
            // System.out.println(" HASH NOT FOUND SEQ ID " + seqId);

            ResponseSequenceHash hash = null;
            hash = (ResponseSequenceHash) outgoingMap.get(seqId);
            if (hash == null) {
                System.out.println("SandeshaQueue: ERROR: HASH NOT FOUND SEQ ID " + seqId);
            }

            /*Iterator it1 = outgoingMap.keySet().iterator();
            while(it1.hasNext()){
                hash = (ResponseSequenceHash) it1.next();
                if(hash.getOutSequenceId().equals(seqId)){
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ FOUND SEQ "+ seqId);
                    foundSeq = true;
                    break;
                }
            } */

            if (hash != null) {
                boolean complete = hash.isAckComplete();
                if (complete)
                //lowPriorityQueue.remove(i);
                    terminateMsg = temp;
                if (terminateMsg != null) {
                    terminateMsg.setSequenceID(hash.getOutSequenceId());
                    lowPriorityQueue.remove(i);
                    break;
                }
            }

        }

        return terminateMsg;
    }
    
    public void addSendMsgNo(String seqId,long msgNo){
    	ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seqId);
    	
        if (rsh == null) {
            System.out.println("ERROR: SEQ IS NULL");
        }
        
    	synchronized(rsh){
    		rsh.addMsgToSendList(msgNo);
    	}
    }
    
    public boolean isSentMsg(String seqId,long msgNo){
    	ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seqId);
    	
        if (rsh == null) {
            System.out.println("ERROR: SEQ IS NULL");
        }
        
    	synchronized(rsh){
    		return rsh.isMsgInSentList(msgNo);
    	}   	
    }
    
    public boolean hasLastMsgReceived(String seqId){
    	ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seqId);
    	
        if (rsh == null) {
            System.out.println("ERROR: SEQ IS NULL");
        }
        
        synchronized(rsh){
        	return rsh.hasLastMsgReceived();
        }        
    }

    public long getLastMsgNo(String seqId){
    	ResponseSequenceHash rsh = (ResponseSequenceHash) outgoingMap.get(seqId);
    	
        if (rsh == null) {
            System.out.println("ERROR: SEQ IS NULL");
        }
        
        synchronized(rsh){
        	return rsh.getLastMsgNumber();
        } 
    }
    
    
}

