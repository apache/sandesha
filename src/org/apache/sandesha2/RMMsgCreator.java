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

package org.apache.sandesha2;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.impl.MIMEOutputUtils;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.wsdl.builder.wsdl4j.WSDL11MEPFinder;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.AcknowledgementRange;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.Address;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.wsdl.WSDLConstants;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */

public class RMMsgCreator {

	public static RMMsgContext createCreateSeqResponseMsg (RMMsgContext createSeqMessage, MessageContext outMessage) throws AxisFault {
		
		IOMRMElement messagePart = 
			createSeqMessage.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ);
		CreateSequence cs = (CreateSequence) messagePart;
		
		CreateSequenceResponse response = new CreateSequenceResponse();
		
		Identifier identifier = new Identifier();
		String newSequenceID = SandeshaUtil.getUUID();
		identifier.setIndentifer(newSequenceID);
		
		response.setIdentifier(identifier);
		
		Accept accept = new Accept();
		EndpointReference acksToEPR = createSeqMessage.getTo();  
		AcksTo acksTo = new AcksTo();
		Address address = new Address ();
		address.setEpr(acksToEPR); 
		acksTo.setAddress(address);
		accept.setAcksTo(acksTo);
		response.setAccept(accept);
	
		SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).getDefaultEnvelope();	
		response.toOMElement(envelope.getBody());
        outMessage.setWSAAction(Constants.WSRM.NS_URI_CREATE_SEQ_RESPONSE);
        
        String newMessageId = SandeshaUtil.getUUID();
        outMessage.setMessageID(newMessageId);
		
        outMessage.setEnvelope(envelope);
        
        RMMsgContext createSeqResponse = null;
        try {
        	createSeqResponse = MsgInitializer.initializeMessage(outMessage);
        }catch (RMException ex) {
        	throw new AxisFault ("Cant initialize the message");
        }
        
        return createSeqResponse;
	}
	
	
	//Adds a ack message to the following message.
	public static void addAckMessage (RMMsgContext applicationMsg) throws AxisFault {
		SOAPEnvelope envelope = applicationMsg.getSOAPEnvelope();
		if(envelope==null) {
			SOAPEnvelope newEnvelope = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).getDefaultEnvelope();
			applicationMsg.setSOAPEnvelop(envelope);
		}
		envelope = applicationMsg.getSOAPEnvelope();
		
		MessageContext requestMessage = applicationMsg.getMessageContext().getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN);

		RMMsgContext reqRMMsgCtx = null;
		
        try {
        	reqRMMsgCtx = MsgInitializer.initializeMessage(requestMessage);
        }catch (RMException ex) {
        	throw new AxisFault ("Cant initialize the message");
        }
		
        
        Sequence reqSequence = (Sequence) reqRMMsgCtx.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
		if (reqSequence==null)
			throw new AxisFault ("Sequence part of application message is null");
        
		String sequenceId = reqSequence.getIdentifier().getIdentifier();
		
        SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement ();
		Identifier id = new Identifier ();
		id.setIndentifer(sequenceId);
		sequenceAck.setIdentifier(id);
		AcknowledgementRange range = new AcknowledgementRange ();
		
		//TODO correct below
		range.setUpperValue(1);
		range.setLowerValue(1);
			
		sequenceAck.addAcknowledgementRanges(range);
		sequenceAck.toOMElement(envelope.getHeader());
		applicationMsg.setAction(Constants.WSRM.ACTION_SEQ_ACK);
		applicationMsg.setMessageId(SandeshaUtil.getUUID()); 
		
	}
}
