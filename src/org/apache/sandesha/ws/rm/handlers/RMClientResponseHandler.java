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
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class RMClientResponseHandler
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class RMClientResponseHandler extends RMHandler {

    /*
     * (non-Javadoc)
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */

    /**
     * Method invoke
     * 
     * @param messageContext 
     * @throws AxisFault 
     */
    public void invoke(MessageContext messageContext) throws AxisFault {

        try {

            // System.out.println("WSRMClientResponseHandler");
            // //System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            // System.out.println(messageContext.getCurrentMessage().getSOAPPartAsString());
            // messageContext.getCurrentMessage()
            SOAPEnvelope soapEnvelope =
                    messageContext.getResponseMessage().getSOAPEnvelope();
            SOAPBody soapBody = soapEnvelope.getBody();

            // System.out.println("In the response handler" );
            // System.out.println(soapEnvelope.toString());
            if (soapBody.getValue() == null) {
                System.out.println("This is what i get");
            } else {

                // System.out.println("In the response handlerrrr" );
                Iterator iterator = soapBody.getChildElements();
                SOAPBodyElement soapBodyElement;

                while (iterator.hasNext()) {
                    soapBodyElement = (SOAPBodyElement) iterator.next();

                    // System.out.println(soapBodyElement.getLocalName());
                    if (soapBodyElement.getLocalName().equals("clientMethodResponse")) {

                        // System.out.println("clientMethodResponse");
                        Iterator ite = soapBodyElement.getChildElements();
                        MessageElement sbe;

                        while (ite.hasNext()) {
                            sbe = (MessageElement) ite.next();

                            if (sbe.getName().equals("clientMethodReturn")) {
                                messageContext.setCurrentMessage(new Message(sbe.getValue()));

                                // System.out.println(sbe.getValue());
                            }
                        }
                    }
                }

                // if()
                if (messageContext.getCurrentMessage().getSOAPEnvelope().getBody().getFault()
                        != null) {
                    soapBodyElement = (SOAPBodyElement) iterator.next();
                    iterator = soapBodyElement.getChildElements();

                    SOAPElement soapElement = (SOAPElement) iterator.next();
                    Message message =
                            new Message(soapElement.getValue());

                    messageContext.setCurrentMessage(message);
                }
            }

            // ////System.out.println("\n" + messageContext.getCurrentMessage().getSOAPPartAsString() + "\n");
        } catch (AxisFault axisFault) {
            System.out.println("axisFault.printStackTrace()");

            // To change body of catch statement use Options | File Templates.
        } catch (SOAPException e) {
            System.out.println("e.printStackTrace()");

            // To change body of catch statement use Options | File Templates.
        }
    }
}
