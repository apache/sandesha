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

import java.util.Date;

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
    
    //tests functions related to outgoing messages.
    public void testOutGoingMessages() throws QueueException {
        SandeshaQueue sq = SandeshaQueue.getInstance();
        ClientStorageManager csm = new ClientStorageManager();
        
        //setup and add 4 messages
        long nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 1);
        RMMessageContext msg = new RMMessageContext();
        msg.setMessageID("rmsg1");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);

        sq.createNewOutgoingSequence("seqid1");
        sq.addMessageToOutgoingSequence("seqid1",msg);

        nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 2);
        msg = new RMMessageContext();
        msg.setMessageID("rmsg2");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);
        sq.addMessageToOutgoingSequence("seqid1",msg);

        nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 3);

        msg = new RMMessageContext();
        msg.setMessageID("rmsg3");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);

        sq.addMessageToOutgoingSequence("seqid1",msg);
        nextMsgNo = csm.getNextMessageNumber("seqid1");
        assertEquals(nextMsgNo, 4);
       
        msg = new RMMessageContext();
        msg.setMessageID("rmsg4");
        msg.setSequenceID("seqid1");
        msg.setMsgNumber(nextMsgNo);
        sq.addMessageToOutgoingSequence("seqid1",msg);
        csm.setTemporaryOutSequence("seqid1","temp1");

        RMMessageContext createSeqReq1 = new RMMessageContext();
        createSeqReq1.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        createSeqReq1.setMessageID("temp1");
        csm.addCreateSequenceRequest(createSeqReq1);

        RMMessageContext msg1;
        ClientStorageManager csm1 = new ClientStorageManager();

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getMessageID(), "temp1");


        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1, null);
        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1, null);

        RMMessageContext ack1 = new RMMessageContext();
        ack1.setMessageID("msgack1");
        csm.addAcknowledgement(ack1);

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getMessageID(), "msgack1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1, null);

        RMMessageContext createSeq = new RMMessageContext();
        createSeq.setMessageID("createSeqid1");

        csm.addCreateSequenceResponse(createSeq);
        
        sq.displayPriorityQueue();
        csm.setApprovedOutSequence("temp1", "approved1");
        sq.displayPriorityQueue();

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getMessageID(), "createSeqid1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1, null);

        Date d1 = new Date();
        long l1 = d1.getTime();

		try{
		    Thread.sleep(5000);
		}catch(InterruptedException e){
		    e.printStackTrace();
		}

        Date d2 = new Date();
        long l2 = d2.getTime();

        if ((l2 - l1) < 4000) {
            System.out.println("Error: Wait time was not enough");
            return;
        }

        sq.displayPriorityQueue();

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");
        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");
        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");
        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");
        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1, null);


        d1 = new Date();
        l1 = d1.getTime();

		try{
		    Thread.sleep(5000);
		}catch(InterruptedException e){
		    e.printStackTrace();
		}

        d2 = new Date();
        l2 = d2.getTime();

        if ((l2 - l1) < 4000) {
            System.out.println("Error: Wait time was not enough");
            return;
        }

        //Old messages must come again
        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");

        csm1.setAcknowledged("approved1", 1);
        csm1.setAcknowledged("approved1", 2);
        csm1.setAcknowledged("approved1", 4);


        d1 = new Date();
        l1 = d1.getTime();

		try{
		    Thread.sleep(5000);
		}catch(InterruptedException e){
		    e.printStackTrace();
		}

        d2 = new Date();
        l2 = d2.getTime();

        if ((l2 - l1) < 4000) {
            System.out.println("Error: Wait time was not enough");
            return;
        }

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), "approved1");
        assertEquals(msg1.getMessageID(), "rmsg3");

        msg1 = csm1.getNextMessageToSend();
        assertEquals(msg1, null);
    }


    protected void setUp() throws Exception {
         super.setUp();
         
         SandeshaQueue sq = SandeshaQueue.getInstance();
         
         sq.clear(true);
         
         sq.createNewIncomingSequence("test");
         
         RMMessageContext msg1 = new RMMessageContext();
         msg1.setMessageID("msgid1"); msg1.setSequenceID("test");
         
         RMMessageContext msg2 = new RMMessageContext();
         msg2.setMessageID("msgid2"); msg2.setSequenceID("test");
         
         RMMessageContext msg3 = new RMMessageContext();
         msg3.setMessageID("msgid3"); msg3.setSequenceID("test");
         
         RMMessageContext msg4 = new RMMessageContext();
         msg4.setMessageID("msgid4"); msg4.setSequenceID("test");
         
         sq.addMessageToIncomingSequence("test", new Long(1), msg1);
         sq.addMessageToIncomingSequence("test", new Long(2), msg2);
         sq.addMessageToIncomingSequence("test", new Long(3), msg3);
         sq.addMessageToIncomingSequence("test", new Long(4), msg4);
         
    }
}