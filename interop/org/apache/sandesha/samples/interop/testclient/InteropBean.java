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

package org.apache.sandesha.samples.interop.testclient;

import java.io.Serializable;

/**
 * This is used to pass parameters from interop.jsp to the Sandesha
 *
 * @auther Chamikara Jayalath
 */
public class InteropBean implements Serializable {

    private String target;
    private String operation;
    private String from;
    private String replyto;
    private int noOfMsgs;

    private String acksTo;
    private String offer;
    private String faultto;
    private String sourceURL;

    public String getFaultto() {
        return faultto;
    }

    public void setFaultto(String faultto) {
        this.faultto = faultto;
    }


    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }


    public String getAcksTo() {
        return acksTo;
    }

    public void setAcksTo(String acksTo) {
        this.acksTo = acksTo;
    }


    public String getFrom() {
        return from;
    }

    public int getNoOfMsgs() {
        return noOfMsgs;
    }

    public String getOperation() {
        return operation;
    }

    public String getReplyto() {
        return replyto;
    }


    public String getTarget() {
        return target;
    }


    /**
     * @param string
     */
    public void setFrom(String string) {
        from = string;
    }

    /**
     * @param i
     */
    public void setNoOfMsgs(int i) {
        noOfMsgs = i;
    }

    /**
     * @param string
     */
    public void setOperation(String string) {
        operation = string;
    }

    /**
     * @param string
     */
    public void setReplyto(String string) {
        replyto = string;
    }

    /**
     * @param string
     */
    public void setTarget(String string) {
        target = string;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }


}
