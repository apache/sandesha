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

package org.apache.sandesha2.storage.beanmanagers;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.context.AbstractContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.beans.RetransmitterBean;

/**
 * @author Chamikara
 * @author Sanka
 */

public interface RetransmitterBeanMgr extends RMBeanManager {

	public boolean delete(String MessageId);

	public RetransmitterBean retrieve(String MessageId);

	public boolean insert(RetransmitterBean bean) throws SandeshaException;

	public ResultSet find(String query);

	public Collection find(RetransmitterBean bean);

	public Collection findMsgsToSend();

	public boolean update(RetransmitterBean bean);

}
