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
 * @author Saminda
 *
 */

public interface Constants {

	public interface WSRM{
		public static final String NS_PREFIX_RM = "wsrm";
        public static final String NS_URI_RM = "http://schemas.xmlsoap.org/ws/2005/02/rm";
        public static final String MSG_NUMBER = "MessageaNumber";
        public static final String LAST_MSG = "LastMessage";
        public static final String SEQUENCE = "Sequence";
        public static final String SEQUENCE_OFFER = "Offer";
        public static final String TERMINATE_SEQUENCE = "TerminateSequence";
        public static final String FAULT_CODE = "FaultCode";
        public static final String SEQUENCE_FAULT = "SequenceFault";
        public static final String ACKS_TO = "AcksTo";
        public static final String CREATE_SEQUENCE = "CreateSequence";
        public static final String CREATE_SEQUENCE_RESPONSE = "CreateSequenceResponse";
        public static final String ACK_REQUESTED = "AckRequested";
        public static final String ACK_RANGE = "AcknowledgementRange";
        public static final String UPPER = "Upper";
        public static final String LOWER = "Lower";
        public static final String NACK = "Nack";
        public static final String SEQUENCE_ACK = "SequenceAcknowledgement";
        public static final String IDENTIFIER = "Identifier";
        public static final String ACCEPT = "Accept";
    }

      String RM_HEADERS="rmHeaders";
      String SEQUENCE="sequence";
      String MESSAGE_NUMBER="messageNumber";

      String MESSAGE_TYPE ="messageType";

      String CREATE_SEQ_REQ="createSequenceReq";
      String CREATE_SEQ_RES="createSequenceRes";
      String ACKNOWLEDGEMENT="acknowledgement";
      String IN_MESSAGE="inMessage";
      String OUT_MESSAGE="outMessage";
      String FAULT_MESSAGE="faultMessage";
      
      int MESSAGE_TYPE_UNKNOWN = 0;
      int MESSAGE_TYPE_CREATE_SEQ = 1;
      int MESSAGE_TYPE_CREATE_SEQ_RESPONSE = 2;
      int MESSAGE_TYPE_APPLICATION = 3;
      int MESSAGE_TYPE_ACK = 4;
      int MESSAGE_TYPE_TERMINATE_SEQ = 5;
      
      int MESSAGE_PART_UNKNOWN = 0;
      int MESSAGE_PART_RM_HEADERS = 1;
      int MESSAGE_PART_ADDR_HEADERS = 2;
      int MESSAGE_PART_CREATE_SEQ = 3;
      int MESSAGE_PART_CREATE_SEQ_RESPONSE = 4;
      int MESSAGE_PART_TERMINATE_SEQ = 5;
      int MAX_MSG_PART_ID = 5;

}
