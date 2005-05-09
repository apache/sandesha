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

package org.apache.sandesha.server;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.SOAPEnvelope;


import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.sandesha.AbstractTestCase;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.AcknowledgementRange;
import org.apache.sandesha.ws.rm.Identifier;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.providers.RMProvider;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;


public class TCMessageValidator extends AbstractTestCase {
    /**
     * Constructor.
     */
    public TCMessageValidator(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {

    }

    public void testValidateNoAddressingHeaders() throws Exception {
        RMMessageContext rmMsgCtx = getRMMessageContext("server/validation/NoAddrHeadersMsg.xml");
        try {
            MessageValidator.validate(rmMsgCtx, false);
        } catch (AxisFault af) {
            assertEquals(af.getFaultString(), Constants.FaultMessages.NO_ADDRESSING_HEADERS);
        }
    }


    public void testValidateNoRMHeaders() throws Exception {

        RMMessageContext rmMsgCtx = getRMMessageContext("server/validation/NoRMHeadersMsg.xml");
        MessageContext msgCtx = rmMsgCtx.getMsgContext();
        AddressingHeaders addrHeaders = new AddressingHeaders(
                rmMsgCtx.getMsgContext().getRequestMessage().getSOAPEnvelope());
        msgCtx.setProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS,
                addrHeaders);
        rmMsgCtx.setMsgContext(msgCtx);

        try {
            MessageValidator.validate(rmMsgCtx, false);
        } catch (AxisFault af) {
            assertEquals(af.getFaultString(), Constants.FaultMessages.NO_RM_HEADES);
        }
    }

    public void testValidateMsgNoRollOver() throws Exception {
        RMMessageContext rmMsgCtx = getRMMessageContext("server/validation/MsgNoRollOver.xml");
        MessageContext msgCtx = rmMsgCtx.getMsgContext();
        AddressingHeaders addrHeaders = new AddressingHeaders(
                rmMsgCtx.getMsgContext().getRequestMessage().getSOAPEnvelope());
        msgCtx.setProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS,
                addrHeaders);
        rmMsgCtx.setMsgContext(msgCtx);

        try {
            MessageValidator.validate(rmMsgCtx, false);
        } catch (AxisFault af) {
            assertEquals(af.getFaultString(), Constants.FaultMessages.MSG_NO_ROLLOVER);
        }
    }

    public void testValidateUnknownSequence() throws Exception {
        RMMessageContext rmMsgCtx = getRMMessageContext("server/validation/UnknownSequenceMsg.xml");
        MessageContext msgCtx = rmMsgCtx.getMsgContext();
        AddressingHeaders addrHeaders = new AddressingHeaders(
                rmMsgCtx.getMsgContext().getRequestMessage().getSOAPEnvelope());
        msgCtx.setProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS,
                addrHeaders);
        rmMsgCtx.setMsgContext(msgCtx);

        try {
            MessageValidator.validate(rmMsgCtx, false);
        } catch (AxisFault af) {
              assertEquals(af.getFaultString(), Constants.FaultMessages.UNKNOWN_SEQUENCE);
        }
    }

    public void testValidateKnonwSequence() throws Exception {

        RMMessageContext rmMsgCtx1 = getRMMessageContext("server/validation/CreateSeqRequest.xml");
        MessageContext msgCtx1 = rmMsgCtx1.getMsgContext();
        AddressingHeaders addrHeaders = new AddressingHeaders(
                rmMsgCtx1.getMsgContext().getRequestMessage().getSOAPEnvelope());
        msgCtx1.setProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS,
                addrHeaders);
        rmMsgCtx1.setMsgContext(msgCtx1);
        rmMsgCtx1.setAddressingHeaders(addrHeaders);

        RMMessageContext rmMsgCtx2 = getRMMessageContext("server/validation/MsgNo1Correct.xml");
        MessageContext msgCtx2 = rmMsgCtx2.getMsgContext();

        addrHeaders = new AddressingHeaders(
                rmMsgCtx2.getMsgContext().getRequestMessage().getSOAPEnvelope());
        msgCtx2.setProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS,
                addrHeaders);
        rmMsgCtx2.setMsgContext(msgCtx2);
        SOAPService soapService1 = new SOAPService();
        msgCtx1.setService(soapService1);
        SOAPService soapService2 = new SOAPService();
        msgCtx2.setService(soapService2);

        try {
            RMProvider rmProvider = new RMProvider();
            rmProvider.processMessage(msgCtx1, null, null, null);

            SOAPEnvelope soapEnv = msgCtx1.getResponseMessage().getSOAPEnvelope();
            RMHeaders rmHeaders = new RMHeaders();
            rmHeaders.fromSOAPEnvelope(soapEnv);
            String seqID = rmHeaders.getCreateSequenceResponse().getIdentifier().getIdentifier();

            RMHeaders rmH = new RMHeaders();

            SOAPEnvelope soapEnv2 = msgCtx2.getRequestMessage().getSOAPEnvelope();
            rmH.fromSOAPEnvelope(soapEnv2);
            Identifier seqIdentifier = rmH.getSequence().getIdentifier();
            seqIdentifier.setIdentifier(seqID);


            rmH.toSoapEnvelop(soapEnv2);
            msgCtx2.setRequestMessage(new Message(soapEnv2));

            rmProvider.processMessage(msgCtx2, null, null, null);
            //System.out.println(msgCtx2.getResponseMessage().getSOAPEnvelope().toString());
            RMHeaders rmHead = new RMHeaders();
            rmHead.fromSOAPEnvelope(msgCtx2.getResponseMessage().getSOAPEnvelope());
            AcknowledgementRange ackRange = (AcknowledgementRange) rmHead.getSequenceAcknowledgement()
                    .getAckRanges().get(0);
            assertEquals(1, ackRange.getMaxValue());
            assertEquals(1, ackRange.getMinValue());


        } catch (AxisFault af) {
            af.printStackTrace();
        }

    }

    public void testForFaults() throws Exception {
        RMMessageContext rmMsgCtx1 = getRMMessageContext("server/validation/MsgNo1Correct.xml");
        MessageContext msgCtx1 = rmMsgCtx1.getMsgContext();
        AddressingHeaders addrHeaders = new AddressingHeaders(
                rmMsgCtx1.getMsgContext().getRequestMessage().getSOAPEnvelope());
        msgCtx1.setProperty(
                org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS,
                addrHeaders);
        rmMsgCtx1.setMsgContext(msgCtx1);
        rmMsgCtx1.setAddressingHeaders(addrHeaders);


        SOAPService soapService1 = new SOAPService();
        msgCtx1.setService(soapService1);


        RMProvider rmProvider = new RMProvider();
        rmProvider.processMessage(msgCtx1, null, null, null);
        SOAPBody sb=msgCtx1.getResponseMessage().getSOAPEnvelope().getBody();
        SOAPFault sf=sb.getFault();
        assertEquals(sf.getFaultString() ,Constants.FaultMessages.UNKNOWN_SEQUENCE);


    }

}
