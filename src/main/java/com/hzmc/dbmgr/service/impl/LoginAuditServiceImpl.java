package com.hzmc.dbmgr.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.hzmc.dbmgr.dto.LoginAudit;
import com.hzmc.dbmgr.service.LoginAuditService;
import com.hzmc.dbmgr.util.DruidDataSourceUtil;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月28日 下午3:41:22
 */
@Service
public class LoginAuditServiceImpl implements LoginAuditService {

	private static final Logger logger = LoggerFactory.getLogger(LoginAuditServiceImpl.class);

	private static final String QUERY_LOGIN_AUDIT_SQL = "select * from mc$login_audit ";

    private static final String DELETE_LOGIN_AUDIT_SQL = "delete from mc$login_audit where id<=%S";

	@Override
	public List<LoginAudit> getLoginAudit(Integer objId, String dbName) {
		List<LoginAudit> list = new ArrayList<LoginAudit>();
		DruidPooledConnection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = DruidDataSourceUtil.getInstance().getConnection(objId);
		} catch (SQLException e) {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
			logger.error("GetLoginAudit error Not DruidDatabase,ObjId:" + objId, e);
			return null;
		}
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(QUERY_LOGIN_AUDIT_SQL);
		} catch (SQLException e) {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
			logger.error("Query table mc$login_audit fail! ", e);
			return null;
		}
		Integer id = 0;
		boolean isCycle = true;
		while (isCycle) {
			try {
				isCycle = resultSet.next();
				if (!isCycle) {
					break;
				}
                LoginAudit audit = loginAuditMapping(resultSet);
                if (Integer.valueOf(audit.getPrimaryKey()) > id) {
                    id = Integer.valueOf(audit.getPrimaryKey());
                }
                list.add(audit);
			} catch (SQLException e) {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
				logger.error("LoginAudit Analysis fail! ", e);
			}
		}
		// 如果删除成功了
		try {
			deleteLoginAudit(connection, id);
		} catch (Exception e) {
			list = null;
			logger.error("LoginAudit delete fail! ", e);
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		return list;
	}

	private boolean deleteLoginAudit(DruidPooledConnection connection, Integer id) {
		boolean result = false;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(String.format(DELETE_LOGIN_AUDIT_SQL, id));
			if (statement != null) {
				statement.close();
			}
			result = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

    private LoginAudit loginAuditMapping(ResultSet resultSet) throws SQLException {
		LoginAudit loginAudit = new LoginAudit();
		loginAudit.setPrimaryKey(resultSet.getString("id"));
		loginAudit.setId(UUID.randomUUID().toString());
		loginAudit.setDataType(resultSet.getString("dataType"));
		loginAudit.setFromAddress(resultSet.getString("fromAddress"));
		loginAudit.setCmdtype(resultSet.getString("cmdtype"));
		loginAudit.setHost(resultSet.getString("clientHostName"));
		loginAudit.setOsUser(resultSet.getString("clientOSUser"));
		loginAudit.setAppname(resultSet.getString("clientAppName"));
		loginAudit.setServerhost(resultSet.getString("serverHostName"));
		loginAudit.setInstanceName(resultSet.getString("dbInstanceName"));
		loginAudit.setDbuser(resultSet.getString("dbUserName"));
		loginAudit.setDbServerName(resultSet.getString("dbServerName"));
		loginAudit.setActionLevel(resultSet.getString("actionLevel"));
		loginAudit.setAuditLevel(resultSet.getString("auditLevel"));
		loginAudit.setRuleName(resultSet.getString("ruleName"));
		loginAudit.setIpAddress(resultSet.getString("clientIP"));
		loginAudit.setPort(resultSet.getString("clientPort"));
		loginAudit.setSvrIp(resultSet.getString("serverIP"));
		loginAudit.setSvrPort(resultSet.getInt("serverPort"));
		loginAudit.setLogonTime(resultSet.getTimestamp("logInTimeStamp"));
		loginAudit.setLogoffTime(resultSet.getTimestamp("logOutTimeStamp"));
		return loginAudit;
	}

	public static void main(String[] args) {
		System.out.println(UUID.randomUUID().toString());
	}

}
