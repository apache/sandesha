package org.apache.sandesha;

/**
 * @author JEkanayake
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

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