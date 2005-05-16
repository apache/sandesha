package org.apache.sandesha.ws.rm;


import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class Identifier
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class Identifier extends URI {

    /**
     * Field identifierElement
     */
    private MessageElement identifierElement;

    /**
     * Field identifier
     */
    private String identifier = null;

    /**
     * Constructor Identifier
     */
    public Identifier() {
        identifierElement = new MessageElement();
        identifierElement.setName(
                Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.IDENTIFIER);
    }

    /**
     * Method setUri
     *
     * @param uri
     * @throws SOAPException
     */
    public void setUri(String uri) throws SOAPException {
        identifierElement.addTextNode(uri);
    }

    /**
     * Method fromSOAPEnvelope
     *
     * @param element
     * @return
     */
    public Identifier fromSOAPEnvelope(MessageElement element) {

        identifier = element.getValue();
        return this;
    }

    /**
     * Method toSOAPEnvelope
     *
     * @param msgElement
     * @return @throws
     *         SOAPException
     */
    public MessageElement toSOAPEnvelope(MessageElement msgElement) throws SOAPException {
        removeIdentifierElementIfAny(msgElement);
        msgElement.addChildElement(Constants.WSRM.IDENTIFIER, Constants.WSRM.NS_PREFIX_RM)
                .addTextNode(identifier);
        return msgElement;
    }

    /**
     * Method getSoapElement
     *
     * @return @throws
     *         SOAPException
     */
    public MessageElement getSoapElement() throws SOAPException {

        // create the soap element for the message no
        identifierElement.addTextNode(identifier);

        return identifierElement;
    }

    /**
     * Method getIdentifier
     *
     * @return String
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Method setIdentifier
     */
    public void setIdentifier(String string) {
        identifier = string;
    }

    /**
     * Method equals
     *
     * @param obj
     * @return boolean
     */
    public boolean equals(Object obj) {

        if (obj instanceof org.apache.sandesha.ws.rm.Identifier) {
            if (this.identifier ==
                    ((String) (((org.apache.sandesha.ws.rm.Identifier) obj).getIdentifier()))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Method hashCode
     *
     * @return int
     */
    public int hashCode() {
        return identifier.hashCode();
    }

    /**
     * Method toString
     *
     * @return String
     */
    public String toString() {
        return identifier;
    }

    private void removeIdentifierElementIfAny(MessageElement msgElement) {

        Iterator ite = msgElement.getChildElements();
        while (ite.hasNext()) {
            MessageElement childElement = (MessageElement) ite.next();
            if (Constants.WSRM.IDENTIFIER.equals(childElement.getName()) &&
                    (Constants.WSRM.NS_URI_RM.equals(childElement.getNamespaceURI()))) {
                childElement.detachNode();
            }
        }
    }
}