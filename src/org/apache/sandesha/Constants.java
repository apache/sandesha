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
	public static final String CLIENT_REFERANCE = "RMClientReferance";

	// Policy related constants.
	// public static final  EXPIRATION=new Date();

	/**
	 * Field INACTIVITY_TIMEOUT
	 */
	public static final long INACTIVITY_TIMEOUT = 60000;
	// double the expectd for breaking of the network in ms.

	/**
	 * Field RETRANSMISSION_INTERVAL
	 */
	public static final long RETRANSMISSION_INTERVAL = 2000;
	// Set to two 2000ms

	/**
	 * Field MAXIMUM_RETRANSMISSION_COUNT
	 */
	public static final int MAXIMUM_RETRANSMISSION_COUNT = 20;

	/**
	 * Field ANONYMOUS_URI
	 */
	public static final String ANONYMOUS_URI =
		"http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous";

	/**
	 * Field NS_PREFIX_RM
	 */
	public static final String NS_PREFIX_RM = "wsrm";

	/**
	 * Field NS_URI_RM
	 */
	public static final String NS_URI_RM =
		"http://schemas.xmlsoap.org/ws/2004/03/rm";

	/**
	 * Field WSU_PREFIX
	 */
	public static final String WSU_PREFIX = "wsu";

	/**
	 * Field WSU_NS
	 */
	public static final String WSU_NS =
		"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	/**
	 * Field MAX_CHECKING_TIME
	 */
	public static final int MAX_CHECKING_TIME = 2;

	/**
	 * Field ENV_RM_REQUEST_HEADERS
	 */
	public static final String ENV_RM_REQUEST_HEADERS =
		"org.apache.sandesha.ws.rm.REQUEST.HEADERS";

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
}
