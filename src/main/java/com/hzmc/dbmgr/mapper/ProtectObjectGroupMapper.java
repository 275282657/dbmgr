package com.hzmc.dbmgr.mapper;

import java.util.List;

import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;

public interface ProtectObjectGroupMapper {
    int deleteByPrimaryKey(Integer groupId);

    int insert(ProtectObjectGroup record);

    int insertSelective(ProtectObjectGroup record);

    ProtectObjectGroup selectByPrimaryKey(Integer groupId);

    int updateByPrimaryKeySelective(ProtectObjectGroup record);

    int updateByPrimaryKey(ProtectObjectGroup record);

    /**
     * 从表mc$asset_db_groups中获取groupName为#{groupName}的记录
     * @param groupName
     * @return
     */
    ProtectObjectGroup getProtectObjectGroupByName(String groupName);

    List<ProtectObjectGroup> getAll();

    /**
     * 从表mc$asset_db_groups中按page查询分组数量
     * @param page
     * @return
     */
    int getGroupCountByPage(Page page);

    /**
     * 从表mc$asset_db_groups中按page获取分组列表
     * @param page
     * @return
     */
    List<ProtectObjectGroup> getProtectObjectGroupListByPage(Page page);
}