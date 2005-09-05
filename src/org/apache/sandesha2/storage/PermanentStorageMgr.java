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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.impl.sql.execute.CreateConstraintConstantAction;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;

/**
 * @author
 * 
 */
public class PermanentStorageMgr implements StorageManager {
	
	private static String Driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private static String protocol = "jdbc:derby:";
	private static String SANDESHA2_DB = "sandesha2_db";
	
	private static String CREATE_SEQUENCE_TABLE = "CREATE TABLE CreateSequence(CreateSeqMsgId VARCHAR(200), SequenceId VARCHAR(200))";
	private static String CREATE_NEXT_MSG_TABLE = "CREATE TABLE NextMsgSequence(SequenceId VARCHAR(200), NextMsgToProcess VARCHAR(200))";
	private static String CREATE_RETRANSMITTER_TABLE = "CREATE TABLE Retransmitter(MessageId VARCHAR(200), RKey VARCHAR(200), LastSentTime BIGINT, Send CHAR(1), CreateSeqMsgId VARCHAR(200))";
	private static String CREATE_STORAGE_MAP_TABLE = "CREATE TABLE StorageMap(SKey VARCHAR(200),MsgNo INTEGER, SequenceId VARCHAR(200))";
	private static String CREATE_SEQUENCE_PROPERTY_TABLE = "CREATE TABLE SequenceProperty(SequenceId VARCHAR(200), Name VARCHAR(50), Value VARCHAR(200))";
		
	private static PermanentStorageMgr self;
	private Connection connection = null;
	
	private PermanentStorageMgr() {
		initialize();
	}
	
	synchronized static PermanentStorageMgr getInstance() {
		if (self ==  null) {
			self = new PermanentStorageMgr();
		}
		return self;
	}
	
	private void initialize() {
		try {
			loadDriver();
			
			if (!isDatabaseExists()) {
				String str = protocol + SANDESHA2_DB + ";create=true";
				connection = DriverManager.getConnection(str);
				Statement statement = connection.createStatement();
				statement.executeUpdate(CREATE_SEQUENCE_TABLE);
				statement.executeUpdate(CREATE_NEXT_MSG_TABLE);
				statement.executeUpdate(CREATE_RETRANSMITTER_TABLE);
				statement.executeUpdate(CREATE_STORAGE_MAP_TABLE);
				statement.executeUpdate(CREATE_SEQUENCE_PROPERTY_TABLE);
				
				connection.setAutoCommit(false);		
				
			} else {
				connection = DriverManager.getConnection(protocol + SANDESHA2_DB);
				connection.setAutoCommit(false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not initialize PersistantStorageManager");
		}
		
	}
	
	private void loadDriver() {
		try {
			Class.forName(Driver).newInstance();
		} catch (Exception ex) {
		}
	}
	
	private boolean isDatabaseExists() {
		try {
			DriverManager.getConnection(protocol + SANDESHA2_DB);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	private Statement getStatement() {
		try {
			return connection.createStatement();
		} catch (SQLException sqlEx) {
			throw new RuntimeException(sqlEx.getMessage());
		}
	}
	
	public boolean createCreateSeq(CreateSeqBean bean) {
		
		String query = ("INSERT INTO CreateSequence VALUES ( " +
				"'" + bean.getCreateSeqMsgId() + "', " +
						"'" + bean.getSequenceId() + "')");
		try {
			getStatement().executeUpdate(query);
			ResultSet executeQuery = getStatement().executeQuery("select * from CreateSequence");
			
			return true;
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}		
	}
	
	public boolean createNextMsg(NextMsgBean bean) {
		String query = ("INSERT INTO NextMsgSequence VALUES ( "
				+ "'" + bean.getSequenceId() + "', " 
				+ "'" + bean.getNextMsgNoToProcess() + "')");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
		
	}
	
	public boolean createRetransmitterBean(RetransmitterBean bean) {
		String query = ("INSERT INTO Retransmitter VALUES ( " 
				+ "'" + bean.getMessageId() + "', "
				+ "'" + bean.getKey() + "', " 
				+ bean.getLastSentTime() +", "
				+ ((bean.isSend()) ? "'T'" : "'F'") + ", "
				+ "'" + bean.getCreateSeqMsgId() + "'"
				+ ")");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	public boolean createStorageMapBean(StorageMapBean bean) {

		String query = ("INSERT INTO StorageMap VALUES ( " +
				"'" + bean.getKey() + "', " 
				+ bean.getMsgNo() +", "
				+ "'" + bean.getSequenceId() + "')");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}

		
		
	}
	public boolean createSequencePropertyBean(SequencePropertyBean bean) {
		String query = ("INSERT INTO SequenceProperty VALUES ( " +
				"'" + bean.getSequenceId() + "', " 
				+"'" + bean.getName() +"', "
				+ "'" + bean.getValue() + "')");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	public boolean deleteCreateSeq(String key) {
		
		String query = "DELETE FROM CreateSequence WHERE CreateSeqMsgId = '" + key + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
			
		}
	}
	
	public boolean deleteNextMsgBean(String key) {
		String query = "DELETE FROM NextMsgSequence WHERE NextMsgSequence = '" + key + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
			
		}
	}
	
	public boolean deleteRetransmitterBean(String key) {
		String query = "DELETE FROM Retransmitter WHERE RKey = '" + key + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
			
		}
	}

	public boolean deleteStorageMapBean(String key) {
		String query = "DELETE FROM StorageMap WHERE SKey = '" + key + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
			
		}
	}
	
	public boolean deleteSequencePropertyBean(String key) {
		String query = "DELETE FROM SequenceProperty WHERE SequenceId = '" + key + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
			
		}
	}
	
	public CreateSeqBean retrieveCreateSeq(String key) {
		
		String query = "SELECT * FROM CreateSequence WHERE  CreateSeqMsgId = '" 
				+ key + "'";
				
		try {
			CreateSeqBean bean = new CreateSeqBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setCreateSeqMsgId(rs.getString("CreateSeqMsgId"));
			bean.setSequenceId(rs.getString("SequenceId"));
			
			return bean;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());			
		}
	}
	
	public NextMsgBean retrieveNextMsgBean(String key) {
		String query = "SELECT * FROM NextMsgSequence WHERE  SequenceId = '" 
			+ key + "'";
			
		try {
			NextMsgBean bean = new NextMsgBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setSequenceId(rs.getString("SequenceId"));
			bean.setNextMsgNoToProcess(rs.getString("NextMsgToProcess"));
			
			return bean;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());			
		}
	}

	public StorageMapBean retrieveStorageMapBean(String key) {
		String query = "SELECT * FROM StorageMap WHERE  SKey = '" 
			+ key + "'";
			
		try {
			StorageMapBean bean = new StorageMapBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setKey(rs.getString("SKey"));
			bean.setMsgNo(rs.getInt("MsgNo"));
			bean.setSequenceId(rs.getString("SequenceId"));
			
			return bean;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());			
		}
	}

	public RetransmitterBean retrieveRetransmitterBean(String key) {
		String query = "SELECT * FROM Retransmitter ";// +
				//"WHERE  RKey = '" 
			//+ key + "'";
			
	try {
		RetransmitterBean bean = new RetransmitterBean();
		ResultSet rs = getStatement().executeQuery(query);
		rs.next();
		bean.setCreateSeqMsgId(rs.getString("CreateSeqMsgId"));
		bean.setKey(rs.getString("RKey"));
		bean.setLastSentTime(rs.getLong("LastSentTime"));
		bean.setMessageId(rs.getString("MessageId"));
		bean.setSend(rs.getBoolean("Send"));
			
		return bean;
		
	} catch (SQLException ex) {
		ex.printStackTrace();
		throw new RuntimeException(ex.getMessage());			
	}
	}
	
	public SequencePropertyBean retrieveSequencePropertyBean(String key) {
		String query = "SELECT * FROM SequenceProperty WHERE  SequenceId = '" 
			+ key + "'";
			
		try {
			SequencePropertyBean bean = new SequencePropertyBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setSequenceId(rs.getString("SequenceId"));
			bean.setName(rs.getString("Name"));
			bean.setValue(rs.getString("Value"));
			
			return bean;
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());			
		}
		
	}
	
	public boolean updateCreateSeq(CreateSeqBean bean) {
		
		String query = "UPDATE CreateSequence SET CreateSeqMsgId = '" + bean.getCreateSeqMsgId() + "', "
				+ "SequenceId = '" + bean.getSequenceId() + "' "
				+ "WHERE CreateSeqMsgId = '" + bean.getCreateSeqMsgId() + "'";
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	public boolean updateNextMsgBean(NextMsgBean bean) {
		String query = ("UPDATE NextMsgSequence SET " 
				+ "SequenceId = '" + bean.getSequenceId() + "', "
				+ "NextMsgToProcess = '" + bean.getNextMsgNoToProcess() + "'");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
	}

	public boolean updateRetransmitterBean(RetransmitterBean bean) {
		String query = "UPDATE Retransmitter SET " 
			+ "MessageId = '" + bean.getMessageId() + "', "
			+ "RKey = '" + bean.getKey() + "', " 
			+ "LastSentTime = "+ bean.getLastSentTime() +", "
			+ "Send = " + ((bean.isSend()) ? "'T'" : "'F'") + ", "
			+ "CreateSeqMsgId = '" + bean.getCreateSeqMsgId() + "'";
	try {
		getStatement().executeUpdate(query);
		return true;
	} catch (SQLException ex) {
		throw new RuntimeException(ex.getMessage());
	}
		
	}
	
	public boolean updateStorageMapBean(StorageMapBean bean) {
		String query = ("UPDATE StorageMap SET " +
				"SKey = '" + bean.getKey() + "', " 
				+ "MsgNo = " + bean.getMsgNo() +", "
				+ "SequenceId = '" + bean.getSequenceId() + "'");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	public boolean updateSequencePropertyBean(SequencePropertyBean bean) {
		String query = ("UPDATE SequenceProperty SET " 
				+ "SequenceId = '" + bean.getSequenceId() + "', " 
				+"Name = '" + bean.getName() +"', "
				+ "Value = '" + bean.getValue() + "'");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
}
