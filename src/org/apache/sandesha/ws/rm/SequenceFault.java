package org.apache.sandesha.ws.rm;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.Constants;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

public class SequenceFault extends MessageElement implements IRmElement {


    private MessageElement sequenceFault;

    private FaultCode faultCode;

    public SequenceFault() {
        sequenceFault = new MessageElement();
        sequenceFault.setName("wsrm:SequenceFault");
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

        Name name = env.createName("", Constants.NS_PREFIX_RM,
                Constants.NS_URI_RM);
        SOAPHeaderElement headerElement = (SOAPHeaderElement) env.getHeader()
                .addHeaderElement(name);

        headerElement.setActor(null);
        headerElement.setName("SequenceFault");
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

            if (childElement.getName().equals("wsrm:FaultCode")) {
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
