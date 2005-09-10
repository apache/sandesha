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

import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class PersistentNextMsgBeanMgr implements NextMsgBeanMgr {

	public boolean delete(String sequenceId) {
		String query = "DELETE FROM NextMsgSequence WHERE NextMsgSequence = '" + sequenceId + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
		}		
		return false;
	}

	public NextMsgBean retrieve(String sequenceId) {
		String query = "SELECT * FROM NextMsgSequence WHERE  SequenceId = '" 
			+ sequenceId + "'";
			
		try {
			NextMsgBean bean = new NextMsgBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setSequenceId(rs.getString("SequenceId"));
			bean.setNextMsgNoToProcess(rs.getString("NextMsgToProcess"));
			return bean;
			
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();			
		}
		return null;
	}

	public boolean insert(NextMsgBean bean) {
		String query = ("INSERT INTO NextMsgSequence VALUES ( "
				+ "'" + bean.getSequenceId() + "', " 
				+ "'" + bean.getNextMsgNoToProcess() + "')");
		
		try {
			getStatement().executeUpdate(query);
			return true;
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
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

	public Collection find(NextMsgBean bean) {
		StringBuffer query = new StringBuffer();
		
		query.append("SELECT * FROM NextMsgSequence WHERE");
		
		query.append((bean.getSequenceId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND SequenceId = " + bean.getSequenceId() 
														: " SequenceId = " + bean.getSequenceId()
				: "");
		query.append((bean.getSequenceId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND NextMsgToProcess = " + bean.getNextMsgNoToProcess()
														: " NextMsgToProcess = " + bean.getNextMsgNoToProcess()
				: "");	
		String queryString = query.toString();
		
		if (queryString.indexOf("=") == -1) {
			query.replace(queryString.indexOf("WHERE"), queryString.length(), "");
		}
			
		try {
			ResultSet rs = getStatement().executeQuery(query.toString().trim());
			ArrayList beans = new ArrayList();
			NextMsgBean nbean;
			while (rs.next()) {
				nbean =new NextMsgBean();
				nbean.setSequenceId(rs.getString("SequenceId"));
				nbean.setNextMsgNoToProcess(rs.getString("NextMsgToProcess"));
				beans.add(nbean);
			}			
			return beans;
		
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean update(NextMsgBean bean) {
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
	
	private Statement getStatement() throws SQLException {
		return PersistentBeanMgrFactory.getConnection().createStatement();		
	}

}
