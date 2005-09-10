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

import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class PersistentCreateSeqBeanMgr implements CreateSeqBeanMgr {
	
	public boolean delete(String msgId) {
		String query = "DELETE FROM CreateSequence WHERE CreateSeqMsgId = '" + msgId + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			//TODO log this error
			ex.printStackTrace();			
		}
		return false;
	}
	
	public CreateSeqBean retrieve(String msgId) {
		String query = "SELECT * FROM CreateSequence WHERE  CreateSeqMsgId = '" 
			+ msgId + "'";
			
		try {
			CreateSeqBean bean = new CreateSeqBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setCreateSeqMsgId(rs.getString("CreateSeqMsgId"));
			bean.setSequenceId(rs.getString("SequenceId"));
			
			return bean;
		
		} catch (SQLException ex) {
			//TODO logs the error ..
		}
		return null;
	}
	
	public boolean insert(CreateSeqBean bean) {
		String query = ("INSERT INTO CreateSequence VALUES ( " 
				+ "'" + bean.getCreateSeqMsgId() + "', "
				+ "'" + bean.getSequenceId() + "')");
		
		try {
			getStatement().executeUpdate(query);
			ResultSet executeQuery = getStatement().executeQuery("select * from CreateSequence");
			
			return true;
		} catch (SQLException ex) {
			// TODO logs the error .. 
		}	
		return false;
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
	
	public Collection find(CreateSeqBean bean) {
		StringBuffer query = new StringBuffer();
		
		query.append("SELECT * FROM CreateSequence WHERE");
		
		query.append((bean.getCreateSeqMsgId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND CreateMsgSeqId = " + bean.getCreateSeqMsgId() 
														: " CreateMsgSeqId = " + bean.getCreateSeqMsgId()
				: "");
		query.append((bean.getSequenceId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND SequenceId = " + bean.getSequenceId()
														: " SequenceId = " + bean.getSequenceId()
				: "");	
		String queryString = query.toString();
		
		if (queryString.indexOf("=") == -1) {
			query.replace(queryString.indexOf("WHERE"), queryString.length(), "");
		}
			
		try {
			ResultSet rs = getStatement().executeQuery(query.toString().trim());
			ArrayList beans = new ArrayList();
			CreateSeqBean nbean;
			while (rs.next()) {
				nbean =new CreateSeqBean();
				nbean.setCreateSeqMsgId(rs.getString("CreateSeqMsgId"));
				nbean.setSequenceId(rs.getString("SequenceId"));
				beans.add(nbean);
			}			
			return beans;
		
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
		}
		return null;
	}

	public boolean update(CreateSeqBean bean) {
		String query = "UPDATE CreateSequence SET CreateSeqMsgId = '" + bean.getCreateSeqMsgId() + "', "
				+ "SequenceId = '" + bean.getSequenceId() + "' "
				+ "WHERE CreateSeqMsgId = '" + bean.getCreateSeqMsgId() + "'";
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			//TODO log the error ..
			ex.printStackTrace();
		}
		
		return false;
	}
	
	private Statement getStatement() throws SQLException {
		return PersistentBeanMgrFactory.getConnection().createStatement();
	}
}
