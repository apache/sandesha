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
package org.apache.sandesha.storage;

import junit.framework.TestCase;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.storage.queue.QueueException;
import org.apache.sandesha.storage.queue.SandeshaQueue;

/**
 * @author Chamikara Jayalath
 * @author Jaliya Ekanayaka
 */
public class TCClientStorageManager extends TestCase {

    //For testing weather messages are re-transmitted correctly
    public void testRetransmission() {

        SandeshaQueue sq = SandeshaQueue.getInstance(Constants.CLIENT);
        ClientStorageManager csm = new ClientStorageManager();
        RMMessageContext msg1;
        
        //approving the out sequence
        csm.setTemporaryOutSequence("seqid1", "uuid:aaaa-bbbb-cccc");
        csm.setApprovedOutSequence("uuid:aaaa-bbbb-cccc", "approved1");

        //messages should be returned (since the out sequence is approved)
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);

        //Waiting for little more than re-transmission interval
        try {
            Thread.sleep(Constants.RETRANSMISSION_INTERVAL + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);

        //Again waiting for little more than re-transmission interval :)
        try {
            Thread.sleep(Constants.RETRANSMISSION_INTERVAL + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Messages should be returned once again
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);

    }

    //Testing weather the tr-transmission stops after a acknowledgement
    public void testAcknowledgement() {
        SandeshaQueue sq = SandeshaQueue.getInstance(Constants.CLIENT);
        ClientStorageManager csm = new ClientStorageManager();

        csm.setTemporaryOutSequence("seqid1", "uuid:aaaa-bbbb-cccc");
        csm.setApprovedOutSequence("uuid:aaaa-bbbb-cccc", "approved1");

        RMMessageContext msg1;
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1.setLocked(false);
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
        
        //Acknowledging messages 1,2 and 4 (without 3)
        csm.setAcknowledged("approved1", 1);
        csm.setAcknowledged("approved1", 2);
        csm.setAcknowledged("approved1", 4);
        
        //Waiting for little more than re-transmission interval
        try {
            Thread.sleep(Constants.RETRANSMISSION_INTERVAL + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Only message no. 3 should be re-transmitted
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        assertEquals(msg1.getMessageID(), "rmsg3");
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
    }
    
    /*
    //For testing weather getNextMsgToSend method works correctly
    public void testNextMsgToSend(){
        SandeshaQueue sq = SandeshaQueue.getInstance(Constants.CLIENT);
        ClientStorageManager csm = new ClientStorageManager();
        RMMessageContext msg1;
        
        //Next message to sent should be null (before approving outsequence)
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
        
        //approving the out sequence
        csm.setTemporaryOutSequence("seqid1","uuid:aaaa-bbbb-cccc");
        csm.setApprovedOutSequence("uuid:aaaa-bbbb-cccc", "approved1");

        //messages should be returned (since the out sequence is approved)
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
    }
    
    //Testing weather the out-sequence concept works correctly.
    //Outgoing messages should be sent only when the out sequence is approved.
    public void testOutSequence(){
        SandeshaQueue sq = SandeshaQueue.getInstance(Constants.CLIENT);
        ClientStorageManager csm = new ClientStorageManager();
        
        //setting temporary out sequence 
        csm.setTemporaryOutSequence("seqid1","uuid:aaaa-bbbb-cccc");
        
        RMMessageContext msg1;
        
        //the message should be null since the out sequence has not been approved
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
        
        //still the message should be null since the out sequence has not been approved
        msg1 = csm.getNextMessageToSend();
        assertNull(msg1);
        
        //approving the out sequence
        csm.setApprovedOutSequence("uuid:aaaa-bbbb-cccc", "approved1");

        //not the message should not be null (since out sequence was approved)
        msg1 = csm.getNextMessageToSend();
        assertNotNull(msg1);
    }
    
    //Testing weather priority messages are sent correctly.
    //They should be sent before application messages and should be sent
    //even before the out-sequence get set.
    public void testPriorityQueue(){
        SandeshaQueue sq = SandeshaQueue.getInstance(Constants.CLIENT);
        ClientStorageManager csm = new ClientStorageManager();
        
        //Addign a create sequence request message (this will be added to the priority 
        //area of the storage)
        RMMessageContext createSeqReq1 = new RMMessageContext();
        createSeqReq1.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        createSeqReq1.setMessageID("temp1");
        
        csm.addCreateSequenceRequest(createSeqReq1);
        
        RMMessageContext msg1;
        ClientStorageManager csm1 = new ClientStorageManager();

        //create sequence message should be returned
        msg1 = csm1.getNextMessageToSend();
        assertNotNull(msg1);
        
        //other messages should not be returned since the out sequence has not been approved.
        msg1 = csm1.getNextMessageToSend();
        assertNull(msg1);

        //Addign a acknowledgement message (this will be added to the priority 
        //area of the storage)
        RMMessageContext ack1 = new RMMessageContext();
        ack1.setMessageID("msgack1");
        csm.addAcknowledgement(ack1);

        //ack message should be returned
        msg1 = csm1.getNextMessageToSend();
        assertNotNull(msg1);
        
        //other messages should not be returned since the out sequence has not been approved.
        msg1 = csm1.getNextMessageToSend();
        assertNull(msg1);
    }*/
    
    
    public void setUp() throws QueueException {

        SandeshaQueue sq = SandeshaQueue.getInstance(Constants.CLIENT);
        ClientStorageManager csm = new ClientStorageManager();
        RMMessageContext msg = new RMMessageContext();
        
        //Creating a new outgoing sequence.
        sq.createNewOutgoingSequence("seqid1");
        
        //Adding messages to the outgoing sequence.
        
        //Adding message 1
        long nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 1);
        msg.setMessageID("rmsg1");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);
        sq.addMessageToOutgoingSequence("seqid1", msg);

        //Adding message 2
        nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 2);
        msg = new RMMessageContext();
        msg.setMessageID("rmsg2");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);
        sq.addMessageToOutgoingSequence("seqid1", msg);

        //Adding message 3
        nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 3);
        msg = new RMMessageContext();
        msg.setMessageID("rmsg3");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);

        //Adding message 4
        sq.addMessageToOutgoingSequence("seqid1", msg);
        nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 4);
        msg = new RMMessageContext();
        msg.setMessageID("rmsg4");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);
        sq.addMessageToOutgoingSequence("seqid1", msg);
    }

    public void tearDown() {

        //clearing the storage
        ClientStorageManager csm = new ClientStorageManager();
        csm.clearStorage();

    }


}