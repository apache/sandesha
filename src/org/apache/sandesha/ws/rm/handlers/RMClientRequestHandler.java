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

package org.apache.sandesha.ws.rm.handlers;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.sandesha.Constants;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * class RMClientRequestHandler
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class RMClientRequestHandler extends RMHandler {

    /**
     * Method invoke
     * 
     * @param messageContext 
     * @throws AxisFault 
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext messageContext) throws AxisFault {

        // Get the options from the client-config.wsdd.
        // Client should specify the URL of the client host and the port number of the Tomcat sever
        // e.g. http://127.2.5.3:8080
        String sourceURI = (String) getOption("sourceURI");

        // Directroy path is still not used.
        String dirPath = (String) getOption("dirPath");

        /*
         * Test the the values of the options.
         * System.out.println(sourceURI);
         * System.out.println(dirPath);
         */

        // Get the properties set by the client by accessing the call object using the message context.
        Call call = (Call) messageContext.getProperty(MessageContext.CALL);

        // Three parameters: sequenceIdentifier, isOneWay and isLastMessage.
        String sequenceID = null;
        if (call.getProperty(Constants.CLIENT_SEQUENCE_IDENTIFIER) != null) {
            sequenceID = (String) call.getProperty(Constants.CLIENT_SEQUENCE_IDENTIFIER);
        }

        boolean isOneWay = false;
        boolean isLastMessage = false;
        boolean isCreateSequence = false;
        boolean isResponseExpected = false;

        if (call.getProperty(Constants.CLIENT_ONE_WAY_INVOKE) != null) {
            isOneWay = ((Boolean) (call.getProperty(Constants.CLIENT_ONE_WAY_INVOKE))).booleanValue();
        }
        if (call.getProperty(Constants.CLIENT_LAST_MESSAGE) != null) {
            isLastMessage = ((Boolean) (call.getProperty(Constants.CLIENT_LAST_MESSAGE))).booleanValue();
        }
        if (call.getProperty(Constants.CLIENT_CREATE_SEQUENCE) != null) {
            isCreateSequence = ((Boolean) (call.getProperty(Constants.CLIENT_CREATE_SEQUENCE))).booleanValue();
        }
        if (call.getProperty(Constants.CLIENT_RESPONSE_EXPECTED) != null) {
            isResponseExpected = ((Boolean) (call.getProperty(Constants.CLIENT_RESPONSE_EXPECTED))).booleanValue();
        }
        

        // Get the SOAP envelop of the request message and send it as a string parameter to the
        // clientService
        SOAPEnvelope requestSOAPEnvelop =
                messageContext.getCurrentMessage().getSOAPEnvelope();

        requestSOAPEnvelop.setSchemaVersion(messageContext.getSchemaVersion());
        requestSOAPEnvelop.setSoapConstants(messageContext.getSOAPConstants());

        // Convert the SOAP envelop to string.
        String strRequestSOAPEnvelop = requestSOAPEnvelop.toString();

        // Get the destination URL from the message context.
        String destinationURL =
                (String) messageContext.getProperty(MessageContext.TRANS_URL);

        // System.out.println("destinationURL :" + destinationURL);
        // Set the destination URL of the message context to the
        String toClientServiceURL =
                sourceURI + org.apache.sandesha.Constants.AXIS_SERVICES
                + org.apache.sandesha.Constants.RM_CLIENT_SERVICE
                + org.apache.sandesha.Constants.QUESTION_WSDL;

        // System.out.println("sourceURI : " + sourceURI);
        String clientReferanceURL =
                sourceURI + org.apache.sandesha.Constants.AXIS_SERVICES
                + org.apache.sandesha.Constants.CLIENT_REFERANCE
                + org.apache.sandesha.Constants.QUESTION_WSDL;

        messageContext.setProperty(MessageContext.TRANS_URL,
                toClientServiceURL);

        try {

            // to the envelploe with CALL String
            SOAPEnvelope soapEnvelope =
                    messageContext.getCurrentMessage().getSOAPEnvelope();
            SOAPBody soapBody = soapEnvelope.getBody();

            soapEnvelope.clearBody();
            soapEnvelope.removeHeaders();

            Name name = soapEnvelope.createName(org.apache.sandesha.Constants.CLIENT_METHOD, "ns1",
                    org.apache.sandesha.Constants.RM_CLIENT_SERVICE);
            SOAPBodyElement soapBodyElement = soapBody.addBodyElement(name);

            // Add the SOAP envelop as a string parameter.
            SOAPElement soapElement = soapBodyElement.addChildElement("arg1",
                    "");

            soapElement.addTextNode(strRequestSOAPEnvelop);

            // Add the sequenceIdnetifier
            soapElement = soapBodyElement.addChildElement("arg2", "");

            soapElement.addTextNode(sequenceID);

            // Add the destination URL
            soapElement = soapBodyElement.addChildElement("arg3", "");

            soapElement.addTextNode(destinationURL);

            // Add the toClientServiceURL. This can be used by the asynchronous server to reference the Client Service
            soapElement = soapBodyElement.addChildElement("arg4", "");

            soapElement.addTextNode(clientReferanceURL);

            // Add the isOneWay as a string value.
            soapElement = soapBodyElement.addChildElement("arg5", "");

            if (isOneWay == true) {
                soapElement.addTextNode("true");
            } else {
                soapElement.addTextNode("false");
            }

            // Add the isLastMessage as a string value
            soapElement = soapBodyElement.addChildElement("arg6", "");

            if (isLastMessage == true) {
                soapElement.addTextNode("true");
            } else {
                soapElement.addTextNode("false");
            }

            // Add the isCreateSequence as a string value
            soapElement = soapBodyElement.addChildElement("arg7", "");

            if (isCreateSequence == true) {
                soapElement.addTextNode("true");
            } else {
                soapElement.addTextNode("false");
            }

            // Add the isLastMessage as a string value
            soapElement = soapBodyElement.addChildElement("arg8", "");

            if (isResponseExpected == true) {
                soapElement.addTextNode("true");
            } else {
                soapElement.addTextNode("false");
            }

            // soapElement = soapBodyElement.addChildElement("arg5", "");
            // soapElement.addTextNode(dirPath);
            System.out.println("This is the end of the RMClientRequesthandler Following is the SOAP MEssage >>");

            // System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            messageContext.getCurrentMessage().getSOAPEnvelope();

            // System.out.println(   messageContext.getCurrentMessage().getSOAPEnvelope().toString());
            // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        } catch (SOAPException soapException) {
            throw AxisFault.makeFault(soapException);
        }
    }
}
