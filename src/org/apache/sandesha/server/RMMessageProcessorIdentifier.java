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
package org.apache.sandesha.server;

import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.msgprocessors.*;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author
 */
public class RMMessageProcessorIdentifier {
    /**
     * This method will identify the messages. Messages specific to the
     * reliablility are identified using the action. Request messages are
     * identified using the message number property.
     *
     * @param rmMessageContext
     * @param storageManager
     * @return
     */
    public static IRMMessageProcessor getMessageProcessor(RMMessageContext rmMessageContext, IStorageManager storageManager) {

        AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();

        if (addrHeaders.getAction() != null) {
            if (addrHeaders.getAction().toString().equals(Constants.ACTION_CREATE_SEQUENCE)) {
                return new CreateSequenceProcessor(storageManager);
            } else if (addrHeaders.getAction().toString().equals(Constants.ACTION_CREATE_SEQUENCE_RESPONSE)) {
                return new CreateSequenceResponseProcessor(storageManager);
            } else if (addrHeaders.getAction().toString().equals(Constants.ACTION_TERMINATE_SEQUENCE)) {
                return new TerminateSequenceProcessor(storageManager);
            } else if ((rmHeaders.getSequenceAcknowledgement() != null) || (rmHeaders.getSequence().getMessageNumber() != null)) {
                return new CompositeProcessor(storageManager);
            } else
                return new FaultProcessor(storageManager);
        } else if ((rmHeaders.getSequenceAcknowledgement() != null) || (rmHeaders.getSequence().getMessageNumber() != null)) {
            return new CompositeProcessor(storageManager);
        } else
            return new FaultProcessor(storageManager);
    }
}