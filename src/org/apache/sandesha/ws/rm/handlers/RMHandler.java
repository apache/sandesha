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
import org.apache.axis.handlers.BasicHandler;
import org.apache.sandesha.ws.rm.RMHeaders;

import javax.xml.soap.SOAPException;

/**
 * class RMHandler
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public abstract class RMHandler extends BasicHandler {

    /**
     * Method setPropertyToMessageContext
     * 
     * @param msgContext 
     * @param property   
     * @throws AxisFault     
     * @throws SOAPException 
     */
    public void setPropertyToMessageContext(MessageContext msgContext, String property)
            throws AxisFault, SOAPException {

        RMHeaders rmHeaders = new RMHeaders();

        rmHeaders.fromSOAPEnvelope(msgContext.getCurrentMessage().getSOAPEnvelope());
       if((rmHeaders.getAckRequest()!=null) ||
               (rmHeaders.getCreateSequence()!=null) ||
               (rmHeaders.getCreateSequenceResponse()!=null) ||
               (rmHeaders.getSequence()!= null) ||
               (rmHeaders.getSequenceAcknowledgement()!=null) ||
               (rmHeaders.getTerminateSequence()!=null)){
                msgContext.setProperty(property, rmHeaders);
               }
        
    }

    /**
     * Method getProperryFromMessageContext
     * 
     * @param msgContext 
     * @param property   
     * @return 
     */
    public RMHeaders getProperryFromMessageContext(MessageContext msgContext,
                                                   String property) {
        return (RMHeaders) msgContext.getProperty(property);
    }
}
