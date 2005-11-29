package org.apache.sandesha2.storage;

import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.SandeshaTestCase;
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

public class CreateSeqBeanMgrTest extends SandeshaTestCase {
    private CreateSeqBeanMgr mgr;

    public CreateSeqBeanMgrTest() {
        super("CreateSeqBeanMgrTest");
    }

    public void setUp() throws Exception {
        AxisConfiguration axisConfig =  new AxisConfigurationImpl();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);

        StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);
        mgr = storageManager.getCreateSeqBeanMgr();
    }

    public void testDelete() {
        mgr.insert(new CreateSeqBean("TmpSeqId1", "CreateSeqMsgId1", "SeqId1"));
        mgr.delete("CreateSeqMsgId1");
        assertNull(mgr.retrieve("CreateSeqMsgId1"));
    }

    public void testFind() {
        mgr.insert(new CreateSeqBean("TmpSeqId2", "CreateSeqMsgId2", "SeqId2"));
        mgr.insert(new CreateSeqBean("TmpSeqId2", "CreateSeqMsgId3", "SeqId3"));

        CreateSeqBean target = new CreateSeqBean();
        target.setInternalSequenceId("TmpSeqId2");

        Iterator iter = mgr.find(target).iterator();
        CreateSeqBean tmp = (CreateSeqBean) iter.next();
        if (tmp.getCreateSeqMsgId().equals("CreateSeqMsgId1")) {
            tmp = (CreateSeqBean) iter.next();
            assertTrue(tmp.getCreateSeqMsgId().equals("CreateSeqMsgId2"));

        }   else {
            tmp = (CreateSeqBean) iter.next();
            assertTrue(tmp.getCreateSeqMsgId().equals("CreateSeqMsgId3"));
        }
    }

    public void testInsert() {
        CreateSeqBean bean = new CreateSeqBean("TmpSeqId4", "CreateSeqMsgId4", "SeqId4");
        mgr.insert(bean);
        CreateSeqBean tmpbean = mgr.retrieve("CreateSeqMsgId4");
        assertTrue(tmpbean.getCreateSeqMsgId().equals("CreateSeqMsgId4"));
        assertTrue(tmpbean.getSequenceId().equals("SeqId4"));
        assertTrue(tmpbean.getInternalSequenceId().equals("TmpSeqId4"));
    }


    public void testRetrieve() {
        assertNull(mgr.retrieve("CreateSeqMsgId5"));

        CreateSeqBean bean = new CreateSeqBean("TmpSeqId5", "CreateSeqMsgId5", "SeqId5");
        mgr.insert(bean);
        CreateSeqBean tmp = mgr.retrieve("CreateSeqMsgId5");
        assertTrue(tmp.getCreateSeqMsgId().equals("CreateSeqMsgId5"));
    }

    public void testUpdate() {

        CreateSeqBean bean = new CreateSeqBean("TmpSeqId6", "CreateSeqMsgId6", "SeqId6");
        mgr.insert(bean);
        bean.setInternalSequenceId("TmpSeqId7");
        mgr.update(bean);
        CreateSeqBean tmp = mgr.retrieve("CreateSeqMsgId6");
        assertTrue(tmp.getInternalSequenceId().equals("TmpSeqId7"));
    }
}
