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
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */


public interface Constants {

	public interface WSRM {
		String NS_PREFIX_RM = "wsrm";

		String NS_URI_RM = "http://schemas.xmlsoap.org/ws/2005/02/rm";
		

		String ACTION_SEQ_ACK = "http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";
		
		String ACTION_CREATE_SEQ = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";
		
		String NS_URI_CREATE_SEQ_RESPONSE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";
		
		String MSG_NUMBER = "MessageNumber";

		String LAST_MSG = "LastMessage";

		String SEQUENCE = "Sequence";

		String SEQUENCE_OFFER = "Offer";

		String TERMINATE_SEQUENCE = "TerminateSequence";

		String FAULT_CODE = "FaultCode";

		String SEQUENCE_FAULT = "SequenceFault";

		String ACKS_TO = "AcksTo";
		
		String EXPIRES = "Expires";

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
		
		String NS_URI_ADDRESSING = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
		
		String NS_PREFIX_ADDRESSING = "wsa";
		
		String ADDRESS = "Address";
	}

	public interface WSP {
		long RETRANSMISSION_INTERVAL = 3000;
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
	
	int MESSAGE_PART_ACK_REQUEST = 7;

	int MAX_MSG_PART_ID = 6;
	
	int STORAGE_TYPE_IN_MEMORY = 1;
	
	int STORAGE_TYPE_PERSISTANCE = 2;
	
	int DEFAULT_STORAGE_TYPE = STORAGE_TYPE_IN_MEMORY;
	
	String IN_HANDLER_NAME = "SandeshaInHandler";
	
	String OUT_HANDLER_NAME = "SandeshaOutHandler";
	
	
	//Sequence properties
	
	String SEQ_PROPERTY_RECEIVED_MESSAGES = "SeqMsgListProperty";
	
	String SEQ_PROPERTY_TO_EPR = "ToEPR";
	
	String SEQ_PROPERTY_ACKS_TO_EPR = "acksToEPR";
	
	String SEQ_PROPERTY_OUT_SEQUENCE_ID = "OutSequenceId";
	
	String SEQ_PROPERTY_REPLY_TO_EPR = "ReplyToEPR";
	
	//SOAP versions
	int SOAP_1_1 = 1;
	
	int SOAP_1_2 = 2;
	
	int DEFAULT_SOAP_VERSION = SOAP_1_1;

	
	//message context properties
	String APPLICATION_PROCESSING_DONE = "APP_PROCESSING_DONE";
	
	//delivery assurance.
	
	String IN_ORDER = "InOrder";
	
	String NOT_IN_ORDER = "NotInOrder";
	
	String DEFAULT_DELIVERY_ASSURANCE = NOT_IN_ORDER;
	
	//invocation type
	
	String EXACTLY_ONCE = "ExactlyOnce";
	
	String MORE_THAN_ONCE = "MoreThanOnce";
	
	String DEFAULT_INVOCATION_TYPE = EXACTLY_ONCE;
	
	
	String CREATE_SEQUECE_BEAN_MAP = "CreateSequenceBeanMap";
	
	String RETRANSMITTER_BEAN_MAP = "RetransmitterBeanMap";
	
	String SEQUENCE_PROPERTY_BEAN_MAP = "SequencePropertyBeanMap";
	
	String STORAGE_MAP_BEAN_MAP = "StorageMapBeanMap";
	
	String NEXT_MESSAGE_BEAN_MAP = "NextMsgBeanMap";

}