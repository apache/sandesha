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
package org.apache.sandesha;

import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPBody;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.From;
import org.apache.axis.message.addressing.MessageID;
import org.apache.axis.message.addressing.ReplyTo;
import org.apache.axis.message.addressing.To;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.sandesha.ws.rm.CreateSequence;
import org.apache.sandesha.ws.rm.CreateSequenceResponse;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;

/**
 * @author JEkanayake
 *  
 */
public class EnvelopeCreator {

    /**
     * This static method will create a SOAPEnvelop for CreateSequenceResponse.
     * 
     * @param String
     *            uuid
     * @param RMMessageContext
     *            rmMessageContext
     * @return SOAPEnvelope
     */
    public static SOAPEnvelope createCreateSequenceResponseEnvelope(
            String uuid, RMMessageContext rmMessageContext) {

        //Set the SOAPEnvelope to the resEnv of the rmMessageContext.
        AddressingHeaders addressingHeaders = rmMessageContext
                .getAddressingHeaders();
        SOAPEnvelope envelope = createBasicEnvelop();

        try {

            AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(
                    envelope, null, true, false, true, false, null);

            Action action = new Action(new URI(
                    Constants.ACTION_CREATE_SEQUENCE_RESPONSE));

            SOAPHeaderElement actionElement = action
                    .toSOAPHeaderElement(envelope);
            //actionElement.setMustUnderstand(true);
            outGoingAddressingHaders.setAction(action);

            //Set the messageID
            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
            MessageID messageId = new MessageID(new URI("uuid:"
                    + uuidGen.nextUUID()));
            outGoingAddressingHaders.setMessageID(messageId);

            //Set the <wsa:From> address from the incoming <wsa:To>
            To incommingTo = addressingHeaders.getTo();
            URI fromAddressURI = new URI(incommingTo.toString());

            Address fromAddress = new Address(fromAddressURI);
            From from = new From(fromAddress);
            outGoingAddressingHaders.setFrom(from);

            //RelatesTo
            MessageID incommingMessageId = addressingHeaders.getMessageID();
            outGoingAddressingHaders.addRelatesTo(
                    incommingMessageId.toString(), new QName(
                            Constants.WSA_PREFIX, Constants.WSA_NS));

            //Setting the <To>
            ReplyTo incommingReplyTo = (ReplyTo) addressingHeaders.getReplyTo();
            Address incommingAddress = incommingReplyTo.getAddress();
            To to = new To(new URI(incommingAddress.toString()));
            outGoingAddressingHaders.setTo(to);

            outGoingAddressingHaders.toEnvelope(envelope, null);

            //now set the body elements
            CreateSequenceResponse response = new CreateSequenceResponse();
            Identifier id = new Identifier();
            id.setIdentifier("uuid:" + uuid);
            response.setIdentifier(id);
            response.toSoapEnvelop(envelope);
        } catch (MalformedURIException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println(envelope.toString());

        return envelope;

    }

    //TODO: Recheck this method.
    //the integer value endPoint is used to identify from where we call
    //this method. If we call this form Client then pass the parameter
    //Constants.CLIENT. if it is called from the server then it should be
    //Constants.SERVER
    
    public static SOAPEnvelope createCreateSequenceEnvelope(String uuid,
            RMMessageContext message, int endPoint) {

        AddressingHeaders addressingHeaders = message.getAddressingHeaders();
        SOAPEnvelope envelope = createBasicEnvelop();

        try {
            AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(
                    envelope, null, true, false, true, false, null);

            Action action = new Action(
                    new URI(Constants.ACTION_CREATE_SEQUENCE));
            SOAPHeaderElement acionElement = action
                    .toSOAPHeaderElement(envelope);
            outGoingAddressingHaders.setAction(action);

            MessageID messageId = new MessageID(new URI("uuid:" + uuid));
            outGoingAddressingHaders.setMessageID(messageId);
            
            if(endPoint==0){
                //Setting from the Client
                outGoingAddressingHaders.setFrom(addressingHeaders.getFrom());
                outGoingAddressingHaders.setTo(addressingHeaders.getTo());
                outGoingAddressingHaders.setReplyTo(addressingHeaders.getReplyTo());
                
            }else if(endPoint==1){
                //Setting from the Server
                //setting FROM and REPLY TO
                To incommingTo = addressingHeaders.getTo();
                URI fromURI = new URI(incommingTo.toString());
                Address addr = new Address(fromURI);
                From from = new From(addr);
                outGoingAddressingHaders.setFrom(from);
                //Reply to is the same.
                outGoingAddressingHaders.setReplyTo(from);

                //Setting To
                ReplyTo incommingReplyTo = (ReplyTo) addressingHeaders.getReplyTo();
                Address incommingAddress = incommingReplyTo.getAddress();
                To to = new To(new URI(incommingAddress.toString()));
                outGoingAddressingHaders.setTo(to);
            }
            
            outGoingAddressingHaders.toEnvelope(envelope);

            CreateSequence createSeq = new CreateSequence();
            createSeq.toSoapEnvelop(envelope);

            System.out.println("+++++++DISPLAYING ENVOLOP++++++++");
            System.out.println(envelope.getAsString());

        } catch (MalformedURIException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return envelope;
    }

    public static SOAPEnvelope createBasicEnvelop() {

        SOAPEnvelope soapEnv = new SOAPEnvelope();
        addNamespaceDeclarations(soapEnv);

        return soapEnv;

    }

    public static void addNamespaceDeclarations(SOAPEnvelope soapEnv) {
        try {
            soapEnv.addNamespaceDeclaration("wsrm", Constants.NS_URI_RM);
            soapEnv
                    .addNamespaceDeclaration(
                            "wsa",
                            org.apache.axis.message.addressing.Constants.NS_URI_ADDRESSING);
            soapEnv.addNamespaceDeclaration("wsu", Constants.NS_URI_WSU);

        } catch (SOAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * @param rmMessageContext
     * @param ackRangeVector
     * @return
     */
    public static SOAPEnvelope createAcknowledgementEnvelope(
            RMMessageContext rmMessageContext, Vector ackRangeVector) {

        AddressingHeaders addressingHeaders = rmMessageContext
                .getAddressingHeaders();
        SOAPEnvelope envelope = createBasicEnvelop();
        try {
            AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(
                    envelope, null, true, false, true, false, null);
            //Action for sequence acknowledgement.
            Action action = new Action(new URI(
                    Constants.RM_SEQUENCE_ACKNOWLEDMENT_ACTION));

            SOAPHeaderElement actionElement = action
                    .toSOAPHeaderElement(envelope);
            //actionElement.setMustUnderstand(true);
            outGoingAddressingHaders.setAction(action);

            //Set the messageID
            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
            MessageID messageId = new MessageID(new URI("uuid:"
                    + uuidGen.nextUUID()));
            outGoingAddressingHaders.setMessageID(messageId);

            //Set the <wsa:From> address from the incoming <wsa:To>
            To incommingTo = addressingHeaders.getTo();
            URI fromAddressURI = new URI(incommingTo.toString());

            Address fromAddress = new Address(fromAddressURI);
            From from = new From(fromAddress);
            outGoingAddressingHaders.setFrom(from);

            //Add to <To>
            Address inFrom = addressingHeaders.getFrom().getAddress();
            To to = new To(new URI(inFrom.toString()));
            outGoingAddressingHaders.setTo(to);

            //Set the addressing headers to the SOAPEnvelope.
            outGoingAddressingHaders.toEnvelope(envelope, null);

            //Set <wsrm:SequenceAcknowldgement>
            SequenceAcknowledgement seqAck = new SequenceAcknowledgement();
            seqAck.setAckRanges(ackRangeVector);
            seqAck.setIdentifier(rmMessageContext.getRMHeaders().getSequence()
                    .getIdentifier());
            seqAck.toSoapEnvelop(envelope);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return envelope;
    }

    /**
     * @param rmMessageContext
     */
    public static SOAPEnvelope createServiceResponseEnvelope(
            RMMessageContext rmMessageContext) {
        AddressingHeaders addressingHeaders = rmMessageContext
                .getAddressingHeaders();
        SOAPEnvelope responseEnvelope = createBasicEnvelop();
        try {
            AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(
                    responseEnvelope, null, true, false, true, false, null);
            //TODO Action for RESPONSE MESSAGE
            //Do we need this????

            //Set the messageID
            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
            MessageID messageId = new MessageID(new URI("uuid:"
                    + uuidGen.nextUUID()));
            outGoingAddressingHaders.setMessageID(messageId);

            //Set the <wsa:From> address from the incoming <wsa:To>
            To incommingTo = addressingHeaders.getTo();
            URI fromAddressURI = new URI(incommingTo.toString());

            Address fromAddress = new Address(fromAddressURI);
            From from = new From(fromAddress);
            outGoingAddressingHaders.setFrom(from);

            //TODO
            //HARD CODED REPLYTO
            //THIS SHOULD BE SET USING A PROPERTY FOR THE SEVER SIDE
            ReplyTo replyTo = new ReplyTo(
                    new Address(
                            "http://localhost:8080/axis/services/EchoStringService?wsdl"));
            outGoingAddressingHaders.setReplyTo(from);

            //Add to <To>
            Address inFrom = addressingHeaders.getFrom().getAddress();
            To to = new To(new URI(inFrom.toString()));
            outGoingAddressingHaders.setTo(to);

            //Set the addressing headers to the SOAPEnvelope.
            outGoingAddressingHaders.toEnvelope(responseEnvelope, null);

            responseEnvelope.setBody((SOAPBody) rmMessageContext.getResEnv()
                    .getBody());

        } catch (SOAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return responseEnvelope;
    }
    
    /**
     * 
     * @param uuid
     * @param rmMessageContext
     * @param endPoint
     * @return
     */
    public static SOAPEnvelope createServiceRequestEnvelope(
            String uuid,RMMessageContext rmMessageContext, int endPoint) {
        //Variable for SOAPEnvelope
        SOAPEnvelope requestEnvelope = null;
        //Get the AddressingHeaders
        AddressingHeaders addressingHeaders = rmMessageContext
                .getAddressingHeaders();

        try {
            //Get the request envelope from the incoming message context.
            requestEnvelope = rmMessageContext.getMsgContext()
                    .getRequestMessage().getSOAPEnvelope();
            
            rmMessageContext.getRMHeaders().toSoapEnvelop(requestEnvelope);
            AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(
                    requestEnvelope, null, true, false, true, false, null);
             
    	//Set the messageID
    	MessageID messageId = new MessageID(new URI("uuid:"
            + uuid));
    	outGoingAddressingHaders.setMessageID(messageId);
    	    	
    	//This method is called from the client only.
    	if(endPoint==0){
             //Setting from the Client
             outGoingAddressingHaders.setFrom(addressingHeaders.getFrom());
             outGoingAddressingHaders.setTo(addressingHeaders.getTo());
             outGoingAddressingHaders.setReplyTo(addressingHeaders.getReplyTo());
         }

    	//Set the addressing headers to the SOAPEnvelope.
    	outGoingAddressingHaders.toEnvelope(requestEnvelope, null);
    	        
        } catch (SOAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return requestEnvelope;
    }

}