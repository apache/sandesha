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
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: May 29, 2005
 * Time: 12:52:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RMInvokerWorker implements Runnable {

    private IStorageManager storageManager = null;
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
                RMMessageContext rmMessageContext = getStorageManager().getNextMessageToProcess();

                if (rmMessageContext != null) {
                    AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
                    boolean isVoid = doRealInvoke(rmMessageContext.getMsgContext());

                    if (!isVoid) {

                        String oldAction = rmMessageContext.getAddressingHeaders().getAction()
                                .toString();
                        rmMessageContext.getAddressingHeaders().setAction(
                                oldAction + Constants.RESPONSE);
                        if (rmMessageContext.isLastMessage()) {
                            //Insert Terminate Sequnce.
                            if (addrHeaders.getReplyTo() != null) {
                                String replyTo = addrHeaders.getReplyTo().getAddress().toString();
                                RMMessageContext terminateMsg = RMMessageCreator.createTerminateSeqMsg(
                                        rmMessageContext, Constants.SERVER);
                                terminateMsg.setOutGoingAddress(replyTo);
                                getStorageManager().insertTerminateSeqMessage(terminateMsg);
                            } else {
                                log.error(Constants.ErrorMessages.CANNOT_SEND_THE_TERMINATE_SEQ);
                            }
                        }
                        //Store the message in the response queue. If there is an application response then that
                        // response is always sent using a new HTTP connection and the <replyTo> header is
                        // used in this case. This is done by the RMSender.
                        rmMessageContext.setMessageType(Constants.MSG_TYPE_SERVICE_RESPONSE);

                        boolean hasResponseSeq = getStorageManager().isResponseSequenceExist(
                                rmMessageContext.getSequenceID());
                        boolean firstMsgOfResponseSeq = false;
                        if (!(hasResponseSeq && rmMessageContext.getRMHeaders().getSequence()
                                .getMessageNumber().getMessageNumber() == 1)) {
                            firstMsgOfResponseSeq = !hasResponseSeq;
                        }

                        rmMessageContext.setMsgNumber(getStorageManager().getNextMessageNumber(
                                rmMessageContext.getSequenceID()));
                        getStorageManager().insertOutgoingMessage(rmMessageContext);


                        if (firstMsgOfResponseSeq) {
                            String msgIdStr = Constants.UUID + uuidGen.nextUUID();

                            RMMessageContext csRMMsgCtx = RMMessageCreator.createCreateSeqMsg(
                                    rmMessageContext, Constants.SERVER, msgIdStr, null);
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
            } catch (InterruptedException error) {
                log.error(error);
            } catch (Exception error) {
                log.error(error);
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
}
