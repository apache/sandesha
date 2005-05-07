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
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class SequenceOffer
 * @author Jaliya Ekanayaka
 * @author Chamikara Jayalath
 */

public class SequenceOffer extends MessageElement implements IRmElement {

    private MessageElement offerElement;
    
    private Identifier identifier;
    
    public SequenceOffer (){
        offerElement = new MessageElement(Constants.WSRM.SEQUENCE_OFFER,Constants.WSRM.NS_PREFIX_RM,Constants.WSRM.NS_URI_RM);
        //offerElement.setName(Constants.WSRM.NS_PREFIX_RM+Constants.COLON+Constants.WSRM.SEQUENCE_OFFER);
    }
    
    public void addChildElement(MessageElement element) throws SOAPException {
        offerElement.addChildElement(element);
    }

    public MessageElement getSoapElement() throws SOAPException {
        offerElement.addChildElement(identifier.getSoapElement());
        return offerElement;
    }
    
    public SequenceOffer fromSOAPEnvelope(MessageElement element) {

        Iterator iterator = element.getChildElements();
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
    
    public MessageElement toSOAPEnvelope(MessageElement element)
            throws SOAPException {


       if(identifier!=null)
           identifier.toSOAPEnvelope(offerElement);

        element.addChildElement(offerElement);
        return element;
    }
    
    
    public Identifier getIdentifier() {
        return identifier;
    }


    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }
}
