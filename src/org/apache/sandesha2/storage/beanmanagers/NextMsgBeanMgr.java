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

import java.util.Collection;

import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.beans.NextMsgBean;

/**
 * Used to manage NextMsg beans.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public interface NextMsgBeanMgr extends RMBeanManager {

	public boolean delete(String sequenceId) throws SandeshaStorageException;

	public NextMsgBean retrieve(String sequenceId) throws SandeshaStorageException;

	public boolean insert(NextMsgBean bean) throws SandeshaStorageException;

	//public ResultSet find(String query);

	public Collection find(NextMsgBean bean) throws SandeshaStorageException;

	public boolean update(NextMsgBean bean) throws SandeshaStorageException;

	public Collection retrieveAll();
}
