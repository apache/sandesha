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

package org.apache.sandesha.ws.rm;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.sandesha.ws.utility.Identifier;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class CreateSequenceResponse
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class CreateSequenceResponse implements IRmElement {

    /**
     * Field createSequenceResponse
     */
    private MessageElement createSequenceResponse;

    /**
     * Field identifier
     */
    private Identifier identifier;

    /**
     * Constructor CreateSequenceResponse
     */
    public CreateSequenceResponse() {

        createSequenceResponse = new MessageElement();

        createSequenceResponse.setName("wsrm:CreateSequenceResponse");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */

    /**
     * Method getSoapElement
     * 
     * @return 
     */
    public MessageElement getSoapElement() {
        return createSequenceResponse;
    }

    /**
     * Method toSoapEnvelop
     * 
     * @param envelop 
     * @return 
     * @throws SOAPException 
     */
    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelop)
            throws SOAPException {

        SOAPEnvelope env = envelop;
        ;

        /*
         * createSequenceResponse.addChildElement(identifier.getSoapElement());
         *
         *       env.addHeader((SOAPHeaderElement)createSequenceResponse);
         */
        if (env.getBody() == null) {
            env.addBody();
        }

        Name name =
                env.createName("", "wsrm",
                        "http://schemas.xmlsoap.org/ws/2004/03/rm");
        SOAPBodyElement bodyElement =
                (SOAPBodyElement) env.getBody().addBodyElement(name);

        bodyElement.setName("CreateSequenceResponse");

        if (identifier != null) {
            identifier.toSOAPEnvelope(bodyElement);
        }

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     * 
     * @param bodyElement 
     * @return 
     */
    public CreateSequenceResponse fromSOAPEnveploe(SOAPBodyElement bodyElement) {

        Iterator iterator = bodyElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {

            // System.out.println(iterator.next());
            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals("wsu:Identifier")) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals("Identifier")) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }
        }

        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
     */

    /**
     * Method addChildElement
     * 
     * @param element 
     * @throws SOAPException 
     */
    public void addChildElement(MessageElement element) throws SOAPException {
        createSequenceResponse.addChildElement(element);
    }

    /**
     * @return 
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier 
     */
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }
}
