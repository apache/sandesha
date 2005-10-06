package org.apache.sandesha2.storage;

import org.apache.sandesha2.SandeshaTestCase;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.context.ConfigurationContext;

import java.util.Iterator;
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

public class RetransmitterBeanMgrTest extends SandeshaTestCase {
    private RetransmitterBeanMgr mgr;

    public RetransmitterBeanMgrTest() {
        super("RetransmitterBeanMgrTest");
    }

    public void setUp() throws Exception {
        AxisConfiguration axisConfig = new AxisConfigurationImpl();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        mgr = AbstractBeanMgrFactory.getInstance(configCtx).getRetransmitterBeanMgr();
    }

    public void testDelete() {
        assertNull(mgr.retrieve(""));
        try {
            mgr.insert(new RetransmitterBean("MsgId1", "Key1", 1001, false, "TmpSeqId1", 1001));
        } catch (Exception ex) {
            fail("should not throw an exception");
        }
        assertNotNull(mgr.retrieve("MsgId1"));
    }

    public void testFind() {
        try {
            mgr.insert(new RetransmitterBean("MsgId2", "Key2", 1002, false, "TmpSeqId2", 1002));
            mgr.insert(new RetransmitterBean("MsgId3", "Key3", 1003, false, "TmpSeqId2", 1003));

            RetransmitterBean target = new RetransmitterBean();
            target.setTempSequenceId("TmpSeqId2");

            Iterator iterator = mgr.find(target).iterator();
            RetransmitterBean tmp = (RetransmitterBean) iterator.next();

            if (tmp.getMessageId().equals("MsgId2")) {
                tmp = (RetransmitterBean) iterator.next();
                assertTrue(tmp.getMessageId().equals("MsgId3"));
            } else {
                tmp = (RetransmitterBean) iterator.next();
                assertTrue(tmp.getMessageId().equals("MsgId2"));
            }


        } catch (SandeshaException e) {
            fail("should not throw an exception");
        }


    }

    public void testInsert() {
        try {
            mgr.insert(new RetransmitterBean());
            fail("should throw an exception");

        } catch (SandeshaException ex) {
        }

        try {
            mgr.insert(new RetransmitterBean("MsgId4","Key4", 1004, false, "TmpSeqId4", 1004));
            RetransmitterBean tmp = mgr.retrieve("MsgId4");
            assertTrue(tmp.getKey().equals("Key4"));


        } catch (SandeshaException e) {
            fail("should not throw an exception");
        }

    }

    public void testRetrieve() {
        assertNull(mgr.retrieve("MsgId5"));
        try {
            mgr.insert(new RetransmitterBean("MsgId5", "Key5", 1005, false, "TmpSeqId5", 1005));
        } catch (SandeshaException e) {
            fail("this should not throw an exception");
        }
        assertNotNull(mgr.retrieve("MsgId5"));
    }

    public void testUpdate() {
        RetransmitterBean bean = new RetransmitterBean("MsgId6", "Key6", 1006, false, "TmpSeqId6", 1006);
        try {
            mgr.insert(bean);
        } catch (SandeshaException e) {
            fail("should not throw an exception");
        }
        bean.setSend(true);
        mgr.update(bean);

        RetransmitterBean tmp = mgr.retrieve("MsgId6");
        assertTrue(tmp.isSend());
    }
}
