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


import org.apache.axis.message.addressing.RelatesTo;
import org.apache.sandesha.RMMessageContext;

import java.util.*;

/*
 * Created on Aug 4, 2004 at 5:08:29 PM
 */

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */

public class IncomingSequence {

    private long lastProcessed;

    private boolean hasProcessableMessages;

    private String sequenceId;

    //private String outSequenceId;
    private HashMap hash;

    private boolean beingProcessedLock = false; //When true messages are


    // currently being processed.

    public IncomingSequence(String sequenceId) {
        lastProcessed = 0;
        //cacheBottom = 1;
        hasProcessableMessages = false;
        this.sequenceId = sequenceId;
        hash = new HashMap();
    }

    public boolean hasProcessableMessages() {
        return hasProcessableMessages;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    /**
     * adds the message to map. Also adds a record to cache if needed.
     */
    public Object putNewMessage(Long key, RMMessageContext value) {

        Object obj = hash.put(key, value);
        //addToCacheIfNeeded(key);
        refreshHasProcessableMessages();
        return obj;
    }

    public boolean removeMessage(long msgId) {
        //TODO: Add messageremoving code if needed.
        boolean removed = false;

        Long key = new Long(msgId);
        Object obj = hash.remove(key);

        if (obj != null)
            removed = true;

        return removed;
    }

    public long getNextMessageIdToProcess() {

        long id = lastProcessed + 1;

        return id;
    }

    public RMMessageContext getNextMessageToProcess() {
        Long nextKey = new Long(lastProcessed + 1);
        RMMessageContext msg = (RMMessageContext) hash.get(nextKey);
        if (msg != null) {
            incrementProcessedCount();
            refreshHasProcessableMessages();
        } else {
            setProcessLock(false); // Not a must. (det done in
            // refreshHasProcessableMessages();)
        }

        return msg;

    }

    public Vector getNextMessagesToProcess() {

        boolean done = false;
        Vector messages = new Vector();

        while (!done) {
            Long nextKey = new Long(lastProcessed + 1);
            Object obj = hash.get(nextKey);
            if (obj != null) {
                messages.add(obj);
                incrementProcessedCount();
            } else {
                setProcessLock(false);
                done = true; //To exit the loop.
            }
        }
        refreshHasProcessableMessages();

        return messages;
    }

    private void incrementProcessedCount() {
        lastProcessed++;
    }

    private void refreshHasProcessableMessages() {
        Long nextKey = new Long(lastProcessed + 1);
        hasProcessableMessages = hash.containsKey(nextKey);

        if (!hasProcessableMessages) //Cant be being procesed if no messages to
        // process.
            setProcessLock(false);
    }

    public boolean hasMessage(Long msgId) {
        Object obj = hash.get(msgId);

        return (!(obj == null));
    }

    public void clearSequence(boolean yes) {
        if (!yes)
            return;

        hash.clear();
        lastProcessed = 0;
        hasProcessableMessages = false;
    }

    public Set getAllKeys() {
        return hash.keySet();
    }

    public void setProcessLock(boolean lock) {
        beingProcessedLock = lock;
    }

    public boolean isSequenceLocked() {
        return beingProcessedLock;
    }

    public String getMessageId(Long key) {
        RMMessageContext msg = (RMMessageContext) hash.get(key);
        if (msg == null)
            return null;

        return msg.getMessageID();

    }

    //Only for client.
    public RMMessageContext getMessageRelatingTo(String relatesTo) {

        Iterator it = hash.keySet().iterator();
        RMMessageContext msgToSend = null;

        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) hash.get(it.next());
            List lst = msg.getAddressingHeaders().getRelatesTo();

            if (lst != null) {
                RelatesTo rl = (RelatesTo) lst.get(0);
                String uri = rl.getURI().toString();
                if (uri.equals(relatesTo))
                    msgToSend = msg;
                break;
            }
        }

        return msgToSend;
        
        /*Long nextKey = new Long(lastProcessed + 1);
        RMMessageContext msg = (RMMessageContext) hash.get(nextKey);
        if (msg != null) {
            incrementProcessedCount();
            refreshHasProcessableMessages();
        } else {
            setProcessLock(false); // Not a must. (det done in
            // refreshHasProcessableMessages();)
        }

        return msg;*/
    }

    public boolean isMessagePresent(String msgId) {
        boolean b = false;

        b = hash.containsKey(msgId);
        return b;
    }

    public boolean hasMessageWithId(String msgId) {
        //boolean b = false;
        
        Iterator it = hash.keySet().iterator();

        while (it.hasNext()) {
            RMMessageContext msg = (RMMessageContext) it.next();
            if (msg.getMessageID().equals(msgId))
                return true;
        }
        return false;
    }
    

    
}