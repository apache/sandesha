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

package org.apache.sandesha;

import org.apache.axis.message.addressing.util.AddressingUtils;

/**
 * class Constants
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public interface Constants {

    public static final int DEFAULR_CLIENT_SIDE_LISTENER_PORT = 9090;
    public static final int DEFAULT_SIMPLE_AXIS_SERVER_PORT = 8080;

    public static final String HTTP = "http";
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String UUID = "uuid:";
    public static final String ASTERISK = "*";

    public static final String URL_RM_SERVICE = "/axis/services/RMService";
    public static final String IGNORE_ACTION = "ignoreAction";

    public static final long RETRANSMISSION_INTERVAL = 4000;
    public static final long ACKNOWLEDGEMENT_INTERVAL = 200;
    public static final long INACTIVITY_TIMEOUT = 600000;
    public static final int MAXIMUM_RETRANSMISSION_COUNT = 20;
    public static final long RMINVOKER_SLEEP_TIME = 2000;
    public static final long SENDER_SLEEP_TIME = 2000;
    public static final int CLIENT_RESPONSE_CHECKING_INTERVAL = 500;
    public static final long CLIENT_WAIT_PERIOD_FOR_COMPLETE = 1000l;

    public int SERVER_QUEUE_ACCESSOR = 1;
    public int SERVER_DATABASE_ACCESSOR = 2;

    public static final int MSG_TYPE_CREATE_SEQUENCE_REQUEST = 1;
    public static final int MSG_TYPE_CREATE_SEQUENCE_RESPONSE = 2;
    public static final int MSG_TYPE_SERVICE_REQUEST = 3;
    public static final int MSG_TYPE_SERVICE_RESPONSE = 4;
    public static final int MSG_TYPE_ACKNOWLEDGEMENT = 5;
    public static final int MSG_TYPE_TERMINATE_SEQUENCE = 6;


    //To identify the end point
    public static final byte SERVER = 1;
    public static final byte CLIENT = 0;

    public interface WSA{
        public static final String NS_ADDRESSING_ANONYMOUS = AddressingUtils.getAnonymousRoleURI();
    }


    public interface WSRM {
        public static final String NS_PREFIX_RM = "wsrm";
        public static final String NS_URI_RM = "http://schemas.xmlsoap.org/ws/2005/02/rm";
        public static final String ACTION_CREATE_SEQUENCE = NS_URI_RM+"/CreateSequence";
        public static final String ACTION_CREATE_SEQUENCE_RESPONSE = NS_URI_RM+"/CreateSequenceResponse";
        public static final String ACTION_TERMINATE_SEQUENCE = NS_URI_RM+"/TerminateSequence";
        public static final String SEQUENCE_ACKNOWLEDGEMENT_ACTION = NS_URI_RM+"/SequenceAcknowledgement";


        public static final String ACK_RANGE = "AcknowledgementRange";
        public static final String UPPER = "Upper";
        public static final String LOWER = "Lower";
        public static final String ACK_REQUESTED = "AckRequested";
        public static final String MSG_NUMBER = "MessageNumber";
        public static final String CREATE_SEQUENCE = "CreateSequence";
        public static final String CREATE_SEQUENCE_RESPONSE = "CreateSequenceResponse";
        public static final String FAULT_CODE = "FaultCode";
        public static final String LAST_MSG = "LastMessage";
        public static final String NACK = "Nack";
        public static final String SEQUENCE = "Sequence";
        public static final String SEQUENCE_ACK = "SequenceAcknowledgement";
        public static final String TERMINATE_DEQUENCE = "TerminateSequence";
        public static final String SEQUENCE_FAULT = "SequenceFault";
        public static final String ACKS_TO = "AcksTo";
        public static final String SEQUENCE_OFFER = "Offer";


        public static final double MAX_MSG_NO = 18446744073709551615d;

    }

    public interface WSU {
        public static final String WSU_PREFIX = "wsrm";
        public static final String WSU_NS = "http://schemas.xmlsoap.org/ws/2005/02/rm";
        public static final String IDENTIFIER = "Identifier";
    }

    public interface FaultMessages {
        public static final String SERVER_INTERNAL_ERROR = "Server Interanal Error";

        public static final String NO_ADDRESSING_HEADERS = "No Addressing Headers Available in this Message";
        public static final String NO_MESSAGE_ID = "MessageID should be present in the message";

        public static final String NO_RM_HEADES = "No RM Headers Available in this Message";

        public static final String INVALID_ACKNOWLEDGEMENT = "The SequenceAcknowledgement violates the cumulative acknowledgement invariant.";
        public static final String UNKNOWN_SEQUENCE = "The value of wsrm:Identifier is not a known Sequence identifier.";
        public static final String MSG_NO_ROLLOVER = "The maximum value for wsrm:MessageNumber has been exceeded.";
        public static final String LAST_MSG_NO_EXCEEDED = "The value for wsrm:MessageNumber exceeds the value of the MessageNumber accompanying a LastMessage element in this Sequence.";
    }

    public interface FaultCodes {
        public static final String WSRM_SERVER_INTERNAL_ERROR = "ServerInternalError";

        public static final String IN_CORRECT_MESSAGE = "Incorrect Message";
        public static final String WSRM_FAULT_INVALID_ACKNOWLEDGEMENT = "wsrm:InvalidAcknowledgement";
        public static final String WSRM_FAULT_UNKNOWN_SEQUENCE = "wsrm:UnknownSequence";
        public static final String WSRM_FAULT_MSG_NO_ROLLOVER = "wsrm:MessageNumberRollover";
        public static final String WSRM_FAULR_LAST_MSG_NO_EXCEEDED = "wsrm:LastMessageNumberExceeded";
    }

    public interface ErrorMessages {
        public static final String CLIENT_PROPERTY_VALIDATION_ERROR = "ERROR: To perform the operation, " +
                "ReplyTo address must be specified. This EPR will not be the Sandesha end point. " +
                "If it should be Sandesha end point, please set the propety 'sync' to false in call.";
        public static final String MESSAGE_NUMBER_NOT_SPECIFIED = "ERROR: Message Number Not Specified or Action is null";

        public static final String SET_APPROVED_OUT_SEQ = "ERROR: setApprovedOutSequence()";

        public static final String CANNOT_SEND_THE_CREATE_SEQ = "SERVER ERROR: Cannot send the CreateSequenceRequest from Server";
        public static final String CANNOT_SEND_THE_TERMINATE_SEQ = "SERVER ERROR: Cannot send the TerminateSequence from Server";

        public static final String NULL_REQUEST_MSG = "ERROR: NULL REQUEST MESSAGE";

        public static final String SEQ_IS_NOT_CREATED = "ERROR: Sequence was not created correcly in the in queue";
    }

    public interface InfomationMessage {
        public static final String SENDING_CREATE_SEQ = "INFO: SENDING CREATE SEQUENCE REQUEST ....\n";
        public static final String SENDING_CREATE_SEQ_RES = "INFO: SENDING CREATE SEQUENCE RESPONSE ....\n";
        public static final String SENDING_TERMINATE_SEQ = "INFO: SENDING TERMINATE SEQUENCE REQUEST ....\n";
        public static final String SENDING_ACK = "INFO: SENDING ACKNOWLEDGEMENT ....\n";
        public static final String SENDING_REQ = "INFO: SENDING REQUEST MESSAGE ....\n";
        public static final String SENDING_RES = "INFO: SENDING RESPONSE MESSAGE ....\n";
        public static final String PROVIDER_RECEIVED_MSG = "INFO: RMPROVIDER RECEIVED A SOAP REQUEST....\n";
        public static final String SENDER_STARTED = "INFO: SENDER STARTED ....\n";
        public static final String RMINVOKER_STARTED = "INFO: RMINVOKER STARTED ....\n";
        public static final String WAITING_TO_STOP_CLIENT = "INFO: WATING TO STOP CLIENT ....\n";
        public static final String CLIENT_LISTENER_STARTED = "NFO: CLIENT LISTENER STARTED ....\n";

    }


    //Constants related to the queue.
    public interface Queue {
        public static final String ADD_ERROR = "Error in adding message";
        public static final String QUEUE_INCONSIS = "Inconsistent queue";
        public static final String MESSAGE_EXISTS = "Message already exists";
        public static final String SEQUENCE_NOTPRESENT = "Sequence not present";
        public static final String SEQUENCE_ABSENT = "Sequence id does not exist";
        public static final String RESPONSE_SEQ_NULL = "ERROR: RESPONSE SEQ IS NULL";
        public static final String SEQUENCE_ID_NULL = "Sequence Id is null";
        public static final String MESSAGE_ID_NULL = "Message is null";
    }


    public interface ClientProperties {

        public static final String PROPERTY_FILE = "sandesha.properties";
        public static final String WSRM_POLICY_FILE = "WSRMPolicy.xml";

        public static final String CLIENT_LISTENER_PORT = "CLIENT_LISTENER_PORT";
        public static final String SIMPLE_AXIS_SERVER_PORT_POPERTY = "SIMPLE_AXIS_SERVER_PORT";

        public static final String FROM = "from";
        public static final String REPLY_TO = "replyTo";
        public static final String MSG_NUMBER = "msgNumber";
        public static final String LAST_MESSAGE = "lastMessage";
        public static final String SYNC = "sync";
        public static final String ACTION = "action";
        public static final String ACKS_TO="acksTo";
        public static final String TO="To";
        public static final String FAULT_TO="faultTo";

        public static final String REQUEST_HANDLER = "requestHandler";
        public static final String RESPONSE_HANDLER = "responseHandler";
        public static final String LISTENER_REQUEST_HANDLER = "listenerRequestHandler";
        public static final String LISTENER_RESPONSE_HANDLER = "listenerResponseHandler";

        public static final String PROVIDER_CLASS = "providerClass";
        public static final String DEFAULT_PROVIDER_CLASS = "org.apache.axis.providers.java.RPCProvider";

        public static final String CLASS_NAME = "className";
        public static final String RMSERVICE = "RMService";
        public static final String RMSERVICE_CLASS = "org.apache.sandesha.client.RMService";
        public static final String ALLOWED_METHODS = "allowedMethods";
    }

    public interface WSRMPolicy {
        public final String INA_TIMEOUT = "InactivityTimeout";
        public final String BASE_TX_INTERVAL = "BaseRetransmissionInterval";
        public final String ACK_INTERVAL = "AcknowledgementInterval";
        public final String EXP_BACKOFF = "ExponentialBackoff";

        public final String WSRM = "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";
    }
}

