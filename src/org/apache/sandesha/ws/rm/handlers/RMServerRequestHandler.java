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
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;

/**
 * class RMServerRequestHandler
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class RMServerRequestHandler extends RMHandler {

    /*
     * (non-Javadoc)
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */

    /**
     * Method invoke
     * 
     * @param msgContext 
     * @throws AxisFault 
     */
    public void invoke(MessageContext msgContext) throws AxisFault {

        // System.out.println("RMServerRequestHandler::invoke");
        try {
            setPropertyToMessageContext(msgContext,
                    Constants.ENV_RM_REQUEST_HEADERS);
        } catch (AxisFault e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SOAPException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
