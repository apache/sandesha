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
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.handler.AddressingHandler;
import org.apache.sandesha.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.test.TestHandler;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.handlers.ClientSyncResponseHandler;
import org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler;

/**
 * @author JEkanayake
 *  
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
            do {
                //System.out.println("SENDER");
                RMMessageContext rmMessageContext = storageManager
                        .getNextMessageToSend();
                if (rmMessageContext == null) {
                    hasMessages = false;
                    //System.out.println("rmMessageContext == null");
                } else {
                    //Send the message.

                    if (rmMessageContext.getMsgContext() == null)
                        System.out
                                .println("WARN: rmMessageContext.getMsgContext()== null ....");
                    if (rmMessageContext.getMsgContext().getRequestMessage() == null)
                        System.out
                                .println("WARN: rmMessageContext.getMsgContext().getRequestMessage()  == null ....");

                    switch (rmMessageContext.getMessageType()) {
                    case Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST: {
                        try{
                        	System.out.println("SENDING CREATE SEQUENCE REQUEST ....");
                            sendCreateSequenceRequest(rmMessageContext);
                        } catch (RMException rmEx) {
                            //TODO log the error.
                            rmEx.printStackTrace();
                            break;
                        }

                        break;
                    }
                    case Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE: {
                        try {
							System.out.println("SENDING CREATE SEQUENCE RESPONSE ....");
                            //Send creat seq message.
                            //No response and we can just close the connection
                            sendCreateSequenceResponse(rmMessageContext);
                        } catch (RMException rmEx) {
                            //TODO log the error.
                            rmEx.printStackTrace();
                            break;
                        }
                        break;
                    }
                    case Constants.MSG_TYPE_TERMINATE_SEQUENCE: {
                        break;
                    }
                    case Constants.MSG_TYPE_ACKNOWLEDGEMENT: {
                        System.out.println("SENDING ACKNOWLEDGEMENT ....\n");
                        try {
                            sendAcknowldgement(rmMessageContext);
                            break;
                        } catch (RMException rmEx) {
                            //TODO log the error.
                            rmEx.printStackTrace();
                            break;
                        }

                    }
                    case Constants.MSG_TYPE_SERVICE_REQUEST: {

                        //Send the response message.
                        //Here we need to figure out a mechanism to load the
                        // response handlers
                        //that are scheduled to run in the original response
                        // path.
                        //Need to re-send messsages if we didn't get a
                        // response.
                        //RMMessageContext a field to store the long
                        // lastProcessedTime
                        //Another field to hold retransmission count.
                        System.out.println("SENDING REQUEST MESSAGE .....\n");
                                               
                        SOAPEnvelope requestEnvelope = null;

                        if (rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT) {
                            if ((System.currentTimeMillis() - rmMessageContext
                                    .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL) {

                                //TODO
                                //We should do this only once and then need to
                                //Save the respones message.
                                //if (rmMessageContext.getReTransmissionCount()
                                // == 0) {
                                //Need to create the response envelope.
                                requestEnvelope = EnvelopeCreator
                                        .createServiceRequestEnvelope(rmMessageContext);
                                rmMessageContext.getMsgContext()
                                        .setRequestMessage(
                                                new Message(requestEnvelope));
                                //System.out.println(requestEnvelope);

                                try {
                                    Service service = new Service();
                                    Call call = (Call) service.createCall();
                                    call
                                            .setTargetEndpointAddress(rmMessageContext
                                                    .getOutGoingAddress());

                                    //NOTE: WE USE THE REQUEST MESSAGE TO SEND
                                    // THE RESPONSE.

                                    call.setRequestMessage(rmMessageContext
                                            .getMsgContext()
                                            .getRequestMessage());
                                    //System.out.println(rmMessageContext.getMsgContext().getResponseMessage().getSOAPPartAsString());
                                    try {
                                        rmMessageContext
                                                .setLastPrecessedTime(System
                                                        .currentTimeMillis());
                                        rmMessageContext
                                                .setReTransmissionCount(rmMessageContext
                                                        .getReTransmissionCount() + 1);
                                        //We are not expecting the ack over the
                                        // same HTTP connection.
                                        call.invoke();
                                        //System.out.println(call.getResponseMessage().getSOAPPartAsString());
                                    } catch (AxisFault e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                    
                                    
                                    
                                    //-----------------------------------------------------------
                                    
                                    
                                    //Check whther we have a response. If so then use to set the
                                    // response.
                                    if (call.getResponseMessage() != null) {
                                        rmMessageContext.getMsgContext().setResponseMessage(
                                                call.getResponseMessage());
                                        IRMMessageProcessor messagePrcessor = RMMessageProcessorIdentifier
                                                .getMessageProcessor(rmMessageContext, storageManager);
                                        if (messagePrcessor instanceof FaultProcessor) {
                                            //process the fault.
                                            //For now just ignore.
                                            System.out.println("Fault for the CreateSequenceRequest");
                                            //For testing only.
                                            //storageManager.setApprovedOutSequence(
                                            //        "abcdefghijk", "1233abcdefghijk");
                                        } else if (messagePrcessor instanceof AcknowledgementProcessor) {
                                            try {
                                                messagePrcessor.processMessage(rmMessageContext);
                                            } catch (RMException rmEx) {
                                                rmEx.printStackTrace();
                                            }
                                        }

                                    }
                                    //-----------------------------------------------------------
                                    
                                    

                                } catch (ServiceException e1) {
                                    System.out
                                            .println("(!)(!)(!)Cannot send the Response message.....");
                                    e1.printStackTrace();
                                    break;
                                }
                            }
                            break;
                        }

                    }
                    case Constants.MSG_TYPE_SERVICE_RESPONSE: {
                        //Send the response message.
                        //Here we need to figure out a mechanism to load the
                        // response handlers
                        //that are scheduled to run in the original response
                        // path.
                        //Need to re-send messsages if we didn't get a
                        // response.
                        //RMMessageContext a field to store the long
                        // lastProcessedTime
                        //Another field to hold retransmission count.

                        System.out
                                .println("INFO: Sending response message ....\n");

                        SOAPEnvelope responseEnvelope = null;

                        if (rmMessageContext.getReTransmissionCount() <= Constants.MAXIMUM_RETRANSMISSION_COUNT) {
                            if ((System.currentTimeMillis() - rmMessageContext
                                    .getLastPrecessedTime()) > Constants.RETRANSMISSION_INTERVAL) {

                                //TODO
                                //We should do this only once and then need to
                                //Save the respones message.
                                //if (rmMessageContext.getReTransmissionCount()
                                // == 0) {
                                //Need to create the response envelope.
                                responseEnvelope = EnvelopeCreator
                                        .createServiceResponseEnvelope(rmMessageContext);
                                rmMessageContext.getMsgContext()
                                        .setRequestMessage(
                                                new Message(responseEnvelope));

                                try {
                                    Service service = new Service();
                                    Call call = (Call) service.createCall();
                                    //call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());
                                    call
                                            .setTargetEndpointAddress(rmMessageContext
                                                    .getAddressingHeaders()
                                                    .getReplyTo().getAddress()
                                                    .toString());

                                    //NOTE: WE USE THE REQUEST MESSAGE TO SEND
                                    // THE RESPONSE.

                                    call.setRequestMessage(rmMessageContext
                                            .getMsgContext()
                                            .getRequestMessage());
                                    //System.out.println(rmMessageContext.getMsgContext().getResponseMessage().getSOAPPartAsString());
                                    try {
                                        rmMessageContext
                                                .setLastPrecessedTime(System
                                                        .currentTimeMillis());
                                        rmMessageContext
                                                .setReTransmissionCount(rmMessageContext
                                                        .getReTransmissionCount() + 1);

                                        //We are not expecting the ack over the
                                        // same HTTP connection.
                                        call.invoke();
                                        //System.out.println(call.getResponseMessage().getSOAPPartAsString());
                                    } catch (AxisFault e) {
                                        e.printStackTrace();
                                        break;
                                    }

                                } catch (ServiceException e1) {
                                    System.out
                                            .println("(!)(!)(!)Cannot send the Response message.....");
                                    e1.printStackTrace();
                                    break;
                                }
                            }
                            break;
                        }

                    }
                        break;
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

    private void sendCreateSequenceRequest(RMMessageContext rmMessageContext)
            throws RMException {
        try {
            System.out.println("INFO: Sending Create Sequence Request Message ....");
            //Send the message.
            //may get the reply back to here.
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());
            
            //Need the addressing header in the return path.
            //Need to add the RMServerRequestHandler but this shoud be check again.
            //We may not need to use this atall.
            //SimpleChain sc=new SimpleChain();
            //sc.addHandler(new ClientSyncResponseHandler());
            //sc.addHandler(new AddressingHandler());
            //call.setClientHandlers(null,sc);
            
            if (rmMessageContext.getMsgContext().getRequestMessage() == null)
                System.out.println("NULL REQUEST MESSAGE");
            
            call.setRequestMessage(rmMessageContext.getMsgContext().getRequestMessage());
            try {
                //Send the createSequnceRequest.
                call.invoke();
            } catch (AxisFault e) {
                e.printStackTrace();
                throw new RMException(
                        "ERROR : Sending the create sequence request message");
            }
            
            //Check whther we have a response. If so then use to set the
            // response.
            if (call.getResponseMessage() != null) {
                try{
                
                RMHeaders rmHeaders = new RMHeaders();
                rmHeaders.fromSOAPEnvelope(call.getResponseMessage().getSOAPEnvelope());
                rmMessageContext.setRMHeaders(rmHeaders);
                       
               // AddressingHeaders addrHeaders=(AddressingHeaders) resMsgCtx.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
                AddressingHeaders addrHeaders = new AddressingHeaders(call.getResponseMessage().getSOAPEnvelope());
                rmMessageContext.setAddressingHeaders(addrHeaders);
                
                
                ////TOTOTOOTOTOOTOTO
                
          
                rmMessageContext.getMsgContext().setResponseMessage(
                        call.getResponseMessage());
                IRMMessageProcessor messagePrcessor = RMMessageProcessorIdentifier
                        .getMessageProcessor(rmMessageContext, storageManager);
                System.out.println(messagePrcessor);                  
                if (messagePrcessor instanceof FaultProcessor) {
                 
                    //process the fault.
                    //For now just ignore.
                    System.out.println("Fault for the CreateSequenceRequest");
                    //For testing only.
                    //storageManager.setApprovedOutSequence(
                    //        "abcdefghijk", "1233abcdefghijk");
                } else if (messagePrcessor instanceof CreateSequenceResponseProcessor) {
                 
                
                        messagePrcessor.processMessage(rmMessageContext);
                  
                }
                }catch (RMException rmEx) {
                    throw new RMException(
                    "ERROR: Processing async create sequence response ....");
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

        } catch (ServiceException e1) {
            throw new RMException(
                    "ERROR: Service exception when sending the create sequence request ....");

        }
    }

    private void sendCreateSequenceResponse(RMMessageContext rmMessageContext)
            throws RMException {
        try {
            System.out.println("INFO: Sending Create sequence response ....");

            Service service = new Service();
            Call call = (Call) service.createCall();
            call
                    .setTargetEndpointAddress(rmMessageContext
                            .getOutGoingAddress());
            if (rmMessageContext.getMsgContext().getResponseMessage() == null)
                System.out.println("NULL RESPONSE MESSAGE");

            call.setRequestMessage(rmMessageContext.getMsgContext()
                    .getResponseMessage());
            call.invoke();
        } catch (ServiceException e1) {
            throw new RMException(
                    "ERROR: Service exception when sending the create sequence response");
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }

    private void sendAcknowldgement(RMMessageContext rmMessageContext)
            throws RMException {
        try {
            System.out.println("INFO: Sending Async Acknowledgement ....");
            Service service = new Service();
            Call call = (Call) service.createCall();
            call
                    .setTargetEndpointAddress(rmMessageContext
                            .getOutGoingAddress());
            call.setRequestMessage(rmMessageContext.getMsgContext()
                    .getResponseMessage());
            call.invoke();
        } catch (ServiceException e1) {
            throw new RMException("ERROR: Sending the acknowledgement message");

        } catch (AxisFault e) {
            e.printStackTrace();

        }
    }

}