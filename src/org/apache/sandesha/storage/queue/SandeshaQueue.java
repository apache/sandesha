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
import org.apache.commons.logging.Log;
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
    private List requestedSequences;
    private static final Log log = LogFactory.getLog(SandeshaQueue.class.getName());

    private SandeshaQueue() {
        incomingMap = new HashMap();
        outgoingMap = new HashMap();
        highPriorityQueue = new ArrayList();
        queueBin = new HashMap();
        lowPriorityQueue = new ArrayList();
        requestedSequences = new ArrayList();
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
                //Messages will not be replaced automatically.

                //setting last message
                if (msgCon.isLastMessage())
                    seqHash.setLastMsg(msgCon.getMsgNumber());

                //setting sequence id
                //TODO: Do this in create seq response processor
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

            }
        }
        return successful;
    }

    public boolean messagePresentInIncomingSequence(String sequenceId, Long messageNo)
            throws QueueException {
        IncomingSequence seqHash = (IncomingSequence) incomingMap.get(sequenceId);

        if (seqHash == null)
            throw new QueueException(Constants.Queue.SEQUENCE_NOTPRESENT);

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
            IncomingSequence sh = null;
            String seqId = null;

            whileLoop: while (it.hasNext()) {
                String tempSeqId = (String) it.next();
                sh = (IncomingSequence) incomingMap.get(tempSeqId);
                if (sh.hasProcessableMessages()) {
                    seqId = tempSeqId;
                    break whileLoop;
                }
            }
            return seqId;
        }
    }

    public RMMessageContext nextIncomingMessageToProcess(String sequenceId) throws QueueException {
        if (sequenceId == null)
            return null;

        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
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
                            if (currentTime >= lastSentTime + Constants.RETRANSMISSION_INTERVAL) {
                                tempMsg.setLastSentTime(currentTime);
                                msg = tempMsg;
                                break forLoop;
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

    public Vector nextAllMessagesToProcess(String sequenceId) throws QueueException {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
        synchronized (sh) {
            Vector v = sh.getNextMessagesToProcess();
            return v;
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

    public void removeAllMsgsFromIncomingSeqence(String seqId, boolean yes) {
        if (!yes)
            return;
        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);
        sh.clearSequence(yes);
    }

    public void removeAllMsgsFromOutgoingSeqence(String seqId, boolean yes) {
        if (!yes)
            return;
        OutgoingSequence sh = (OutgoingSequence) outgoingMap.get(seqId);
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
        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
        sh.setProcessLock(lock);
    }

    public Set getAllReceivedMsgNumsOfIncomingSeq(String sequenceId) {
        Vector v = new Vector();
        IncomingSequence sh = (IncomingSequence) incomingMap.get(sequenceId);
        if (sh != null)
            return sh.getAllKeys();
        else
            return null;
    }

    public Set getAllReceivedMsgNumsOfOutgoingSeq(String sequenceId) {
        Vector v = new Vector();
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(sequenceId);
        synchronized (rsh) {
            return rsh.getAllKeys();
        }
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
            log.info("ERROR: RESPONSE SEQ IS NULL");
            return;
        }

        synchronized (rsh) {
            rsh.setOutSequenceId(outSeqId);
        }
    }

    public void setOutSequenceApproved(String seqId, boolean approved) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
            log.info("ERROR: RESPONSE SEQ IS NULL");
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

        Iterator it = outgoingMap.keySet().iterator();
        synchronized (outgoingMap) {
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

    public void moveOutgoingMsgToBin(String sequenceId, Long messageNo) {
        String sequence = getSequenceOfOutSequence(sequenceId);
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(sequence);

        if (rsh == null) {
            System.out.println(Constants.Queue.RESPONSE_SEQ_NULL);
            return;
        }

        synchronized (rsh) {
            //Deleting retuns the deleted message.
            RMMessageContext msg = rsh.deleteMessage(messageNo);
            //If we jave already deleted then no message to return.
            if (msg != null) {

                String msgId = msg.getMessageID();
                log.info("INFO: Moving out going messages to bin");
                //Add msg to bin if id isnt null.
                if (msgId != null)
                    queueBin.put(msgId, msg);
            }

        }
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
                if (msg.getMessageID().equals(messageId)) {
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

    public RMMessageContext checkForResponseMessage(String requestId, String seqId) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);
        if (sh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
            return null;
        }
        synchronized (sh) {
            RMMessageContext msg = sh.getMessageRelatingTo(requestId);
            return msg;
        }
    }

    public boolean isRequestMsgPresent(String seqId, String messageId) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);
        if (rsh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
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
                OutgoingSequence hash = (OutgoingSequence) obj;
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
            if (seqId != sId) //Sorry. Wrong sequence.
                continue;

            List ackList = seqAck.getAckRanges();
            Iterator ackIt = ackList.iterator();
            while (ackIt.hasNext()) {
                AcknowledgementRange ackRng = (AcknowledgementRange) ackIt.next();
                long min = ackRng.getMinValue();
                long temp = min;
                while (temp <= ackRng.getMaxValue()) {
                    Long lng = new Long(temp);
                    if (!msgNumbers.contains(lng)) //vector cant hv duplicate
                        // entries.
                        msgNumbers.add(new Long(temp));
                    temp++;
                }
            }
        }
        return msgNumbers;
    }

    public Vector getAllOutgoingMsgNumbers(String seqId) {
        Vector msgNumbers = new Vector();
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);
        if (rsh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
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
                OutgoingSequence hash = (OutgoingSequence) obj;
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
                OutgoingSequence hash = (OutgoingSequence) obj;
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

    public void addSendMsgNo(String seqId, long msgNo) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);
        if (rsh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
        }
        synchronized (rsh) {
            rsh.addMsgToSendList(msgNo);
        }
    }

    public boolean isSentMsg(String seqId, long msgNo) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
        }
        synchronized (rsh) {
            return rsh.isMsgInSentList(msgNo);
        }
    }

    public boolean hasLastOutgoingMsgReceived(String seqId) {

        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
        }
        synchronized (rsh) {
            return rsh.hasLastMsgReceived();
        }
    }

    public long getLastOutgoingMsgNo(String seqId) {
        OutgoingSequence rsh = (OutgoingSequence) outgoingMap.get(seqId);

        if (rsh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);

        }
        synchronized (rsh) {
            return rsh.getLastMsgNumber();
        }
    }

    public boolean hasLastIncomingMsgReceived(String seqId) {

        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);

        if (sh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);
            return false;
        }
        synchronized (sh) {
            return sh.hasLastMsgReceived();
        }
    }

    public long getLastIncomingMsgNo(String seqId) {
        IncomingSequence sh = (IncomingSequence) incomingMap.get(seqId);
        if (sh == null) {
            log.error(Constants.Queue.SEQUENCE_ABSENT);

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

    public String getKeyFromOutgoingSequenceId(String seqId) {
        Iterator it = outgoingMap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            OutgoingSequence os = (OutgoingSequence) outgoingMap.get(key);

            String seq = os.getSequenceId();
            if (seq == null)
                continue;

            if (seq.equals(seqId))
                return key;
        }
        return null;
    }
}

