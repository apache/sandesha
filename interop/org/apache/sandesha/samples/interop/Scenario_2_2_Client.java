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
package org.apache.sandesha.samples.interop;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.encoding.XMLType;
import org.apache.sandesha.Constants;

/**
 * @author SNimalan
 *
 */
public class Scenario_2_2_Client {
	public static void main(String[] args) {
		System.out.println("Client started......");
		try {

			Service service = new Service();

			Call call = (Call) service.createCall();
			if(args[0].equals("")){
				System.out.println("Pass Target End Point Address as a Parametter");
				throw new Exception("Target End Point Address did not Set");
			}

			call.setTargetEndpointAddress(args[0]);
			call.setOperationName(new QName("EchoStringService", "echoString"));
			call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter("Sequence", XMLType.XSD_STRING, ParameterMode.IN);
			call.setReturnType(XMLType.SOAP_STRING);

			UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

			call.setProperty(Constants.CLIENT_SEQUENCE_IDENTIFIER,"uuid:" + uuidGen.nextUUID());
			call.setProperty(Constants.CLIENT_ONE_WAY_INVOKE,(new Boolean(true)));
			call.setProperty(Constants.CLIENT_RESPONSE_EXPECTED,(new Boolean(false)));
			call.setProperty(Constants.CLIENT_CREATE_SEQUENCE,(new Boolean(false)));

			String seq=uuidGen.nextUUID();
			System.out.println(call.invoke(new Object[] {"Hello",seq}));
			System.out.println(call.invoke(new Object[] {"World",seq}));
			//call.setLastMessage(true); //ALEK: was AXIS Call patched for it?
			System.out.println(call.invoke(new Object[] {"Bye",seq}));

		} catch (Exception e) {
			System.err.println(e.toString());
		}

	}
}