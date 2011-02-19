/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sandesha2.policy.builders;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.policy.SandeshaPolicyBean;

public class AcknowledgementIntervalAssertionBuilder implements AssertionBuilder<OMElement> {
    public QName[] getKnownElements() {
        return new QName[] { Sandesha2Constants.Assertions.Q_ELEM_ACK_INTERVAL };
    }

    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        final long value = Long.parseLong(element.getText().trim());
        return new SandeshaPropertyAssertion() {
            public void apply(SandeshaPolicyBean propertyBean) {
                propertyBean.setAcknowledgementInterval(value);
            }
        };
    }
}
