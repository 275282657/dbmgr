package com.hzmc.dbmgr.bean;

import java.util.Date;
import java.util.List;

/**
 * 保护对象
 * @author wangsh
 *
 */
public class ProtectObject {
	/**
	 * 保护对象id
	 */
	private Integer objId;

	/**
	 * 所属分组id
	 */
	private Integer groupId;

	/**
	 * 父节点id,对集群模式下有效
	 */
	private Integer parentId;

	/**
	 * 数据库类型
	 */
	private Integer dbType;

	/**
	 * 保护对象名称
	 */
	private String objName;

	/**
	 * IP地址
	 */
	private String ip;

	/**
	 * 端口
	 */
	private Integer port;
	/**
	 * udp端口
	 */
	private Integer udpPort;

	/**
	 * 实例名
	 */
	private String instanceName;

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
	 * 运行模式
	 */
	private Integer runMode;

	/**
	 * 对于模拟和学习模式有效，模式的持续时间，单位天
	 */
	private Integer period;

	/**
	 * 代理IP
	 */
	private String proxyIp;

	/**
	 * 代理端口
	 */
	private Integer proxyPort;

	/**
	 * 数据库版本
	 */
	private String version;

	/**
	 * 状态 0停用，1启用
	 */
	private Integer status;

	/**
	 * 保护对象类型 1单库、2集群
	 * 有子节点认为是集群，没有则认为是单库
	 */
	private Integer objectType;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	/**
	 * 子节点集合
	 */
	private List<ProtectObject> nodes;

	/**
	 * 错误消息，用户批量导入保护对象的时候设置导入失败原因
	 */
	private String errMsg;

	/**
	 * 是否是动态端口
	 */
	private Integer isUdp;// 1为动态端口,反之非动态

	/**
	 * udp消息头部
	 */
	private String udpMsgHead;

    /**
     * keyTab文件路径
     */
    private String keyTab;

    /**
     * krb5.conf文件路径
     */
    private String krbConf;

    /**
     * 是否是Kerberos认证
     */
    private Integer isKrbs;

    private String keyTabValue;

    /**
     * keyTabServer文件路径
     */
    private String keyTabServer;
    
    
    /**
     * 认证方式
     */
    private Integer verificationType;

    /**
     * 认证token
     */
    private String verificationData;

    private Integer managePort;

    /**
     * 关联的zookeeper 保护对象id
     */
    private Integer zkObjId;

    /**
     * 代理域名
     */
    private String proxyDomainName;

    private ProtectObject zkProtectObject;

    /**
     * Tunnel服务的访问链接
     */
    private String tunnelUrl;

	public Integer getObjId() {
		return objId;
	}

	public void setObjId(Integer objId) {
		this.objId = objId;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getDbType() {
		return dbType;
	}

	public void setDbType(Integer dbType) {
		this.dbType = dbType;
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

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
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

	public Integer getRunMode() {
		return runMode;
	}

	public void setRunMode(Integer runMode) {
		this.runMode = runMode;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public String getProxyIp() {
		return proxyIp;
	}

	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getObjectType() {
		return objectType;
	}

	public void setObjectType(Integer objectType) {
		this.objectType = objectType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public List<ProtectObject> getNodes() {
		return nodes;
	}

	public void setNodes(List<ProtectObject> nodes) {
		this.nodes = nodes;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public Integer getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(Integer udpPort) {
		this.udpPort = udpPort;
	}

	public Integer getIsUdp() {
		return isUdp;
	}

	public void setIsUdp(Integer isUdp) {
		this.isUdp = isUdp;
	}

	public String getUdpMsgHead() {
		return udpMsgHead;
	}

	public void setUdpMsgHead(String udpMsgHead) {
		this.udpMsgHead = udpMsgHead;
	}

    public String getKeyTab() {
        return keyTab;
    }

    public void setKeyTab(String keyTab) {
        this.keyTab = keyTab;
    }

    public String getKrbConf() {
        return krbConf;
    }

    public void setKrbConf(String krbConf) {
        this.krbConf = krbConf;
    }

    public Integer getIsKrbs() {
        return isKrbs;
    }

    public void setIsKrbs(Integer isKrbs) {
        this.isKrbs = isKrbs;
    }

    public String getKeyTabValue() {
        return keyTabValue;
    }

    public void setKeyTabValue(String keyTabValue) {
        this.keyTabValue = keyTabValue;
    }

    public String getKeyTabServer() {
        return keyTabServer;
    }

    public void setKeyTabServer(String keyTabServer) {
        this.keyTabServer = keyTabServer;
    }
    
    public Integer getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(Integer verificationType) {
        this.verificationType = verificationType;
    }

    public String getVerificationData() {
        return verificationData;
    }

    public void setVerificationData(String verificationData) {
        this.verificationData = verificationData;
    }

    public Integer getManagePort() {
        return managePort;
    }

    public void setManagePort(Integer managePort) {
        this.managePort = managePort;
    }

    public String getTunnelUrl() {
        return tunnelUrl;
    }

    public void setTunnelUrl(String tunnelUrl) {
        this.tunnelUrl = tunnelUrl;
    }

    public Integer getZkObjId() {
        return zkObjId;
    }

    public void setZkObjId(Integer zkObjId) {
        this.zkObjId = zkObjId;
    }

    public String getProxyDomainName() {
        return proxyDomainName;
    }

    public void setProxyDomainName(String proxyDomainName) {
        this.proxyDomainName = proxyDomainName;
    }

    public ProtectObject getZkProtectObject() {
        return zkProtectObject;
    }

    public void setZkProtectObject(ProtectObject zkProtectObject) {
        this.zkProtectObject = zkProtectObject;
    }

    @Override
	public String toString() {
		return "ProtectObject{" +
				"objId=" + objId +
				", groupId=" + groupId +
				", parentId=" + parentId +
				", dbType=" + dbType +
				", objName='" + objName + '\'' +
				", ip='" + ip + '\'' +
				", port=" + port +
				", instanceName='" + instanceName + '\'' +
				", serviceName='" + serviceName + '\'' +
				", dbUser='" + dbUser + '\'' +
				", dbPassword='" + dbPassword + '\'' +
				", runMode=" + runMode +
				", period=" + period +
				", proxyIp='" + proxyIp + '\'' +
				", proxyPort=" + proxyPort +
				", status=" + status +
				", objectType=" + objectType +
				", createTime=" + createTime +
				", updateTime=" + updateTime +
				", nodes=" + nodes +
				",verificationType" + verificationType +
				",verificationData" + verificationData +
				", errMsg='" + errMsg + '\'' +
				'}';
	}
}
