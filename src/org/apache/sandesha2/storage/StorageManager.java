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

import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;

/**
 * @author 
 * 
 */
public interface StorageManager {

	public boolean createCreateSeq(CreateSeqBean bean);

	public CreateSeqBean retrieveCreateSeq(String key);

	public boolean updateCreateSeq(CreateSeqBean bean);

	public boolean deleteCreateSeq(String key);
	
	public boolean createNextMsg(NextMsgBean bean);
	
	public NextMsgBean retrieveNextMsgBean(String key);
	
	public boolean updateNextMsgBean(NextMsgBean bean);
	
	public boolean deleteNextMsgBean(String key);
	
	public boolean createRetransmitterBean(RetransmitterBean bean);
	
	public RetransmitterBean retrieveRetransmitterBean(String key);
	
	public boolean updateRetransmitterBean(RetransmitterBean bean);
	
	public boolean deleteRetransmitterBean(String key);
	
	public boolean createStorageMapBean(StorageMapBean bean);
	
	public StorageMapBean retrieveStorageMapBean(String key);
	
	public boolean updateStorageMapBean(StorageMapBean bean);
	
	public boolean deleteStorageMapBean(String key);
	
	public boolean createSequencePropertyBean(SequencePropertyBean bean);
	
	public SequencePropertyBean retrieveSequencePropertyBean(String key);
	
	public boolean updateSequencePropertyBean(SequencePropertyBean bean);
	
	public boolean deleteSequencePropertyBean(String key);	
}
