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

import javax.xml.soap.SOAPException;

/**
 * class IRmElement
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 *         <p/>
 *         <p/>
 *         This is the parent interface for the rm-protocol elements except the element who are
 *         in the format of a URI<br>
 *         <p/>
 *         <b>If require to add the attributes, extend the concrete class with MessageElement</b><br>
 *         <b>If require to add child elements, extend the concrete class with MessageElement</b><br>
 */
public interface IRmElement {

    /**
     * This gives the soap element of the protocol element. Each implementation
     * must implement this method such that then it can be easily get the
     * related soap element
     * 
     * @return the soap element
     * @throws SOAPException 
     */
    MessageElement getSoapElement() throws SOAPException;

    /**
     * Adds an element to this element as a child element.
     * If not required to add a child element, just provide an empty
     * implementation in the concrete class
     * 
     * @param element the child element
     * @throws SOAPException 
     */
    void addChildElement(MessageElement element) throws SOAPException;
}
