/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:s
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.sandesha.ws.rm.handlers;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPEnvelope;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * @author Amila Navarathna<br>
 *         Jaliya Ekanayaka<br>
 *         Sudar Nimalan<br>
 *         (Apache Sandesha Project)
 */
public class RMClientRequestHandler extends RMHandler {

    /* (non-Javadoc)
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext messageContext) throws AxisFault {

        //Get the options from the client-config.wsdd.
        //Client should specify the URL of the client host and the port number of the Tomcat sever
        //e.g. http://127.2.5.3:8080
        String sourceURI = (String) getOption("sourceURI");
        //Directroy path is still not used.
        String dirPath = (String) getOption("dirPath");

        /*Test the the values of the options.
        System.out.println(sourceURI);
        System.out.println(dirPath);
        */

        //Get the properties set by the client by accessing the call object using the message context.
        Call call = (Call) messageContext.getProperty(MessageContext.CALL);
        //Three parameters: sequenceIdentifier, isOneWay and isLastMessage.
        String sequenceID = call.getSequenceIdentifier();
        boolean isOneWay = call.isOneWayInvoke();
        boolean isLastMessage = call.isLastMessage();
        boolean isCreateSequence = call.isCreateSequence();
        boolean isResponseExpected = call.isResponseExpected();

        //Get the SOAP envelop of the request message and send it as a string parameter to the 
        //clientService
        SOAPEnvelope requestSOAPEnvelop =
                messageContext.getCurrentMessage().getSOAPEnvelope();
        requestSOAPEnvelop.setSchemaVersion(messageContext.getSchemaVersion());
        requestSOAPEnvelop.setSoapConstants(messageContext.getSOAPConstants());

        //Convert the SOAP envelop to string.
        String strRequestSOAPEnvelop = requestSOAPEnvelop.toString();

        //Get the destination URL from the message context.
        String destinationURL =
                (String) messageContext.getProperty(MessageContext.TRANS_URL);
        //System.out.println("destinationURL :" + destinationURL);

        //Set the destination URL of the message context to the
        String toClientServiceURL =
                sourceURI
                + org.apache.sandesha.Constants.AXIS_SERVICES
                + org.apache.sandesha.Constants.RM_CLIENT_SERVICE
                + org.apache.sandesha.Constants.QUESTION_WSDL;
        //System.out.println("sourceURI : " + sourceURI);
        String clientReferanceURL = sourceURI
                + org.apache.sandesha.Constants.AXIS_SERVICES
                + org.apache.sandesha.Constants.CLIENT_REFERANCE
                + org.apache.sandesha.Constants.QUESTION_WSDL;

        messageContext.setProperty(MessageContext.TRANS_URL,
                toClientServiceURL);

        try {
            //to the envelploe with CALL String
            SOAPEnvelope soapEnvelope =
                    messageContext.getCurrentMessage().getSOAPEnvelope();
            SOAPBody soapBody = soapEnvelope.getBody();

            soapEnvelope.clearBody();
            soapEnvelope.removeHeaders();

            Name name =
                    soapEnvelope.createName(org.apache.sandesha.Constants.CLIENT_METHOD, "ns1", org.apache.sandesha.Constants.RM_CLIENT_SERVICE);
            SOAPBodyElement soapBodyElement = soapBody.addBodyElement(name);

            //Add the SOAP envelop as a string parameter.
            SOAPElement soapElement =
                    soapBodyElement.addChildElement("arg1", "");
            soapElement.addTextNode(strRequestSOAPEnvelop);

            //Add the sequenceIdnetifier
            soapElement = soapBodyElement.addChildElement("arg2", "");
            soapElement.addTextNode(sequenceID);

            //Add the destination URL
            soapElement = soapBodyElement.addChildElement("arg3", "");
            soapElement.addTextNode(destinationURL);

            //Add the toClientServiceURL. This can be used by the asynchronous server to reference the Client Service		
            soapElement = soapBodyElement.addChildElement("arg4", "");
            soapElement.addTextNode(clientReferanceURL);

            //Add the isOneWay as a string value.
            soapElement = soapBodyElement.addChildElement("arg5", "");
            if (isOneWay == true)
                soapElement.addTextNode("true");
            else
                soapElement.addTextNode("false");

            //Add the isLastMessage as a string value
            soapElement = soapBodyElement.addChildElement("arg6", "");
            if (isLastMessage == true)
                soapElement.addTextNode("true");
            else
                soapElement.addTextNode("false");

            //Add the isCreateSequence as a string value
            soapElement = soapBodyElement.addChildElement("arg7", "");
            if (isCreateSequence == true)
                soapElement.addTextNode("true");
            else
                soapElement.addTextNode("false");
				
				
            //Add the isLastMessage as a string value
            soapElement = soapBodyElement.addChildElement("arg8", "");
            if (isResponseExpected == true)
                soapElement.addTextNode("true");
            else
                soapElement.addTextNode("false");

            //soapElement = soapBodyElement.addChildElement("arg5", "");
            //soapElement.addTextNode(dirPath);

            System.out.println("This is the end of the RMClientRequesthandler Following is the SOAP MEssage >>");
            //System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            messageContext.getCurrentMessage().getSOAPEnvelope();
            //System.out.println(	messageContext.getCurrentMessage().getSOAPEnvelope().toString());
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        } catch (SOAPException soapException) {
            //TODO implement the exception.
        }

    }

}
