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

package org.apache.sandesha2.msgprocessors;

import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.axis2.context.ConfigurationContext;

import java.util.Iterator;


public class CreateSeqResponseMsgProcessor implements MsgProcessor {
    public void processMessage(RMMsgContext rmMsgCtx) throws SandeshaException {

    	System.out.println("IN CREATE SEQ RESPONSE PROCESSOR");
        ConfigurationContext configCtx = rmMsgCtx.getMessageContext().getSystemContext();
        String msgId = rmMsgCtx.getMessageContext().getRelatesTo().getValue();
        RetransmitterBeanMgr mgr =
                AbstractBeanMgrFactory.getInstance(configCtx).getRetransmitterBeanMgr();

        RetransmitterBean createSeqBean = mgr.retrieve(msgId);
        String tempSeqId = createSeqBean.getTempSequenceId();
        mgr.delete(msgId);

        RetransmitterBean target = new RetransmitterBean();
        target.setTempSequenceId(tempSeqId);

        Iterator iterator = mgr.find(target).iterator();
        while (iterator.hasNext()) {
            ((RetransmitterBean) iterator.next()).setSend(true);
        }
    }
}
