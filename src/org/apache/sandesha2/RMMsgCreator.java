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
import org.apache.axis2.addressing.om.AddressingHeaders;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.wsrm.Accept;
import org.apache.sandesha2.wsrm.AcksTo;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.IOMRMElement;
import org.apache.sandesha2.wsrm.Identifier;

/**
 * @author
 */
public class RMMsgCreator {

	public static RMMsgContext createCreateSeqResponseMsg (RMMsgContext createSeqMessage, MessageContext outMessage) throws AxisFault {
		
		IOMRMElement messagePart = 
			createSeqMessage.getMessagePart(Constants.MESSAGE_PART_CREATE_SEQ);
		CreateSequence cs = (CreateSequence) messagePart;
		
		CreateSequenceResponse response = new CreateSequenceResponse();
		
		Identifier identifier = new Identifier();
		// TODO : set the an appropriate id 
		identifier.setIndentifer("temp-id-of-sandesha");
		response.setIdentifier(identifier);
		Accept accept = new Accept();
		EndpointReference acksToEPR = createSeqMessage.getTo();  
		AcksTo acksTo = new AcksTo(acksToEPR);
		accept.setAcksTo(acksTo);
		response.setAccept(accept);
		
		SOAPEnvelope envelope = createSeqMessage.getSOAPEnvelope();
		envelope.getBody().getFirstChild().detach();
		
		response.toSOAPEnvelope(envelope);
        EndpointReference fromEPR= new EndpointReference("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous");

        outMessage.setFrom(fromEPR);
        outMessage.setWSAAction("http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse");
        
        /*try {
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			envelope.serialize(writer);
		}catch (Exception ex){
			ex.printStackTrace();
		}*/
		
        outMessage.setEnvelope(envelope);
        RMMsgContext createSeqResponse = new RMMsgContext(outMessage);
		return createSeqResponse;
	}
}
