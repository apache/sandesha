package org.apache.sandesha2;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SandeshaTestCase extends TestCase {
    String resourceDir = "test-resources";

    public SandeshaTestCase(String name) {
        super(name);
        File baseDir = new File("");
        String testRource = baseDir.getAbsolutePath() + File.separator + "test-resources";
        resourceDir = new File(testRource).getPath();

    }

    protected InputStreamReader getResource(String relativePath, String resourceName) {
        String resourceFile = resourceDir + relativePath + File.separator + resourceName;
        String file = new File("/home/sanka/sandesha2-src/trunk/test-resources/CreateSequence.xml").getPath();

        try {
            FileReader reader = new FileReader(resourceFile);
            return reader;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("cannot load the test-resource", e);
        }
    }

    protected SOAPEnvelope getSOAPEnvelope() {
        return OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
    }

    protected SOAPEnvelope getSOAPEnvelope(String relativePath, String resourceName) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    getResource(relativePath, resourceName));
            OMXMLParserWrapper wrapper = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                    OMAbstractFactory.getSOAP11Factory(), reader);
            return (SOAPEnvelope) wrapper.getDocumentElement();

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected SOAPEnvelope getEmptySOAPEnvelope() {
        return OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
    }


    private MessageContext getMessageContext() throws Exception{
        AxisConfiguration axisConfig = new AxisConfiguration ();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        MessageContext msgCtx = new MessageContext(configCtx);
        return msgCtx;

    }


}
