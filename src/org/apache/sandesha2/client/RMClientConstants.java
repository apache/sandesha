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

package org.apache.sandesha2.client;

public class RMClientConstants {
	public static String AcksTo = "Sandesha2AcksTo";
	public static String LAST_MESSAGE = "Sandesha2LastMessage";
	public static String OFFERED_SEQUENCE_ID = "Sandesha2OfferedSequenceId";
	public static String SANDESHA_DEBUG_MODE = "Sandesha2DebugMode";
	public static String SEQUENCE_KEY = "Sandesha2SequenceKey";
	public static String MESSAGE_NUMBER = "Sandesha2MessageNumber";
	public static String RM_SPEC_VERSION = "Sandesha2RMSpecVersion";
	public static String DUMMY_MESSAGE = "Sandesha2DummyMessage"; //If this property is set, even though this message will invoke the RM handlers, this will not be sent as an actual application message
	public static String RM_FAULT_CALLBACK = "Sandesha2RMFaultCallback";
}
