package com.hzmc.dbmgr.bean;

import org.apache.commons.lang.StringUtils;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年7月2日 上午10:36:13
 */
public class SchemaTables {

	private String serverName;// 库名

	private String schemaName;// schema

	private String tableName;// 表名

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		if (StringUtils.isNotBlank(serverName)) {
			serverName = serverName.trim();
		}
		this.serverName = serverName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		if (StringUtils.isNotBlank(schemaName)) {
			schemaName = schemaName.trim();
		}
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
