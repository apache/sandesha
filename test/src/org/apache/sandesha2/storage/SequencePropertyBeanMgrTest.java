package org.apache.sandesha2.storage;

import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
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

public class SequencePropertyBeanMgrTest extends SandeshaTestCase {
    SequencePropertyBeanMgr mgr;

    public SequencePropertyBeanMgrTest() {
        super("SequencePropertyBeanMgrTest");
    }

    public void setUp() throws Exception {
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);;
        mgr = storageManager.getSequencePropretyBeanMgr();

    }

    public void testDelete() {
        mgr.insert(new SequencePropertyBean("SeqId1", "Name1", "Value1"));
        mgr.delete("SeqId1", "Name1");
        assertNull(mgr.retrieve("SeqId1", "Name1"));
    }

    public void testFind() {
        mgr.insert(new SequencePropertyBean("SeqId2", "Name2", "Value2"));
        mgr.insert(new SequencePropertyBean("SeqId3", "Name3", "Value2"));

        SequencePropertyBean bean = new SequencePropertyBean();
        bean.setValue("Value2");
        Iterator iter = mgr.find(bean).iterator();
        SequencePropertyBean tmp = (SequencePropertyBean) iter.next();
        if (tmp.getSequenceId().equals("SeqId2")) {
            tmp = (SequencePropertyBean) iter.next();
            assertTrue(tmp.getSequenceId().equals("SeqId3"));
        } else {
            tmp = (SequencePropertyBean) iter.next();
            assertTrue(tmp.getSequenceId().equals("SeqId2"));
        }
    }

    public void testInsert() {
        mgr.insert(new SequencePropertyBean("SeqId4", "Name4", "Value4"));
        SequencePropertyBean tmp = mgr.retrieve("SeqId4", "Name4");
        assertTrue(tmp.getValue().equals("Value4"));

    }

    public void testRetrieve() {
        assertNull(mgr.retrieve("SeqId5", "Name5"));
        mgr.insert(new SequencePropertyBean("SeqId5", "Name5", "Value5"));
        assertNotNull(mgr.retrieve("SeqId5", "Name5"));
    }


    public void testUpdate() {
        SequencePropertyBean bean = new SequencePropertyBean("SeqId6", "Name6", "Value6");
        mgr.insert(bean);
        bean.setValue("Value7");
        mgr.update(bean);
        SequencePropertyBean tmp = mgr.retrieve("SeqId6", "Name6");
        assertTrue(tmp.getValue().equals("Value7"));
    }
}
