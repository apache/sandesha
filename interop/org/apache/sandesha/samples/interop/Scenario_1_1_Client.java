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
 * Run client for
 * <a href="http://www-106.ibm.com/developerworks/offers/WS-Specworkshops/ws-rm200405.html">interop scenario 1.1</a>
 *
 * @author Aleksander Slominski
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class Scenario_1_1_Client {
    public static void main(String[] args) {
        System.out.println("Client started......");
        try {
            
            Service service = new Service();
            
            Call call = (Call) service.createCall();
            if(args.length == 0 || args[0].equals("")){
                throw new Exception("Error: pass Target End Point Address as a Parametter");
            }
            
            call.setTargetEndpointAddress(args[0]);
            call.setOperationName(new QName("PingService", "Ping"));
            call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.AXIS_VOID);
            
            UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
            
            call.setProperty(Constants.CLIENT_SEQUENCE_IDENTIFIER,"uuid:" + uuidGen.nextUUID());
            call.setProperty(Constants.CLIENT_ONE_WAY_INVOKE,(new Boolean(true)));
            call.setProperty(Constants.CLIENT_RESPONSE_EXPECTED,(new Boolean(false)));
            call.setProperty(Constants.CLIENT_CREATE_SEQUENCE,(new Boolean(false)));
            
            call.invoke(new Object[] {"Ping 1"});
            call.invoke(new Object[] {"Ping 2"});
            //call.setLastMessage(true); //ALEK: was AXIS Call patched for it?
            call.invoke(new Object[] {"Ping 3"});
            
        } catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}

