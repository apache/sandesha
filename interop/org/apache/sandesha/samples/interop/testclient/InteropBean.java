package org.apache.sandesha.samples.interop.testclient;

import java.io.Serializable;


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
