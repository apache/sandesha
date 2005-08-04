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
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 */
public class AcknowledgementRange implements IOMRMElement {
	private OMElement acknowledgementRangeElement;
	private long maxValue;
	private long minValue;
	OMNamespace ackRangeNamespace = 
        OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public AcknowledgementRange(){
		acknowledgementRangeElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.ACK_RANGE,ackRangeNamespace);
	}
	public OMElement getSOAPElement() throws OMException {
		acknowledgementRangeElement.addAttribute(Constants.WSRM.LOWER,
				new Long(minValue).toString(),ackRangeNamespace);
		acknowledgementRangeElement.addAttribute(Constants.WSRM.UPPER,
				new Long(maxValue).toString(),ackRangeNamespace);
		return acknowledgementRangeElement;
	}

	private boolean readACKRangeElement(OMElement element){
		Iterator iterator = element.getChildren();
		while(iterator.hasNext()){
			OMNode omNode = (OMNode)iterator.next();
			if(omNode.getType() != OMNode.ELEMENT_NODE){
				continue;
			}
			OMElement childElement = (OMElement)omNode;
			if(childElement.getLocalName().equals(Constants.WSRM.ACK_RANGE)){
				Iterator ite = childElement.getAttributes();
				while(ite.hasNext()){
					OMAttribute attr = (OMAttribute)ite.next();
					if (attr.getLocalName().equals(Constants.WSRM.LOWER)){
						minValue = Long.parseLong(attr.getValue());
					}
					else{										
						maxValue = Long.parseLong(attr.getValue());
						return true;
					}
				}
			}else{
				readACKRangeElement(childElement);
			}
		}
		return false;
	}
	//this fromSOAPEnvelope(OMElement element) for the purpose of making the coming iteration easier
	public Object fromSOAPEnvelope(OMElement element) throws OMException{
		Iterator iterator = element.getAttributes();
		while(iterator.hasNext()){
			OMAttribute attr = (OMAttribute)iterator.next();
			if (attr.getLocalName().equals(Constants.WSRM.LOWER)){
				minValue = Long.parseLong(attr.getValue());
			}
			else{										
				maxValue = Long.parseLong(attr.getValue());
			}
		}
		return this;
	} 		
	
	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		readACKRangeElement(envelope);
		return this;
	}

	public OMElement toSOAPEnvelope(OMElement messageElement) throws OMException {
		acknowledgementRangeElement.addAttribute(Constants.WSRM.LOWER,
				new Long(minValue).toString(),ackRangeNamespace);
		acknowledgementRangeElement.addAttribute(Constants.WSRM.UPPER,
				new Long(maxValue).toString(),ackRangeNamespace);
		messageElement.addChild(acknowledgementRangeElement);
		return messageElement;
	}
	public void setMinValue(long minValue){
		this.minValue = minValue;
	}
	public void setMaxValue(long maxValue){
		this.maxValue = maxValue;		
	}
	public long getMinValue(){
		return minValue;
	}
	public long getMaxValue(){
		return maxValue;
	}

}
