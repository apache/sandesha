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
package org.apache.sandesha.client;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.From;
import org.apache.axis.message.addressing.MessageID;
import org.apache.axis.message.addressing.ReplyTo;
import org.apache.axis.message.addressing.To;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessage;
import org.apache.sandesha.RMSequence;
import org.apache.sandesha.ws.rm.AckRequested;
import org.apache.sandesha.ws.rm.CreateSequence;
import org.apache.sandesha.ws.rm.LastMessage;
import org.apache.sandesha.ws.rm.MessageNumber;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.Sequence;
import org.apache.sandesha.ws.utility.Identifier;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

/**
 * @author
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class RMClientService {

    /**
     * Field log
     */
    protected static Log log = LogFactory.getLog(RMSequence.class.getName());

    /**
     * Method clientMethod
     * 
     * @param reqSOAPEnvelop     
     * @param sequenceID         
     * @param destinationURL     
     * @param sourceURL 
     * @param isSynchronous           
     * @param isLastMessage 
     * @param isResponseExpected 
     * @param action
     * @param replyTo
     * 
     * @return String
     */
    public String clientMethod(
        String reqSOAPEnvelop,
        String sequenceID,
        String destinationURL,
        String sourceURL,
        String isSynchronous,
        String isLastMessage,
        String isResponseExpected,
        String action,
        String replyTo)
        throws SOAPException, ServiceException, AxisFault {

        String stringReturn = null;
        ClientMessageController clientMessageController =
            ClientMessageController.getInstance();
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

        try {

            if (sequenceID.equals("")) {
                if ((clientMessageController.getSequenceIdentifier()
                    == null)) {

                    URI messageIDURIForCreateSequence;
                    messageIDURIForCreateSequence =
                        new URI("uuid:" + uuidGen.nextUUID());
                    MessageID messageIDForCreateSequence =
                        new MessageID(messageIDURIForCreateSequence);
                    SOAPEnvelope envelopToSend =
                        getSimpleEnvelope(
                            Constants.ACTION_CREATE_SEQUENCE,
                            messageIDForCreateSequence);

                    Address anonymousAddress =
                        new Address(Constants.ANONYMOUS_URI);
                    ReplyTo replyToForCreateSequence =
                        new ReplyTo(anonymousAddress);
                    replyToForCreateSequence.toSOAPHeaderElement(envelopToSend);

                    To toForCreateSequence = new To(destinationURL);
                    toForCreateSequence.toSOAPHeaderElement(envelopToSend);

                    CreateSequence createSequence = new CreateSequence();
                    createSequence.toSoapEnvelop(envelopToSend);

                    Call call = new Call(destinationURL);
                    call.setRequestMessage(new Message(envelopToSend));
                    call.invoke();
                    SOAPEnvelope responseEnvelope =
                        call.getResponseMessage().getSOAPEnvelope();
                    RMHeaders rmHeaders = new RMHeaders();
                    rmHeaders.fromSOAPEnvelope(responseEnvelope);
                    Identifier tempIdentifier =
                        rmHeaders.getCreateSequenceResponse().getIdentifier();

                    if (tempIdentifier != null) {
                        sequenceID = tempIdentifier.getIdentifier();
                        clientMessageController.setSequenceIdentifier(
                            tempIdentifier);
                    } else {
                        throw new AxisFault("No Response for Create Sequence Request..");
                    }

                } else {
                    sequenceID =
                        clientMessageController
                            .getSequenceIdentifier()
                            .getIdentifier();
                }
            }

            Identifier identifier = new Identifier();
            identifier.setIdentifier(sequenceID);
            Message message = new Message(reqSOAPEnvelop);
            RMMessage rmMessage = new RMMessage(message);

            rmMessage.setDestinationURL(destinationURL);
            rmMessage.setToClientServiceURL(sourceURL);
            rmMessage.setIsOneWay(isSynchronous);

            rmMessage.setIsResponseExpected(isResponseExpected);
            rmMessage.setIdentifier(identifier);

            URI messageIDURI = new URI("uuid:" + uuidGen.nextUUID());
            MessageID messageID = new MessageID(messageIDURI);
            rmMessage.setMessageID(messageID);

            RMSequence sequence =
                clientMessageController.retrieveIfSequenceExists(identifier);

            if (sequence == null) { //means there is no sequence
                sequence = new RMSequence(identifier);
                //add this message to the sequence
                sequence.insertClientMessage(rmMessage);
                clientMessageController.storeSequence(sequence);
                //Store the message with messageID to be used in <relatesTo>
                clientMessageController.storeMessage(rmMessage);
            } else {
                //means that there exists a sequence for this identifier
                sequence.insertClientMessage(rmMessage);
                //Store the message with messageID to be used in <relatesTo>
                clientMessageController.storeMessage(rmMessage);
            }

            if (isSynchronous.equals("true")) {
                //TODO: normal invocation. but for future...
                SOAPEnvelope syncReqEnv =
                    getInitialMessageWithRMHeaders(
                        reqSOAPEnvelop,
                        rmMessage,
                        identifier,
                        destinationURL,
                        Constants.ANONYMOUS_URI,
                        isLastMessage,
                        isResponseExpected,
                        action,
                        replyTo);

                CreateSequence createSqe = new CreateSequence();
                createSqe.toSoapEnvelop(syncReqEnv);

                Service service = new Service();
                Call call = (Call) service.createCall();
                call.setTargetEndpointAddress(destinationURL);
                call.invoke(syncReqEnv);
                stringReturn = call.getResponseMessage().getSOAPPartAsString();

            } else {

                if (isResponseExpected.equals("true")) {
                    //Response Expected
                    SOAPEnvelope envelopToSend =
                        getInitialMessageWithRMHeaders(
                            reqSOAPEnvelop,
                            rmMessage,
                            identifier,
                            destinationURL,
                            sourceURL,
                            isLastMessage,
                            isResponseExpected,
                            action,
                            replyTo);

                    Message newMessage = new Message(envelopToSend);
                    rmMessage.setRequestMessage(newMessage);

                    //Invoke the expected service.
                    Service service = new Service();
                    Call call = (Call) service.createCall();
                    call.setTargetEndpointAddress(destinationURL);
                    call.invoke(envelopToSend);

                    boolean gotResponce = false;
                    int count = 0;

                    Message tempMessage = rmMessage.getRequestMessage();
                    AckRequested ackRequested = new AckRequested();
                    ackRequested.setIdentifier(rmMessage.getIdentifier());
                    SOAPEnvelope retransmissionEnvelop =
                        tempMessage.getSOAPEnvelope();

                    ackRequested.toSoapEnvelop(retransmissionEnvelop);

                    while (count < Constants.MAXIMUM_RETRANSMISSION_COUNT) {
                        count++;
                        //TODO:
                        Thread.sleep(2000);

                        if (new Boolean(rmMessage.getIsResponseExpected())
                            .booleanValue()) {

                            if (rmMessage.getResponseMessage() != null) {
                                gotResponce = true;
                                stringReturn =
                                    rmMessage
                                        .getResponseMessage()
                                        .getSOAPPartAsString();
                                break;
                            }
                        } else {
                            break;
                        }

                        if (!rmMessage.isAcknowledged()) {

                            Message retransmissionMessage =
                                new Message(retransmissionEnvelop);
                            Service retransmissionService = new Service();
                            Call retransmissionCall =
                                (Call) service.createCall();
                            retransmissionCall.setTargetEndpointAddress(
                                destinationURL);
                            retransmissionCall.invoke(envelopToSend);

                        }

                    }

                    if (!gotResponce) {
                        throw new Exception("No Response from the Service");
                    }

                } else {
                    //No Response
                    SOAPEnvelope envelopToSend =
                        getInitialMessageWithRMHeaders(
                            reqSOAPEnvelop,
                            rmMessage,
                            identifier,
                            destinationURL,
                            sourceURL,
                            isLastMessage,
                            isResponseExpected,
                            action,
                            replyTo);

                    Message newMessage = new Message(envelopToSend);
                    rmMessage.setRequestMessage(newMessage);
                    //Invoke the expected service.
                    Service service = new Service();
                    Call call = (Call) service.createCall();
                    call.setTargetEndpointAddress(destinationURL);
                    call.invoke(envelopToSend);

                    boolean gotResponce = false;
                    int count = 0;
                    Message tempMessage = rmMessage.getRequestMessage();
                    AckRequested ackRequested = new AckRequested();
                    ackRequested.setIdentifier(rmMessage.getIdentifier());
                    SOAPEnvelope retransmissionEnvelop =
                        tempMessage.getSOAPEnvelope();
                    ackRequested.toSoapEnvelop(retransmissionEnvelop);

                    while (count < Constants.MAXIMUM_RETRANSMISSION_COUNT) {
                        count++;
                        //Wait for RETRANSMISSION_INTERVAL
                        Thread.sleep(Constants.RETRANSMISSION_INTERVAL);
                        if (!rmMessage.isAcknowledged()) {
                            Message retransmissionMessage =
                                new Message(retransmissionEnvelop);
                            Service retransmissionService = new Service();
                            Call retransmissionCall =
                                (Call) service.createCall();
                            retransmissionCall.setTargetEndpointAddress(
                                destinationURL);
                            retransmissionCall.invoke(envelopToSend);

                        } else {
                            break;
                        }
                    }

                    stringReturn = null;

                }

            }

            if (isLastMessage.equals("true")) {
                clientMessageController.setSequenceIdentifier(null);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            log.error(e);
        }

        return stringReturn;

    }

    /**
     * Method getInitialMessageWithRMHeaders
     * 
     * @param reqSOAPEnvelop
     * @param rmMessage
     * @param identifier
     * @param destinationURL
     * @param toClientServiceURL
     * @param isResponseExpected
     * @param strAction
     * @param strReplyTo
     *     
     * @return SOAPEnvelope
     */

    private SOAPEnvelope getInitialMessageWithRMHeaders(
        String reqSOAPEnvelop,
        RMMessage rmMessage,
        Identifier identifier,
        String destinationURL,
        String toClientServiceURL,
        String isLastMessage,
        String isResponseExpected,
        String strAction,
        String strReplyTo) throws Exception {

        SOAPEnvelope envelopToSend = null;

        
            //Crate amessage using the reqSOAPEnvelop string parameter.
            Message msg = new Message(reqSOAPEnvelop);

            //Get the envelop using the message.
            SOAPEnvelope requestEnvelop = msg.getSOAPEnvelope();
            envelopToSend = new SOAPEnvelope();
            envelopToSend.setSchemaVersion(requestEnvelop.getSchemaVersion());
            envelopToSend.setSoapConstants(requestEnvelop.getSOAPConstants());
            envelopToSend.setBody(
                (org.apache.axis.message.SOAPBody) requestEnvelop.getBody());
            envelopToSend.addNamespaceDeclaration(
                "wsrm",
                "http://schemas.xmlsoap.org/ws/2003/03/rm");
            envelopToSend.addNamespaceDeclaration(
                "wsa",
                "http://schemas.xmlsoap.org/ws/2003/03/addressing");
            envelopToSend.addNamespaceDeclaration(
                "wsu",
                "http://schemas.xmlsoap.org/ws/2003/07/utility");

            //New envelop to create the SOAP envelop to send. Why use of two envelop is not clear.
            //adding the name spaces to the env
            // now get the sequence element
            Sequence seqElement = new Sequence();
            seqElement.setIdentifier(identifier);

            MessageNumber msgNumber = new MessageNumber();
            msgNumber.setMessageNumber(rmMessage.getMessageNumber());

            if (isLastMessage.equals("true")) {
                LastMessage lastMessage = new LastMessage();
                seqElement.setLastMessage(lastMessage);
            }

            seqElement.setMessageNumber(msgNumber);

            //add the sequence element to the envelop to send
            seqElement.toSoapEnvelop(envelopToSend);

            //set the action
            URI actionURI = new URI(strAction);
            Action action = new Action(actionURI);
            action.toSOAPHeaderElement(envelopToSend);

            //Set from address.

            URI fromAddressURI = new URI(toClientServiceURL);
            Address fromAddress = new Address(fromAddressURI);

            //Set the from header.
            From from = new From(fromAddress);
            from.toSOAPHeaderElement(envelopToSend);

            rmMessage.getMessageID().toSOAPHeaderElement(envelopToSend);

            if (!strReplyTo.equals("")) {
                URI relyToAddressURI = new URI(strReplyTo);
                Address replyToAddress = new Address(relyToAddressURI);
                ReplyTo replyTo = new ReplyTo(replyToAddress);
                replyTo.toSOAPHeaderElement(envelopToSend);
            }

            //Set the to address.

            URI toAddress = new To(destinationURL);
            To to = new To(toAddress);
            to.toSOAPHeaderElement(envelopToSend);

            //now store this new message in the rmMessage
            //so that it can be used for retrasmission

        
        return envelopToSend;
    }

    /**
     * Method getSimpleEnvelope
     * 
     * @param action     
     * @param messageID         
     * 
     * @return SOAPEnvelope
     */

    private SOAPEnvelope getSimpleEnvelope(
        String action,
        MessageID messageID) throws Exception {
        SOAPEnvelope envelopToSend = new SOAPEnvelope();
    

            envelopToSend.addNamespaceDeclaration(
                "wsrm",
                "http://schemas.xmlsoap.org/ws/2003/03/rm");
            envelopToSend.addNamespaceDeclaration(
                "wsa",
                "http://schemas.xmlsoap.org/ws/2003/03/addressing");
            envelopToSend.addNamespaceDeclaration(
                "wsu",
                "http://schemas.xmlsoap.org/ws/2003/07/utility");

            URI actionURI = new URI(action);
            Action tempAction = new Action(actionURI);
            tempAction.toSOAPHeaderElement(envelopToSend);

            messageID.toSOAPHeaderElement(envelopToSend);

    
        return envelopToSend;
    }

}
