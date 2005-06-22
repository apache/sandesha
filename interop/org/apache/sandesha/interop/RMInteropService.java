/*
* Copyright 1999-2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/

package org.apache.sandesha.interop;

import org.apache.commons.logging.Log;
import org.apache.axis.components.logger.LogFactory;
import org.apache.sandesha.client.RMSender;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the service that is used for the interop testing. Two operations, ping and echoString
 * are defined as per the interop scenarios.
 *
 * @auther Jaliya Ekanayake
 */
public class RMInteropService {
    private static Map sequences = new HashMap();
    private static final Log log = LogFactory.getLog(RMInteropService.class.getName());

    public String echoString(String text, String sequence) {

        if (sequences.get(sequence) != null) {
            text = (String) sequences.get(sequence) + text;
            sequences.put(sequence, new String(text));
        } else {
            sequences.put(sequence, (new String(text)));

        }
        log.debug("Echo Service "+text);
        return text;
    }

    public void ping(String text) {
        log.debug("Ping Service "+text);
    }
}
