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

package org.apache.sandesha2.storage;

/**
 * @author
 * 
 */
public class StorageManagerFactory {
	
	public static final int IN_MEMORY_STORAGE_TYPE = 1;
	public static final int PERSISTANT_STORAGE_TYPE = 2;
	
	public static StorageManager getStorageManager(int storageType) {
		if (storageType == IN_MEMORY_STORAGE_TYPE) {
			return InMemoryStorageMgr.getInstance();
		} else if (storageType == PERSISTANT_STORAGE_TYPE) {
			return PermanentStorageMgr.getInstance();
		} else {
			throw new IllegalArgumentException("invalid storage type");
		}
	}
}
