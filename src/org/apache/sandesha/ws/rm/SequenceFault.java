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
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.Constants;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * This is the SequenceFault element.
 *
 * @auther Jaliya Ekanayake
 */
public class SequenceFault extends MessageElement implements IRmElement {


    private MessageElement sequenceFault;

    private FaultCode faultCode;

    public SequenceFault() {
        sequenceFault = new MessageElement();
        sequenceFault.setName(Constants.WSRM.NS_PREFIX_RM+Constants.COLON+Constants.WSRM.SEQUENCE_FAULT);
    }


    public MessageElement getSoapElement() throws SOAPException {


        sequenceFault.addChildElement(faultCode.getSoapElement());

        return sequenceFault;
    }


    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelope) throws Exception {

        SOAPEnvelope env = envelope;

        if (env.getHeader() == null) {
            env.addHeader();
        }

        Name name = env.createName("", Constants.WSRM.NS_PREFIX_RM,
                Constants.WSRM.NS_URI_RM);
        SOAPHeaderElement headerElement = (SOAPHeaderElement) env.getHeader()
                .addHeaderElement(name);

        headerElement.setActor(null);
        headerElement.setName(Constants.WSRM.SEQUENCE_FAULT);
        headerElement.setMustUnderstand(true);

        if (faultCode != null) {
            faultCode.toSOAPEnvelope(headerElement);
        }

        return env;
    }


    public SequenceFault fromSOAPEnveploe(SOAPHeaderElement headerElement) {
        Iterator iterator = headerElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {
            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals(Constants.WSRM.NS_PREFIX_RM+Constants.COLON+Constants.WSRM.FAULT_CODE)) {
                faultCode = new FaultCode();
                faultCode.fromSOAPEnvelope(childElement);
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
        sequenceFault.addChildElement(element);
    }

    public MessageElement getSequenceFault() {
        return sequenceFault;
    }

    public void setSequenceFault(MessageElement sequenceFault) {
        this.sequenceFault = sequenceFault;
    }
}
