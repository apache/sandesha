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

package org.apache.sandesha.client;

import org.apache.sandesha.RMSequence;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * class ClientMessageController
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class ClientMessageController {
    /**
     * Field instance
     */
    private static ClientMessageController instance;

    /**
     * Field sequenceMap
     */
    private Map sequenceMap;

    /**
     * Field seqAck
     */
    private SequenceAcknowledgement seqAck;

    /**
     * Constructor ClientMessageController
     */
    private ClientMessageController() {
        sequenceMap = new HashMap();
    }

    /**
     * Method getInstance
     * 
     * @return 
     */
    public static ClientMessageController getInstance() {

        System.out.println("MessageController::getInstance");

        if (instance == null) {
            instance = new ClientMessageController();
        }

        return instance;
    }

    /**
     * Method storeSequence
     * <p/>
     * stores a sequence object in the map. Each of these sequence objects
     * consists of one or more message objects.
     * The sequences are stored as the sequenceIdentifier as a key
     * 
     * @param sequence 
     */
    public void storeSequence(RMSequence sequence) {

        // System.out.println("----------------storeSequence::"+sequence.getSequenceIdetifer());
        sequenceMap.put(sequence.getSequenceIdentifier().toString(), sequence);
    }

    /**
     * Method retrieveIfSequenceExists
     * <p/>
     * returns a RMSequence if a sequence for the identifier exists.
     * else return a null value
     * <b>developer must handle the null value returned</b>
     * 
     * @param identifier 
     * @return RMSequence
     */
    public RMSequence retrieveIfSequenceExists(Identifier identifier) {

        if (sequenceMap.get(identifier.toString()) != null) {
            return ((RMSequence) sequenceMap.get(identifier.toString()));
        } else {
            return null;
        }
    }

    /**
     * Method getSeqAck
     * 
     * @return SequenceAcknowledgement
     */
    public SequenceAcknowledgement getSeqAck() {
        return seqAck;
    }

    /**
     * Method setSeqAck
     * 
     * @param acknowledgement 
     */
    public void setSeqAck(SequenceAcknowledgement acknowledgement) {
        seqAck = acknowledgement;
    }
}
