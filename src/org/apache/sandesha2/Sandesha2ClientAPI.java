/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */

package org.apache.sandesha2;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;

/**
 * Contains all the Sandesha2Constants of Sandesha2.
 * Please see sub-interfaces to see grouped data.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class Sandesha2ClientAPI {

	public static String AcksTo = "Sandesha2ClientAPIPropertyAcksTo";
	public static String LAST_MESSAGE = "Sandesha2ClientAPIPropertyWSRMLastMessage";
	public static String OFFERED_SEQUENCE_ID = "Sandesha2ClientAPIPropertyOfferedSequenceId";
	public static String SANDESHA_DEBUG_MODE = "Sandesha2ClientAPIPropertyDebugMode";
	public static String SEQUENCE_KEY = "Sandesha2ClientAPIPropertySequenceKey";

	public static RMReport getRMReport (String to, String sequenceKey,ConfigurationContext configurationContext) throws SandeshaException {
		
		String internalSequenceID = SandeshaUtil.getInternalSequenceID (to,sequenceKey);
		RMReport report = new RMReport ();
		
		report.setAckedMessageCount(SequenceManager.getOutGoingSequenceAckedMessageCount (internalSequenceID,configurationContext));
		report.setSequenceCompleted(SequenceManager.isOutGoingSequenceCompleted (internalSequenceID,configurationContext));
		report.setOutGoingSequence(true);
					
		return report;
	}
	
	public static RMReport getIncomingSequenceReport (String sequenceID,ConfigurationContext configurationContext) throws SandeshaException {
		
		RMReport report = new RMReport ();
		report.setOutGoingSequence(false);
		report.setAckedMessageCount(SequenceManager.getIncomingSequenceAckedMessageCount(sequenceID,configurationContext));
		report.setSequenceCompleted(SequenceManager.isIncomingSequenceCompleted(sequenceID,configurationContext));
		
		return report;
	}
}
