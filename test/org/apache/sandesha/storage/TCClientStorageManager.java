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

    public void testOutGoingMessages() throws QueueException {

        SandeshaQueue sq = SandeshaQueue.getInstance();

        ClientStorageManager csm = new ClientStorageManager();

        long l = csm.getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(l, 1);

        RMMessageContext msg = new RMMessageContext();
        msg.setMessageID("rmsg1");
        msg.setSequenceID(Constants.CLIENT_DEFAULD_SEQUENCE_ID);

        sq.createNewOutgoingSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        sq.addMessageToOutgoingSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID,
                msg);

        l = csm.getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(l, 2);

        msg = new RMMessageContext();
        msg.setMessageID("rmsg2");
        msg.setSequenceID(Constants.CLIENT_DEFAULD_SEQUENCE_ID);

        sq.addMessageToOutgoingSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID,
                msg);

        l = csm.getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(l, 3);

        msg = new RMMessageContext();
        msg.setMessageID("rmsg3");
        msg.setSequenceID(Constants.CLIENT_DEFAULD_SEQUENCE_ID);

        sq.addMessageToOutgoingSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID,
                msg);

        l = csm.getNextMessageNumber(Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(l, 4);

        msg = new RMMessageContext();
        msg.setMessageID("rmsg4");
        msg.setSequenceID(Constants.CLIENT_DEFAULD_SEQUENCE_ID);

        sq.addMessageToOutgoingSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID,
                msg);

        //csm.

        //sq.displayOutgoingMap();

        csm.setTemporaryOutSequence(Constants.CLIENT_DEFAULD_SEQUENCE_ID,
                "temp1");

        RMMessageContext createSeqReq1 = new RMMessageContext();
        createSeqReq1
                .setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        createSeqReq1.setMessageID("temp1");

        //RMMessageContext createSeqReq2 = new RMMessageContext();
        //createSeqReq2.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);
        //createSeqReq2.setMessageID("temp2");

        //sq.diaplayPriorityQueue();
        //csm.addCreateSequenceRequest(createSeqReq1);
        //sq.diaplayPriorityQueue();
        csm.addCreateSequenceRequest(createSeqReq1);
        //sq.displayPriorityQueue();

        RMMessageContext msg1;
        ClientStorageManager ssm1 = new ClientStorageManager();

        //sq.displayPriorityQueue();

        //msg1 = ssm1.getNextMessageToSend();
        //assertEquals(msg1.getMessageID(),"temp1");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getMessageID(), "temp1");

        //sq.displayPriorityQueue();
        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1, null);

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1, null);

        RMMessageContext ack1 = new RMMessageContext();
        ack1.setMessageID("msgack1");
        csm.addAcknowledgement(ack1);

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getMessageID(), "msgack1");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1, null);

        RMMessageContext createSeq = new RMMessageContext();
        createSeq.setMessageID("createSeqid1");

        csm.addCreateSequenceResponse(createSeq);

        sq.displayPriorityQueue();

        csm.setApprovedOutSequence("temp1", "approved1");

        sq.displayPriorityQueue();

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getMessageID(), "createSeqid1");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg2");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg4");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg1");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg3");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1, null);

        Date d1 = new Date();
        long l1 = d1.getTime();

        for (int i = 0; i < 2000000000; i++) {
        }

        Date d2 = new Date();
        long l2 = d2.getTime();

        if ((l2 - l1) < 4000) {
            System.out.println("Error: Wait time was not enough");
            return;
        }

        //msg1 = ssm1.getNextMessageToSend();
        //assertEquals(msg1.getMessageID(),"temp2");

        //ssm1.setApprovedOutSequence("temp2","approved2");

        sq.displayPriorityQueue();

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg2");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg4");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg1");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg3");

        msg1 = ssm1.getNextMessageToSend();
        //System.out.println(msg1.getMessageID());
        assertEquals(msg1, null);

        //sq.displayResponseMap();

        d1 = new Date();
        l1 = d1.getTime();

        for (int i = 0; i < 2140000000; i++) {
        }

        d2 = new Date();
        l2 = d2.getTime();

        if ((l2 - l1) < 4000) {
            System.out.println("Error: Wait time was not enough");
            return;
        }

        //Old messages must come again

        msg1 = ssm1.getNextMessageToSend();
        //System.out.println(msg1.getMessageID());
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg2");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg4");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg1");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg3");

        //sq.displayResponseMap();
        ssm1.setAcknowledged(Constants.CLIENT_DEFAULD_SEQUENCE_ID, 1);
        ssm1.setAcknowledged(Constants.CLIENT_DEFAULD_SEQUENCE_ID, 2);
        ssm1.setAcknowledged(Constants.CLIENT_DEFAULD_SEQUENCE_ID, 4);

        //sq.displayResponseMap();

        d1 = new Date();
        l1 = d1.getTime();

        for (int i = 0; i < 2140000000; i++) {
        }

        d2 = new Date();
        l2 = d2.getTime();

        if ((l2 - l1) < 4000) {
            System.out.println("Error: Wait time was not enough");
            return;
        }

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1.getSequenceID(), Constants.CLIENT_DEFAULD_SEQUENCE_ID);
        assertEquals(msg1.getMessageID(), "rmsg3");

        msg1 = ssm1.getNextMessageToSend();
        assertEquals(msg1, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        /*
         * super.setUp();
         * 
         * SandeshaQueue sq = SandeshaQueue.getInstance();
         * 
         * sq.clear(true);
         * 
         * sq.createNewIncomingSequence("test");
         * 
         * RMMessageContext msg1 = new RMMessageContext();
         * msg1.setMessageID("msgid1"); msg1.setSequenceID("test");
         * 
         * RMMessageContext msg2 = new RMMessageContext();
         * msg2.setMessageID("msgid2"); msg2.setSequenceID("test");
         * 
         * RMMessageContext msg3 = new RMMessageContext();
         * msg3.setMessageID("msgid3"); msg3.setSequenceID("test");
         * 
         * RMMessageContext msg4 = new RMMessageContext();
         * msg4.setMessageID("msgid4"); msg4.setSequenceID("test");
         * 
         * sq.addMessageToIncomingSequence("test", new Long(1), msg1);
         * sq.addMessageToIncomingSequence("test", new Long(2), msg2);
         * sq.addMessageToIncomingSequence("test", new Long(3), msg3);
         * sq.addMessageToIncomingSequence("test", new Long(4), msg4);
         */
    }
}