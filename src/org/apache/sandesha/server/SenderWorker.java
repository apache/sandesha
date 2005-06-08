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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.msgprocessors.IRMMessageProcessor;
import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.CallbackData;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.sandesha.ws.rm.RMHeaders;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

/**
 * This is the worker for the Sender. Sender will start several workers depending on the
 * Constants value SENDER_THREADS in the Constants file.
 */
public class SenderWorker implements Runnable {
    private static final Log log = LogFactory.getLog(SenderWorker.class.getName());
    public static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
    public static Callback callback;
    public boolean running = true;
    private IStorageManager storageManager;


    public static synchronized Callback getCallback() {
        return callback;
    }

    public static synchronized void setCallback(Callback cb) {
        callback = cb;
    }

    private SimpleChain requestChain = null;
    private SimpleChain responseChain = null;

    public SimpleChain getRequestChain() {
        return requestChain;
    }

    public void setRequestChain(SimpleChain requestChain) {
        this.requestChain = requestChain;
    }

    public SimpleChain getResponseChain() {
        return responseChain;
    }

    public void setResponseChain(SimpleChain responseChanin) {
        this.responseChain = responseChanin;
    }

    public SenderWorker() {
        storageManager = new ServerStorageManager();
    }

    public SenderWorker(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void run() {

        while (running) {
            long startTime = System.currentTimeMillis();
            boolean hasMessages = true;
            //Take a messge from the storage and check whether we can send it.
            do {

                RMMessageContext rmMessageContext = storageManager.getNextMessageToSend();
                if (rmMessageContext == null) {
                    hasMessages = false;
                } else {
                    long inactivityTimeout = PolicyLoader.getInstance().getInactivityTimeout();
                    long retransmissionInterval = PolicyLoader.getInstance()
                            .getBaseRetransmissionInterval();

                    if (rmMessageContext.getFristProcessedTime() == 0)
                        rmMessageContext.setFristProcessedTime(System.currentTimeMillis());

                    if ((System.currentTimeMillis() - rmMessageContext.getFristProcessedTime()) >
                            inactivityTimeout) {
                        log.error("Inactivity Time Out Reached for the message with <wsa:MessageID> " +
                                rmMessageContext.getMessageID());
                        storageManager.clearStorage();


                    } else if (rmMessageContext.getRetransmissionTime() <
                            (System.currentTimeMillis() - rmMessageContext.getLastPrecessedTime())) {
                        try {

                            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());

                            if (PolicyLoader.getInstance().getExponentialBackoff() != null) {
                                long newRtTime = ((long) Math.pow(retransmissionInterval / 1000,
                                        rmMessageContext.getReTransmissionCount())) * 1000;
                                rmMessageContext.setRetransmissionTime(newRtTime);

                            } else {
                                //Let's do Binary Back Off
                                long rtTime = rmMessageContext.getRetransmissionTime();
                                rmMessageContext.setRetransmissionTime(2 * rtTime);

                            }
                            sendMessage(rmMessageContext);
                            rmMessageContext.setReTransmissionCount(
                                    rmMessageContext.getReTransmissionCount() + 1);

                            rmMessageContext.setLocked(false);

                        } catch (AxisFault e) {
                            rmMessageContext.setLocked(false);
                            log.error(e);
                        } catch (SOAPException e) {
                            rmMessageContext.setLocked(false);
                            log.error(e);
                        } catch (Exception e) {
                            rmMessageContext.setLocked(false);
                            log.error(e);
                        }
                    }
                    rmMessageContext.setLocked(false);

                }
            } while (hasMessages);

            long timeGap = System.currentTimeMillis() - startTime;
            if ((timeGap - Constants.SENDER_SLEEP_TIME) <= 0) {
                try {
                    Thread.sleep(Constants.SENDER_SLEEP_TIME - timeGap);
                } catch (Exception ex) {
                    log.error(ex);
                }
            }
        }
    }

    private void sendMessage(RMMessageContext rmMessageContext) throws Exception {
        switch (rmMessageContext.getMessageType()) {
            case Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST:
                {
                    System.out.println(Constants.InfomationMessage.SENDING_CREATE_SEQ);
                    sendCreateSequenceRequest(rmMessageContext);
                    break;
                }
            case Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE:
                {
                    System.out.println(Constants.InfomationMessage.SENDING_CREATE_SEQ_RES);
                    sendCreateSequenceResponse(rmMessageContext);
                    break;
                }
            case Constants.MSG_TYPE_TERMINATE_SEQUENCE:
                {
                    System.out.println(Constants.InfomationMessage.SENDING_TERMINATE_SEQ);
                    sendTerminateSequenceRequest(rmMessageContext);
                    storageManager.setTerminateSend(storageManager.getKeyFromOutgoingSeqId(
                            rmMessageContext.getSequenceID()));
                    break;
                }
            case Constants.MSG_TYPE_ACKNOWLEDGEMENT:
                {
                    System.out.println(Constants.InfomationMessage.SENDING_ACK);
                    sendAcknowldgement(rmMessageContext);
                    break;
                }
            case Constants.MSG_TYPE_SERVICE_REQUEST:
                {
                    System.out.println(Constants.InfomationMessage.SENDING_REQ);
                    sendServiceRequest(rmMessageContext);
                    break;
                }
            case Constants.MSG_TYPE_SERVICE_RESPONSE:
                {
                    System.out.println(Constants.InfomationMessage.SENDING_RES);
                    sendServiceResponse(rmMessageContext);
                    break;
                }
        }
    }


    /**
     * @param rmMessageContext
     */
    private void sendTerminateSequenceRequest(RMMessageContext rmMessageContext) throws Exception {
        SOAPEnvelope terSeqEnv = EnvelopeCreator.createTerminatSeqMessage(rmMessageContext);

        Message terSeqMsg = new Message(terSeqEnv);
        rmMessageContext.getMsgContext().setRequestMessage(terSeqMsg);

        Call call;
        call = prepareCall(rmMessageContext);
        call.invoke();

        processResponseMessage(call, rmMessageContext);
    }

    private void sendServiceResponse(RMMessageContext rmMessageContext) throws Exception {
        SOAPEnvelope responseEnvelope = null;
        responseEnvelope = EnvelopeCreator.createServiceResponseEnvelope(rmMessageContext);
        rmMessageContext.getMsgContext().setRequestMessage(new Message(responseEnvelope));
        rmMessageContext.getMsgContext().setResponseMessage(new Message(responseEnvelope));

        Service service = new Service();
        Call call = (Call) service.createCall();

        if (rmMessageContext.getAddressingHeaders().getAction() != null) {
            call.setSOAPActionURI(rmMessageContext.getAddressingHeaders().getAction().toString());
        }

        call.setTargetEndpointAddress(
                rmMessageContext.getAddressingHeaders().getReplyTo().getAddress().toString());
        //NOTE: WE USE THE REQUEST MESSAGE TO SEND THE RESPONSE.
        String soapMsg = rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
        call.setRequestMessage(new Message(soapMsg));

        // rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
        // rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
        //We are not expecting the ack over the  same connection
        storageManager.addSendMsgNo(rmMessageContext.getSequenceID(),
                rmMessageContext.getMsgNumber());
        call.invoke();

    }

    private void sendCreateSequenceRequest(RMMessageContext rmMsgCtx) throws Exception {
        Call call;

        SOAPEnvelope reqEnvelope = EnvelopeCreator.createCreateSequenceEnvelope(rmMsgCtx);
        rmMsgCtx.getMsgContext().setRequestMessage(new Message(reqEnvelope));

        call = prepareCall(rmMsgCtx);
        call.invoke();

        processResponseMessage(call, rmMsgCtx);

    }

    private void sendCreateSequenceResponse(RMMessageContext rmMessageContext) throws Exception {
        //Here there is no concept of sending synchronous CreateSequenceRequest
        // response.
        //i.e. we are not expecting any response for this.
        if (rmMessageContext.getMsgContext().getResponseMessage() == null) {
            //The code should not come to this point.
            System.err.println(Constants.ErrorMessages.NULL_REQUEST_MSG);
        } else {
            Call call = prepareCall(rmMessageContext);
            call.setRequestMessage(rmMessageContext.getMsgContext().getResponseMessage());
            call.invoke();
        }
    }

    private void sendAcknowldgement(RMMessageContext rmMessageContext) throws Exception {
        // Here there is no concept of sending synchronous CreateSequenceRequest
        // resposne.
        if (rmMessageContext.getMsgContext().getResponseMessage() == null) {
            log.error(Constants.ErrorMessages.NULL_REQUEST_MSG);
        } else {
            Call call = prepareCall(rmMessageContext);
            call.setRequestMessage(rmMessageContext.getMsgContext().getResponseMessage());
            call.invoke();
        }
    }

    private Call prepareCall(RMMessageContext rmMessageContext) throws ServiceException, AxisFault {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());

        call.setClientHandlers(requestChain, responseChain);
        if (rmMessageContext.getMsgContext().getRequestMessage() != null) {
            String soapMsg = rmMessageContext.getMsgContext().getRequestMessage()
                    .getSOAPPartAsString();
            call.setRequestMessage(new Message(soapMsg));
            if (rmMessageContext.getAddressingHeaders().getAction() != null) {
                call.setSOAPActionURI(
                        rmMessageContext.getAddressingHeaders().getAction().toString());
            }
        }
        return call;
    }

    private void sendServiceRequest(RMMessageContext rmMessageContext) throws Exception {

        SOAPEnvelope requestEnvelope = null;
        //Need to create the response envelope.

        requestEnvelope = EnvelopeCreator.createServiceRequestEnvelope(rmMessageContext);
        rmMessageContext.getMsgContext().setRequestMessage(new Message(requestEnvelope));
        if (rmMessageContext.getSync()) {
            Call call;
            call = prepareCall(rmMessageContext);
            //CHECK THIS
            storageManager.addSendMsgNo(rmMessageContext.getSequenceID(),
                    rmMessageContext.getMsgNumber());
            call.invoke();
            processResponseMessage(call, rmMessageContext);

        } else {
            Call call = prepareCall(rmMessageContext);
            storageManager.addSendMsgNo(rmMessageContext.getSequenceID(),
                    rmMessageContext.getMsgNumber());
            call.invoke();
            processResponseMessage(call, rmMessageContext);

        }
    }

    private void processResponseMessage(Call call, RMMessageContext rmMessageContext)
            throws Exception {

        if (call.getResponseMessage() != null) {
            RMHeaders rmHeaders = new RMHeaders();
            rmHeaders.fromSOAPEnvelope(call.getResponseMessage().getSOAPEnvelope());
            rmMessageContext.setRMHeaders(rmHeaders);
            AddressingHeaders addrHeaders = new AddressingHeaders(
                    call.getResponseMessage().getSOAPEnvelope());
            rmMessageContext.setAddressingHeaders(addrHeaders);
            rmMessageContext.getMsgContext().setResponseMessage(call.getResponseMessage());
            IRMMessageProcessor messagePrcessor = RMMessageProcessorIdentifier.getMessageProcessor(
                    rmMessageContext, storageManager);
            messagePrcessor.processMessage(rmMessageContext);
        }

        if (getCallback() != null) {
            CallbackData data = new CallbackData();
            data.setMessageId(rmMessageContext.getMessageID());
            data.setMessageType(rmMessageContext.getMessageType());
            data.setSequenceId(rmMessageContext.getSequenceID());
            callback.onIncomingMessage(data);
        }

    }

}
