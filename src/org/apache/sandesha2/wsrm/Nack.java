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
public class Nack implements IOMRMElement {
	private OMElement nackElement;
	private long notAckNumber;
	OMNamespace nackNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public Nack(){
		nackElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.NACK,nackNamespace);
	}
	public OMElement getSOAPElement() throws OMException {
		nackElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(
				new Long(notAckNumber).toString()));
		return nackElement;
	}
	private boolean readNackElement(OMElement element){
		Iterator iterator = element.getChildren();
		while(iterator.hasNext()){
			OMNode omNode = (OMNode)iterator.next();
			if(omNode.getType() != OMNode.ELEMENT_NODE){
				continue;
			}
			OMElement childElement = (OMElement)omNode;
			if(childElement.getLocalName().equals(Constants.WSRM.NACK)){
				notAckNumber = Long.parseLong(childElement.getText());
				return true;
			}else{
				readNackElement(childElement);
			}
		}
		return false;	
	}
	//duplicate method for the purpose of using comming iteration easily
	public Object fromSOAPEnvelope(OMElement element) throws OMException{
		notAckNumber = Long.parseLong(element.getText());
		return this;
	} 
	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		readNackElement(envelope);
		return this;
	}
	public OMElement toSOAPEnvelope(OMElement messageElement) throws OMException {
		nackElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(
				new Long(notAckNumber).toString()));
		messageElement.addChild(nackElement);
		return messageElement;
	}
	public void setNackNumber(long notAckNumber){
		this.notAckNumber = notAckNumber;
	}
	public long getNackNumber(){
		return notAckNumber;
	}

}
