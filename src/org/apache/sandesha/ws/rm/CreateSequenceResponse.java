/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
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
package org.apache.sandesha.ws.rm;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.sandesha.ws.utility.Identifier;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * @author Amila Navarathna<br>
 *         Jaliya Ekanayaka<br>
 *         Sudar Nimalan<br>
 *         (Apache Sandesha Project)
 */
public class CreateSequenceResponse implements IRmElement {

    private MessageElement createSequenceResponse;
    private Identifier identifier;

    public CreateSequenceResponse() {
        createSequenceResponse = new MessageElement();
        createSequenceResponse.setName("wsrm:CreateSequenceResponse");
    }

    /* (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */
    public MessageElement getSoapElement() {
        return createSequenceResponse;
    }

    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelop)
            throws SOAPException {
        SOAPEnvelope env = envelop;

        ;
        /*createSequenceResponse.addChildElement(identifier.getSoapElement());
		
                env.addHeader((SOAPHeaderElement)createSequenceResponse);*/

        if (env.getBody() == null) {
            env.addBody();
        }
        Name name = env.createName("", "wsrm", "http://schemas.xmlsoap.org/ws/2004/03/rm");
        SOAPBodyElement bodyElement =
                (SOAPBodyElement) env.getBody().addBodyElement(name);
        bodyElement.setName("CreateSequenceResponse");

        if (identifier != null) {
            identifier.toSOAPEnvelope(bodyElement);
        }

        return env;
    }

    public CreateSequenceResponse fromSOAPEnveploe(SOAPBodyElement bodyElement) {
        Iterator iterator = bodyElement.getChildElements();
        MessageElement childElement;
        while (iterator.hasNext()) {
            //System.out.println(iterator.next());
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

    /* (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
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
