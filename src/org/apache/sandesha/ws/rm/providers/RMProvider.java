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
package org.apache.sandesha.ws.rm.providers;

import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.sandesha.RMMessage;
import org.apache.sandesha.RMSequence;
import org.apache.sandesha.client.ClientMessageController;
import org.apache.sandesha.server.MessageInserter;
import org.apache.sandesha.server.ServerMessageController;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;

import java.lang.reflect.Method;

/**
 * class RMProvider
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class RMProvider extends RPCProvider {

    /**
     * Method processMessage
     * 
     * @param msgContext 
     * @param reqEnv     
     * @param resEnv     
     * @param obj        
     * @throws Exception 
     */
    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv, Object obj)
            throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Enter: RPCProvider.processMessage()");
        }

        // ////////////To test whether the addressing headers are accessible at this point
        System.out.println("This is from the RMProvider");
        System.out.println("-----------------------------------------------------------");
        System.out.println(obj);
        System.out.println("-----------------------------------------------------------");
        System.out.println(msgContext.getRequestMessage().getSOAPPartAsString());
        System.out.println("-----------------------------------------------------------");

        RMHeaders rmHeaders =
                (RMHeaders) msgContext.getProperty(org.apache.sandesha.Constants.ENV_RM_REQUEST_HEADERS);
        AddressingHeaders addressingHeaders = (AddressingHeaders) msgContext.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);

        if (rmHeaders == null) {
            System.out.println("rmHeaders==null");
            System.out.println("Calling to super");
            super.processMessage(msgContext, reqEnv, resEnv, obj);
        } else {
            System.out.println("rmHeaders!=null");

            String anonymous =
                    new String(org.apache.sandesha.Constants.ANONYMOUS_URI);
            boolean asynchronous = true;

            /*
             * if(addressingHeaders.getFrom().getAddress().toString().equals(org.apache.sandesha.Constants.ANONYMOUS_URI)){
             *   asynchronous=false;
             *
             * }
             */
            if (asynchronous) {
                System.out.println("asynchronous=true");

                MessageInserter messageInserter =
                        new MessageInserter(msgContext, obj);
                Thread thread = new Thread(messageInserter);

                thread.start();
                msgContext.setResponseMessage(null);
            } else {
                System.out.println("asynchronous=false");

                ServerMessageController serverMessageController =
                        ServerMessageController.getInstance();
                ClientMessageController clientMessageController =
                        ClientMessageController.getInstance();
                RMMessage message =
                        new RMMessage();

                // System.out.println(addressingHeaders);
                message.setAddressingHeaders(addressingHeaders);
                message.setRMHeaders(rmHeaders);
                message.setRequestMessage(msgContext.getRequestMessage());
                msgContext.setEncodingStyle(msgContext.getEncodingStyle());
                message.setRMHeaders(rmHeaders);
                message.setAddressingHeaders(addressingHeaders);

                if (rmHeaders.getCreateSequence() != null) {
                }

                if (rmHeaders.getCreateSequenceResponse() != null) {
                }

                if (rmHeaders.getSequenceAcknowledgement() != null) {
                    System.out.println("rmHeaders.getSequenceAcknowledgement() != null");

                    Identifier seqAckID =
                            rmHeaders.getSequenceAcknowledgement().getIdentifier();
                    RMSequence clientSeq =
                            clientMessageController.retrieveIfSequenceExists(seqAckID);
                    RMSequence serverSeq =
                            serverMessageController.retrieveIfSequenceExists(seqAckID);

                    if (clientSeq != null) {
                        System.out.println("clientSeq!=null");
                        clientSeq.setSequenceAcknowledgement(rmHeaders.getSequenceAcknowledgement());
                    }

                    if (serverSeq != null) {
                        System.out.println("serverSeq!=null");
                        serverSeq.setSequenceAcknowledgement(rmHeaders.getSequenceAcknowledgement());
                    }
                }

                if (rmHeaders.getSequence() != null) {
                    System.out.println("rmHeaders.getSequence()!=null");
                    message.setIdentifier(rmHeaders.getSequence().getIdentifier());

                    if (msgContext.getOperation() != null) {
                        System.out.println("msgContext.getOperation() != null");
                        message.setOperation(msgContext.getOperation());
                        message.setServiceDesc(msgContext.getService().getServiceDescription());
                        message.setServiceObject(obj);

                        // serverMessageController.insertMessage(message);
                        // ///////
                        // keeping message in the server no meaningfull in this mode
                        // becouse the response is going in the same HTTP connection
                        // just to have a in-order invoketion
                        // we have to reffer the last message number
                        Identifier seqAckID =
                                rmHeaders.getSequenceAcknowledgement().getIdentifier();
                        RMSequence serverSeq =
                                serverMessageController.retrieveIfSequenceExists(seqAckID);

                        if (serverSeq != null) {
                            long msgNo =
                                    rmHeaders.getSequence().getMessageNumber().getMessageNumber();
                            long lastProcessedMsgNo;

                            serverSeq.getMessageList().put(new Long(msgNo),
                                    message);

                            while (true) {
                                lastProcessedMsgNo =
                                        serverSeq.getLastProcessedMessageNumber();

                                if (msgNo + 1 == lastProcessedMsgNo) {
                                    super.processMessage(msgContext, reqEnv,
                                            resEnv, obj);

                                    lastProcessedMsgNo++;

                                    break;
                                }

                                Thread.sleep(200);
                            }
                        }

                        // //////
                    } else {

                        // haveing is?
                        System.out.println("msgContext.getOperation() == null");

                        RMSequence responsedSeq =
                                clientMessageController.retrieveIfSequenceExists(rmHeaders.getSequence().getIdentifier());

                        if (responsedSeq != null) {
                            RMMessage resMsg = responsedSeq.retrieveMessage(new Long(message.getMessageNumber()));

                            resMsg.setResponseMessage(message.getRequestMessage());
                        }

                        responsedSeq.setResponseMessage(message);
                    }
                }

                if (rmHeaders.getAckRequest() != null) {
                    System.out.println("rmHeaders.getAckRequest() != null");

                    RMSequence serSeq =
                            serverMessageController.retrieveIfSequenceExists(rmHeaders.getAckRequest().getIdentifier());
                    SequenceAcknowledgement seqAck =
                            serSeq.getSequenceAcknowledgement();    // =serverMessageController.getAcknowledgement(    rmHeaders.getAckRequest().getIdentifier());
                    RMHeaders ackResRMHeaders = new RMHeaders();

                    ackResRMHeaders.setSequenceAcknowledgement(seqAck);

                    try {
                        ackResRMHeaders.toSoapEnvelop(msgContext.getResponseMessage().getSOAPEnvelope());
                    } catch (Exception e1) {
                        throw AxisFault.makeFault(e1);
                    }
                }
            }
        }

        // System.out.println();
        // System.out.println(msgContext.getResponseMessage());
        System.out.println("RMProvider finished");
    }

    /**
     * This method encapsulates the method invocation.
     * 
     * @param msgContext MessageContext
     * @param method     the target method.
     * @param obj        the target object
     * @param argValues  the method arguments
     * @return 
     * @throws Exception 
     */
    protected Object invokeMethod(MessageContext msgContext, Method method, Object obj, Object[] argValues)
            throws Exception {
        return (method.invoke(obj, argValues));
    }

    /**
     * Throw an AxisFault if the requested method is not allowed.
     * 
     * @param msgContext     MessageContext
     * @param allowedMethods list of allowed methods
     * @param methodName     name of target method
     * @throws Exception 
     */
    protected void checkMethodName(MessageContext msgContext, String allowedMethods, String methodName)
            throws Exception {

        // Our version doesn't need to do anything, though inherited
        // ones might.
    }
}
