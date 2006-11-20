/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.sandesha2.msgprocessors;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.wsrm.UsesSequenceSTR;

/**
 * Responsible for processing the UsesSequenceSTR header which may be present in a CreateSequence message.
 */
public class UsesSequenceSTRProcessor {

	private static final Log log = LogFactory.getLog(UsesSequenceSTRProcessor.class);
	
	public void processUseSequenceSTRHeader (RMMsgContext rmMsgContext) throws AxisFault {
		
		if (log.isDebugEnabled())
			log.debug("Enter: UseSequenceSTRProcessor::processSequenceHeader");
		
		//mustUnderstand processing
		UsesSequenceSTR usesSequenceSTR = (UsesSequenceSTR) rmMsgContext.getMessagePart(Sandesha2Constants.MessageParts.USES_SEQUENCE_STR);
		
		if (usesSequenceSTR!=null) {
			usesSequenceSTR.setMustUnderstand(false);
			usesSequenceSTR.toSOAPEnvelope (rmMsgContext.getSOAPEnvelope());
		}
		
		if (log.isDebugEnabled())
			log.debug("Exit: UseSequenceSTRProcessor::processSequenceHeader");
	}
}
