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

import org.apache.axis2.addressing.EndpointReference;
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
public class AcksTo implements IOMRMElement{
	private EndpointReference EPR;
	private OMElement acksToElement;
	
	OMNamespace acksToNameSpace = 
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public AcksTo(EndpointReference EPR){
		acksToElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.ACKS_TO,acksToNameSpace);
		this.EPR = EPR;
	}
	public OMElement getSOAPElement() throws OMException {
		acksToElement.addChild(OMAbstractFactory.getSOAP12Factory()
				.createText(EPR.getAddress()));
		return acksToElement;
	}

	public boolean readEPRElement(OMElement element){
		Iterator iterator = element.getChildren();
		while(iterator.hasNext()){
			OMNode omNode = (OMNode)iterator.next();
			if(omNode.getType() != OMNode.ELEMENT_NODE){
				continue;
			}else{
				OMElement omElement = (OMElement)omNode;
				if (omElement.getLocalName().equals(Constants.WSRM.ACKS_TO)){
					String uri = omElement.getText();
					EPR = new EndpointReference("",uri);
					return true;
				}else{
					readEPRElement(omElement);
				}
			}			
		}
		return false;
	}
	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		readEPRElement(envelope);
		return this;
	}
	public OMElement toSOAPEnvelope(OMElement messageElement) throws OMException {
		acksToElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(
				EPR.getAddress()));
		messageElement.addChild(acksToElement);
		return messageElement;
	}
	public EndpointReference getEndPointReference(){
		return EPR;
	}
	public void setEndPointReference(EndpointReference EPR){
		this.EPR = EPR;
	}
	
}
