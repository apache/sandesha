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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.From;
import org.apache.axis.message.addressing.MessageID;
import org.apache.axis.message.addressing.RelatesTo;
import org.apache.axis.message.addressing.ReplyTo;
import org.apache.axis.message.addressing.To;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.types.URI;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessage;
import org.apache.sandesha.RMSequence;
import org.apache.sandesha.client.ClientMessageController;
import org.apache.sandesha.server.MessageInserter;
import org.apache.sandesha.server.ServerMessageController;
import org.apache.sandesha.ws.rm.CreateSequenceResponse;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;

import javax.xml.namespace.QName;
import java.util.List;


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

    public void processMessage(
        MessageContext msgContext,
        SOAPEnvelope reqEnv,
        SOAPEnvelope resEnv,
        Object obj)
        throws Exception {
            
        
        if (!(isRmHeadersAvailable(msgContext) && isAddressingHeadersAvailable(msgContext))) {
            throw new AxisFault("Insufficient Headers to Process Reliability.");
        } else {
            AddressingHeaders addressingHeaders =
                (AddressingHeaders) msgContext.getProperty(
                    org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
            RMHeaders rmHeaders =
                (RMHeaders) msgContext.getProperty(org.apache.sandesha.Constants.ENV_RM_REQUEST_HEADERS);

            boolean anonymousFrom = false;
            boolean anonymousReplyTo = false;

            if (addressingHeaders.getFrom() != null) {
                if (addressingHeaders.getFrom().getAddress().toString().equals(Constants.ANONYMOUS_URI)) {
                    anonymousFrom = true;
                }
            }

            if (addressingHeaders.getReplyTo() != null) {
                if (addressingHeaders.getReplyTo().getAddress().toString().equals(Constants.ANONYMOUS_URI)) {
                    anonymousReplyTo = true;
                }
            }

            ServerMessageController serverMessageController = ServerMessageController.getInstance();
            ClientMessageController clientMessageController = ClientMessageController.getInstance();

            Action action = addressingHeaders.getAction();
            String strAction = action.toString();
            //this returns something like "http://schemas.xmlsoap.org/ws/2004/03/rm/TerminateSequence" 

            if (strAction.equals(Constants.ACTION_CREATE_SEQUENCE)) {
                //TODO:create the required env and send it to the source
                
 
                if (!anonymousReplyTo) {
                    //TODO:add the create seq flags
                    ////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////
                    

                    SOAPEnvelope envelope = createSequenceResponseEnvelop(addressingHeaders, msgContext);
                    //create the call
                    Service service = new Service();
                    Call call = (Call) service.createCall();
                    

                    //get  the reply address
                    ReplyTo replyTo = (ReplyTo) addressingHeaders.getReplyTo();
                    URI replyToAddress = replyTo.getAddress();
                    call.setTargetEndpointAddress(replyToAddress.toString());
                    
                    RMHeaders createSeqRMHeadres = new RMHeaders();
                    createSeqRMHeadres.fromSOAPEnvelope(envelope);
                    RMSequence createSeq = new RMSequence();
                    createSeq.setSequenceIdetifer(
                    
                    createSeqRMHeadres.getCreateSequenceResponse().getIdentifier());
                    createSeq.setClientDidReclamtion(true);
                    serverMessageController.storeSequence(createSeq);
                    
                    
                    
                    
                    Message msg=new Message(envelope);
                    call.setRequestMessage(msg);
                    
                    //call.setReturnType(XMLType.AXIS_VOID);
                    
                    

                    //invoke
                    call.invoke();
                    
                    //disconnect http
                    msgContext.setResponseMessage(null);
                } else {
                    

                    //KEEP THE MESSAGE/////////////////////////////////////////
                    //TODO:

                    SOAPEnvelope envelope = createSequenceResponseEnvelop(addressingHeaders, msgContext);
                    RMHeaders createSeqRMHeadres = new RMHeaders();
                    createSeqRMHeadres.fromSOAPEnvelope(envelope);
                    RMSequence createSeq = new RMSequence();
                    createSeq.setSequenceIdetifer(createSeqRMHeadres.getCreateSequenceResponse().getIdentifier());
                    createSeq.setClientDidReclamtion(true);
                    serverMessageController.storeSequence(createSeq);
                    msgContext.setResponseMessage(new Message(envelope));
                }

            } else if (strAction.equals(Constants.ACTION_CREATE_SEQUENCE_RESPONSE)) {
                //TODO:
                
                createSequenceResponse(msgContext);

            } else if (strAction.equals(Constants.ACTION_TERMINATE_SEQUENCE)) {
                //TODO:
                //
                terminateSequence(rmHeaders.getTerminateSequence().getIdentifier());
            } else {
                if (!anonymousFrom) {
                    ///TODO Check MessageInserter
                    MessageInserter messageInserter = new MessageInserter(msgContext, obj);
                    Thread thread = new Thread(messageInserter);
                    thread.start();
                    msgContext.setResponseMessage(null);

                } else {

                    RMMessage message = new RMMessage();

                    
                    message.setAddressingHeaders(addressingHeaders);
                    message.setRMHeaders(rmHeaders);
                    message.setRequestMessage(msgContext.getRequestMessage());
                    msgContext.setEncodingStyle(msgContext.getEncodingStyle());

                    if (rmHeaders.getSequenceAcknowledgement() != null) {
                        

                        Identifier seqAckID = rmHeaders.getSequenceAcknowledgement().getIdentifier();
                        RMSequence clientSeq = clientMessageController.retrieveIfSequenceExists(seqAckID);
                        RMSequence serverSeq = serverMessageController.retrieveIfSequenceExists(seqAckID);

                        if (clientSeq != null) {
                            
                            clientSeq.setSequenceAcknowledgement(rmHeaders.getSequenceAcknowledgement());
                        }

                        if (serverSeq != null) {
                            
                            serverSeq.setSequenceAcknowledgement(rmHeaders.getSequenceAcknowledgement());
                        }
                    }

                    if (rmHeaders.getSequence() != null) {
                        
                        message.setIdentifier(rmHeaders.getSequence().getIdentifier());

                        if (msgContext.getOperation() != null) {
                            
                            message.setOperation(msgContext.getOperation());
                            message.setServiceDesc(msgContext.getService().getServiceDescription());
                            message.setServiceObject(obj);

                            // serverMessageController.insertMessage(message);
                            // ///////
                            // keeping message in the server no meaningfull in this mode
                            // becouse the response is going in the same HTTP connection
                            // just to have a in-order invoketion
                            // we have to reffer the last message number
                            Identifier seqAckID = rmHeaders.getSequenceAcknowledgement().getIdentifier();
                            RMSequence serverSeq = serverMessageController.retrieveIfSequenceExists(seqAckID);

                            if (serverSeq != null) {
                                long msgNo = rmHeaders.getSequence().getMessageNumber().getMessageNumber();
                                long lastProcessedMsgNo;

                                serverSeq.getMessageList().put(new Long(msgNo), message);

                                while (true) {
                                    lastProcessedMsgNo = serverSeq.getLastProcessedMessageNumber();

                                    if (msgNo + 1 == lastProcessedMsgNo) {
                                        super.processMessage(msgContext, reqEnv, resEnv, obj);

                                        lastProcessedMsgNo++;

                                        break;
                                    }

                                    Thread.sleep(Constants.SERVICE_INVOKE_INTERVAL);
                                }
                            }

                            // //////
                        } else {
                            //TODO: response message processing 
                            //This is when we got a a response with "From as anonymous...." 

                            // haveing is?
                            
                            
                            RMSequence responsedSeq =
                                    clientMessageController.retrieveIfSequenceExists(rmHeaders.getSequence().getIdentifier());
                            
                            if (responsedSeq != null) {
                                RMMessage resMsg = responsedSeq.retrieveMessage(new Long(message.getMessageNumber()));
                            
                                resMsg.setResponseMessage(message.getRequestMessage());
                            }
                            
                            responsedSeq.insertResponseMessage(message);
                        }
                    }

                    if (rmHeaders.getAckRequest() != null) {
                        

                        RMSequence serSeq =
                            serverMessageController.retrieveIfSequenceExists(
                                rmHeaders.getAckRequest().getIdentifier());
                        SequenceAcknowledgement seqAck = serSeq.getSequenceAcknowledgement();
                        
                        RMHeaders ackResRMHeaders = new RMHeaders();

                        ackResRMHeaders.setSequenceAcknowledgement(seqAck);
                        ackResRMHeaders.toSoapEnvelop(msgContext.getResponseMessage().getSOAPEnvelope());
    
                    }
                }
            }

        }

    }
    
    /**
     * Method isRmHeadersAvailable
     * 
     * @param messageContext MessageContext
     * @return boolean
     * 
     */
    
    

    private boolean isRmHeadersAvailable(MessageContext messageContext) {
        RMHeaders rmHeaders =
            (RMHeaders) messageContext.getProperty(org.apache.sandesha.Constants.ENV_RM_REQUEST_HEADERS);

        if ((rmHeaders.getAckRequest() != null)
            || (rmHeaders.getCreateSequence() != null)
            || (rmHeaders.getCreateSequenceResponse() != null)
            || (rmHeaders.getSequence() != null)
            || (rmHeaders.getSequenceAcknowledgement() != null)
            || (rmHeaders.getTerminateSequence() != null)) {
            return true;
        } else
            return true;
    }
    
    /**
     * Method isAddressingHeadersAvailable
     * 
     * @param messageContext MessageContext
     * @return boolean
     * 
     */

    private boolean isAddressingHeadersAvailable(MessageContext messageContext) {
        AddressingHeaders addressingHeaders =
            (AddressingHeaders) messageContext.getProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);

        if ((addressingHeaders.getAction() != null) && (addressingHeaders.getTo() != null))
            return true;
        else
            return false;
    }
    


    /**
     * Method terminateSequence
     * 
     * @param identifier 
     *  
     */
    private void terminateSequence(Identifier identifier) {
        //TODO: is there a way to identify whether this is the server or client
        //try to get both the MesageControllers and check them for this sequence
        ServerMessageController serverMessageController = ServerMessageController.getInstance();
        ClientMessageController clientMessageController = ClientMessageController.getInstance();

        //check in serverMessageController
        if (serverMessageController != null) {
            serverMessageController.removeIfSequenceExists(identifier);
        }
        //check in clientMessageController
        if (clientMessageController != null) {
            clientMessageController.removeIfSequenceExists(identifier);
        }
    }

    /**
     * Method createSequenceResponse
     * 
     * Called when the createSequenceResponse message arrives
     * Now we have received a sequenceIdentifier, So, store it
     * To do this, use the relates to tag.
     * <b>Everytime the server send the responses, it acts as a client and hence it calls ClientMessageController</b>
     * 
     * @param messageContext 
     *  
     */
    private void createSequenceResponse(MessageContext messageContext) {
        //get the ClientMessageController
        ClientMessageController clientMessageController = ClientMessageController.getInstance();

        //get the Relates to id from the env
        AddressingHeaders addressingHeaders =
            (AddressingHeaders) messageContext.getProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
        List relatesToList = addressingHeaders.getRelatesTo();
        if (relatesToList.size() > 0) {
            RelatesTo relatesTo = (RelatesTo) relatesToList.get(0);
            MessageID messageId = new MessageID(relatesTo.getURI());

            if (clientMessageController != null) {
                RMMessage rmMessage = clientMessageController.retrieveIfMessageExists(messageId);
                rmMessage.setResponseMessage(messageContext.getCurrentMessage());
            }
        }
    }

    /**
     * Method createSequenceResponseEnvelop
     * 
     * create the envelope to send to the source who require the sequence identifier
     * 
     * @param addressingHeaders
     *     
     * 
     */
    private SOAPEnvelope createSequenceResponseEnvelop(
        AddressingHeaders addressingHeaders,
        MessageContext messageContext)
        throws Exception {

        SOAPEnvelope envelope = new SOAPEnvelope();

        envelope.addNamespaceDeclaration(
            Constants.NS_PREFIX_RM,
            Constants.NS_URI_RM);
        envelope.addNamespaceDeclaration(
            Constants.WSA_PREFIX,
            Constants.WSA_NS);
        envelope.addNamespaceDeclaration(
            Constants.WSU_PREFIX,
            Constants.WSU_NS);

        Action action = new Action(new URI(Constants.ACTION_CREATE_SEQUENCE_RESPONSE));
        action.toSOAPHeaderElement(envelope);

        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        MessageID messageId = new MessageID(new URI("uuid:" + uuidGen.nextUUID()));
        messageId.toSOAPHeaderElement(envelope);

        To incommingTo = addressingHeaders.getTo();
        URI fromAddressURI = new URI(incommingTo.toString());

        Address fromAddress = new Address(fromAddressURI);
        From from = new From(fromAddress);
        from.toSOAPHeaderElement(envelope);

        ReplyTo incommingReplyTo = (ReplyTo) addressingHeaders.getReplyTo();
        Address incommingAddress = incommingReplyTo.getAddress();
        To to = new To(new URI(incommingAddress.toString()));
        to.toSOAPHeaderElement(envelope);

        MessageID incommingMessageId = addressingHeaders.getMessageID();
        AddressingHeaders outgoingAddressingHaders = new AddressingHeaders();
        outgoingAddressingHaders.addRelatesTo(
             incommingMessageId.toString(),
            (new QName(Constants.WSA_PREFIX, Constants.WSA_NS)));

        //now set the body elements
        CreateSequenceResponse response = new CreateSequenceResponse();
        //UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        Identifier id=new Identifier();
        id.setIdentifier("uuid:"+uuidGen.nextUUID());
        response.setIdentifier(id);
        response.toSoapEnvelop(envelope);

        outgoingAddressingHaders.toEnvelope(envelope);

        return envelope;
    }

}
