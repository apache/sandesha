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
package org.apache.sandesha.ws.utility;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;

import javax.xml.soap.SOAPException;

/**
 * class Identifier
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class Identifier extends URI {

    /**
     * Field identifierElement
     */
    private MessageElement identifierElement;

    /**
     * Field identifier
     */
    private String identifier = null;

    /**
     * Constructor Identifier
     */
    public Identifier() {
        identifierElement = new MessageElement();
        identifierElement.setName("wsu:Identifier");
    }

    /**
     * Method setUri
     *
     * @param uri
     * @throws SOAPException
     */
    public void setUri(String uri) throws SOAPException {
        identifierElement.addTextNode(uri);
    }

    /**
     * Method fromSOAPEnvelope
     *
     * @param element
     * @return
     */
    public Identifier fromSOAPEnvelope(MessageElement element) {

        identifier = element.getValue();
        return this;
    }

    /**
     * Method toSOAPEnvelope
     *
     * @param msgElement
     * @return @throws
     *         SOAPException
     */
    public MessageElement toSOAPEnvelope(MessageElement msgElement)
            throws SOAPException {

        msgElement.addChildElement("Identifier", "wsu").addTextNode(identifier);
        return msgElement;
    }

    /**
     * Method getSoapElement
     *
     * @return @throws
     *         SOAPException
     */
    public MessageElement getSoapElement() throws SOAPException {

        // create the soap element for the message no
        identifierElement.addTextNode(identifier);

        return identifierElement;
    }

    /**
     * Method getIdentifier
     *
     * @return String
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Method setIdentifier
     */
    public void setIdentifier(String string) {
        identifier = string;
    }

    /**
     * Method equals
     *
     * @param obj
     * @return boolean
     */
    public boolean equals(Object obj) {

        if (obj instanceof Identifier) {
            if (this.identifier == ((String) (((Identifier) obj)
                    .getIdentifier()))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Method hashCode
     *
     * @return int
     */
    public int hashCode() {
        return identifier.hashCode();
    }

    /**
     * Method toString
     *
     * @return String
     */
    public String toString() {
        return identifier;
    }
}