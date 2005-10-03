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

package org.apache.sandesha2.samples;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

/**
 * @author 
 * 
 */
public class RMInteropService {

    private static Map sequences = new HashMap();

    public OMElement echoString(OMElement in) {
        System.out.println("EchoString was called");

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://tempuri.org/", "echoString");
        OMElement method = fac.createOMElement("echoStringResponse", omNs);

        OMElement value = fac.createOMElement("Text", omNs);
        value.setText("echo response");

        method.addChild(value);

        return method;
    }


    public void ping(OMElement in) {
        //Just accept the message and do some processing
    	System.out.println("Ping was called");
    }
}
