/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.sandesha2.wsrm;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;

import org.apache.sandesha2.Constants;
import org.apache.sandesha2.util.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class MessageNumber implements IOMRMElement {
	
	private long messageNumber;
	private OMElement messageNoElement;
	
	OMNamespace msgNoNamespace =
		SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.DEFAULT).createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	
	public MessageNumber(){
		messageNoElement = SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.DEFAULT).createOMElement(Constants.WSRM.MSG_NUMBER,msgNoNamespace);
	}
	
	public long getMessageNumber(){
		return messageNumber;
	}
	public void setMessageNumber(long messageNumber){
		this.messageNumber = messageNumber;
	}
	
	public Object fromOMElement(OMElement seqenceElement) throws OMException {
		OMElement msgNumberPart = seqenceElement.getFirstChildWithName( 
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.MSG_NUMBER));
		if (msgNumberPart==null)
			throw new OMException ("The passed sequnce element does not contain a message number part");
		
		messageNoElement = SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.DEFAULT).createOMElement(Constants.WSRM.MSG_NUMBER,msgNoNamespace);

		String msgNoStr = msgNumberPart.getText();
		messageNumber = Long.parseLong(msgNoStr);
		return this;
	}
	
	public OMElement toOMElement(OMElement element) throws OMException {
		if (messageNumber <= 0 ){
			throw new OMException("Set A Valid Message Number");
		}
		
		messageNoElement.setText(Long.toString(messageNumber));
		element.addChild(messageNoElement);
		
		messageNoElement = SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.DEFAULT).createOMElement(Constants.WSRM.MSG_NUMBER,msgNoNamespace);
		
		return element;
	}
	
	public OMElement getOMElement() throws OMException {
		return messageNoElement;
	}
	
	public OMElement getMessageNumberElement(){
		return messageNoElement;
	}


}
