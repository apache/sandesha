/*
 * Created on Apr 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.ws.rm.handlers;

import java.util.Iterator;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;

/**
 * @author 
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class RMClientResponseHandler extends RMHandler {

	/* (non-Javadoc)
	 * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
	 */
	public void invoke(MessageContext messageContext) throws AxisFault {
		try {
			//System.out.println("WSRMClientResponseHandler");
			////System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			//System.out.println(messageContext.getCurrentMessage().getSOAPPartAsString());
			//messageContext.getCurrentMessage()
			SOAPEnvelope soapEnvelope =	messageContext.getCurrentMessage().getSOAPEnvelope();
			SOAPBody soapBody = soapEnvelope.getBody();

			Iterator iterator = soapBody.getChildElements();
			SOAPBodyElement soapBodyElement;
			
			while (iterator.hasNext()) {
				soapBodyElement=(SOAPBodyElement) iterator.next();
				//System.out.println(soapBodyElement.getLocalName());
				if(soapBodyElement.getLocalName().equals("clientMethodResponse")){
					//System.out.println("clientMethodResponse");
					Iterator ite=soapBodyElement.getChildElements();
					MessageElement sbe;
					while(ite.hasNext()){
						sbe=  (MessageElement) ite.next();
						if(sbe.getName().equals("clientMethodReturn")){
							messageContext.setCurrentMessage(new Message(sbe.getValue()));
						}
					}
				}				
			}
			//if()
			if (messageContext
				.getCurrentMessage()
				.getSOAPEnvelope()
				.getBody()
				.getFault()
				== null) {

				soapBodyElement = (SOAPBodyElement) iterator.next();

				iterator = soapBodyElement.getChildElements();
				SOAPElement soapElement = (SOAPElement) iterator.next();

				Message message = new Message(soapElement.getValue());
				messageContext.setCurrentMessage(message);
			}

			//////System.out.println("\n" + messageContext.getCurrentMessage().getSOAPPartAsString() + "\n");

		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			//To change body of catch statement use Options | File Templates.
		} catch (SOAPException e) {
			e.printStackTrace();
			//To change body of catch statement use Options | File Templates.
		}

	}

}
