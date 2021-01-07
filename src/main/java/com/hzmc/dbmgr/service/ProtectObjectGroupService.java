package com.hzmc.dbmgr.service;

import java.util.List;

import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;

public interface ProtectObjectGroupService {

	Page getProtectObjectGroupList(Page page);

	/**
	 * 创建一个保护对象分组
	 * @param protectObjectGroup
	 * @return 
	 */
	Boolean createProtectObjectGroup(ProtectObjectGroup protectObjectGroup);

	/**
	 * 更新某条分组，若更改了数据库类型且分组内有数据库，会更新失败
	 * @param protectObjectGroup
	 * @return 
	 */
	Boolean updateProtectObjectGroup(ProtectObjectGroup protectObjectGroup);

	/**
	 * 删除分组,并将其下所有保护对象退组
	 * @param ids
	 */
	Boolean deleteProtectObjectGroup(String[] ids);

	/**
	 * 按id获取一个分组
	 * @param id
	 * @return
	 */
	ProtectObjectGroup getProtectObjectGroupById(Integer id);

	/**
	 * 分组的外部API接口
	 * @return
	 */
	List<ProtectObjectGroup> getProtectObjectGroupListPublic();

	/**
	 * 按id删除单个分组,并将其下所有保护对象退组
	 * @param id
	 */
	Boolean deleteProtectObjectGroup(Integer id);

}
