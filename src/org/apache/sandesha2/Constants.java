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

/**
 * @author  
 */

public interface Constants {

	public interface WSRM {
		String NS_PREFIX_RM = "wsrm";

		String NS_URI_RM = "http://schemas.xmlsoap.org/ws/2005/02/rm";

		String NS_URI_CREATE_SEQ_RESPONSE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";
		
		String MSG_NUMBER = "MessageaNumber";

		String LAST_MSG = "LastMessage";

		String SEQUENCE = "Sequence";

		String SEQUENCE_OFFER = "Offer";

		String TERMINATE_SEQUENCE = "TerminateSequence";

		String FAULT_CODE = "FaultCode";

		String SEQUENCE_FAULT = "SequenceFault";

		String ACKS_TO = "AcksTo";

		String CREATE_SEQUENCE = "CreateSequence";

		String CREATE_SEQUENCE_RESPONSE = "CreateSequenceResponse";

		String ACK_REQUESTED = "AckRequested";

		String ACK_RANGE = "AcknowledgementRange";

		String UPPER = "Upper";

		String LOWER = "Lower";

		String NACK = "Nack";

		String SEQUENCE_ACK = "SequenceAcknowledgement";

		String IDENTIFIER = "Identifier";

		String ACCEPT = "Accept";
	}

	public interface WSA {
		String NS_URI_ANONYMOUS = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
		String NS_ADDRESSING = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
	}

	String RM_HEADERS = "rmHeaders";

	String SEQUENCE = "sequence";

	String MESSAGE_NUMBER = "messageNumber";

	String MESSAGE_TYPE = "messageType";

	String CREATE_SEQ_REQ = "createSequenceReq";

	String CREATE_SEQ_RES = "createSequenceRes";

	String ACKNOWLEDGEMENT = "acknowledgement";

	String IN_MESSAGE = "inMessage";

	String OUT_MESSAGE = "outMessage";

	String FAULT_MESSAGE = "faultMessage";

	int MESSAGE_TYPE_UNKNOWN = 0;

	int MESSAGE_TYPE_CREATE_SEQ = 1;

	int MESSAGE_TYPE_CREATE_SEQ_RESPONSE = 2;

	int MESSAGE_TYPE_APPLICATION = 3;

	int MESSAGE_TYPE_ACK = 4;

	int MESSAGE_TYPE_TERMINATE_SEQ = 5;

	int MAX_MSG_TYPE = 5;

	int MESSAGE_PART_UNKNOWN = 0;

	int MESSAGE_PART_SEQUENCE = 1;

	int MESSAGE_PART_SEQ_ACKNOWLEDGEMENT = 2;

	int MESSAGE_PART_ADDR_HEADERS = 3;

	int MESSAGE_PART_CREATE_SEQ = 4;

	int MESSAGE_PART_CREATE_SEQ_RESPONSE = 5;

	int MESSAGE_PART_TERMINATE_SEQ = 6;

	int MAX_MSG_PART_ID = 6;

}