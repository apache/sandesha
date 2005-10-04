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
		
		public interface Actions {
			
			String CREATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";
			
			String CREATE_SEQUENCE_RESPONSE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";
			
			String SEQUENCE_ACKNOWLEDGEMENT = "http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";
			
			String TERMINATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";
		}
	}

	public interface WSA {
		String NS_URI_ANONYMOUS = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";

		String NS_URI_ADDRESSING = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

		String NS_PREFIX_ADDRESSING = "wsa";

		String ADDRESS = "Address";
	}

	public interface WSP {
		long RETRANSMISSION_INTERVAL = 20000;
	}

	public interface MessageTypes {
		int UNKNOWN = 0;

		int CREATE_SEQ = 1;

		int CREATE_SEQ_RESPONSE = 2;

		int APPLICATION = 3;

		int ACK = 4;

		int TERMINATE_SEQ = 5;

		int MAX_MESSAGE_TYPE = 5;
	}

	public interface MessageParts {
		int UNKNOWN = 0;

		int SEQUENCE = 6;

		int SEQ_ACKNOWLEDGEMENT = 7;

		int ADDR_HEADERS = 8;

		int CREATE_SEQ = 9;

		int CREATE_SEQ_RESPONSE = 10;

		int TERMINATE_SEQ = 11;

		int ACK_REQUEST = 12;

		int MAX_MSG_PART_ID = 13;
	}

	public interface SequenceProperties {
		
		String ALL_SEQUENCES = "AllSequences";  //this is not a sequence property. This is used as the sequenceId to share data b/w sequences

		String RECEIVED_MESSAGES = "SeqMsgListProperty";

		String TO_EPR = "ToEPR";

		String ACKS_TO_EPR = "acksToEPR";

		String OUT_SEQUENCE_ID = "OutSequenceId";
		
		String INCOMING_SEQUENCE_ID = "IncomingSequenceId";
		
		String TEMP_SEQUENCE_ID = "TempSequenceId";

		String REPLY_TO_EPR = "ReplyToEPR";
		
		String APP_MSG_PROCESSOR_LIST = "AppMsgProcessorList";
		
		String OUT_CREATE_SEQUENCE_SENT = "OutCreateSeqSent";
		
		String NEXT_MESSAGE_NUMBER = "NextMsgNo";
		
		String LAST_OUT_MESSAGE = "LastOutMessage";
		
		String INCOMING_SEQUENCE_LIST = "IncomingSequenceList";
	}

	public interface SOAPVersion {
		int v1_1 = 1;

		int v1_2 = 2;

		int DEFAULT = v1_1;
	}

	public interface QOS {
		
		public interface DeliveryAssurance {
		
			String IN_ORDER = "InOrder";

			String NOT_IN_ORDER = "NotInOrder";

			String DEFAULT_DELIVERY_ASSURANCE = IN_ORDER;
		}
		
		public interface InvocationType {
			
			//invocation types
			String EXACTLY_ONCE = "ExactlyOnce";

			String MORE_THAN_ONCE = "MoreThanOnce";

			String DEFAULT_INVOCATION_TYPE = EXACTLY_ONCE;
		}
		
		
	}

	public interface BeanMAPs {
		String CREATE_SEQUECE = "CreateSequenceBeanMap";

		String RETRANSMITTER = "RetransmitterBeanMap";

		String SEQUENCE_PROPERTY = "SequencePropertyBeanMap";

		String STORAGE_MAP = "StorageMapBeanMap";

		String NEXT_MESSAGE = "NextMsgBeanMap";
	}

	//TODO remove following three
	int STORAGE_TYPE_IN_MEMORY = 1;

	int STORAGE_TYPE_PERSISTANCE = 2;

	int DEFAULT_STORAGE_TYPE = STORAGE_TYPE_IN_MEMORY;

	String IN_HANDLER_NAME = "SandeshaInHandler";

	String OUT_HANDLER_NAME = "SandeshaOutHandler";

	//message context properties
	String APPLICATION_PROCESSING_DONE = "AppProcessingDone";
	
	String ACK_WRITTEN = "AckWritten";

	int INVOKER_SLEEP_TIME = 1000;

	int SENDER_SLEEP_TIME = 1000;

	int TERMINATE_DELAY = 1000;
}