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
 * Contains all the Sandesha2Constants of Sandesha2.
 * Please see sub-interfaces to see grouped data.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Jaliya Ekanayaka <jaliya@opensource.lk>
 */

public interface Sandesha2Constants {

	
	public interface SPEC_VERSIONS {
		String v1_0 = "Spec_2005_02";
		String v1_1 = "Spec_2005_10";
	}
	
	public interface SPEC_2005_02 {
		
		String NS_URI = "http://schemas.xmlsoap.org/ws/2005/02/rm";
		
		public interface Actions {

			String ACTION_CREATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";

			String ACTION_CREATE_SEQUENCE_RESPONSE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";

			String ACTION_SEQUENCE_ACKNOWLEDGEMENT = "http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";

			String ACTION_TERMINATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";

			String SOAP_ACTION_CREATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";

			String SOAP_ACTION_CREATE_SEQUENCE_RESPONSE = "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";

			String SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT = "http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";

			String SOAP_ACTION_TERMINATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";
		}
	}
	
	public interface SPEC_2005_10 {
		
		String NS_URI = "http://docs.oasis-open.org/ws-rx/wsrm/200602";
		
		public interface Actions {
			
			String ACTION_CREATE_SEQUENCE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CreateSequence";

			String ACTION_CREATE_SEQUENCE_RESPONSE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CreateSequenceResponse";

			String ACTION_SEQUENCE_ACKNOWLEDGEMENT = "http://docs.oasis-open.org/ws-rx/wsrm/200602/SequenceAcknowledgement";

			String ACTION_TERMINATE_SEQUENCE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/TerminateSequence";
			
			String ACTION_TERMINATE_SEQUENCE_RESPONSE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/TerminateSequenceResponse";
			
			String ACTION_ACK_REQUEST = "http://docs.oasis-open.org/ws-rx/wsrm/200602/AckRequested";
			
			String ACTION_CLOSE_SEQUENCE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CloseSequence";
			
			String ACTION_CLOSE_SEQUENCE_RESPONSE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CloseSequenceResponse";
			
			String SOAP_ACTION_CREATE_SEQUENCE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CreateSequence";

			String SOAP_ACTION_CREATE_SEQUENCE_RESPONSE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CreateSequenceResponse";

			String SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT = "http://docs.oasis-open.org/ws-rx/wsrm/200602/SequenceAcknowledgement";

			String SOAP_ACTION_TERMINATE_SEQUENCE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/TerminateSequence";
			
			String SOAP_ACTION_TERMINATE_SEQUENCE_RESPONSE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/TerminateSequenceResponse";
			
			String SOAP_ACTION_ACK_REQUEST = "http://docs.oasis-open.org/ws-rx/wsrm/200602/AckRequested";
			
			String SOAP_ACTION_CLOSE_SEQUENCE = "http://docs.oasis-open.org/ws-rx/wsrm/200602/CloseSequence";
		}
	}
	
	public interface WSRM_COMMON {
		
		String NS_PREFIX_RM = "wsrm";

		

		String MSG_NUMBER = "MessageNumber";

		String LAST_MSG = "LastMessage";

		String SEQUENCE = "Sequence";

		String SEQUENCE_OFFER = "Offer";

		String TERMINATE_SEQUENCE = "TerminateSequence";

		String CLOSE_SEQUENCE = "CloseSequence";
		
		String CLOSE_SEQUENCE_RESPONSE = "CloseSequenceResponse";
		
		String TERMINATE_SEQUENCE_RESPONSE = "TerminateSequenceResponse";
		
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
		
		String NONE = "None";
		
		String FINAL = "Final";
	}

	public interface WSA {
		
		String NS_PREFIX_ADDRESSING = "wsa";

		String ADDRESS = "Address";

//		String SOAP_FAULT_ACTION = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
		
	}

	public interface MessageTypes {
		int UNKNOWN = 0;

		int CREATE_SEQ = 1;

		int CREATE_SEQ_RESPONSE = 2;

		int APPLICATION = 3;

		int ACK = 4;
		
		int CLOSE_SEQUENCE = 5;

		int CLOSE_SEQUENCE_RESPONSE = 6;
		
		int TERMINATE_SEQ = 7;
		
		int ACK_REQUEST = 8;
		
		int TERMINATE_SEQ_RESPONSE = 9;

		int FAULT_MSG = 10;
		
		int MAX_MESSAGE_TYPE = 10;
	}

	public interface MessageParts {
		int UNKNOWN = 0;

		int SEQUENCE = 6;

		int SEQ_ACKNOWLEDGEMENT = 7;

		int ADDR_HEADERS = 8;

		int CREATE_SEQ = 9;

		int CREATE_SEQ_RESPONSE = 10;

		int TERMINATE_SEQ = 11;
		
		int CLOSE_SEQUENCE = 12;
		
		int CLOSE_SEQUENCE_RESPONSE = 13;

		int TERMINATE_SEQ_RESPONSE = 14;
		
		int ACK_REQUEST = 15;

		int MAX_MSG_PART_ID = 15;
	}

	public interface SequenceProperties {
		
		String RM_SPEC_VERSION = "WSRMSpecVersion";

		String ALL_SEQUENCES = "AllSequences"; //this is not a sequence
											   // property. This is used as the
											   // sequenceId to share data b/w
											   // sequences
		
		//Addressing version of a sequence. All messages of a sequence should have this addressing versio
		String ADDRESSING_NAMESPACE_VALUE = "AddressingNamespaceValue";

		//For incoming sequences this gives the msg no's of the messages that were
		//received (may be an ack was sent - depending on the policy)
		//For out going sequences this gives the messages that were sent and that were successfully
		//acked by the other end point.
		String CLIENT_COMPLETED_MESSAGES = "ClientCompletedMessages";
		String SERVER_COMPLETED_MESSAGES = "ServerCompletedMessages";
		
		String TO_EPR = "ToEPR";

		String ACKS_TO_EPR = "acksToEPR";

		String OUT_SEQUENCE_ID = "OutSequenceId";

		String INTERNAL_SEQUENCE_ID = "TempSequenceId";

		String REPLY_TO_EPR = "ReplyToEPR";

		String APP_MSG_PROCESSOR_LIST = "AppMsgProcessorList";

		String OUT_CREATE_SEQUENCE_SENT = "OutCreateSeqSent";

		String NEXT_MESSAGE_NUMBER = "NextMsgNo";

		String INCOMING_SEQUENCE_LIST = "IncomingSequenceList";

		String CHECK_RESPONSE = "CheckResponse";

		String OFFERED_SEQUENCE = "OfferedSequence";

		String TERMINATE_ADDED = "TerminateAdded";
		
		String TERMINATE_RECEIVED = "TerminateReceived";
		
		String LAST_ACTIVATED_TIME = "LastActivatedTime";
		
		String NO_OF_OUTGOING_MSGS_ACKED = "NoOfOutGoingMessagesAcked";
		
		String TRANSPORT_TO = "TransportTo";
		
		String OUT_SEQ_ACKSTO = "OutSequenceAcksTo";
		
		String SEQUENCE_CLOSED = "SequenceClosed";
		
		String SEQUENCE_TERMINATED = "SequenceTerminated";
		
		String SEQUENCE_TIMED_OUT = "SequenceTimedOut";
		
//		String LAST_MESSAGE = "LastMessage";

		String REQUEST_SIDE_SEQUENCE_ID = "RequestSideSequenceID"; 		//used only at the server side
		
		String HIGHEST_IN_MSG_NUMBER = "HighestInMsgNumber";
		
		String HIGHEST_IN_MSG_KEY = "HighestInMsgKey";
		
		String HIGHEST_OUT_MSG_NUMBER = "HighestOutMsgNumber";
		
		String HIGHEST_OUT_MSG_KEY = "HighestOutMsgKey";
		
		String LAST_OUT_MESSAGE_NO = "LastOutMessage";
		
		String LAST_IN_MESSAGE_NO = "LastInMessage";
	}

	public interface SOAPVersion {
		int v1_1 = 1;

		int v1_2 = 2;
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

	public interface SOAPFaults {

		public interface Subcodes {

			String SEQUENCE_TERMINATED = "wsrm:SequenceTerminated";
			
			String SEQUENCE_CLOSED = "wsrm:SequenceClosed";

			String UNKNOWN_SEQUENCE = "wsrm:UnknownSequence";

			String INVALID_ACKNOWLEDGEMENT = "wsrm:InvalidAcknowledgement";

			String MESSAGE_NUMBER_ROLEOVER = "wsrm:MessageNumberRollover";

			String LAST_MESSAGE_NO_EXCEEDED = "wsrm:LastMessageNumberExceeded";

			String CREATE_SEQUENCE_REFUSED = "wsrm:CreateSequenceRefused";
			

		}

		public interface FaultType {

			public static final int UNKNOWN_SEQUENCE = 1;

			public static final int MESSAGE_NUMBER_ROLLOVER = 2;

			public static final int INVALID_ACKNOWLEDGEMENT = 3;

			public static final int CREATE_SEQUENCE_REFUSED = 4;
			
			public static final int LAST_MESSAGE_NO_EXCEEDED = 5;
		}
	}

	public interface Properties {
		
		String RetransmissionInterval = "RetransmissionInterval";
		
		String AcknowledgementInterval = "AcknowledgementInterval";
		
		String ExponentialBackoff = "ExponentialBackoff";
		
		String InactivityTimeout = "InactivityTimeout";
		
		String InactivityTimeoutMeasure = "InactivityTimeoutMeasure";
		
		String StorageManager = "StorageManager";
		
		String InOrderInvocation = "InvokeInOrder";
		
		String MessageTypesToDrop = "MessageTypesToDrop";
		
		String RetransmissionCount = "RetransmissionCount";
		
		public interface DefaultValues {
			
			int RetransmissionInterval = 20000;
			
			int AcknowledgementInterval = 4000;
			
			boolean ExponentialBackoff = true;
			
			int InactivityTimeout = -1;
			
			String InactivityTimeoutMeasure = "seconds";   //this can be - seconds,minutes,hours,days
			
			String StorageManager = "org.apache.sandesha2.storage.inmemory.InMemoryStorageManager";
		
			boolean InvokeInOrder = true;
			
			String MessageTypesToDrop=VALUE_NONE;
			
			int RetransmissionCount = 8;
			
			int MaximumRetransmissionCount = 10;
		}
	}
	
	public interface DatabaseParams {
		
	}
	
	String IN_HANDLER_NAME = "SandeshaInHandler";

	String OUT_HANDLER_NAME = "SandeshaOutHandler";

	String GLOBAL_IN_HANDLER_NAME = "GlobalInHandler";

	String APPLICATION_PROCESSING_DONE = "Sandesha2AppProcessingDone";

	String ACK_WRITTEN = "AckWritten";

	int INVOKER_SLEEP_TIME = 1000;

	int SENDER_SLEEP_TIME = 500;

	int CLIENT_SLEEP_TIME = 10000;

	int TERMINATE_DELAY = 100;

	String TEMP_SEQUENCE_ID = "uuid:tempID";

	String ACK_PROCSSED = "AckProcessed";

	String RM_ENABLE_KEY = "RMEnabled";

	int MAXIMUM_RETRANSMISSION_ATTEMPTS = 5;
	
	String PROPERTY_FILE = "sandesha2.properties";
	
	String VALUE_NONE = "none";
	
	String VALUE_EMPTY = "empty";
	
	String VALUE_TRUE = "true";
	
	String VALUE_FALSE = "false";
	
	String MESSAGE_STORE_KEY = "Sandesha2MessageStoreKey";

	String ORIGINAL_TRANSPORT_OUT_DESC = "Sandesha2OriginalTransportSender";
	
	String SET_SEND_TO_TRUE = "Sandesha2SetSendToTrue";
	
	String MESSAGE_TYPE = "Sandesha2MessageType";
	
	String QUALIFIED_FOR_SENDING = "Sandesha2QualifiedForSending";  //Sender will send messages only if this property is null (not set) or true.

	String QNAME_SEPERATOR = ",";
	
	String EXECUTIN_CHAIN_SEPERATOR = ".";
	
	String INTERNAL_SEQUENCE_PREFIX = "Sandesha2InternalSequence";
	
	String SANDESHA_PROPERTY_BEAN = "Sandesha2PropertyBean";
	
	String LIST_SEPERATOR = ",";
	
	String LIST_PART_SEPERATOR = "-";
	
	String STORAGE_MANAGER = "StorageManager";
	
	String SENDER = "Sender";
	
	String INVOKER = "Invoker";
	
}