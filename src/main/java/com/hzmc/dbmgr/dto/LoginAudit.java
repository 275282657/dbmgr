package com.hzmc.dbmgr.dto;

import java.util.Date;

/**
 * 登陆审计
 * 
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月28日 下午3:16:28
 */
public class LoginAudit {

	private String primaryKey;

	private String id;

	private String dbname;

	private String host;

	private String dataType;// 数据类型，运维流量、业务流量

	private String fromAddress; // 数据来源

	private String cmdtype;

	private String osUser;

	private String appname;

	private String serverhost;

	private String instanceName;

	private String dbuser;

	private String dbServerName;

	private String actionLevel;// 0通过 1阻断 2模拟阻断

	private String auditLevel;

	private String ruleName;

	private String port;

	private String ipAddress;

	private String svrIp;

	private int svrPort;

	private Date logonTime;

	private Date logoffTime;

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getCmdtype() {
		return cmdtype;
	}

	public void setCmdtype(String cmdtype) {
		this.cmdtype = cmdtype;
	}

	public String getOsUser() {
		return osUser;
	}

	public void setOsUser(String osUser) {
		this.osUser = osUser;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getServerhost() {
		return serverhost;
	}

	public void setServerhost(String serverhost) {
		this.serverhost = serverhost;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getDbuser() {
		return dbuser;
	}

	public void setDbuser(String dbuser) {
		this.dbuser = dbuser;
	}

	public String getDbServerName() {
		return dbServerName;
	}

	public void setDbServerName(String dbServerName) {
		this.dbServerName = dbServerName;
	}

	public String getActionLevel() {
		return actionLevel;
	}

	public void setActionLevel(String actionLevel) {
		this.actionLevel = actionLevel;
	}

	public String getAuditLevel() {
		return auditLevel;
	}

	public void setAuditLevel(String auditLevel) {
		this.auditLevel = auditLevel;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSvrIp() {
		return svrIp;
	}

	public void setSvrIp(String svrIp) {
		this.svrIp = svrIp;
	}

	public int getSvrPort() {
		return svrPort;
	}

	public void setSvrPort(int svrPort) {
		this.svrPort = svrPort;
	}

	public Date getLogonTime() {
		return logonTime;
	}

	public void setLogonTime(Date logonTime) {
		this.logonTime = logonTime;
	}

	public Date getLogoffTime() {
		return logoffTime;
	}

	public void setLogoffTime(Date logoffTime) {
		this.logoffTime = logoffTime;
	}

}
