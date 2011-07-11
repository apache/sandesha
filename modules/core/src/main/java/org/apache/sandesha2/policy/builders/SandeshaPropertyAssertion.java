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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.Assertion;
import org.apache.neethi.PolicyComponent;
import org.apache.sandesha2.policy.SandeshaPolicyBean;

public abstract class SandeshaPropertyAssertion implements Assertion {
    public abstract void apply(SandeshaPolicyBean propertyBean);

    public QName getName() {
        throw new UnsupportedOperationException();
    }

    public boolean isIgnorable() {
        throw new UnsupportedOperationException();
    }

    public boolean isOptional() {
        throw new UnsupportedOperationException();
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public boolean equal(PolicyComponent policyComponent) {
        throw new UnsupportedOperationException();
    }

    public short getType() {
        throw new UnsupportedOperationException();
    }
}
