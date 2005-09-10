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

import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class PersistentSequencePropretyBeanMgr implements SequencePropertyBeanMgr {

	public boolean delete(String sequenceId, String name) {
		String query = "DELETE FROM SequenceProperty WHERE SequenceId = '" 
			+ sequenceId + ":" + name + "'" ;
		
		try {
			getStatement().executeUpdate(query);
			return true;
			
		} catch (SQLException ex) {
			// TODO logs the error ..
			ex.printStackTrace();
		}
		return false;
	}

	public SequencePropertyBean retrieve(String sequenceId, String name) {
		String query = "SELECT * FROM SequenceProperty WHERE  SequenceId = '" 
			+ sequenceId + ":" + name + "'";
			
		try {
			SequencePropertyBean bean = new SequencePropertyBean();
			ResultSet rs = getStatement().executeQuery(query);
			rs.next();
			bean.setSequenceId(rs.getString("SequenceId"));
			bean.setName(rs.getString("Name"));
			bean.setValue(rs.getString("Value"));
			
			return bean;
			
		} catch (SQLException ex) {
			//TODO logs the error ..
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean insert(SequencePropertyBean bean) {
		String query = ("INSERT INTO SequenceProperty VALUES ( " 
				+ "'" + bean.getSequenceId() + "', "
				+ "'" + bean.getName() + "', " 
				+ "'" + bean.getValue() + ", "
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

	public Collection find(SequencePropertyBean bean) {
		StringBuffer query = new StringBuffer();
		
		query.append("SELECT * FROM SequenceProperty WHERE");
		
		query.append((bean.getSequenceId() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND SequenceId = " + bean.getSequenceId() 
														: " SequenceId = " + bean.getSequenceId()
				: "");
		query.append((bean.getName() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND Name = " + bean.getName()
														: " Name = " + bean.getName()
				: "");	
		query.append((bean.getValue() != null) 
				? (query.toString().indexOf("=") != -1) ? " AND Value = " + bean.getValue()
														: " Value = " + bean.getValue()
				: "");	
		String queryString = query.toString();
		
		if (queryString.indexOf("=") == -1) {
			query.replace(queryString.indexOf("WHERE"), queryString.length(), "");
		}
			
		try {
			ResultSet rs = getStatement().executeQuery(query.toString().trim());
			ArrayList beans = new ArrayList();
			SequencePropertyBean nbean;
			while (rs.next()) {
				nbean = new SequencePropertyBean();
				nbean.setSequenceId(rs.getString("SequenceId"));
				nbean.setName(rs.getString("Name"));
				nbean.setValue(rs.getString("Value"));
				beans.add(nbean);
			}			
			return beans;
		
		} catch (SQLException ex) {
			//TODO logs the error ..
		}
		return null;
	}

	public boolean update(SequencePropertyBean bean) {
		String query = ("UPDATE SequenceProperty SET " 
				+ "SequenceId = '" + bean.getSequenceId() + "', " 
				+"Name = '" + bean.getName() +"', "
				+ "Value = '" + bean.getValue() + "'");
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
