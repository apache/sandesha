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

import org.apache.axis.message.addressing.MessageID;
import org.apache.sandesha.RMMessage;
import org.apache.sandesha.RMSequence;
import org.apache.sandesha.ws.utility.Identifier;

import java.util.HashMap;
import java.util.Map;


/**
 * @author
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class ClientMessageController {
    /**
     * Field instance
     */
    private static ClientMessageController instance;
    /**
     * Field messageMap
     */
    private Map messageMap;
    /**
     * Field sequenceMap
     */
    private Map sequenceMap;
    /**
     * Field seqAck
     */
 
    private Identifier sequenceIdentifier;
    
    /**
     * Constructor ClientMessageController
     */  
    private ClientMessageController() {
        sequenceMap = new HashMap();
        messageMap = new HashMap();
    }
    
    /**
     * Method getInstance
     * 
     * @return ClientMessageController
     */
     public static ClientMessageController getInstance() {
       
        if (instance == null) {
            instance = new ClientMessageController();
        }

        return instance;
    }
    
    /**
     * Method retrieveIfMessageExists
     *
     * returns a RMMessage if a message for the message id exists.
     * else return a null value
     * <b>developer must handle the null value returned</b>
     * 
     * @param messageID
     * @return RMMessage
     *
     * 
     */

    public RMMessage retrieveIfMessageExists(MessageID messageID) {
        RMMessage rmMessage = (RMMessage)messageMap.get(messageID.toString());
        if (rmMessage!= null) {
            return rmMessage;
        } else {
            return null;
        }
    }


    /**
     * Method storeSequence
     * 
     * stores a sequence object in the map. Each of these sequence objects
     * consists of one or more message objects.
     * The sequences are stored as the sequenceIdentifier as a key
     *
     * @param sequence
     *
     * 
     */
    public void storeSequence(RMSequence sequence) {
      
        sequenceMap.put(sequence.getSequenceIdetifer().toString(), sequence);
    }
    
    /**
     * Method storeMessage
     * 
     * stores a message object in the map. 
     * The message are stored as the message id as a key
     *
     * @param message
     *
     * 
     */

    public void storeMessage(RMMessage message) {
        messageMap.put(message.getMessageID().toString(), message);
    }


    /**
     * Method retrieveIfSequenceExists
     *
     * returns a RMSequence if a sequence for the identifier exists.
     * else return a null value
     * <b>developer must handle the null value returned</b>
     * @param identifier
     * 
     * @return RMSequence
     *
     * 
     */
    public RMSequence retrieveIfSequenceExists(Identifier identifier) {
        RMSequence rmSequence = (RMSequence)sequenceMap.get(identifier.getIdentifier().toString());
        if (rmSequence != null) {
            return rmSequence;
        } else {
            return null;
        }
    }


    
    /**
     * Method removeIfSequenceExists
     * 
     * Search for a sequence and if it exists(means it is in the map),
     * remove it from the map
     * 
     * @param identifier
     * 
     * 
     */
    public void removeIfSequenceExists(Identifier identifier) {
        if (sequenceMap.get(identifier.toString()) != null) {
            sequenceMap.remove(identifier.toString());
        }
    }
    /**
     * Method getSequenceIdentifier
     * 
     * @return Identifier
     */
    public Identifier getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    /**
     * Method setSequenceIdentifier
     * 
     * @param  identifier
     * 
     */
    public void setSequenceIdentifier(Identifier identifier) {
        sequenceIdentifier = identifier;
    }

}
