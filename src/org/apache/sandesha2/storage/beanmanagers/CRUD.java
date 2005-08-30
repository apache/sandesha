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

import org.apache.sandesha2.storage.beans.RMBean;

/**
 * @author 
 * 
 */
public interface CRUD {
	public static final int IN_MEMORY_STORAGE_TYPE = 1;
	public static final int PERSISTANT_STORAGE_TYPE = 2;
	
	public boolean create (RMBean object);
	public RMBean retrieve (String primaryKey);
	public boolean update (RMBean bean);
	public boolean delete (String primaryKey);

		
}
