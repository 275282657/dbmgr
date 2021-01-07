package com.hzmc.dbmgr.bean;

import java.util.Date;

/**
 * 保护对象分组
 * @author wangsh
 *
 */
public class ProtectObjectGroup {
	/**
	 * 保护对象分组id
	 */
	private Integer groupId;

	/**
	 * 分组名称
	 */
	private String groupName;

	/**
	 * 分组数据库类型
	 */
	private Integer dbType;

	/**
	 * 分组描述
	 */
	private String description;

	private Date createTime;

	private Date updateTime;

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getDbType() {
		return dbType;
	}

	public void setDbType(Integer dbType) {
		this.dbType = dbType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "ProtectObjectGroup{" +
				"groupId=" + groupId +
				", groupName='" + groupName + '\'' +
				", dbType=" + dbType +
				", description='" + description + '\'' +
				", createTime=" + createTime +
				", updateTime=" + updateTime +
				'}';
	}
}
