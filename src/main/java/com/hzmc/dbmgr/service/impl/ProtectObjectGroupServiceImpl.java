package com.hzmc.dbmgr.service.impl;

import java.util.List;

import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.mapper.ProtectObjectGroupMapper;
import com.hzmc.dbmgr.mapper.ProtectObjectMapper;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProtectObjectGroupServiceImpl implements ProtectObjectGroupService {
	
	private static final Logger logger = LoggerFactory.getLogger(ProtectObjectGroupServiceImpl.class);

	@Autowired
	ProtectObjectGroupMapper protectObjectGroupMapper;
	@Autowired
	ProtectObjectMapper protectObjectMapper;
	
	@Override
	public Page getProtectObjectGroupList(Page page) {

		int count = protectObjectGroupMapper.getGroupCountByPage(page);
		List<ProtectObjectGroup> list = protectObjectGroupMapper.getProtectObjectGroupListByPage(page);
		page.setTotalCount(count);
		page.setItems((List)list);
		return page;
	}

	@Transactional
	@Override
	public Boolean createProtectObjectGroup(ProtectObjectGroup protectObjectGroup) {
		checkGroupParam(protectObjectGroup);
		checkGroupNameUnique(protectObjectGroup);
		logger.info("add protectObjectGroup:" + protectObjectGroup);
		protectObjectGroupMapper.insertSelective(protectObjectGroup);
		logger.info("add protectObjectGroup successfully, id:{}", protectObjectGroup.getGroupId());
		return true;
	}

	@Override
	@Transactional
	public Boolean updateProtectObjectGroup(ProtectObjectGroup protectObjectGroup) {
		ProtectObjectGroup oldProtectObjectGroup = protectObjectGroupMapper.selectByPrimaryKey(protectObjectGroup.getGroupId());
		if(oldProtectObjectGroup == null)
			throw new RestfulException(ErrCode.UNKNOWN_ID);
		checkGroupParam(protectObjectGroup);
		checkGroupNameUnique(protectObjectGroup);
		//分组内有成员的时候，分组数据库类型不能变更
		checkDbType(protectObjectGroup,oldProtectObjectGroup);
		logger.info("update protectObjectGroup:" + protectObjectGroup);
		protectObjectGroupMapper.updateByPrimaryKeySelective(protectObjectGroup);
		logger.info("update protectObjectGroup successfully, id:{}", protectObjectGroup.getGroupId());
		return true;
	}

	@Override
	@Transactional
	public Boolean deleteProtectObjectGroup(String[] ids) {
		for(String id :ids)
			deleteProtectObjectGroup(Integer.parseInt(id));
		return true;
	}

	@Transactional
	@Override
	public Boolean deleteProtectObjectGroup(Integer id) {
		if (protectObjectGroupMapper.selectByPrimaryKey(id) == null)
			throw new RestfulException(ErrCode.UNKNOWN_ID);
		protectObjectGroupMapper.deleteByPrimaryKey(id);
		//把原来的组员退组，即把group_id置为0
		List<ProtectObject> objectsInFolder = protectObjectMapper.getByGroupId(id);
		for(ProtectObject i:objectsInFolder) {
			i.setGroupId(0);
			protectObjectMapper.updateByPrimaryKeySelective(i);
		}
		logger.info("delete protectObjectGroup successfully, id:{}", id);
		return true;
	}

	@Override
	public ProtectObjectGroup getProtectObjectGroupById(Integer id) {
		return protectObjectGroupMapper.selectByPrimaryKey(id);
	}

	@Override
	public List<ProtectObjectGroup> getProtectObjectGroupListPublic() {
		return protectObjectGroupMapper.getAll();
	}

	/**
	 * 判断分组的参数是否合法
	 * @param protectObjectGroup
	 */
	private void checkGroupParam(ProtectObjectGroup protectObjectGroup) {
		if (StringUtils.isBlank(protectObjectGroup.getGroupName()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "分组名称为空");
		if (protectObjectGroup.getDbType() == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库类型为空");
		else if (!DBEnum.isValidDbType(protectObjectGroup.getDbType()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库类型错误:" + protectObjectGroup.getDbType());
	}

	/**
	 * 判断分组名是否重复
	 * @param protectObjectGroup
	 */
	private void checkGroupNameUnique(ProtectObjectGroup protectObjectGroup) {
		ProtectObjectGroup group = protectObjectGroupMapper.getProtectObjectGroupByName((protectObjectGroup.getGroupName()));
		if(group != null){
			if(protectObjectGroup.getGroupId() == null)  //新增分组的时候还没有id
				throw new RestfulException(ErrCode.EXIST_OBJECT,"分组名称已存在");
			if(protectObjectGroup.getGroupId().intValue() != group.getGroupId().intValue())  //更新分组的时候，排除与自己相同
				throw new RestfulException(ErrCode.EXIST_OBJECT,"分组名称已存在");
		}
	}

	/**
	 * 判断分组的数据库类型是否可以变更
	 * @param protectObjectGroup
	 * @param oldProtectObjectGroup
	 */
	private void checkDbType(ProtectObjectGroup protectObjectGroup,ProtectObjectGroup oldProtectObjectGroup) {
		if(protectObjectGroup.getDbType().intValue() == oldProtectObjectGroup.getDbType().intValue())  //数据库类型没有发生变化
			return;
		List<ProtectObject> objectsInFolder = protectObjectMapper.getByGroupId(protectObjectGroup.getGroupId());
		if(objectsInFolder.size() > 0)   //如果分组里面已经有成员了，则不能变更数据库类型
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "分组内已有成员，无法修改数据库类型");
	}
}
