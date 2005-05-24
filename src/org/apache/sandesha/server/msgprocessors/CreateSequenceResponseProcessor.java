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
package org.apache.sandesha.server.msgprocessors;

import org.apache.axis.AxisFault;
import org.apache.axis.message.addressing.RelatesTo;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.CreateSequenceResponse;

/**
 * @author JEkanayake
 */
public class CreateSequenceResponseProcessor implements IRMMessageProcessor {
    IStorageManager storageManager = null;

    public CreateSequenceResponseProcessor(IStorageManager storageManger) {
        this.storageManager = storageManger;
    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {

        CreateSequenceResponse createSeqRes = rmMessageContext.getRMHeaders()
                .getCreateSequenceResponse();

        RelatesTo relatesTo = (RelatesTo) rmMessageContext.getAddressingHeaders().getRelatesTo()
                .get(0);
        String sequenceID = createSeqRes.getIdentifier().toString();
        //Approve the sequences. Now we can start sending the messages using
        // that sequence.

        storageManager.setApprovedOutSequence(relatesTo.getURI().toString(), sequenceID);

        String offerID = storageManager.getOffer(relatesTo.getURI().toString());

        if (createSeqRes.getAccept() != null) {
            String key = (new Long(System.currentTimeMillis())).toString();

            storageManager.addRequestedSequence(offerID);
            storageManager.setAcksTo(offerID,createSeqRes.getAccept().getAcksTo().getAddress().toString());

        }
        //No response to this message.
        return false;
    }

}