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

/**
 * class Constants
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public interface Constants {

    /**
     * This is the port that is used when generating the source address. Two
     * constants SOURCE_ADDRESS_PORT and SOURCE_LISTEN_PORT are used to help
     * debuging using TCP monitor. TCP mon can listen in 8080 while the
     * SimpleAxisServer listens in 8090. In the final version these two should
     * contain the same value.
     */

    public static final int SOURCE_ADDRESS_PORT = 8080;

    /**
     * This is the port that the SimpleAxisServer in the client side listen for
     * asynchronous responses /acks.
     */
    public static final int SOURCE_LISTEN_PORT = 8090;

    /**
     * Namespace for wsu.
     */
    public static final String NS_URI_WSU = "http://schemas.xmlsoap.org/ws/2002/07/utility";

    /**
     * Field RM_CLIENT_SERVICE
     */
    public static final String RM_CLIENT_SERVICE = "RMClientService";

    /**
     * Field CLIENT_METHOD
     */
    public static final String CLIENT_METHOD = "clientMethod";

    /**
     * Field AXIS_SERVICES
     */
    public static final String AXIS_SERVICES = "/axis/services/";

    /**
     * Field QUESTION_WSDL
     */
    public static final String QUESTION_WSDL = "?wsdl";

    /**
     * Field CLIENT_REFERANCE
     */
    public static final String CLIENT_REFERENCE = "RMClientReference";

    // Policy related constants.
    // public static final EXPIRATION=new Date();

    /**
     * Field INACTIVITY_TIMEOUT
     */
    public static final long INACTIVITY_TIMEOUT = 120000;

    // double the expectd for breaking of the network in ms.

    /**
     * Field RETRANSMISSION_INTERVAL
     */
    public static final long RETRANSMISSION_INTERVAL = 4000;

    // Set to two 2000ms

    /**
     * Field MAXIMUM_RETRANSMISSION_COUNT
     */
    public static final int MAXIMUM_RETRANSMISSION_COUNT = 20;

    /**
     * Field ANONYMOUS_URI
     */
    public static final String ANONYMOUS_URI = "http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous";

    /**
     * Field NS_PREFIX_RM
     */
    public static final String NS_PREFIX_RM = "wsrm";

    /**
     * Field NS_URI_RM
     */
    public static final String NS_URI_RM = "http://schemas.xmlsoap.org/ws/2004/03/rm";

    /**
     * Field WSU_PREFIX
     */
    public static final String WSU_PREFIX = "wsu";

    /**
     * Field WSU_NS
     */
    public static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    /**
     * Field MAX_CHECKING_TIME
     */
    public static final int MAX_CHECKING_TIME = 2;

    /**
     * Field ENV_RM_REQUEST_HEADERS
     */
    public static final String ENV_RM_REQUEST_HEADERS = "org.apache.sandesha.ws.rm.REQUEST.HEADERS";

    /**
     * Field CLIENT_SEQUENCE_IDENTIFIER
     */
    public static final String CLIENT_SEQUENCE_IDENTIFIER = "SequenceIdetifier";

    /**
     * Field CLIENT_ONE_WAY_INVOKE
     */
    public static final String CLIENT_ONE_WAY_INVOKE = "OneWayInvoke";

    /**
     * Field CLIENT_RESPONSE_EXPECTED
     */
    public static final String CLIENT_RESPONSE_EXPECTED = "ResponseExpected";

    /**
     * Field CLIENT_CREATE_SEQUENCE
     */
    public static final String CLIENT_CREATE_SEQUENCE = "CreateSequence";

    /**
     * Field CLIENT_LAST_MESSAGE
     */
    public static final String CLIENT_LAST_MESSAGE = "LastMessage";

    /**
     * Field MAXIMAM_SERVER_RETRANSMISION_COUNT
     */
    public static final int MAXIMAM_SERVER_RETRANSMISION_COUNT = 2;

    /**
     * Field SERVER_RETRANSMISION_INTERVAL
     */
    public static final long SERVER_RETRANSMISION_INTERVAL = 4000;

    /**
     * Field ACTION_CREATE_SEQUENCE
     */
    public static final String ACTION_CREATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2004/03/rm/CreateSequence";

    /**
     * Field WSA_NS
     */
    public static final String WSA_NS = "http://schemas.xmlsoap.org/ws/2003/03/addressing";

    /**
     * Field WSA_PREFIX
     */
    public static final String WSA_PREFIX = "wsa";

    /**
     * Field ACTION_CREATE_SEQUENCE_RESPONSE
     */
    public static final String ACTION_CREATE_SEQUENCE_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/03/rm/CreateSequenceResponse";

    /**
     * Field ACTION_TERMINATE_SEQUENCE
     */
    public static final String ACTION_TERMINATE_SEQUENCE = "http://schemas.xmlsoap.org/ws/2004/03/rm/TerminateSequence";

    /**
     * Field SERVICE_INVOKE_INTERVAL
     */
    public static final long SERVICE_INVOKE_INTERVAL = 500;

    /**
     * Field SERVER_RESPONSE_CREATE_SEQUENCE_MAX_CHECK_COUNT
     */
    public static final int SERVER_RESPONSE_CREATE_SEQUENCE_MAX_CHECK_COUNT = 16;

    /**
     * Field SERVER_RESPONSE_CREATE_SEQUENCE_CHECKING_INTERVAL
     */
    public static final long SERVER_RESPONSE_CREATE_SEQUENCE_CHECKING_INTERVAL = 2000;

    /**
     * Field WSRM_SEQUENCE_ACKNOWLEDGEMENT_ACTION
     */
    public static final String WSRM_SEQUENCE_ACKNOWLEDGEMENT_ACTION = "http://schemas.xmlsoap.org/ws/2004/03/rm/SequenceAcknowledgement";

    /**
     * Field RESPONSE_NAME_SPACE
     */
    public static final String RESPONSE_NAME_SPACE = "http://www.w3.org/2001/XMLSchema";

    /**
     * Field WS_ADDRESSING_NAMESPACE
     */
    public static final String WS_ADDRESSING_NAMESPACE = "http://schemas.xmlsoap.org/ws/2003/03/addressing";

    /**
     * Field RM_SEQUENCE_ACKNOWLEDMENT_ACTION
     */
    public static final String RM_SEQUENCE_ACKNOWLEDMENT_ACTION = "http://schemas.xmlsoap.org/ws/2004/03/rm/SequenceAcknowledgement";

    public int SERVER_QUEUE_CACHE = 100;

    public int SERVER_QUEUE_ACCESSOR = 1;

    public int SERVER_DATABASE_ACCESSOR = 2;

    //Constant for the RMInvoker sleep time in ms.
    public static final long RMINVOKER_SLEEP_TIME = 2000;

    //Constant for the Sender sleep time in ms.
    public static final long SENDER_SLEEP_TIME = 2000;

    public static final int MSG_TYPE_CREATE_SEQUENCE_REQUEST = 1;

    public static final int MSG_TYPE_CREATE_SEQUENCE_RESPONSE = 2;

    public static final int MSG_TYPE_SERVICE_REQUEST = 3;

    public static final int MSG_TYPE_SERVICE_RESPONSE = 4;

    public static final int MSG_TYPE_ACKNOWLEDGEMENT = 5;

    public static final int MSG_TYPE_TERMINATE_SEQUENCE = 6;

    public static final String CLIENT_DEFAULD_SEQUENCE_ID = "ClientDefSeq";

    //To identify the end point
    public static final int SERVER = 1;

    public static final int CLIENT = 0;
}

