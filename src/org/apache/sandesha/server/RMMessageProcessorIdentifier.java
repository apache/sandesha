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
package org.apache.sandesha.server;

import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.sandesha.RMMessageContext;

import org.apache.sandesha.Constants;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author 
 * 
 */
public class RMMessageProcessorIdentifier {

	public IRMMessageProcessor getMessageProcessor(RMMessageContext rmMessageContext) {

		AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
		RMHeaders rmHeaders = rmMessageContext.getRMHeaders();

		if (addrHeaders.getAction().toString().equals(Constants.ACTION_CREATE_SEQUENCE)) {
			return new CreateSequenceProcessor();
		} else if (
			addrHeaders.getAction().toString().equals(Constants.ACTION_CREATE_SEQUENCE_RESPONSE)) {
			return new CreateSequenceResponseProcessor();
		} else if (
			addrHeaders.getAction().toString().equals(Constants.ACTION_TERMINATE_SEQUENCE)) {
			return new TerminateSequenceProcessor();
		} else {
			return new CompositeProcessor();
		}
	}
}
