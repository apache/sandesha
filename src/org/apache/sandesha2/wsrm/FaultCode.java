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

import javax.xml.namespace.QName;

import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class FaultCode implements IOMRMElement {

	private OMElement faultCodeElement;
	String faultCode = null;
	SOAPFactory factory;
	OMNamespace rmNameSpace = null;
	String namespaceValue = null;
	
	public FaultCode(SOAPFactory factory, String namespaceValue) {
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNameSpace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		faultCodeElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.FAULT_CODE, rmNameSpace);
	}

	public OMElement getOMElement() throws OMException {
		return faultCodeElement;
	}

	public Object fromOMElement(OMElement sequenceFault) throws OMException {

		if (sequenceFault == null)
			throw new OMException(
					"Can't add Fault Code part since the passed element is null");

		OMElement faultCodePart = sequenceFault
				.getFirstChildWithName(new QName(namespaceValue,
						Sandesha2Constants.WSRM_COMMON.FAULT_CODE));

		if (faultCodePart == null)
			throw new OMException(
					"Passed element does not contain a Fauld Code part");

		this.faultCode = faultCodePart.getText();

		faultCodeElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.FAULT_CODE, rmNameSpace);

		return sequenceFault;

	}

	public OMElement toOMElement(OMElement sequenceFault) throws OMException {

		if (sequenceFault == null)
			throw new OMException(
					"Can't add Fault Code part since the passed element is null");

		if (faultCode == null || faultCode == "")
			throw new OMException(
					"Cant add fault code since the the value is not set correctly.");

		if (faultCodeElement == null)
			throw new OMException(
					"Cant add the fault code since the internal element is null");

		faultCodeElement.setText(faultCode);
		sequenceFault.addChild(faultCodeElement);

		return sequenceFault;
	}
    
    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }
    
    public String getFaultCode() {
        return faultCode;
    }

}
