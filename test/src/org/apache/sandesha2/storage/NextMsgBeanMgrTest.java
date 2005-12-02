package org.apache.sandesha2.storage;

import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.SandeshaTestCase;
import org.apache.axis2.engine.AxisConfiguration;
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

public class NextMsgBeanMgrTest extends SandeshaTestCase {
    private NextMsgBeanMgr mgr;

    public NextMsgBeanMgrTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);
        mgr = storageManager.getNextMsgBeanMgr();

    }

    public void testDelete() {
        mgr.insert(new NextMsgBean("SeqId1", 1001));
        mgr.delete("SeqId1");
        assertNull(mgr.retrieve("SeqId1"));
    }

    public void testFind() {
        mgr.insert(new NextMsgBean("SeqId2", 1002));
        mgr.insert(new NextMsgBean("SeqId3", 1002));

        NextMsgBean target = new NextMsgBean();
        target.setNextMsgNoToProcess(1002);

        Iterator iterator = mgr.find(target).iterator();
        NextMsgBean tmp = (NextMsgBean) iterator.next();

        if (tmp.getSequenceId().equals("SeqId2")) {
            tmp = (NextMsgBean) iterator.next();
            tmp.getSequenceId().equals("SeqId3");
        } else {
            tmp = (NextMsgBean) iterator.next();
            tmp.getSequenceId().equals("SeqId2");
        }

    }

    public void testInsert() {
        NextMsgBean bean = new NextMsgBean("SeqId4", 1004);
        mgr.insert(bean);
        NextMsgBean tmp = mgr.retrieve("SeqId4");
        assertTrue(tmp.getNextMsgNoToProcess() == 1004);
    }

    public void testRetrieve() {
        assertNull(mgr.retrieve("SeqId5"));
        mgr.insert(new NextMsgBean("SeqId5", 1005));

        NextMsgBean tmp = mgr.retrieve("SeqId5");
        assertTrue(tmp.getNextMsgNoToProcess() == 1005);
    }

    public void testUpdate() {
        NextMsgBean bean = new NextMsgBean("SeqId6", 1006);
        mgr.insert(bean);
        bean.setNextMsgNoToProcess(1007);
        mgr.update(bean);
        NextMsgBean tmp = mgr.retrieve("SeqId6");
        assertTrue(tmp.getNextMsgNoToProcess() ==1007);
    }

}
