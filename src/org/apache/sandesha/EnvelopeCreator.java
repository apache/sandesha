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
	 * @param String uuid
	 * @param RMMessageContext rmMessageContext
	 * @return SOAPEnvelope
	 */
	public static SOAPEnvelope createCreateSequenceResponseEnvelope(
		String uuid,
		RMMessageContext rmMessageContext) {

		//Set the SOAPEnvelope to the resEnv of the rmMessageContext.
		AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
		SOAPEnvelope envelope = createBasicEnvelop();

		try {

			AddressingHeaders outGoingAddressingHaders =
				new AddressingHeaders(envelope, null, true, false, true, false, null);

			Action action = new Action(new URI(Constants.ACTION_CREATE_SEQUENCE_RESPONSE));

			SOAPHeaderElement actionElement = action.toSOAPHeaderElement(envelope);
			//actionElement.setMustUnderstand(true);
			outGoingAddressingHaders.setAction(action);

			//Set the messageID
			UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
			MessageID messageId = new MessageID(new URI("uuid:" + uuidGen.nextUUID()));
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
				incommingMessageId.toString(),
				new QName(Constants.WSA_PREFIX, Constants.WSA_NS));

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
	
	

	public static SOAPEnvelope createBasicEnvelop() {

		SOAPEnvelope soapEnv = new SOAPEnvelope();
		addNamespaceDeclarations(soapEnv);

		return soapEnv;

	}

	public static void addNamespaceDeclarations(SOAPEnvelope soapEnv) {
		try {
			soapEnv.addNamespaceDeclaration("wsrm", Constants.NS_URI_RM);
			soapEnv.addNamespaceDeclaration(
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
	public static SOAPEnvelope createAcknowledgement(RMMessageContext rmMessageContext, Vector ackRangeVector) {
		
		AddressingHeaders addressingHeaders = rmMessageContext.getAddressingHeaders();
		SOAPEnvelope envelope = createBasicEnvelop();
		try {
		AddressingHeaders outGoingAddressingHaders = new AddressingHeaders(envelope, null, true, false, true, false, null);
		//Action for sequence acknowledgement.
		Action action = new Action(new URI(Constants.RM_SEQUENCE_ACKNOWLEDMENT_ACTION));

		SOAPHeaderElement actionElement = action.toSOAPHeaderElement(envelope);
		//actionElement.setMustUnderstand(true);
		outGoingAddressingHaders.setAction(action);
		
		//Set the messageID
		UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
		MessageID messageId = new MessageID(new URI("uuid:" + uuidGen.nextUUID()));
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
		SequenceAcknowledgement seqAck= new SequenceAcknowledgement();
		seqAck.setAckRanges(ackRangeVector);
		seqAck.setIdentifier(rmMessageContext.getRMHeaders().getSequence().getIdentifier());
		seqAck.toSoapEnvelop(envelope);
		
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return envelope;
	}

}
