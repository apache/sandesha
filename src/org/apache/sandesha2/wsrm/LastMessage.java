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
public class LastMessage implements IOMRMElement {

	private OMElement lastMessageElement;
	OMNamespace lastMsgNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public LastMessage(){
		lastMessageElement = OMAbstractFactory.getSOAP11Factory().createOMElement(Constants.WSRM.LAST_MSG,lastMsgNamespace);	
	}
	public OMElement getSOAPElement() throws OMException {
		return lastMessageElement;
	}

	private boolean readMNElement(OMElement element){
		Iterator iterator = element.getChildren();
		while(iterator.hasNext()){
			OMNode omNode = (OMNode)iterator.next();
			if( omNode.getType() != OMNode.ELEMENT_NODE){
				continue;
			}
			OMElement childElement = (OMElement)omNode;
			if (childElement.getLocalName().equals(Constants.WSRM.LAST_MSG)){
				lastMessageElement = childElement;
				return true;
			}else{
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
		//soapheaderblock element will be given
		messageElement.addChild(lastMessageElement);
		return messageElement;
	}
	public void setLastMessageElement(OMElement lastMsgElement){
		lastMessageElement = lastMsgElement;
	}
	public OMElement getLastMessageElement(){
		return lastMessageElement;
	}
	public boolean isPresent(){
		return (lastMessageElement != null) ? true:false;
	}
	

}
