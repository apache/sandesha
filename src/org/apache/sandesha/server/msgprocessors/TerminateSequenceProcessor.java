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

import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.TerminateSequence;

/**
 * @author
 */
public class TerminateSequenceProcessor implements IRMMessageProcessor {

    IStorageManager storageManger = null;

    public TerminateSequenceProcessor(IStorageManager storageManger) {
        this.storageManger = storageManger;
    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws RMException {

        TerminateSequence terminateSeq = rmMessageContext.getRMHeaders().getTerminateSequence();

        if (terminateSeq != null && terminateSeq.getIdentifier() != null) {
            String seqID = terminateSeq.getIdentifier().getIdentifier();
        }


        //TODO
        // *****************************************************************************
        //storageManger.terminateSequence(sequenceID);


        return false;
    }

}