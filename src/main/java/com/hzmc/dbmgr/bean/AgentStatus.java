package com.hzmc.dbmgr.bean;

import java.util.Date;

/**
 * 准入
 * 
 * 版权所有：美创科技 创建者: gpchen 创建日期: 2019年10月14日 下午3:10:18
 */
public class AgentStatus {

	/**
	 * 保护对象id
	 */
	private Integer objId;

	/**
	 * 保护对象名称
	 */
	private String objName;

	/**
	 * 保护对象代理ip
	 */
	private String ip;

	/**
	 * 保护对象端口
	 */
	private Integer port;

	/**
	 * 状态 0未安装,1启用,2模拟启用,3停用,-1异常
	 */
	private Integer status;

	/**
	 * 数据库类型
	 */
	private Integer dbType;

	/**
	 * 服务名/库名
	 */
	private String serviceName;

	/**
	 * 数据库用户名
	 */
	private String dbUser;

	/**
	 * 数据库密码
	 */
	private String dbPassword;

	/**
	 * 状态信息
	 */
	private String message;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	/**
	 * 保护对象代理ip
	 */
	private String poxyIp;

    private String haIp;

    private String haServiceHost;

	private String objIds;

	public Integer getObjId() {
		return objId;
	}

	public void setObjId(Integer objId) {
		this.objId = objId;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getDbType() {
		return dbType;
	}

	public void setDbType(Integer dbType) {
		this.dbType = dbType;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getObjIds() {
		return objIds;
	}

	public void setObjIds(String objIds) {
		this.objIds = objIds;
	}

	public String getPoxyIp() {
		return poxyIp;
	}

	public void setPoxyIp(String poxyIp) {
		this.poxyIp = poxyIp;
	}

    public String getHaIp() {
        return haIp;
    }

    public void setHaIp(String haIp) {
        this.haIp = haIp;
    }

    public String getHaServiceHost() {
        return haServiceHost;
    }

    public void setHaServiceHost(String haServiceHost) {
        this.haServiceHost = haServiceHost;
    }

    @Override
	public String toString() {
		return "AgentStatus [objId=" + objId + ", objName=" + objName + ", ip=" + ip + ", port=" + port + ", status="
				+ status + ", dbType=" + dbType + ", serviceName=" + serviceName + ", dbUser=" + dbUser
				+ ", dbPassword=" + dbPassword + ", message=" + message + ", createTime=" + createTime + ", updateTime="
				+ updateTime + "]";
	}
}
