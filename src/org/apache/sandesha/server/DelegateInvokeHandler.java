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

import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.sandesha.Constants;

import java.util.Collections;
import java.util.Map;

/**
 * Use the configured handler or <code>org.apache.axis.providers.java.RPCProvider</code>
 * <p/>
 * if none was explicitly set to execute the web service invoke.
 *
 * @author Patrick Collins
 */

public class DelegateInvokeHandler implements InvokeHandler {
    private Map params = Collections.EMPTY_MAP;

    public boolean handleInvoke(MessageContext aMessageContext) throws Exception {
        Handler handler = (Handler) Class.forName(getActualInvoker()).newInstance();
        handler.invoke(aMessageContext);
        return aMessageContext.getOperation().getMethod().getReturnType() == Void.TYPE;
    }

    protected String getActualInvoker() {
        String invoker = (String) getParms().get(Constants.INVOKER);
        if (invoker == null || invoker.length() == 0) {
            invoker = RPCProvider.class.getName();
        }
        return invoker;
    }

    protected Map getParms() {
        return params;
    }

    public void addParams(Map aParams) {
        if (aParams != null) {
            params = aParams;
        }
    }
}

