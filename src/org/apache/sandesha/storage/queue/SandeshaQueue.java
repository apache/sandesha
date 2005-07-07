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

import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.util.PolicyLoader;

import java.util.*;


/*
 * Created on Aug 4, 2004 at 4:49:49 PM
 */

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class SandeshaQueue {

    private static SandeshaQueue clientQueue = null;
    private static SandeshaQueue serverQueue = null;
    HashMap incomingMap; //In comming messages.
    HashMap outgoingMap; //Response messages
    ArrayList highPriorityQueue; // Acks and create seq. responses.
    HashMap queueBin; // Messaged processed from out queue will be moved
    ArrayList lowPriorityQueue;
    private List requestedSequences;
    HashMap acksToMap;
    HashMap offerMap;
    private static final Log log = LogFactory.getLog(SandeshaQueue.class.getName());

    public static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    private SandeshaQueue() {
        incomingMap = new HashMap();
        outgoingMap = new HashMap();
        highPriorityQueue = new ArrayList();
        queueBin = new HashMap();
        lowPriorityQueue = new ArrayList();
        requestedSequences = new ArrayList();
        acksToMap = new HashMap();
        offerMap = new HashMap();
    }

    public static SandeshaQueue getInstance(byte endPoint) {
        if (endPoint == Constants.CLIENT) {
            if (clientQueue == null) {
                clientQueue = new SandeshaQueue();
            }
            return clientQueue;
        } else {
            if (serverQueue == null) {
                serverQueue = new SandeshaQueue();
            }
            return serverQueue;
        }

    }

    public boolean addMessageToIncomingSequence(String seqId, Long messageNo,
                                                RMMessageContext msgCon) throws QueueException {
        boolean successful = false;

        if (seqId == null || msgCon == null)
            throw new QueueException(Constants.Queue.ADD_ERROR);

        if (isIncomingSequenceExists(seqId)) {
            IncomingSequence seqHash = (IncomingSequence) incomingMap.get(seqId);

            synchronized (seqHash) {
                if (seqHash == null)
                    throw new QueueException(Constants.Queue.QUEUE_INCONSIS);

                if (seqHash.hasMessage(messageNo))
                    throw new QueueException(Constants.Queue.MESSAGE_EXISTS);

                if (msgCon.isLastMessage())
                    seqHash.setLastMsg(msgCon.getMsgNumber());

                seqHash.setSequenceId(msgCon.getSequenceID());
                seqHash.putNewMessage(messageNo, msgCon);
                successful = true;
            }
        }

        return successful;
    }

    public boolean addMessageToOutgoingSequence(String seqId, RMMessageContext msgCon)
            throws QueueException {
        boolean successful = false;

        if (seqId == null || msgCon == null)
            throw new QueueException(Constants.Queue.ADD_ERROR);

        if (isOutgoingSequenceExists(seqId)) {
            OutgoingSequence resSeqHash = (OutgoingSequence) outgoingMap.get(seqId);

            synchronized (resSeqHash) {
                if (resSeqHash == null)
                    throw new QueueException(Constants.Queue.QUEUE_INCONSIS);
                resSeqHash.putNewMessage(msgCon);
                successful = true;

                //if last message
                if (msgCon.isLastMessage())
                    resSeqHash.setLastMsg(msgCon.getMsgNumber());

                if (msgCon.isHasResponse())
                    resSeqHash.setHasResponse(true);
            }
        }
        return successful;
    }

    public boolean isIncomingSequenceExists(String seqId) {
        synchronized (incomingMap) {
            return incomingMap.containsKey(seqId);
        }
    }

    public synchronized boolean isOutgoingSequenceExists(String resSeqId) {
        synchronized (outgoingMap) {
            return outgoingMap.containsKey(resSeqId);
        }
    }

    public RMMessageContext nextIncomingMessageToProcess(Object sequence) throws QueueException {
        if (sequence == null)
            return null;

        AbstractSequence absSeq=(AbstractSequence)sequence;

       IncomingSequence sh = (IncomingSequence) incomingMap.get(absSeq.getSequenceId());
        synchronized (sh) {
            if (sh == null)
                throw new QueueException(Constants.Queue.SEQUENCE_ABSENT);

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
                OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(tempKey);
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

    public void createNewIncomingSequence(String sequenceId) throws QueueException {
        if (sequenceId == null)
            throw new QueueException(Constants.Queue.SEQUENCE_ID_NULL);

        synchronized (incomingMap) {
            IncomingSequence sh = new IncomingSequence(sequenceId);
            incomingMap.put(sequenceId, sh);

        }
    }

    public void createNewOutgoingSequence(String sequenceId) throws QueueException {
        if (sequenceId == null)
            throw new QueueException(Constants.Queue.SEQUENCE_ID_NULL);

        synchronized (outgoingMap) {
            OutgoingSequence rsh = new OutgoingSequence(sequenceId);
            outgoingMap.put(sequenceId, rsh);
        }

    }

    /**
     * Adds a new message to the responses queue.
     */
    public void addPriorityMessage(RMMessageContext msg) throws QueueException {
        synchronized (highPriorityQueue) {
            if (msg == null)
                throw new QueueException(Constants.Queue.MESSAGE_ID_NULL);

            highPriorityQueue.add(msg);
        }
    }

    public void addLowPriorityMessage(RMMessageContext msg) throws QueueException {
        synchronized (lowPriorityQueue) {
            if (msg == null)
                throw new QueueException(Constants.Queue.MESSAGE_ID_NULL);
            lowPriorityQueue.add(msg);
        }
    }


    public RMMessageContext nextPriorityMessageToSend() throws QueueException {

        synchronized (highPriorityQueue) {


            if (highPriorityQueue.size() <= 0)
                return null;

            RMMessageContext msg = null;
            int size = highPriorityQueue.size();
            synchronized (highPriorityQueue) {
                forLoop: //Label
                for (int i = 0; i < size; i++) {
                    RMMessageContext tempMsg = (RMMessageContext) highPriorityQueue.get(i);
                    if (tempMsg != null) {
                        switch (tempMsg.getMessageType()) {
                            //Create seq messages will not be removed.
                            case Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST:
                                long lastSentTime = tempMsg.getLastSentTime();
                                Date d = new Date();
                                long currentTime = d.getTime();
                                if (currentTime >=
                                        lastSentTime + Constants.RETRANSMISSION_INTERVAL) {

                                    //EDITED FOR MSG NO REPITITION
                                    String oldOutSeqId, newOutSeqId;

                                    String oldCreateSeqId = tempMsg.getMessageID().toString();
                                    String uuid = uuidGen.nextUUID();


                                    String newCreateSeqId = Constants.UUID + uuid;
                                    tempMsg.setMessageID(newCreateSeqId);

                                    oldOutSeqId = oldCreateSeqId;
                                    newOutSeqId = newCreateSeqId;


                                    try {

                                        tempMsg.setLastSentTime(currentTime);
                                        msg = tempMsg;


                                        break forLoop;

                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                }
                                break;
                            case Constants.MSG_TYPE_ACKNOWLEDGEMENT:
                                
                                //acks are send in the folowing manner.
                                //If a ack the system has asked to send a ack (sequence.sendAck==true)
                                //then send it immediately.
                                //Also send a ack when a interval (ACKNOWLEDGEMENT_INTERVAL) has passed
                                //since last message arrived.
                                
                                String sequenceId = tempMsg.getSequenceID();
                                if (sequenceId == null)
                                    continue;

                                String key = getKeyFromIncomingSequenceId(sequenceId);
                                IncomingSequence sequence = (IncomingSequence) incomingMap.get(key);
                                if (sequence == null)
                                    continue;

                                d = new Date();
                                currentTime = d.getTime();

                                if (sequence.isSendAck()) {

                                    tempMsg.setLastSentTime(currentTime);
                                    msg = tempMsg;
                                    sequence.setSendAck(false);
                                    sequence.setFinalAckedTime(currentTime);
                                    break forLoop;

                                } else {
                                    long ackInterval = PolicyLoader.getInstance()
                                            .getAcknowledgementInterval();
                                    long finalAckedTime = sequence.getFinalAckedTime();
                                    long finalMsgArrivedTime = sequence.getFinalMsgArrivedTime();

                                    if ((finalMsgArrivedTime > finalAckedTime) &&
                                            (currentTime > finalMsgArrivedTime + ackInterval))
                                        sequence.setSendAck(true);
                                }

                                break;
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

       public Vector nextAllSeqsToProcess() {
           Vector seqs = new Vector();

        synchronized (incomingMap) {
            Iterator it = incomingMap.keySet().iterator();

            while (it.hasNext()) {
                Object tempKey = it.next();
                IncomingSequence sh = (IncomingSequence) incomingMap.get(tempKey);
                if (sh.hasProcessableMessages() && !sh.isSequenceLocked())
                    seqs.add(sh);
            }
            return seqs;
        }
       }

    public Vector nextAllSeqIdsToProcess() {
        Vector ids = new Vector();

        synchronized (incomingMap) {
            Iterator it = incomingMap.keySet().iterator();

            while (it.hasNext()) {
                Object tempKey = it.next();
                IncomingSequence sh = (IncomingSequence) incomingMap.get(tempKey);
                if (sh.hasProcessableMessages() && !sh.isSequenceLocked())
                    ids.add(sh.getSequenceId());
            }
            return ids;
        }
    }

    public void clear(boolean yes) {
        if (!yes)
            return;
        incomingMap.clear();
        highPriorityQueue.clear();
        outgoingMap.clear();
        queueBin.clear();
    }

    // --Commented out by Inspection START (6/8/05 1:19 PM):
    //    public void removeIncomingSequence(String sequenceId, boolean yes) {
    //        if (!yes)
    //            return;
    //        incomingMap.remove(sequenceId);
    //    }
    // --Commented out by Inspection STOP (6/8/05 1:19 PM)

    public void setSequenceLock(String sequenceId, boolean lock) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
        sh.setProcessLock(lock);
    }

    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
        if (sh != null)
            return sh.getAllKeys();
        else
            return null;
    }

    public boolean isIncomingMessageExists(String sequenceId, Long messageNo) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
        //sh can be null if there are no messages at the initial point.
        if (sh != null)
            return sh.hasMessage(messageNo);
        else
            return false;
    }

    public void setOutSequence(String seqId, String outSeqId) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
             if(log.isDebugEnabled())
            log.debug("ERROR: RESPONSE SEQ IS NULL");
            return;
        }

        synchronized (rsh) {
            rsh.setOutSequenceId(outSeqId);
        }
    }

    public void setOutSequenceApproved(String seqId, boolean approved) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
             if(log.isDebugEnabled())
            log.debug("ERROR: RESPONSE SEQ IS NULL");
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
        synchronized (outgoingMap) {
            Iterator it = outgoingMap.keySet().iterator();
            while (it.hasNext()) {

                String tempSeqId = (String) it.next();
                OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(tempSeqId);
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
            OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(s);

            System.out.println("out seq id:" + rsh.getOutSequenceId());
            Iterator it1 = rsh.getAllKeys().iterator();
            while (it1.hasNext()) {
                Long l = (Long) it1.next();
                String msgId = rsh.getMessageId(l);
                System.out.println("* key -" + l.longValue() + "- MessageID -" + msgId + "-");
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

            IncomingSequence sh = (IncomingSequence) incomingMap.get(s);

            Iterator it1 = sh.getAllKeys().iterator();
            while (it1.hasNext()) {
                Long l = (Long) it1.next();
                String msgId = sh.getMessageId(l);
                System.out.println("* key -" + l.longValue() + "- MessageID -" + msgId + "-");
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

    public void markOutgoingMessageToDelete(String sequenceId, Long messageNo) {
        String sequence = getSequenceOfOutSequence(sequenceId);
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(sequence);

        if (rsh == null) {
            log.error(Constants.Queue.RESPONSE_SEQ_NULL);
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

                String tempMsgId;
                try {
                    tempMsgId = (String) msg.getMessageIdList().get(0);
                } catch (Exception ex) {
                    tempMsgId = msg.getMessageID();
                }
                if (tempMsgId.equals(messageId)) {
                    highPriorityQueue.remove(i);
                    queueBin.put(messageId, msg);
                    return;
                }
            }
        }
    }

    public long getNextOutgoingMessageNumber(String seq) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seq);
        if (rsh == null) { //saquence not created yet.
            try {
                createNewOutgoingSequence(seq);
            } catch (QueueException q) {
                log.error(q.getStackTrace());
            }
        }
        rsh = (OutgoingSequence) outgoingMap.get(seq);
        synchronized (rsh) {
            Iterator keys = rsh.getAllKeys().iterator();

            long msgNo = rsh.nextMessageNumber();
            return (msgNo);
        }
    }

    public synchronized RMMessageContext checkForResponseMessage(String requestId, String seqId) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);
        if (sh == null) {
            return null;
        }
        synchronized (sh) {
            RMMessageContext msg = sh.getMessageRelatingTo(requestId);
            return msg;
        }
    }

    public String searchForSequenceId(String messageId) {
        Iterator it = outgoingMap.keySet().iterator();

        String key = null;
        while (it.hasNext()) {
            key = (String) it.next();
            Object obj = outgoingMap.get(key);
            if (obj != null) {
                OutgoingSequence hash = (OutgoingSequence) obj;
                boolean hasMsg = hash.hasMessageWithId(messageId);

                if (!hasMsg)
                    key = null;
                else
                    break;

            }

        }

        return key;
    }

    public void setAckReceived(String seqId, long msgNo) {
        Iterator it = outgoingMap.keySet().iterator();
        String key = null;
        while (it.hasNext()) {
            key = (String) it.next();
            Object obj = outgoingMap.get(key);

            if (obj != null) {
                OutgoingSequence hash = (OutgoingSequence) obj;
                if (hash.getOutSequenceId().equals(seqId)) {
                    hash.setAckReceived(msgNo);
                }
            }
        }

    }

    public RMMessageContext getLowPriorityMessageIfAcked() {
        synchronized (lowPriorityQueue) {
            int size = lowPriorityQueue.size();
            RMMessageContext terminateMsg = null;
            for (int i = 0; i < size; i++) {

                RMMessageContext temp;
                temp = (RMMessageContext) lowPriorityQueue.get(i);
                String seqId = temp.getSequenceID();
                OutgoingSequence hash = null;
                hash = (OutgoingSequence) outgoingMap.get(seqId);
                if (hash == null) {
                    log.error("ERROR: HASH NOT FOUND SEQ ID " + seqId);
                }
                if (hash != null) {
                    boolean complete = hash.isAckComplete();
                    if (complete)
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

    }

    public void addSendMsgNo(String seqId, long msgNo) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);
        if (rsh != null) {

            synchronized (rsh) {
                rsh.addMsgToSendList(msgNo);
            }
        }
    }

    public boolean isSentMsg(String seqId, long msgNo) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
            return false;
        }
        synchronized (rsh) {
            return rsh.isMsgInSentList(msgNo);
        }


    }

    public boolean hasLastIncomingMsgReceived(String seqId) {

        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);

        if (sh == null) {
            return false;
        }
        synchronized (sh) {
            return sh.hasLastMsgReceived();
        }
    }

    public long getLastIncomingMsgNo(String seqId) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);
        if (sh == null) {
            return 0;
        }
        synchronized (sh) {
            return sh.getLastMsgNumber();
        }
    }

    public void addRequestedSequence(String seqId) {
        requestedSequences.add(seqId);
    }

    public boolean isRequestedSeqPresent(String seqId) {
        return requestedSequences.contains(seqId);
    }

    public String getKeyFromIncomingSequenceId(String seqId) {
        synchronized (incomingMap) {
            Iterator it = incomingMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                IncomingSequence is = (IncomingSequence) incomingMap.get(key);
                String seq = is.getSequenceId();
                if (seq == null)
                    continue;

                if (seq.equals(seqId))
                    return key;
            }
            return null;
        }
    }

    /*public String getKeyFromOutgoingSequenceId(String seqId) {

        synchronized (outgoingMap) {
            System.out.println(" getKeyFromOutgoingSequenceId Received "+seqId);
            String key = null;
            Iterator it = outgoingMap.keySet().iterator();

            while (it.hasNext()) {
                key = (String) it.next();
                OutgoingSequence os = (OutgoingSequence) outgoingMap.get(key);

                String seq = os.getSequenceId();
                if (seq == null)
                    continue;

                if (seq.equals(seqId)) {
                     System.out.println(" getKeyFromOutgoingSequenceId Found "+key);
                    return key;

                }
            }
            System.out.println(" getKeyFromOutgoingSequenceId Found "+key);
            return key;
        }


    }*/

    public String getKeyFromOutgoingSequenceId(String seqId) {
        synchronized (outgoingMap) {
            Iterator it = outgoingMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                OutgoingSequence is = (OutgoingSequence) outgoingMap.get(key);
                String seq = is.getOutSequenceId();
                if (seq == null)
                    continue;

                if (seq.equals(seqId))
                    return key;
            }
            return null;
        }
    }

    public boolean isAllOutgoingTerminateSent() {
        synchronized (outgoingMap) {
            Iterator keys = outgoingMap.keySet().iterator();
            boolean found = false;

            while (keys.hasNext()) {
                OutgoingSequence ogs = (OutgoingSequence) outgoingMap.get(keys.next());
                if (ogs.isTerminateSent()) {
                    found = true;
                    break;
                }
            }

            return found;
        }
    }

    public boolean isAllIncommingTerminateReceived() {
        synchronized (incomingMap) {
            Iterator keys = incomingMap.keySet().iterator();

            while (keys.hasNext()) {
                Object key = keys.next();
                IncomingSequence ics = (IncomingSequence) incomingMap.get(key);
                OutgoingSequence ogs = (OutgoingSequence) outgoingMap.get(key);

                boolean hasResponse = ogs.hasResponse();

                if (hasResponse && !ics.isTerminateReceived())
                    return false;
            }

            return true;
        }
    }

    public void setTerminateSend(String seqId) {
        synchronized (outgoingMap) {
            OutgoingSequence ogs = (OutgoingSequence) outgoingMap.get(seqId);
            ogs.setTerminateSent(true);
        }
    }

    public void setTerminateReceived(String seqId) {
        IncomingSequence ics = (IncomingSequence) incomingMap.get(getKeyFromIncomingSequenceId(seqId));
        ics.setTerminateReceived(true);
    }

    public void setAcksTo(String seqId, String acksTo) {

        if (seqId == null) {
            log.error("ERROR: seq is null in setAcksTo");
            return;
        }

        acksToMap.put(seqId, acksTo);
    }

    public String getAcksTo(String seqId) {

        if (seqId == null) {
            log.error("ERROR: seq is null in getAcksTo");
            return null;
        }

        return (String) acksToMap.get(seqId);
    }


    public void addOffer(String msgID, String offerID) {
        if (msgID == null) {
            log.error(" MessageID is null in addOffer");
        }
        offerMap.put(msgID, offerID);
    }

    public String getOffer(String msgID) {
        if (msgID == null) {
            log.error(" MessageID is null in getOffer");
            return null;
        }
        return (String) offerMap.get(msgID);
    }

    public boolean isOutgoingTerminateSent(String seqId) {
        synchronized (outgoingMap) {
            OutgoingSequence ogs = (OutgoingSequence) outgoingMap.get(seqId);
            if (ogs != null) {
                if (ogs.isTerminateSent())
                    return true;
                else
                    return false;
            }
            return false;
        }

    }

    public boolean isIncommingTerminateReceived(String seqId) {
        synchronized (incomingMap) {

            IncomingSequence ics = (IncomingSequence) incomingMap.get(seqId);
            OutgoingSequence ogs = (OutgoingSequence) outgoingMap.get(seqId);

            boolean hasResponse = false;
            if (ogs != null) {
                hasResponse = ogs.hasResponse();
            }

            if (hasResponse && ics != null && !ics.isTerminateReceived())
                return false;
            else
                return true;
        }

    }

    public void updateFinalMessageArrivedTime(String sequenceId) {
        synchronized (incomingMap) {
            IncomingSequence ics = (IncomingSequence) incomingMap.get(sequenceId);
            if (ics == null)
                return;

            Date d = new Date();
            long time = d.getTime();
            ics.setFinalMsgArrivedTime(time);
        }
    }

    public void sendAck(String sequenceId) {
        synchronized (incomingMap) {
            IncomingSequence ics = (IncomingSequence) incomingMap.get(sequenceId);
            if (ics == null)
                return;

            ics.setSendAck(true);
        }
    }

    public void removeAllAcks(String sequenceID) {
        synchronized (highPriorityQueue) {
            int size = highPriorityQueue.size();

            for (int i = 0; i < size; i++) {
                RMMessageContext msg = (RMMessageContext) highPriorityQueue.get(i);
                if (msg.getSequenceID().equals(sequenceID) &&
                        msg.getMessageType() == Constants.MSG_TYPE_ACKNOWLEDGEMENT)
                    highPriorityQueue.remove(i);
            }
        }
    }


}

