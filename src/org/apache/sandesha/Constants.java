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
 * @author Chamikara Jayalath
 */
public interface Constants {

    int INVOKER_THREADS = 10;
    int SENDER_THREADS = 2;

    int DEFAULR_CLIENT_SIDE_LISTENER_PORT = 9090;
    int DEFAULT_SIMPLE_AXIS_SERVER_PORT = 8080;

    String HTTP = "http";
    String COLON = ":";
    String SLASH = "/";
    String UUID = "uuid:";
    String ASTERISK = "*";

    String URL_RM_SERVICE = "/axis/services/RMService";
    String IGNORE_ACTION = "ignoreAction";
    String RESPONSE = "Response";
    String CONTEXT = "context";
    String INVOKER = "invoker";
    String THREAD_POOL_SIZE = "threadPoolSize";

    long RETRANSMISSION_INTERVAL = 4000L;
    long ACKNOWLEDGEMENT_INTERVAL = 200L;
    long INACTIVITY_TIMEOUT = 600000L;
    long RMINVOKER_SLEEP_TIME = 2000L;
    long SENDER_SLEEP_TIME = 2000L;
    long CLIENT_RESPONSE_CHECKING_INTERVAL = 500L;
    long CLIENT_WAIT_PERIOD_FOR_COMPLETE = 1000L;

    int SERVER_QUEUE_ACCESSOR = 1;
    int SERVER_DATABASE_ACCESSOR = 2;

    int MSG_TYPE_CREATE_SEQUENCE_REQUEST = 1;
    int MSG_TYPE_CREATE_SEQUENCE_RESPONSE = 2;
    int MSG_TYPE_SERVICE_REQUEST = 3;
    int MSG_TYPE_SERVICE_RESPONSE = 4;
    int MSG_TYPE_ACKNOWLEDGEMENT = 5;
    int MSG_TYPE_TERMINATE_SEQUENCE = 6;

    //To identify the end point
    byte SERVER = (byte) 1;
    byte CLIENT = (byte) 0;

    int SYNCHRONOUS = 0;

    public interface WSA {
        String NS_ADDRESSING_ANONYMOUS = AddressingUtils.getAnonymousRoleURI();
    }

    public interface WSRM {
        String NS_PREFIX_RM = "wsrm";
        String NS_URI_RM = "http://schemas.xmlsoap.org/ws/2005/02/rm";
        String ACTION_CREATE_SEQUENCE = WSRM.NS_URI_RM + "/CreateSequence";
        String ACTION_CREATE_SEQUENCE_RESPONSE = WSRM.NS_URI_RM + "/CreateSequenceResponse";
        String ACTION_TERMINATE_SEQUENCE = WSRM.NS_URI_RM + "/TerminateSequence";
        String SEQUENCE_ACKNOWLEDGEMENT_ACTION = WSRM.NS_URI_RM + "/SequenceAcknowledgement";

        String ACK_RANGE = "AcknowledgementRange";
        String UPPER = "Upper";
        String LOWER = "Lower";
        String ACK_REQUESTED = "AckRequested";
        String MSG_NUMBER = "MessageNumber";
        String CREATE_SEQUENCE = "CreateSequence";
        String CREATE_SEQUENCE_RESPONSE = "CreateSequenceResponse";
        String FAULT_CODE = "FaultCode";
        String LAST_MSG = "LastMessage";
        String NACK = "Nack";
        String SEQUENCE = "Sequence";
        String SEQUENCE_ACK = "SequenceAcknowledgement";
        String TERMINATE_DEQUENCE = "TerminateSequence";
        String SEQUENCE_FAULT = "SequenceFault";
        String ACKS_TO = "AcksTo";
        String SEQUENCE_OFFER = "Offer";
        String ACCEPT = "Accept";
        String IDENTIFIER = "Identifier";

        double MAX_MSG_NO = 18446744073709551615.0d;
    }

    public interface FaultMessages {
        String SERVER_INTERNAL_ERROR = "Server Interanal Error";

        String NO_ADDRESSING_HEADERS = "No Addressing Headers Available in this Message";
        String NO_TO = "Required header <wsa:To> NOT found.";

        String NO_RM_HEADES = "No RM Headers Available in this Message";

        String INVALID_ACKNOWLEDGEMENT = "The SequenceAcknowledgement violates the cumulative " +
                "cknowledgement invariant.";
        String UNKNOWN_SEQUENCE = "The value of wsrm:Identifier is not a known Sequence " +
                "identifier.";
        String MSG_NO_ROLLOVER = "The maximum value for wsrm:MessageNumber has been exceeded.";
        String LAST_MSG_NO_EXCEEDED = "The value for wsrm:MessageNumber exceeds the value of the" +
                " MessageNumber accompanying a LastMessage element in this Sequence.";
        String INVALID_MESSAGE = "Invalid Message";
    }

    public interface FaultCodes {
        String WSRM_SERVER_INTERNAL_ERROR = "ServerInternalError";
        String IN_CORRECT_MESSAGE = "Incorrect Message";
        String WSRM_FAULT_INVALID_ACKNOWLEDGEMENT = "wsrm:InvalidAcknowledgement";
        String WSRM_FAULT_UNKNOWN_SEQUENCE = "wsrm:UnknownSequence";
        String WSRM_FAULT_MSG_NO_ROLLOVER = "wsrm:MessageNumberRollover";
        String WSRM_FAULR_LAST_MSG_NO_EXCEEDED = "wsrm:LastMessageNumberExceeded";
    }

    public interface ErrorMessages {
        String CLIENT_PROPERTY_VALIDATION_ERROR = "ERROR: To perform the operation, " +
                "ReplyTo address must be specified. This EPR will not be the Sandesha end point. " +
                "If it should be Sandesha end point, please set the propety 'sync' " +
                "to false in call.";
        String MESSAGE_NUMBER_NOT_SPECIFIED = "ERROR: Message Number Not Specified or Action " +
                "is null";

        String SET_APPROVED_OUT_SEQ = "ERROR: setApprovedOutSequence()";

        String CANNOT_SEND_THE_TERMINATE_SEQ = "SERVER ERROR: Cannot send the TerminateSequence " +
                "from Server";

        String NULL_REQUEST_MSG = "ERROR : NULL REQUEST MESSAGE";

        String SEQ_IS_NOT_CREATED = "ERROR: Sequence was not created correcly in the in queue";

    }

    public interface InfomationMessage {
        String SENDING_CREATE_SEQ = "INFO: SENDING CREATE SEQUENCE REQUEST ....\n";
        String SENDING_CREATE_SEQ_RES = "INFO: SENDING CREATE SEQUENCE RESPONSE ....\n";
        String SENDING_TERMINATE_SEQ = "INFO: SENDING TERMINATE SEQUENCE REQUEST ....\n";
        String SENDING_ACK = "INFO: SENDING ACKNOWLEDGEMENT ....\n";
        String SENDING_REQ = "INFO: SENDING REQUEST MESSAGE ....\n";
        String SENDING_RES = "INFO: SENDING RESPONSE MESSAGE ....\n";
        String PROVIDER_RECEIVED_MSG = "INFO: RMPROVIDER RECEIVED A SOAP REQUEST....\n";
        String SENDER_STARTED = "INFO: SENDER STARTED ....\n";
        String WAITING_TO_STOP_CLIENT = "INFO: WATING TO STOP CLIENT ....\n";

    }

    //Constants related to the queue.
    public interface Queue {
        String ADD_ERROR = "Error in adding message";
        String QUEUE_INCONSIS = "Inconsistent queue";
        String MESSAGE_EXISTS = "Message already exists";
        String SEQUENCE_ABSENT = "Sequence id does not exist............";
        String RESPONSE_SEQ_NULL = "ERROR: RESPONSE SEQ IS NULL";
        String SEQUENCE_ID_NULL = "Sequence Id is null";
        String MESSAGE_ID_NULL = "Message is null";
    }


    public interface ClientProperties {

        short IN_OUT = (short) 1;
        short IN_ONLY = (short) 2;

        String PROPERTY_FILE = "sandesha.properties";
        String WSRM_POLICY_FILE = "WSRMPolicy.xml";

        String CLIENT_LISTENER_PORT = "CLIENT_LISTENER_PORT";
        String SIMPLE_AXIS_SERVER_PORT_POPERTY = "SIMPLE_AXIS_SERVER_PORT";

        String FROM = "from";
        String REPLY_TO = "replyTo";
        String MSG_NUMBER = "msgNumber";
        String LAST_MESSAGE = "lastMessage";
        String SYNC = "sync";
        String ACTION = "action";
        String ACKS_TO = "acksTo";
        String TO = "to";
        String FAULT_TO = "faultTo";
        String SEND_OFFER = "offer";
        String SOURCE_URL = "sourceURL";
        String MEP = "mep";
        String CALL_KEY = "callKey";
        String REPORT = "report";

        String REQUEST_HANDLER = "requestHandler";
        String RESPONSE_HANDLER = "responseHandler";
        String LISTENER_REQUEST_HANDLER = "listenerRequestHandler";
        String LISTENER_RESPONSE_HANDLER = "listenerResponseHandler";

        String PROVIDER_CLASS = "providerClass";
        String DEFAULT_PROVIDER_CLASS = "org.apache.axis.providers.java.RPCProvider";

        String CLASS_NAME = "className";
        String RMSERVICE = "RMService";
        String RMSERVICE_CLASS = "org.apache.sandesha.client.RMService";
        String ALLOWED_METHODS = "allowedMethods";
    }

    public interface WSRMPolicy {
        String INA_TIMEOUT = "InactivityTimeout";
        String BASE_TX_INTERVAL = "BaseRetransmissionInterval";
        String ACK_INTERVAL = "AcknowledgementInterval";
        String EXP_BACKOFF = "ExponentialBackoff";

        String WSRM = "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";
        String BIN_BACKOFF = "BinaryBackoff";
    }

    String INVOKE_STRATEGY = "invokeStrategy";
    String DEFAULT_STRATEGY = "org.apache.sandesha.server.ThreadPoolInvokeStrategy:threadPoolSize=10";
    String INVOKE_HANDLER = "invokeHandler";
    String DEFAULT_HANDLER = "org.apache.sandesha.server.InvokeHandlerImpl:invoker=org.apache.axis.providers.java." +
            "RPCProvider";

}

