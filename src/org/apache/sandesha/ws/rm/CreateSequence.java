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
 * class CreateSequence
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class CreateSequence implements IRmElement {

    /**
     * Field createSequence
     */
    private MessageElement createSequence;

    /**
     * Constructor CreateSequence
     */
    public CreateSequence() {
        createSequence = new MessageElement();
        createSequence.setName("wsrm:CreateSequence");
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
     */
    public MessageElement getSoapElement() {
        return createSequence;
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

        // env.addHeader((SOAPHeaderElement)createSequence);
        if (env.getBody() == null) {
            env.addBody();
        }

        Name name = env.createName("", Constants.NS_PREFIX_RM, Constants.NS_URI_RM);
        SOAPBodyElement bodyElement = (SOAPBodyElement) env.getBody() .addBodyElement(name);

        bodyElement.setName("CreateSequence");

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     *
     * @param bodyElement
     * @return CreateSequence
     */
    public CreateSequence fromSOAPEnveploe(SOAPBodyElement bodyElement) {

        Iterator iterator = bodyElement.getChildElements();

        while (iterator.hasNext()) {

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
     */
    public void addChildElement(MessageElement element) {
    }
}