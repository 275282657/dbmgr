package com.hzmc.dbmgr.dto;
/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月29日 下午5:45:21
 */
public class SqlScript {

	private String name;

	private Integer dbType;

	private String version;

	private String fileCapacity;

	private String url;

	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDbType() {
		return dbType;
	}

	public void setDbType(Integer dbType) {
		this.dbType = dbType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFileCapacity() {
		return fileCapacity;
	}

	public void setFileCapacity(String fileCapacity) {
		this.fileCapacity = fileCapacity;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
