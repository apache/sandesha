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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;

import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 *
 */
public class MessageNumber implements IOMRMElement {
	
	private long messageNumber;
	private OMElement messageNoElement;
	
	OMNamespace msgNoNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);	
	public MessageNumber(){
		messageNoElement = OMAbstractFactory.getSOAP11Factory().createOMElement(Constants.WSRM.MSG_NUMBER,msgNoNamespace);
	}
	
	public long getMessageNumber(){
		return messageNumber;
	}
	public void setMessageNumber(long messageNumber){
		this.messageNumber = messageNumber;
	}
	private boolean readMNElement(OMElement element) {
		Iterator iterator = element.getChildren();
		while (iterator.hasNext()) {
			OMNode omnode = (OMNode)iterator.next();
			if(omnode.getType() != OMNode.ELEMENT_NODE){
				continue ;
			}				
			OMElement childElement = (OMElement)omnode ;
			if (childElement.getLocalName().equals(Constants.WSRM.MSG_NUMBER)) {
				messageNumber = Long.parseLong(childElement.getText());
				return true;
			}else {
			   readMNElement(childElement);	
			}
		}
		return false;
	}
	
	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		readMNElement(envelope);
		return this;
	}
	public OMElement toSOAPEnvelope(OMElement messageElement) throws OMException {
		//soapheaderblock element will be given. 
		long msgNo = getMessageNumber();
		if (msgNo <= 0 ){
			throw new OMException("Set A Valid Message Number");
		}
		messageNoElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(
				new Long(msgNo).toString()));
		messageElement.addChild(messageNoElement);
		
		return messageElement;
	}
	public OMElement getSOAPElement() throws OMException {
		long msgNo = getMessageNumber();
		messageNoElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(new 
					Long(msgNo).toString()));
		
		return messageNoElement;
	}
	public OMElement getMessageNumberElement(){
		return messageNoElement;
	}


}
