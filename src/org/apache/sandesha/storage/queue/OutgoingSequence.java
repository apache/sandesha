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

import java.util.*;

/*
 * Created on Aug 4, 2004 at 5:08:29 PM
 */

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

/**
 * This class works as a hash map for storing response messages until they are
 * sent.
 */
public class OutgoingSequence {

    private String sequenceId;
    private String outSequenceId;
    private boolean outSeqApproved;
    private HashMap hash;
    private Vector markedAsDelete;
    private Vector sendMsgNoList;
    private long lastMsgNo = -1;
    private long nextAutoNumber; // key for storing messages.
    private static final Log log = LogFactory.getLog(OutgoingSequence.class.getName());
    public boolean terminateSent = false;
    private boolean hasResponse = false;

    public boolean hasResponse() {
        return hasResponse;
    }

    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    public boolean isTerminateSent() {
        return terminateSent;
    }

    public void setTerminateSent(boolean terminateSent) {
        this.terminateSent = terminateSent;
    }

    public OutgoingSequence(String sequenceId) {
        this.sequenceId = sequenceId;
        hash = new HashMap();
        markedAsDelete = new Vector();
        nextAutoNumber = 1; //This is the key for storing messages.
        outSeqApproved = false;
        sendMsgNoList = new Vector();
    }

    /*
     * public boolean hasMessagesToSend(){ return hasMessagesToSend; }
     */

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public boolean isOutSeqApproved() {
        return outSeqApproved;
    }

    public void setOutSeqApproved(boolean b) {
        outSeqApproved = b;
    }

    public String getOutSequenceId() {
        return outSequenceId;
    }

    public void setOutSequenceId(String string) {
        outSequenceId = string;
    }

    /**
     * adds the message to map.
     */
    public Object putNewMessage(RMMessageContext msg) {
        Long key = new Long(nextAutoNumber);
        Object obj = hash.put(key, msg);
        increaseAutoNo();
        return obj;
    }

    /**
     * Increases auto number by 1.
     */
    private void increaseAutoNo() {
        nextAutoNumber++;
    }

    /**
     * Removes a message from the hash map.
     */
    public boolean removeMessage(long id) {
        //TODO: Add messageremoving code if needed.
        boolean removed = false;

        Long key = new Long(id);
        Object obj = hash.remove(key);

        if (obj != null)
            removed = true;
        return removed;
    }

    /**
     * Returns the next deliverable message if has any. Otherwise returns null.
     */
    public RMMessageContext getNextMessageToSend() {
        RMMessageContext minMsg = null;
        Iterator keys = hash.keySet().iterator();

        whileLoop: while (keys.hasNext()) {
            RMMessageContext tempMsg;
            tempMsg = (RMMessageContext) hash.get(keys.next());
            Long msgNo = new Long(tempMsg.getMsgNumber());
            if (markedAsDelete.contains(msgNo)) {
                continue;
            }
            long lastSentTime = tempMsg.getLastSentTime();
            Date d = new Date();
            long currentTime = d.getTime();
            if (currentTime >= lastSentTime + Constants.RETRANSMISSION_INTERVAL) {
                if (minMsg == null)
                    minMsg = tempMsg;
                else {
                    long msgNo1, msgNo2;
                    msgNo1 = tempMsg.getMsgNumber();
                    msgNo2 = minMsg.getMsgNumber();
                    if (msgNo1 < msgNo2)
                        minMsg = tempMsg;
                }
            }
        }

        Date d = new Date();
        long time = d.getTime();
        if (minMsg != null) {
            minMsg.setLastSentTime(time);
        }

        return minMsg;
    }

    public boolean hasMessage(Long key) {
        Object obj = hash.get(key);

        return (!(obj == null));
    }

    public void clearSequence(boolean yes) {
        if (!yes)
            return;
        hash.clear();
        nextAutoNumber = 1;
        outSeqApproved = false;
        outSequenceId = null;
        sequenceId = null;
    }

    public Set getAllKeys() {
        return hash.keySet();
    }

    public String getMessageId(Long key) {
        RMMessageContext msg = (RMMessageContext) hash.get(key);
        if (msg == null)
            return null;

        return msg.getMessageID();

    }

    //Deleting returns the deleted message.
    public RMMessageContext deleteMessage(Long msgId) {
        RMMessageContext msg = (RMMessageContext) hash.get(msgId);
        if (msg == null)
            return null;
        hash.remove(msgId);
        return msg;
    }

    public boolean markMessageDeleted(Long messageNo) {
        if (hash.containsKey(messageNo)) {
            markedAsDelete.add(messageNo);
            String msgId = ((RMMessageContext) hash.get(messageNo)).getMessageID();
            log.info("INFO: Marking outgoing message deleted : msgId " + msgId);
            return true;
        }
        return false;
    }

    public long nextMessageNumber() {
        return nextAutoNumber;
    }

    public boolean isMessagePresent(String msgId) {
        boolean b = false;
        b = hash.containsKey(msgId);
        return b;
    }

    public boolean hasMessageWithId(String msgId) {
        Iterator it = hash.keySet().iterator();
        while (it.hasNext()) {

            RMMessageContext msg = (RMMessageContext) hash.get(it.next());
            if (msg.getMessageID().equals(msgId))
                return true;
        }
        return false;
    }

    public Vector getReceivedMsgNumbers() {
        Vector result = new Vector();
        Iterator it = hash.keySet().iterator();

        while (it.hasNext()) {
            Object key = it.next();
            RMMessageContext msg = (RMMessageContext) hash.get(key);
            long l = msg.getMsgNumber();
            result.add(new Long(l));
        }
        return result;
    }

    public void setResponseReceived(String msgID) {
        Iterator it = hash.keySet().iterator();
        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) hash.get(it.next());
            if (msg.getMessageID().equals(msgID))
                msg.setResponseReceived(true);
        }
    }

    public void setAckReceived(String msgID) {
        Iterator it = hash.keySet().iterator();
        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) hash.get(it.next());
            if (msg.getMessageID().equals(msgID))
                msg.setAckReceived(true);
        }
    }

    public void setAckReceived(long msgNo) {
        RMMessageContext msg = (RMMessageContext) hash.get(new Long(msgNo));
        if (msg != null) {
            msg.setAckReceived(true);
        } else {
            log.error("ERROR: MESSAGE IS NULL IN ResponseSeqHash");
        }

    }

    public boolean isAckComplete() {
        try {
            long lastMsgNo = getLastMsgNumber();
            if (lastMsgNo <= 0) {
                return false;
            }
            Iterator it = hash.keySet().iterator();
            for (long i = 1; i < lastMsgNo; i++) {
                if (!hasMessage(new Long(i))) {
                    return false;
                }
            }

            it = hash.keySet().iterator();
            while (it.hasNext()) {
                RMMessageContext msg = (RMMessageContext) hash.get(it.next());
                if (!msg.isAckReceived()) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private long getLastMessage() {
        Iterator it = hash.keySet().iterator();
        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) hash.get(it.next());
            if (msg.isLastMessage()) {
                return msg.getMsgNumber();
            }
        }
        return -1;
    }

    public void addMsgToSendList(long msgNo) {
        sendMsgNoList.add(new Long(msgNo));
    }

    public boolean isMsgInSentList(long msgNo) {
        return sendMsgNoList.contains(new Long(msgNo));
    }

    public boolean hasLastMsgReceived() {
        if (lastMsgNo > 0)
            return true;

        return false;
    }

    public long getLastMsgNumber() {
        if (lastMsgNo > 0)
            return lastMsgNo;

        return -1;
    }

    public void setLastMsg(long lastMsg) {
        lastMsgNo = lastMsg;
    }

}