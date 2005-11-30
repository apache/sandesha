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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class Nack implements IOMRMElement {
	private OMElement nackElement;
	private long nackNumber;
	
	SOAPFactory factory;
	
	OMNamespace rmNamespace = null;
	
		
	public Nack(SOAPFactory factory){
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.NS_PREFIX_RM);
		nackElement = factory.createOMElement(
				Sandesha2Constants.WSRM.NACK,rmNamespace);
	}
	
	public OMElement getOMElement() throws OMException {
		return nackElement;
	}
	

	public Object fromOMElement(OMElement nackElement) throws OMException{
		/*OMElement nackPart = sequenceAckElement.getFirstChildWithName(
				new QName (Sandesha2Constants.WSRM.NS_URI_RM,Sandesha2Constants.WSRM.NACK));*/
		
		if (nackElement==null)
			throw new OMException ("Passed seq ack element does not contain a nack part");
		
		try {
			nackNumber = Long.parseLong(nackElement.getText());
		}catch (Exception ex ) {
			throw new OMException ("Nack element does not contain a valid long value");
		}
		
		nackElement = factory.createOMElement(
				Sandesha2Constants.WSRM.NACK,rmNamespace);
		
		return this;
	} 
	
	public OMElement toOMElement(OMElement sequenceAckElement) throws OMException {
		if (sequenceAckElement==null)
			throw new OMException ("Cant set the nack part since the seq ack element is null");
		
		if (nackNumber<=0)
			throw new OMException ("Cant set the nack part since the nack number does not have a valid value");
		
		if (nackElement==null) 
		    throw new OMException ("Cant set the nack part since the element is null");
		
		nackElement.setText(Long.toString(nackNumber));
		sequenceAckElement.addChild(nackElement);

		nackElement = factory.createOMElement(
				Sandesha2Constants.WSRM.NACK,rmNamespace);
		
		return sequenceAckElement;
	}

	public long getNackNumber() {
		return nackNumber;
	}

	public void setNackNumber(long nackNumber) {
		this.nackNumber = nackNumber;
	}
	
}
