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

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.util.RMMessageCreator;

import javax.xml.soap.SOAPEnvelope;

/**
 * @author JEkanayake
 *         <p/>
 *         This class will act as the service dispatcher for Sandesha. By default it
 *         will use the RPCProvider to invoke the service but need to improve this to
 *         use any Provider depending on the configuration.
 */
public class RMInvoker implements Runnable {
    private IStorageManager storageManager = null;
    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());

    public RMInvoker() {
        storageManager = new ServerStorageManager();
        storageManager.init();

    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(Constants.RMINVOKER_SLEEP_TIME);

                RMMessageContext rmMessageContext = storageManager.getNextMessageToProcess();

                if (rmMessageContext != null) {
                    //Currently RPCProvider is used as the default provider and this is used to actually invoke the service.
                    //To provide a maximum flexibility the actual provider should be a configurable entity
                    // where the class can be loaded at runtime.
                    RPCProvider rpcProvider = new RPCProvider();
                    rpcProvider.invoke(rmMessageContext.getMsgContext());

                    if (rmMessageContext.getMsgContext().getOperation().getMethod().getReturnType() != Void.TYPE) {
                        if (rmMessageContext.isLastMessage()) {
                            //Insert Terminate Sequnce.
                            AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
                            if (addrHeaders.getReplyTo() != null) {
                                String replyTo = addrHeaders.getReplyTo().getAddress().toString();
                                RMMessageContext terminateMsg = RMMessageCreator.createTerminateSeqMsg(rmMessageContext);
                                terminateMsg.setOutGoingAddress(replyTo);
                                storageManager.insertTerminateSeqMessage(terminateMsg);
                            } else {
                              log.error(Constants.ErrorMessages.CANNOT_SEND_THE_CREATE_SEQ);
                            }
                        }
                        //Store the message in the response queue. If there is an application response then that
                        // response is always sent using a new HTTP connection and the <replyTo> header is
                        // used in this case. This is done by the RMSender.
                        rmMessageContext.setMessageType(Constants.MSG_TYPE_SERVICE_RESPONSE);

                        boolean firstMsgOfResponseSeq = !storageManager.isResponseSequenceExist(rmMessageContext.getSequenceID());
                        rmMessageContext.setMsgNumber(storageManager.getNextMessageNumber(rmMessageContext
                                .getSequenceID()));
                        storageManager.insertOutgoingMessage(rmMessageContext);

                        if (firstMsgOfResponseSeq) {
                            RMMessageContext rmMsgContext = new RMMessageContext();
                            rmMessageContext.copyContents(rmMsgContext);
                            MessageContext msgContext = new MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
                            RMMessageContext.copyMessageContext(rmMessageContext.getMsgContext(), msgContext);
                            //Set this new msgContext to the rmMsgContext.

                            rmMsgContext.setMsgContext(msgContext);
                            rmMsgContext.setMessageType(Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST);

                            UUIDGen uuid = UUIDGenFactory.getUUIDGen();
                            String id = uuid.nextUUID();

                            storageManager.setTemporaryOutSequence(rmMsgContext.getSequenceID(), Constants.UUID + id);
                            SOAPEnvelope createSequenceEnvelope = EnvelopeCreator.createCreateSequenceEnvelope(id,
                                    rmMsgContext, Constants.SERVER);

                            rmMsgContext.getMsgContext().setRequestMessage(new Message(createSequenceEnvelope));

                            //TODO Check : Are We are sending only to the ReplyTo?
                            rmMsgContext.setOutGoingAddress(rmMsgContext.getAddressingHeaders().getReplyTo()
                                    .getAddress().toString());
                            rmMsgContext.setMessageID(Constants.UUID + id);
                            storageManager.addCreateSequenceRequest(rmMsgContext);
                        }
                    }
                }
            } catch (InterruptedException error) {
                log.error(error);
            } catch (Exception error) {
                log.error(error);
            }
        }
    }
}