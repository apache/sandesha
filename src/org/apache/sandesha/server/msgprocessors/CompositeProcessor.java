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
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.RelatesTo;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.ws.rm.RMHeaders;

import javax.xml.namespace.QName;

/**
 * This will process messages with sequence elements. Mainly the requests/responses.
 *
 * @auther Jaliya Ekanayake
 */
public class CompositeProcessor implements IRMMessageProcessor {

    private IStorageManager storageManager = null;
    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());

    public CompositeProcessor(IStorageManager storageManger) {
        this.storageManager = storageManger;
    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {

        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
        AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
        AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor(this.storageManager);
        if (rmHeaders.getSequenceAcknowledgement() != null) {
            ackProcessor.processMessage(rmMessageContext);
        }

        if (rmHeaders.getSequence() != null) {
            if (rmHeaders.getSequence().getMessageNumber() != null) {
                String sequenceUUID = rmHeaders.getSequence().getIdentifier().getIdentifier();
                long messageNumber = rmHeaders.getSequence().getMessageNumber().getMessageNumber();

                String seqId = storageManager.getOutgoingSeqenceIdOfIncomingMsg(rmMessageContext);
                boolean hasSequence = storageManager.isSequenceExist(seqId);

                if (addrHeaders.getRelatesTo() != null && !addrHeaders.getRelatesTo().isEmpty()) {
                    RelatesTo relatesTo = (RelatesTo) addrHeaders.getRelatesTo().get(0);
                    String messageId = relatesTo.getURI().toString();
                    seqId = storageManager.getOutgoingSeqOfMsg(messageId);
                }
                if (!hasSequence) {
                    storageManager.addIncomingSequence(seqId);
                }
                if (storageManager.isMessageExist(seqId, messageNumber) != true) {
                    //Create a copy of the RMMessageContext.
                    RMMessageContext rmMsgContext = new RMMessageContext();
                    //Copy the RMMEssageContext
                    rmMessageContext.copyContents(rmMsgContext);
                    rmMsgContext.setSequenceID(sequenceUUID);
                    rmMsgContext.setMsgNumber(messageNumber);
                    try {
                        MessageContext msgContext = new MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
                        RMMessageContext.copyMessageContext(rmMessageContext.getMsgContext(), msgContext);
                        String soapMsg = rmMessageContext.getMsgContext().getRequestMessage().getSOAPEnvelope().toString();
                        Message reqMsg = new Message(soapMsg);

                        msgContext.setRequestMessage(reqMsg);
                        rmMsgContext.setMsgContext(msgContext);
                        rmMsgContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
                    } catch (Exception e) {
                        log.error(e);
                        throw new AxisFault(new QName(Constants.FaultCodes.WSRM_SERVER_INTERNAL_ERROR), Constants.FaultMessages.SERVER_INTERNAL_ERROR, null, null);
                    }
                    storageManager.insertIncomingMessage(rmMsgContext);
                }

                //Send an Ack for every message received by the server.
                //This should be changed according to the WS-policy.
                 if(rmHeaders.getAckRequest()!=null ||rmHeaders.getSequence().getLastMessage()!=null){
                      return ackProcessor.sendAcknowledgement(rmMessageContext);
                 } else{
                     return false;
                 }
            }
        }
        return false;
    }

}