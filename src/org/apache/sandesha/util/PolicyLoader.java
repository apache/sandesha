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
package org.apache.sandesha.util;

/**
 * @author Saminda Wishwajith Abeyruwan
 *
 */


import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;


public class PolicyLoader {

    private static final Log log = LogFactory.getLog(PolicyLoader.class.getName());

    private long inactivityTimeout;
    private long baseRetransmissionInterval;
    private long acknowledgementInterval;
    private String exponentialBackoff;
    private String binaryBackOff;

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document document;

    private static PolicyLoader instance;
    private static boolean policyInstance = false;

    private Element rootNodeElement;

    private PolicyLoader() {
            helper();
        policyInstance = true;
    }

    public static PolicyLoader getInstance() {
        if (policyInstance == false)
            return instance = new PolicyLoader();
        else
            return instance;
    }

    public int getRetransmissionCount() {
        return Constants.MAXIMUM_RETRANSMISSION_COUNT;
    }

    public long getInactivityTimeout() {

        if (inactivityTimeout == 0)
            return Constants.INACTIVITY_TIMEOUT;
        else
            return inactivityTimeout;
    }

    public long getBaseRetransmissionInterval() {
        if (baseRetransmissionInterval == 0)
            return Constants.RETRANSMISSION_INTERVAL;
        else
            return baseRetransmissionInterval;
    }

    public long getAcknowledgementInterval() {
        if (acknowledgementInterval == 0)
            return Constants.ACKNOWLEDGEMENT_INTERVAL;
        else
            return acknowledgementInterval;
    }

    public String getExponentialBackoff() {
        return exponentialBackoff;
    }

    public void helper() {
        init();
        try {
            inactivityTimeout =
                    getAttributeValue(Constants.WSRMPolicy.WSRM, Constants.WSRMPolicy.INA_TIMEOUT);
            baseRetransmissionInterval = getAttributeValue(Constants.WSRMPolicy.WSRM,
                    Constants.WSRMPolicy.BASE_TX_INTERVAL);
            acknowledgementInterval =
                    getAttributeValue(Constants.WSRMPolicy.WSRM, Constants.WSRMPolicy.ACK_INTERVAL);
            exponentialBackoff = getExpBackoffInterval(Constants.WSRMPolicy.WSRM,
                    Constants.WSRMPolicy.EXP_BACKOFF);
            binaryBackOff = geBinaryBackoffInterval(Constants.WSRMPolicy.WSRM,
                    Constants.WSRMPolicy.BIN_BACKOFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String geBinaryBackoffInterval(String namespaceURI, String elementName) {
        String name = null;
        NodeList list = rootNodeElement.getElementsByTagNameNS(namespaceURI, elementName);
        if (list != null) {
            Node node = list.item(0);
            if (node != null)
                name = list.item(0).getLocalName();
        }
        return name;
    }

    private long getAttributeValue(String namespaceURI, String elementName) {
        NodeList list = rootNodeElement.getElementsByTagNameNS(namespaceURI, elementName);
        NamedNodeMap map = list.item(0).getAttributes();
        Attr att = (Attr) map.item(0);
        String value = att.getNodeValue();
        return Long.parseLong(value.trim());

    }

    private String getExpBackoffInterval(String namespaceURI, String elementName) {
       String name = null;
        NodeList list = rootNodeElement.getElementsByTagNameNS(namespaceURI, elementName);
        if (list != null) {
            Node node = list.item(0);
            if (node != null)
                name = list.item(0).getLocalName();
        }
        return name;
    }

    private void init() {
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    Constants.ClientProperties.WSRM_POLICY_FILE);

            if (in != null) {
                document = builder.parse(in);
                rootNodeElement = document.getDocumentElement();
            } else
                log.error("No WSRMPolicy.xml Found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getBinaryBackOff() {
        helper();
        return binaryBackOff;

    }
}
