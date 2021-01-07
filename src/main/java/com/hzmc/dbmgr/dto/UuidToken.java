package com.hzmc.dbmgr.dto;

import java.util.Date;
import java.util.List;

/**
 * 安全客户端开启后心跳实体
 * 
 * Created by chengp on 2020年9月7日
 */
public class UuidToken {
    
    private String uuid;
    
    private List<String> ip;
    
    private Date date;

    private Integer status;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getIp() {
        return ip;
    }

    public void setIp(List<String> ip) {
        this.ip = ip;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "UuidToken [uuid=" + uuid + ", ip=" + ip + ", date=" + date + ", status=" + status + "]";
    }

}
