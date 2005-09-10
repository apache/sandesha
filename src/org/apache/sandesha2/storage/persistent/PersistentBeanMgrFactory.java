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
 * 
 */
package org.apache.sandesha2.storage.persistent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class PersistentBeanMgrFactory extends AbstractBeanMgrFactory {
	
	private static String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static String PROTOCOL = "jdbc:derby:";
	private static String SANDESHA2_DB = "sandesha2_db";
	
	private static String CREATE_SEQUENCE_TABLE = "CREATE TABLE CreateSequence(CreateSeqMsgId VARCHAR(200), SequenceId VARCHAR(200))";
	private static String CREATE_NEXT_MSG_TABLE = "CREATE TABLE NextMsgSequence(SequenceId VARCHAR(200), NextMsgToProcess VARCHAR(200))";
	private static String CREATE_RETRANSMITTER_TABLE = "CREATE TABLE Retransmitter(MessageId VARCHAR(200), RKey VARCHAR(200), LastSentTime BIGINT, Send CHAR(1), CreateSeqMsgId VARCHAR(200))";
	private static String CREATE_STORAGE_MAP_TABLE = "CREATE TABLE StorageMap(SKey VARCHAR(200),MsgNo INTEGER, SequenceId VARCHAR(200))";
	private static String CREATE_SEQUENCE_PROPERTY_TABLE = "CREATE TABLE SequenceProperty(SequenceId VARCHAR(200), Name VARCHAR(50), Value VARCHAR(200))";
	
	private static Connection connection = null;
	
	static {
		try {
			Class.forName(DRIVER);
		} catch (Exception ex) {
			throw new RuntimeException("cannot load the driver", ex);
		}
		
		try {
			connection = DriverManager.getConnection(PROTOCOL + SANDESHA2_DB);
			connection.setAutoCommit(false);
			
		} catch (Exception ex) {
			// db might not exist ..
			try {
				String str = PROTOCOL + SANDESHA2_DB + ";create=true";
				connection = DriverManager.getConnection(str);
				Statement statement = connection.createStatement();
				statement.executeUpdate(CREATE_SEQUENCE_TABLE);
				statement.executeUpdate(CREATE_NEXT_MSG_TABLE);
				statement.executeUpdate(CREATE_RETRANSMITTER_TABLE);
				statement.executeUpdate(CREATE_STORAGE_MAP_TABLE);
				statement.executeUpdate(CREATE_SEQUENCE_PROPERTY_TABLE);
				
				connection.setAutoCommit(false);
			} catch (Exception e) {
				
				throw new RuntimeException("cannot create the db", e);
			}
			
		}
	}
	
	public static Connection getConnection() {
		return connection;
	}

	public CreateSeqBeanMgr getCreateSeqBeanMgr() {
		return new PersistentCreateSeqBeanMgr();
	}
	
	public NextMsgBeanMgr getNextMsgBean() {
		return new PersistentNextMsgBeanMgr();
	}
	
	public RetransmitterBeanMgr getRetransmitterBeanMgr() {
		return new PersistentRetransmitterBeanMgr();
	}
	
	public SequencePropertyBeanMgr getSequencePropretyBeanMgr() {
		return new PersistentSequencePropretyBeanMgr();
	}

	public StorageMapBeanMgr getStorageMapBeanMgr() {
		return new PersistentStorageMapBeanMgr();
	}
}
