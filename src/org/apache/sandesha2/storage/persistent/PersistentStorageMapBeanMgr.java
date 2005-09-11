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

import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class PersistentStorageMapBeanMgr implements StorageMapBeanMgr {

	
	
	public boolean insert(StorageMapBean bean) {
		
		throw new UnsupportedOperationException ();
		
		//TODO: verify weather following works.
		
//		String query = ("INSERT INTO StorageMap VALUES ( " 
//				+ "'" + bean.getKey() + "', "
//				+ "'" + bean.getMsgNo() + "',"
//				+ "'" + bean.getSequenceId() + "')");
//		
//		try {
//			getStatement().executeUpdate(query);
//			ResultSet executeQuery = getStatement().executeQuery("select * from CreateSequence");
//			
//			return true;
//		} catch (SQLException ex) {
//			// TODO logs the error .. 
//		}	
//		return false;
	}
	public boolean delete(String key) {		
		String query = "DELETE FROM StorageMap WHERE SKey = '" + key + "'" ;
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();			
		}
		return false;
	}
	
	public StorageMapBean retrieve(String key) {
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
			// TODO logs the error ..
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

	public Collection find(StorageMapBean bean) {
		
		
		throw new UnsupportedOperationException ();
		
		//TODO recheck the folowing implementation. Had to change In-Memory one.
		
//		StringBuffer query = new StringBuffer();
//
//		query.append("SELECT * FROM StorageMap WHERE");
//		query.append((bean.getKey() != null) 
//				? (query.toString().indexOf("=") != -1) ? " AND SKey = " + bean.getKey() 
//														: " SKey = " + bean.getKey()
//				: "");
//		query.append((bean.getMsgNo() != -1) 
//				? (query.toString().indexOf("=") != -1) ? " AND MsgNo = " + bean.getMsgNo()
//														: " MsgNo = " + bean.getMsgNo()
//				: "");
//		query.append((bean.getSequenceId() != null) 
//				? (query.toString().indexOf("=") != -1) ? " AND SequenceId = " + bean.getSequenceId()
//														: " SequenceId = " + bean.getSequenceId()
//				: "");
//		
//		String queryString = query.toString();
//		
//		if (queryString.indexOf("=") == -1) {
//			query.replace(queryString.indexOf("WHERE"), queryString.length(), "");
//		}
//			
//		try {
//			ResultSet rs = getStatement().executeQuery(query.toString().trim());
//			ArrayList beans = new ArrayList();
//			StorageMapBean nbean;
//			while (rs.next()) {
//				nbean = new StorageMapBean();
//				nbean.setKey(rs.getString("SKey"));
//				nbean.setSequenceId(rs.getString("SequenceId"));
//				nbean.setMsgNo(rs.getInt("MsgNo"));
//				beans.add(nbean);
//			}			
//			return beans;
//		
//		} catch (SQLException ex) {
//			//TODO logs the error ..
//			ex.printStackTrace();
//		}
//		return null;
	}

	public boolean update(StorageMapBean bean) {
		
		throw new UnsupportedOperationException();
	}
	
	public Statement getStatement() throws SQLException {
		return PersistentBeanMgrFactory.getConnection().createStatement();
	}

}
