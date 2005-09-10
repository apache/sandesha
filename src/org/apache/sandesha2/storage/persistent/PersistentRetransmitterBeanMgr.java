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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;


/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class PersistentRetransmitterBeanMgr implements RetransmitterBeanMgr {
	
	public boolean delete(String messageId) {
		String query = "DELETE FROM Retransmitter WHERE RKey = '" + messageId + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			//TODO
			ex.printStackTrace();
			
		}
		return false;
	}
	
	public boolean insert(RetransmitterBean bean) {
		String query = ("INSERT INTO Retransmitter VALUES ( " 
				+ "'" + bean.getMessageId() + "', "
				+ "'" + bean.getKey() + "', " 
				+ bean.getLastSentTime() +", "
				+ ((bean.isSend().booleanValue()) ? "'T'" : "'F'") + ", "
				+ "'" + bean.getCreateSeqMsgId() + "'"
				+ ")");
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			//TODO logs the error .. 
			ex.printStackTrace();
		}
		return false;
	}
	
	public RetransmitterBean retrieve(String messageId) {
		String query = "SELECT * FROM Retransmitter WHERE MessageId = '" + messageId + "'";

		try {
			RetransmitterBean bean = new RetransmitterBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setCreateSeqMsgId(rs.getString("CreateSeqMsgId"));
			bean.setKey(rs.getString("RKey"));
			bean.setLastSentTime(rs.getLong("LastSentTime"));
			bean.setMessageId(rs.getString("MessageId"));
			bean.setSend(new Boolean(rs.getBoolean("Send")));
				
			return bean;
			
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();					
		}
		return null;
	}

	public ResultSet find(String query) {
		try {
			getStatement().executeUpdate(query);
			ResultSet rs = getStatement().executeQuery(query);
			return rs;
		} catch (SQLException ex) {
			// TODO logs the error .. 
			ex.printStackTrace();
		}	
		return null;
	}
	
	public Collection find(RetransmitterBean bean) {
		StringBuffer query = new StringBuffer();
		
		query.append("SELECT * FROM Retransmitter WHERE");
		
		query.append((bean.getMessageId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND MessageId = " + bean.getMessageId() 
														: " MessageId = " + bean.getMessageId()
				: "");
		query.append((bean.getCreateSeqMsgId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND CreateSequenceMsgId = " + bean.getCreateSeqMsgId()
														: " CreateSequenceMsgId = " + bean.getCreateSeqMsgId()
				: "");
		query.append((bean.getKey() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND RKey = " + bean.getKey()
														: " RKey = " + bean.getKey()
				: "");
		query.append((bean.getLastSentTime() != -1) 
				? (query.toString().indexOf("=") != -1) ? " AND LastSentTime = " + bean.getLastSentTime()
														: " LastSentTime = " + bean.getLastSentTime()
				: "");
		query.append((bean.isSend() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND Send = '" + bean.isSend().booleanValue() + "'"
														: " Send = '" + bean.isSend().booleanValue() + "'"
				: "");
		
		String queryString = query.toString();
		
		if (queryString.indexOf("=") == -1) {
			query.replace(queryString.indexOf("WHERE"), queryString.length(), "");
		}
			
		try {
			ResultSet rs = getStatement().executeQuery(query.toString().trim());
			ArrayList beans = new ArrayList();
			RetransmitterBean nbean;
			while (rs.next()) {
				nbean = new RetransmitterBean();
				nbean.setMessageId(rs.getString("MessageId"));
				nbean.setCreateSeqMsgId(rs.getString("CreateSeqMsgId"));
				nbean.setKey(rs.getString("RKey"));
				nbean.setLastSentTime(rs.getLong("LastSentTime"));
				nbean.setSend(new Boolean(rs.getBoolean("Send")));
				beans.add(nbean);
			}			
			return beans;
		
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean update(RetransmitterBean bean) {
		String query = "UPDATE Retransmitter SET " 
			+ "MessageId = '" + bean.getMessageId() + "', "
			+ "RKey = '" + bean.getKey() + "', " 
			+ "LastSentTime = "+ bean.getLastSentTime() +", "
			+ "Send = " + ((bean.isSend().booleanValue()) ? "'T'" : "'F'") + ", "
			+ "CreateSeqMsgId = '" + bean.getCreateSeqMsgId() + "'";
		
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
		}
		return false;
	}
	
	private Statement getStatement() throws SQLException {
		return PersistentBeanMgrFactory.getConnection().createStatement();
	}
}
