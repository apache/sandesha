/*
 * Created on Apr 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.ws.utility;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;

import javax.xml.soap.SOAPException;
//import //org.apache.xalan.templates.ElemApplyImport;

/**
 * @author 
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class Identifier extends URI {

	private MessageElement identifierElement;
	private String identifier = null;

	public Identifier() {
		identifierElement = new MessageElement();
		identifierElement.setName("wsu:Identifier");
	}

	public void setUri(String uri) throws SOAPException {
		identifierElement.addTextNode(uri);
	}

	
	public Identifier fromSOAPEnvelope(MessageElement element) {
		//System.out.println("Identifier::getSequenceAcknowledgement");
		identifier=element.getValue();
		System.out.println(identifier);
		
		return this;
	}
	public MessageElement toSOAPEnvelope(MessageElement msgElement) throws SOAPException{
	 
		msgElement.addChildElement("Identifier","wsu").addTextNode(identifier);
		//System.out.println("--------------"+msgElement);
		
		return msgElement;
	}
	
	public MessageElement getSoapElement() throws SOAPException {
		// create the soap element for the message no
		identifierElement.addTextNode(identifier);
		return identifierElement;
	}
	/**
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param string
	 */
	public void setIdentifier(String string) {
		identifier = string;
	}

	/* (non-Javadoc)
	 * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
	 */
	public void addChildElement(MessageElement element) throws SOAPException {
		// TODO Auto-generated method stub

	}
	public boolean equals(Object obj) {
			if (obj instanceof Identifier) {
				if (this.identifier
					== ((String) (((Identifier) obj).getIdentifier())))
					return true;
				else
					return false;
			} else
				return false;
		}

		public int hashCode(){

			return identifier.hashCode();
		}
		public String toString(){
			return identifier;
		}


}
