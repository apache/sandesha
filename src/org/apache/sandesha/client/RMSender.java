/*
* Copyright 1999-2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/
package org.apache.sandesha.client;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.util.RMMessageCreator;

public class RMSender extends BasicHandler {

    private IStorageManager storageManager;

    public void invoke(MessageContext msgContext) throws AxisFault {

        //TODO This we need to check.

        //Initialize the storage manager. We are in the client side So initialize the client Storage Manager.
        storageManager = new ClientStorageManager();
        //  RMInitiator.initClient(requestMesssageContext.getSync());
        try {
            RMMessageContext requestMesssageContext = RMMessageCreator.createServiceRequestMsg(msgContext);
            String sequenceID = requestMesssageContext.getSequenceID();
            //AddressingHeaders addrHeaders = getAddressingHeaders(requestMesssageContext);
            long msgNo = requestMesssageContext.getMsgNumber();

            if (msgNo == 1) {
                requestMesssageContext = processFirstRequestMessage(requestMesssageContext, requestMesssageContext.getSync());
            } else {
                requestMesssageContext = processRequestMessage(requestMesssageContext);
            }

            if (requestMesssageContext.isLastMessage()) {
                storageManager.insertTerminateSeqMessage(RMMessageCreator.createTerminateSeqMsg(requestMesssageContext));
            }

            if (requestMesssageContext.isHasResponse() && !requestMesssageContext.getSync()) {
                RMMessageContext responseMessageContext = null;
                while (responseMessageContext == null) {
                    //TODO Need to check for errors in the queue.
                    //If the queue has an error message, then need to report it
                    // to client.
                    responseMessageContext = checkTheQueueForResponse(sequenceID, requestMesssageContext.getMessageID());
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }
            } else {
                boolean gotAck = false;
                while (!gotAck) {
                    gotAck = checkTheQueueForAck(requestMesssageContext.getSequenceID(),
                            requestMesssageContext.getMessageID());
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }
                msgContext.setResponseMessage(null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private RMMessageContext processFirstRequestMessage(RMMessageContext reqRMMsgContext, boolean sync) throws Exception {
        RMMessageContext createSeqRMMsgContext = RMMessageCreator.createCreateSeqMsg(reqRMMsgContext);
        storageManager.addOutgoingSequence(reqRMMsgContext.getSequenceID());
        storageManager.setTemporaryOutSequence(reqRMMsgContext.getSequenceID(), createSeqRMMsgContext.getMessageID());

        //Set the processing state to the RMMessageContext
        createSeqRMMsgContext.setSync(sync);

        storageManager.addCreateSequenceRequest(createSeqRMMsgContext);
        processRequestMessage(reqRMMsgContext);
        return reqRMMsgContext;
    }

    private RMMessageContext processRequestMessage(RMMessageContext reqRMMsgContext) throws Exception {
        storageManager.insertOutgoingMessage(reqRMMsgContext);
        return reqRMMsgContext;
    }


    private boolean checkTheQueueForAck(String sequenceId, String reqMessageID) {
        return storageManager.checkForAcknowledgement(sequenceId, reqMessageID);
    }

    private RMMessageContext checkTheQueueForResponse(String sequenceId, String reqMessageID) {
        return storageManager.checkForResponseMessage(sequenceId, reqMessageID);
    }


}



