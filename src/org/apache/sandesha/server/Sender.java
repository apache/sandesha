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

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.SimpleChain;
import org.apache.axis.Handler;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.handler.AddressingHandler;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.handlers.RMHandler;
import org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler;

/**
 * @author JEkanayake
 */
public class Sender implements Runnable {
    private IStorageManager storageManager;

    public Sender() {
        storageManager = new ServerStorageManager();
    }

    public Sender(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public void run() {

        while (true) {
            long startTime = System.currentTimeMillis();
            boolean hasMessages = true;
            //Take a messge from the storage and check whether we can send it.
            do {
                RMMessageContext rmMessageContext = storageManager.getNextMessageToSend();
                if (rmMessageContext == null) {
                    hasMessages = false;
                } else {
                    //Send the message.
                    switch (rmMessageContext.getMessageType()) {
                        case Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST:
                            {
                                System.out.println("INFO: SENDING CREATE SEQUENCE REQUEST ....");
                                if ((rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT)
                                        && ((System.currentTimeMillis() - rmMessageContext
                                        .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL)) {
                                    sendCreateSequenceRequest(rmMessageContext);
                                } else {
                                    //TODO REPORT ERROR
                                }
                                break;
                            }
                        case Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE:
                            {
                                System.out.println("INFO: SENDING CREATE SEQUENCE RESPONSE ....");
                                if ((rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT)
                                        && ((System.currentTimeMillis() - rmMessageContext
                                        .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL)) {
                                    sendCreateSequenceResponse(rmMessageContext);
                                } else {
                                    //TODO REPORT ERROR
                                }
                                break;
                            }
                        case Constants.MSG_TYPE_TERMINATE_SEQUENCE:
                            {
                                System.out.println("INFO: SENDING TERMINATE SEQUENCE REQUEST ....");
                                if ((rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT)
                                        && ((System.currentTimeMillis() - rmMessageContext
                                        .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL)) {
                                    sendTerminateSequenceRequest(rmMessageContext);
                                } else {
                                    //TODO REPORT ERROR
                                }
                                break;
                            }
                        case Constants.MSG_TYPE_ACKNOWLEDGEMENT:
                            {
                                System.out.println("INFO: SENDING ACKNOWLEDGEMENT ....\n");
                                if ((rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT)
                                        && ((System.currentTimeMillis() - rmMessageContext
                                        .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL)) {
                                    sendAcknowldgement(rmMessageContext);
                                } else {
                                    //TODO REPORT ERROR
                                }
                                break;
                            }
                        case Constants.MSG_TYPE_SERVICE_REQUEST:
                            {
                                System.out.println("INFO: SENDING REQUEST MESSAGE ....\n");
                                if ((rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT)
                                        && ((System.currentTimeMillis() - rmMessageContext
                                        .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL)) {
                                    sendServiceRequest(rmMessageContext);
                                } else { //TODO REPORT ERROR
                                }
                                break;
                            }
                        case Constants.MSG_TYPE_SERVICE_RESPONSE:
                            {
                                System.out.println("INFO: SENDING RESPONSE MESSAGE ....\n");
                                if ((rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT)
                                        && ((System.currentTimeMillis() - rmMessageContext
                                        .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL)) {
                                    sendServiceResponse(rmMessageContext);
                                } else {
                                    //TODO REPORT ERROR
                                }
                                break;
                            }
                    }

                }
            } while (hasMessages);

            long timeGap = System.currentTimeMillis() - startTime;
            if ((timeGap - Constants.SENDER_SLEEP_TIME) <= 0) {
                try {

                    System.out.print("|"); //Sender THREAD IS SLEEPING
                    // -----------XXX----------\n");
                    Thread.sleep(Constants.SENDER_SLEEP_TIME - timeGap);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    /**
     * @param rmMessageContext
     */
    private void sendTerminateSequenceRequest(RMMessageContext rmMessageContext) {
        SOAPEnvelope terSeqEnv = EnvelopeCreator.createTerminatSeqMessage(rmMessageContext);
        Message terSeqMsg = new Message(terSeqEnv);
        rmMessageContext.getMsgContext().setRequestMessage(terSeqMsg);

        Call call;
        try {
            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
            rmMessageContext
                    .setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
            call = prepareCall(rmMessageContext);
            call.invoke();
            if (call.getResponseMessage() != null) {
                RMHeaders rmHeaders = new RMHeaders();
                rmHeaders.fromSOAPEnvelope(call.getResponseMessage().getSOAPEnvelope());
                rmMessageContext.setRMHeaders(rmHeaders);
                AddressingHeaders addrHeaders = new AddressingHeaders(call.getResponseMessage()
                        .getSOAPEnvelope());
                rmMessageContext.setAddressingHeaders(addrHeaders);
                rmMessageContext.getMsgContext().setResponseMessage(call.getResponseMessage());
                IRMMessageProcessor messagePrcessor = RMMessageProcessorIdentifier
                        .getMessageProcessor(rmMessageContext, storageManager);
                messagePrcessor.processMessage(rmMessageContext);
            }
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SOAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private void sendServiceResponse(RMMessageContext rmMessageContext) {
        SOAPEnvelope responseEnvelope = null;
        responseEnvelope = EnvelopeCreator.createServiceResponseEnvelope(rmMessageContext);
        //org.apache.axis.MessageContext resMsgCtx=new org.apache.axis.MessageContext(rmMessageContext.getMsgContext().getAxisEngine());
         rmMessageContext.getMsgContext().setRequestMessage(new Message(responseEnvelope));
        rmMessageContext.getMsgContext().setResponseMessage(new Message(responseEnvelope));
        //resMsgCtx.setRequestMessage(new Message(responseEnvelope));
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            //call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());
            call.setTargetEndpointAddress(rmMessageContext.getAddressingHeaders().getReplyTo()
                    .getAddress().toString());
            //NOTE: WE USE THE REQUEST MESSAGE TO SEND THE RESPONSE.
            String soapMsg=rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
            call.setRequestMessage(new Message(soapMsg));

                rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
                rmMessageContext
                        .setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
                //We are not expecting the ack over the
                // same HTTP connection.
                call.invoke();
                //System.out.println(call.getResponseMessage().getSOAPPartAsString());


        } catch (ServiceException e1) {
            System.err.println("ERROR: SENDING RESPONSE MESSAGE ....");
            e1.printStackTrace();
        }
        catch(AxisFault af){
            af.printStackTrace();
        }


    }

    private void sendCreateSequenceRequest(RMMessageContext rmMessageContext) {
        if (rmMessageContext.getMsgContext().getRequestMessage() == null) {
            //The code should not come to this point.
            System.err.println("ERROR: NULL REQUEST MESSAGE");
        } else {
            Call call;
            try {
                rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
                rmMessageContext
                        .setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
                call = prepareCall(rmMessageContext);
                call.invoke();
                if (call.getResponseMessage() != null) {
                    RMHeaders rmHeaders = new RMHeaders();
                    rmHeaders.fromSOAPEnvelope(call.getResponseMessage().getSOAPEnvelope());
                    rmMessageContext.setRMHeaders(rmHeaders);
                    AddressingHeaders addrHeaders = new AddressingHeaders(call.getResponseMessage()
                            .getSOAPEnvelope());
                    rmMessageContext.setAddressingHeaders(addrHeaders);
                    rmMessageContext.getMsgContext().setResponseMessage(call.getResponseMessage());
                    IRMMessageProcessor messagePrcessor = RMMessageProcessorIdentifier
                            .getMessageProcessor(rmMessageContext, storageManager);
                    messagePrcessor.processMessage(rmMessageContext);
                }
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SOAPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void sendCreateSequenceResponse(RMMessageContext rmMessageContext) {
        //Here there is no concept of sending synchronous CreateSequenceRequest
        // response.
        //i.e. we are not expecting any response for this.
        if (rmMessageContext.getMsgContext().getResponseMessage() == null) {
            //The code should not come to this point.
            System.err.println("ERROR: NULL REQUEST MESSAGE");
        } else {
            try {
                rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
                rmMessageContext
                        .setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
                Call call = prepareCall(rmMessageContext);
                call.setRequestMessage(rmMessageContext.getMsgContext().getResponseMessage());
                call.invoke();

            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (AxisFault e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAcknowldgement(RMMessageContext rmMessageContext) {
        // Here there is no concept of sending synchronous CreateSequenceRequest
        // resposne.
        if (rmMessageContext.getMsgContext().getResponseMessage() == null) {
            System.err.println("ERROR: NULL RESPONSE MESSAGE");
        } else {
            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
            rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
            try {
                Call call = prepareCall(rmMessageContext);
                call.setRequestMessage(rmMessageContext.getMsgContext().getResponseMessage());
                call.invoke();
                 if (call.getResponseMessage() != null) {
                        System.out.println("RESPONSE MESSAGE IS NOT NULL");
                     System.out.println(call.getResponseMessage().getSOAPEnvelope().toString());
                 }
            } catch (ServiceException e1) {
                System.err.println("ERROR: SERVICE EXCEPTION WHEN SENDING RESPONSE");
                e1.printStackTrace();
            } catch (AxisFault e) {
                e.printStackTrace();
            }
        }
    }

    private Call prepareCall(RMMessageContext rmMessageContext) throws ServiceException, AxisFault {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());

       //We need these two handlers in our
        SimpleChain sc= new SimpleChain();
        Handler serverRequestHandler= new RMServerRequestHandler();
        Handler addressingHandler = new AddressingHandler();

        sc.addHandler(addressingHandler);
        sc.addHandler(serverRequestHandler);

        call.setClientHandlers(null,sc);

        //call.setRequestMessage(rmMessageContext.getMsgContext().getRequestMessage());

        if (rmMessageContext.getMsgContext().getRequestMessage() != null){
            String soapMsg=rmMessageContext.getMsgContext().getRequestMessage().getSOAPPartAsString();
            call.setRequestMessage(new Message(soapMsg));
        }
        return call;
    }

    private void sendServiceRequest(RMMessageContext rmMessageContext) {
        if (rmMessageContext.getMsgContext().getRequestMessage() == null) {
            System.err.println("ERROR: NULL REQUEST MESSAGE");
        } else {
            SOAPEnvelope requestEnvelope = null;
            //Need to create the response envelope.
            requestEnvelope = EnvelopeCreator.createServiceRequestEnvelope(rmMessageContext);
            rmMessageContext.getMsgContext().setRequestMessage(new Message(requestEnvelope));
            rmMessageContext.setLastPrecessedTime(System.currentTimeMillis());
            rmMessageContext.setReTransmissionCount(rmMessageContext.getReTransmissionCount() + 1);
            if (rmMessageContext.getSync()) {
                Call call;
                try {
                    call = prepareCall(rmMessageContext);
                    //Send the createSequnceRequest Synchronously
                    call.invoke();
                    if (call.getResponseMessage() != null) {
                        System.out.println("RESPONSE MESSAGE IS NOT NULL");
                        RMHeaders rmHeaders = new RMHeaders();
                        rmHeaders.fromSOAPEnvelope(call.getResponseMessage().getSOAPEnvelope());
                        rmMessageContext.setRMHeaders(rmHeaders);
                        AddressingHeaders addrHeaders = new AddressingHeaders(call
                                .getResponseMessage().getSOAPEnvelope());
                        rmMessageContext.setAddressingHeaders(addrHeaders);
                        rmMessageContext.getMsgContext().setResponseMessage(call.getResponseMessage());
                        IRMMessageProcessor messageProcessor = RMMessageProcessorIdentifier
                                .getMessageProcessor(rmMessageContext, storageManager);
                        messageProcessor.processMessage(rmMessageContext);
                        System.out.println(messageProcessor);
                    }

                } catch (AxisFault e) {
                    // TODO Auto-generated catch block
                    System.err.println("ERROR: SENDING REQUEST ....");
                    e.printStackTrace();
                } catch (SOAPException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {
                    Call call = prepareCall(rmMessageContext);
                    //Send the createSequnceRequest Asynchronously.
                    call.invoke();
                } catch (AxisFault e) {
                    System.err.println("ERROR: SENDING REQUEST ....");
                    e.printStackTrace();
                } catch (ServiceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

    }

}