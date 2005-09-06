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

package org.apache.sandesha2;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.SOAPFactory;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */


public class SOAPAbstractFactory {

	public static SOAPFactory getSOAPFactory (int SOAPVersion) {
		if (SOAPVersion==Constants.SOAP_1_1)
			return OMAbstractFactory.getSOAP11Factory();
		else if(SOAPVersion==Constants.SOAP_1_2)
			return OMAbstractFactory.getSOAP12Factory();
		else
			return null;
	}
}
