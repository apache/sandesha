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
import org.apache.axis.message.addressing.MessageID;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.msgprocessors.IRMMessageProcessor;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.sandesha.ws.rm.RMHeaders;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import java.util.List;

/**
 * @author JEkanayake
 */
public class Sender implements Runnable {

    private static final Log log = LogFactory.getLog(Sender.class.getName());
    public boolean running = true;
    private IStorageManager storageManager;

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

    public Sender() {
        storageManager = new ServerStorageManager();
    }

    public Sender(IStorageManager storageManager) {
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
                    //Send the message.
                    if ((rmMessageContext.getReTransmissionCount() <= PolicyLoader.getInstance().getRetransmissionCount())
                            && ((System.currentTimeMillis() - rmMessageContext
                            .getLastPrecessedTime()) > PolicyLoader.getInstance().getBaseRetransmissionInterval())) {
                        try {
                            sendMessage(rmMessageContext);
                        } catch (AxisFault e) {
                            log.error(e);
                        } catch (SOAPException e) {
                            log.error(e);
                        } catch (Exception e) {
                            log.error(e);
                        }
                    } else {
                        //TODO REPORT ERROR
                    }
                }
            } while (hasMessages);

            long timeGap = System.currentTimeMillis() - startTime;
            if ((timeGap - Constants.SENDER_SLEEP_TIME) <= 0) {
                try {
                    //System.out.print("|"); //Sender THREAD IS SLEEPING
                    Thread.sleep(Constants.SENDER_SLEEP_TIME - timeGap);
                } catch (Exception ex) {
                    log.error(ex);
                }
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
        rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
        rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
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

        call.setTargetEndpointAddress(rmMessageContext.getAddressingHeaders().getReplyTo().getAddress().toString());
        //NOTE: WE USE THE REQUEST MESSAGE TO SEND THE RESPONSE.
        String soapMsg = rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
        call.setRequestMessage(new Message(soapMsg));

        rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
        rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
        //We are not expecting the ack over the  same connection
        storageManager.addSendMsgNo(rmMessageContext.getSequenceID(), rmMessageContext.getMsgNumber());
        call.invoke();

    }

    private void sendCreateSequenceRequest(RMMessageContext rmMessageContext) throws Exception {
        if (rmMessageContext.getMsgContext().getRequestMessage() == null) {
            //The code should not come to this point.
            System.err.println(Constants.ErrorMessages.NULL_REQUEST_MSG);
        } else {
            Call call;



            //EDITED FOR MSG NO REPITITION
            String oldOutSeqId, newOutSeqId;

            String oldCreateSeqId = rmMessageContext.getMessageID().toString();
            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
            String uuid = uuidGen.nextUUID();


            String newCreateSeqId = Constants.UUID + uuid;
            rmMessageContext.setMessageID(newCreateSeqId);

            oldOutSeqId = oldCreateSeqId;
            newOutSeqId = newCreateSeqId;


            //MessageContext msgContext = tempMsg.getMsgContext();
            //String toAddress = tempMsg.getOutGoingAddress();


            AddressingHeaders addrHeaders = new AddressingHeaders(rmMessageContext.getMsgContext().getRequestMessage().getSOAPEnvelope());
            addrHeaders.setMessageID(new MessageID(new URI(newCreateSeqId)));
            addrHeaders.toEnvelope(rmMessageContext.getMsgContext().getRequestMessage().getSOAPEnvelope());


            rmMessageContext.addToMsgIdList(rmMessageContext.getMessageID().toString());
            List msgIdList = rmMessageContext.getMessageIdList();
            //Iterator it = msgIdList.iterator();
            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
            rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
            call = prepareCall(rmMessageContext);
            call.invoke();

            processResponseMessage(call, rmMessageContext);
        }
    }

    private void sendCreateSequenceResponse(RMMessageContext rmMessageContext) throws Exception {
        //Here there is no concept of sending synchronous CreateSequenceRequest
        // response.
        //i.e. we are not expecting any response for this.
        if (rmMessageContext.getMsgContext().getResponseMessage() == null) {
            //The code should not come to this point.
            System.err.println(Constants.ErrorMessages.NULL_REQUEST_MSG);
        } else {
            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
            rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
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
            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
            rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
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
            String soapMsg = rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
            call.setRequestMessage(new Message(soapMsg));
        }
        return call;
    }

    private void sendServiceRequest(RMMessageContext rmMessageContext) throws Exception {

        SOAPEnvelope requestEnvelope = null;
        //Need to create the response envelope.
        requestEnvelope = EnvelopeCreator.createServiceRequestEnvelope(rmMessageContext);
        rmMessageContext.getMsgContext().setRequestMessage(new Message(requestEnvelope));
        rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
        rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
        if (rmMessageContext.getSync()) {
            Call call;
            call = prepareCall(rmMessageContext);
            //CHECK THIS
            storageManager.addSendMsgNo(rmMessageContext.getSequenceID(), rmMessageContext.getMsgNumber());
            call.invoke();
            processResponseMessage(call, rmMessageContext);

        } else {
            Call call = prepareCall(rmMessageContext);
            storageManager.addSendMsgNo(rmMessageContext.getSequenceID(), rmMessageContext.getMsgNumber());
            call.invoke();
            processResponseMessage(call, rmMessageContext);

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
                    storageManager.setTerminateSend(storageManager.getKeyFromOutgoingSeqId(rmMessageContext.getSequenceID()));
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

    private void processResponseMessage(Call call, RMMessageContext rmMessageContext) throws Exception {
        if (call.getResponseMessage() != null) {
            RMHeaders rmHeaders = new RMHeaders();
            rmHeaders.fromSOAPEnvelope(call.getResponseMessage().getSOAPEnvelope());
            rmMessageContext.setRMHeaders(rmHeaders);
            AddressingHeaders addrHeaders = new AddressingHeaders(call.getResponseMessage().getSOAPEnvelope());
            rmMessageContext.setAddressingHeaders(addrHeaders);
            rmMessageContext.getMsgContext().setResponseMessage(call.getResponseMessage());
            IRMMessageProcessor messagePrcessor = RMMessageProcessorIdentifier
                    .getMessageProcessor(rmMessageContext, storageManager);
            messagePrcessor.processMessage(rmMessageContext);
        }
    }

}