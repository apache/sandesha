/*
* Copyright 1999-2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/

package org.apache.sandesha;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Transport;
import org.apache.axis.transport.http.HTTPConstants;

/**
 * A File Transport class.
 *
 * @author Rob Jellinghaus (robj@unrealities.com)
 * @author Doug Davis (dug@us.ibm.com)
 * @author Glen Daniels (gdaniels@allaire.com)
 */
public class RMTransport extends Transport {

    private String action;

    private String cookie;

    private String cookie2;

    public RMTransport() {
        transportName = "RMTransport";
    }

    public RMTransport(String url, String action) {
        transportName = "RMTransport";
        this.url = url;
        this.action = action;

    }

    public void setupMessageContextImpl(MessageContext mc, Call call,
                                        AxisEngine engine) throws AxisFault {
        if (action != null) {
            mc.setUseSOAPAction(true);
            mc.setSOAPActionURI(action);
        }

        // Set up any cookies we know about
        if (cookie != null)
            mc.setProperty(HTTPConstants.HEADER_COOKIE, cookie);
        if (cookie2 != null)
            mc.setProperty(HTTPConstants.HEADER_COOKIE2, cookie2);

        // Allow the SOAPAction to determine the service, if the service
        // (a) has not already been determined, and (b) if a service matching
        // the soap action has been deployed.
        if (mc.getService() == null) {
            mc.setTargetService((String) mc.getSOAPActionURI());
        }
    }

    public void processReturnedMessageContext(MessageContext context) {
        cookie = context.getStrProp(HTTPConstants.HEADER_COOKIE);
        cookie2 = context.getStrProp(HTTPConstants.HEADER_COOKIE2);
    }

}