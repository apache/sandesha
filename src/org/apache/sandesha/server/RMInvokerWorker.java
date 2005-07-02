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

import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.JavaProvider;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.util.PropertyLoader;
import org.apache.sandesha.util.RMMessageCreator;

/**
 * This is the worker class for the RMInvoker. RMInvoker instantiate several RMInvokerWorkers to
 * keep on monitoring SandeshaQueue and invoke the web services. The run method is synchronized
 * to get the IN-ORDER message invocation.
 */
public class RMInvokerWorker implements Runnable {

    private IStorageManager storageManager;
    private static final Log log = LogFactory.getLog(RMInvokerWorker.class.getName());
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    public RMInvokerWorker() {
        setStorageManager(new ServerStorageManager());
        getStorageManager().init();
    }


    protected boolean doRealInvoke(MessageContext aMessageContext) throws Exception {
        Class c = Class.forName(PropertyLoader.getProvider());
        JavaProvider provider = (JavaProvider) c.newInstance();
        provider.invoke(aMessageContext);
        return aMessageContext.getOperation().getMethod().getReturnType() == Void.TYPE;
    }


    public void run() {
        while (true) {

            try {
                Thread.sleep(Constants.RMINVOKER_SLEEP_TIME);


                Object seq = getStorageManager().getNextSeqToProcess();
                synchronized (seq) {
                    RMMessageContext rmMessageContext = getStorageManager().getNextMessageToProcess(seq);
                    doWork(rmMessageContext);
                }


            } catch (InterruptedException error) {
                RMInvokerWorker.log.error(error);
            } catch (Exception error) {
                RMInvokerWorker.log.error(error);
            }
        }
    }

    /**
     * @param storageManager The storageManager to set.
     */
    protected void setStorageManager(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * @return Returns the storageManager.
     */
    protected IStorageManager getStorageManager() {
        return storageManager;
    }

    protected void doWork(RMMessageContext rmMessageContext) throws Exception {
        if (rmMessageContext != null) {
            AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
            boolean isVoid = doRealInvoke(rmMessageContext.getMsgContext());

            if (!isVoid) {

                String oldAction = rmMessageContext.getAddressingHeaders().getAction()
                        .toString();
                rmMessageContext.getAddressingHeaders().setAction(oldAction + Constants.RESPONSE);
                if (rmMessageContext.isLastMessage()) {
                    //Insert Terminate Sequnce.
                    if (addrHeaders.getReplyTo() != null) {
                        String replyTo = addrHeaders.getReplyTo().getAddress().toString();
                        RMMessageContext terminateMsg = RMMessageCreator.createTerminateSeqMsg(rmMessageContext, Constants.SERVER);
                        terminateMsg.setOutGoingAddress(replyTo);
                        getStorageManager().insertTerminateSeqMessage(terminateMsg);
                    } else {
                        RMInvokerWorker.log.error(Constants.ErrorMessages.CANNOT_SEND_THE_TERMINATE_SEQ);
                    }
                }
                //Store the message in the response queue. If there is an application
                // response then that response is always sent using a new HTTP connection
                // and the <replyTo> header is used in this case. This is done by the
                // RMSender.
                rmMessageContext.setMessageType(Constants.MSG_TYPE_SERVICE_RESPONSE);

                boolean hasResponseSeq = getStorageManager().isResponseSequenceExist(rmMessageContext.getSequenceID());
                boolean firstMsgOfResponseSeq = false;
                if (!(hasResponseSeq && rmMessageContext.getRMHeaders().getSequence()
                        .getMessageNumber().getMessageNumber() == 1)) {
                    firstMsgOfResponseSeq = !hasResponseSeq;
                }

                rmMessageContext.setMsgNumber(getStorageManager().getNextMessageNumber(rmMessageContext.getSequenceID()));
                getStorageManager().insertOutgoingMessage(rmMessageContext);


                if (firstMsgOfResponseSeq) {
                    String msgIdStr = Constants.UUID + RMInvokerWorker.uuidGen.nextUUID();

                    RMMessageContext csRMMsgCtx = RMMessageCreator.createCreateSeqMsg(rmMessageContext, Constants.SERVER, msgIdStr, null);
                    csRMMsgCtx.setOutGoingAddress(rmMessageContext.getAddressingHeaders()
                            .getReplyTo().getAddress().toString());

                    csRMMsgCtx.addToMsgIdList(msgIdStr);
                    csRMMsgCtx.setMessageID(msgIdStr);

                    getStorageManager().setTemporaryOutSequence(csRMMsgCtx.getSequenceID(),
                            msgIdStr);
                    getStorageManager().addCreateSequenceRequest(csRMMsgCtx);
                }
            }
        }
    }
}
