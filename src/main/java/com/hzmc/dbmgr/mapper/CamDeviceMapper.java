package com.hzmc.dbmgr.mapper;

import com.hzmc.dbmgr.bean.CamDevice;

import java.util.List;

public interface CamDeviceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CamDevice record);

    int insertSelective(CamDevice record);

    CamDevice selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CamDevice record);

    int updateByPrimaryKey(CamDevice record);

    List<CamDevice> getAll();

    CamDevice getByProxyIp(String proxyIp);
}