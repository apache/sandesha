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
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 *
 */
public class SequenceFault implements IOMRMElement {
	private OMElement sequenceFaultElement;
	private FaultCode faultCode;
	
	OMNamespace sequenceFaultNameSpace = 
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public SequenceFault(){
		sequenceFaultElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.SEQUENCE_FAULT,sequenceFaultNameSpace);
	}
	public OMElement getSOAPElement() throws OMException {
		sequenceFaultElement.addChild(faultCode.getSOAPElement());
		return sequenceFaultElement;
	}

	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		//only for fault thus not intereated in refactoring
		Iterator iterator = envelope.getChildren();
		OMElement childElement;
		OMElement siblings;
		OMElement grandSiblings;
		while(iterator.hasNext()){
			childElement = (OMElement)iterator.next();
			Iterator iteSib1 = childElement.getChildren();
			while(iteSib1.hasNext()){
				siblings = (OMElement)iteSib1.next();
				Iterator iteSib2 = siblings.getChildren(); 
				while(iteSib2.hasNext()){
					grandSiblings = (OMElement)iteSib2.next();
					if(grandSiblings.getLocalName().equals(Constants.WSRM.SEQUENCE_FAULT)){
						faultCode = new FaultCode();
						faultCode.fromSOAPEnvelope(envelope);
					}
				}
			}
		}
		return this;
	}

	public OMElement toSOAPEnvelope(OMElement envelope) throws OMException {
//		soapelevement will be given here. 
		SOAPEnvelope soapEnvelope = (SOAPEnvelope)envelope;
		
		SOAPHeader soapHeader = soapEnvelope.getHeader();
		SOAPHeaderBlock soapHeaderBlock = soapHeader.addHeaderBlock(
				Constants.WSRM.SEQUENCE_FAULT,sequenceFaultNameSpace);
		soapHeaderBlock.setMustUnderstand(true);
		
		
        if (faultCode != null) {
            faultCode.toSOAPEnvelope(soapHeaderBlock);
        }
		
		return envelope;
		
	}
	public void setFaultCode(FaultCode faultCode){
		this.faultCode = faultCode;
	}
	public FaultCode getFaultCode(){
		return faultCode;
	}
	public void setSequenceFaultElement(OMElement sequenceFault){
		sequenceFaultElement = sequenceFault;
	}
	public OMElement getSequenceFaultElement(){
		return sequenceFaultElement;
	}

}
