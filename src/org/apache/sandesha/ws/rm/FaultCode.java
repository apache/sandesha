package org.apache.sandesha.ws.rm;

import org.apache.axis.message.MessageElement;
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Feb 15, 2005
 * Time: 4:51:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FaultCode extends MessageElement implements IRmElement {

    /**
     * Field lastMsgElement
     */
    private MessageElement faultCode;

    /**
     * Constructor LastMessage
     */
    public FaultCode() {
        faultCode = new MessageElement();
        faultCode.setName(Constants.WSRM.NS_PREFIX_RM+Constants.COLON+Constants.WSRM.FAULT_CODE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     */
    public MessageElement getSoapElement() {
        return faultCode;
    }

    /**
     * Method fromSOAPEnvelope
     *
     * @param element
     * @return LastMessage
     */
    public FaultCode fromSOAPEnvelope(MessageElement element) {
        return this;
    }

    /**
     * Method toSOAPEnvelope
     *
     * @param msgElement
     * @return MessageElement
     * @throws SOAPException
     */
    public MessageElement toSOAPEnvelope(MessageElement msgElement)
            throws SOAPException {
        msgElement.addChildElement(Constants.WSRM.FAULT_CODE, Constants.WSRM.NS_PREFIX_RM);
        return msgElement;
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
     */
    public void addChildElement(MessageElement element) {

        // no child elements in LastMessage element
    }


    public MessageElement getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(MessageElement faultCode) {
        this.faultCode = faultCode;
    }
}
