package com.hzmc.dbmgr.mapper;

import com.hzmc.dbmgr.bean.CamProxy;
import org.apache.ibatis.annotations.Param;

public interface CamProxyMapper {
    int insert(CamProxy record);

    int insertSelective(CamProxy record);

    CamProxy getByDbid(Long dbid);

    int updateByDbid(CamProxy record);

    int deleteByDbid(Long dbid);

    CamProxy getByProxyIpAndProxyPort(@Param("proxyIp") String proxyIp, @Param("proxyPort") String proxyPort);

    CamProxy getByProxyPort(@Param("proxyPort") String proxyPort);

}