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

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.RelatesTo;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author
 */
public class CompositeProcessor implements IRMMessageProcessor {

    IStorageManager storageManager = null;

    public CompositeProcessor(IStorageManager storageManger) {
        this.storageManager = storageManger;
    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {

        //if the message is and Ack then process the ack
        //if the message has a body then insert it to the queue

        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
        AddressingHeaders addrHeaders =rmMessageContext.getAddressingHeaders();
        AcknowledgementProcessor ackProcessor = new AcknowledgementProcessor(this.storageManager);
        if (rmHeaders.getSequenceAcknowledgement() != null) {
            ackProcessor.processMessage(rmMessageContext);
        }

        if (rmHeaders.getSequence() != null) {
            System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE 1");
            if (rmHeaders.getSequence().getMessageNumber() != null) {
                System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE 2");
                String sequenceUUID = rmHeaders.getSequence().getIdentifier().getIdentifier();
                long messageNumber = rmHeaders.getSequence().getMessageNumber().getMessageNumber();
               // System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE "+storageManger.isMessageExist(sequenceID, messageNumber));


                String seqId = storageManager.getOutgoingSeqenceIdOfIncomingMsg(rmMessageContext);
                boolean hasSequence = storageManager.isSequenceExist(seqId);

                

                if(addrHeaders.getRelatesTo()!=null && !addrHeaders.getRelatesTo().isEmpty()){
                   RelatesTo relatesTo = (RelatesTo) addrHeaders.getRelatesTo().get(0);
                   String messageId = relatesTo.getURI().toString();
                   seqId=storageManager.getOutgoingSeqOfMsg(messageId);
                   System.out.println("(((((((99999999999999999999999999999999999999 "+seqId);
                }

                if(!hasSequence){
                    storageManager.addIncomingSequence(seqId);
                }



                if (storageManager.isMessageExist(seqId, messageNumber) != true) {
                    //Create a copy of the RMMessageContext.
                    RMMessageContext rmMsgContext = new RMMessageContext();
                    //Copy the RMMEssageContext
                    rmMessageContext.copyContents(rmMsgContext);
                    System.out.println("SETTING THE RESPONSE " + sequenceUUID + "  " + messageNumber);
                    rmMsgContext.setSequenceID(sequenceUUID);
                    rmMsgContext.setMsgNumber(messageNumber);
                    try {
                        //Create a new MessageContext, by pasing the axis engine.
                        MessageContext msgContext = new MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
                        //Copy the existing message context to the new message context.
                        RMMessageContext.copyMessageContext(rmMessageContext.getMsgContext(), msgContext);
                        //Copy the request and response messages.
                        String soapMsg = rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
                        Message reqMsg = new Message(soapMsg);
                        //Message resMsg = new Message(rmMessageContext
                        //        .getMsgContext().getResponseMessage()
                        //        .getSOAPPartAsString());
                        //Set the request and response messages of the message
                        // context.
                        msgContext.setRequestMessage(reqMsg);
                        //msgContext.setResponseMessage(resMsg);

                        //rmMsgContext.setReqEnv(reqMsg.getSOAPEnvelope());
                        //rmMsgContext.setResEnv(resMsg.getSOAPEnvelope());

                        rmMsgContext.setMsgContext(msgContext);
                        //Set the message type for this message.
                        rmMsgContext.setMessageType(Constants.MSG_TYPE_SERVICE_REQUEST);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //TODO: Add to log.
                    }

                    System.out.println("INFO: Inserting the request message ....\n");
                    //Insert the message to the INQUEUE
                    storageManager.insertIncomingMessage(rmMsgContext);
                }

                //Send an Ack for every message received by the server.
                //This should be changed according to the WS-policy.
                return ackProcessor.sendAcknowledgement(rmMessageContext);
            }
        }
        //If we don't have the sequence in the message then we have to send some errors.
        return false;
    }

}