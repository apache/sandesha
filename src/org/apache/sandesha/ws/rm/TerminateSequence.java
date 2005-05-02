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
import org.apache.sandesha.Constants;


import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class TerminateSequence
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class TerminateSequence implements IRmElement {

    /**
     * Field terminateSequence
     */
    private MessageElement terminateSequence;

    /**
     * Field identifier
     */
    private Identifier identifier;

    /**
     * Constructor TerminateSequence
     */
    public TerminateSequence() {
        terminateSequence = new MessageElement();
        terminateSequence.setName(Constants.WSRM.NS_PREFIX_RM+Constants.COLON+Constants.WSRM.TERMINATE_DEQUENCE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     * @throws SOAPException
     */
    public MessageElement getSoapElement() throws SOAPException {

        terminateSequence.addChildElement(identifier.getSoapElement());

        return terminateSequence;
    }

    /**
     * Method toSoapEnvelop
     *
     * @param envelope
     * @return SOAPEnvelope
     * @throws SOAPException
     */
    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelope)
            throws SOAPException {

        SOAPEnvelope env = envelope;

        if (env.getBody() == null) {
            env.addBody();
        }

        Name name = env.createName("", Constants.WSRM.NS_PREFIX_RM,
                Constants.WSRM.NS_URI_RM);
        SOAPBodyElement bodyElement = (SOAPBodyElement) env.getBody()
                .addBodyElement(name);

        bodyElement.setName(Constants.WSRM.TERMINATE_DEQUENCE);

        if (identifier != null) {
            identifier.toSOAPEnvelope(bodyElement);
        }

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     *
     * @param bodyElement
     * @return TerminateSequence
     */
    public TerminateSequence fromSOAPEnveploe(SOAPBodyElement bodyElement) {

        Iterator iterator = bodyElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {

            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals(Constants.WSU.WSU_PREFIX+Constants.COLON+Constants.WSU.IDENTIFIER)) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSU.IDENTIFIER)) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }
        }

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
     */

    /**
     * Method addChildElement
     *
     * @param element
     * @throws SOAPException
     */
    public void addChildElement(MessageElement element) throws SOAPException {

        terminateSequence.addChildElement(element);

    }

    /**
     * Method getIdentifier
     *
     * @return Identifier
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Method setIdentifier
     *
     * @param identifier
     */
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }
}