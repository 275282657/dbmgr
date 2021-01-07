package com.hzmc.dbmgr.bean;

public class CamProxy {
    /**
     * 对应保护对象的objId
     * 由于表结构不修改，所以这里用的是Long
     */
    private Long dbid;

    /**
     * 代理端口
     */
    private String proxyPort;

    /**
     * 对应的是CamDevice的id,不是CamDevice的deviceId
     */
    private Integer deviceId;

    /**
     * 不入库，用于连接查询的时候设置值
     */
    private String proxyIp;

    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }
}