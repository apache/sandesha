/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.sandesha;

import junit.framework.TestCase;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPEnvelope;

import java.io.File;
import java.io.FileInputStream;

/**
 * Abstract base class for test cases.
 */
public abstract class AbstractTestCase
        extends TestCase {
    protected String testDir = "test";
    //protected String sampleDir = "src/samples/";
    //protected String outDir = "target/generated/samples/";
    //protected String tempDir = "target/generated/temp";
    protected String testResourceDir = "test-resources";


    /**
     * Basedir for all file I/O. Important when running tests from
     * the reactor.
     */
    public String basedir = System.getProperty("basedir");

    /**
     * Constructor.
     */
    public AbstractTestCase(String testName) {
        super(testName);
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
        testDir = new File(basedir, testDir).getAbsolutePath();
        //sampleDir = new File(basedir,sampleDir).getAbsolutePath();
        //outDir = new File(basedir,outDir).getAbsolutePath();
        //tempDir = new File(basedir,tempDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
    }


    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    public RMMessageContext getRMMessageContext(String relativePath) throws Exception {

        FileInputStream fin = new FileInputStream(getTestResourceFile(relativePath));
        SOAPEnvelope sEnv = new SOAPEnvelope(fin);
        MessageContext msgCtx = new MessageContext(null);
        msgCtx.setRequestMessage(new Message(sEnv));
        RMMessageContext rmMsgCtx = new RMMessageContext();
        rmMsgCtx.setMsgContext(msgCtx);
        return rmMsgCtx;
    }


}

