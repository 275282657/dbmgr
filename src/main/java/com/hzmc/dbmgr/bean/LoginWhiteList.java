package com.hzmc.dbmgr.bean;

import java.util.Date;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 上午8:54:50
 */
public class LoginWhiteList {

	private Integer id;

	/**
	 * 应用名称id
	 */
	private String appId;

	/**
	 * 保护对象id
	 */
	private Integer objId;

	/**
	 * 0表示单个ip,1表示通配符,2表示地址段
	 */
	private Integer ipMode;

	/**
	 * ip或ip地址段开始
	 */
	private String ip;

	/**
	 * IP地址段结束
	 */
	private String ipEnd;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 代理ip标志
	 */
	private String poxyLogo;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	/**
	 * 类型：新增,修改,复制
	 */
	private String type;

    /**
     * HA ip
     */
    private String haIp;

    private String haServiceHost;

    private String tokenId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getObjId() {
		return objId;
	}

	public void setObjId(Integer objId) {
		this.objId = objId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Integer getIpMode() {
		return ipMode;
	}

	public void setIpMode(Integer ipMode) {
		this.ipMode = ipMode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIpEnd() {
		return ipEnd;
	}

	public void setIpEnd(String ipEnd) {
		this.ipEnd = ipEnd;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPoxyLogo() {
		return poxyLogo;
	}

	public void setPoxyLogo(String poxyLogo) {
		this.poxyLogo = poxyLogo;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
