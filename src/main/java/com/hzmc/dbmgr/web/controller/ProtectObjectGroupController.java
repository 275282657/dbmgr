package com.hzmc.dbmgr.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.mapper.ProtectObjectMapper;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;

@RestController
public class ProtectObjectGroupController {
	
	@Autowired
	ProtectObjectGroupService protectObjectGroupService;
	
    @Autowired
    ProtectObjectMapper protectObjectMapper;

	/**
	 * 获取分组列表（带查询条件分页）
	 * @return
	 */
    @RequestMapping(value = "/assetProtectObject/getGroups", method = RequestMethod.GET)
    public ResultBean<Page> getProtectObjectGroupList(Page page, String groupName, Integer dbType, Long createTime) {
        Long startTime = null;
        Long endTime = null;
        if (createTime != null) {
            startTime = createTime - (createTime + 28800000L) % 86400000L;
            endTime = createTime - (createTime + 28800000L) % 86400000L + 86400000L;
        }
        //设置查询条件
		if (StringUtils.isNotBlank(groupName))
			page.addSearchParameter("groupName", groupName);
        page.addSearchParameter("dbType", dbType);
        if (startTime != null)
            page.addSearchParameter("startTime", new Date(startTime));
        if (endTime != null)
            page.addSearchParameter("endTime", new Date(endTime));
        page = protectObjectGroupService.getProtectObjectGroupList(page);
        return new ResultBean<>(page);
    }

	/**
	 * 按照id获取单个分组信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/group/{id}", method = RequestMethod.GET)
    public ResultBean<ProtectObjectGroup> getGroupByid(@PathVariable int id) {
		return new ResultBean<>(protectObjectGroupService.getProtectObjectGroupById(id));
	}
	
	/**
	 * 新建分组
	 * @param protectObjectGroup
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/group/create", method = RequestMethod.POST)
    public ResultBean createProtectObjectGroup(@RequestBody ProtectObjectGroup protectObjectGroup) {
		protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
		return new ResultBean();
	}

	/**
	 * 更新分组
	 * @param id
	 * @param protectObjectGroup
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/group/update/{id}", method = RequestMethod.POST)
    public ResultBean updateProtectObjectGroup(@PathVariable int id, @RequestBody ProtectObjectGroup protectObjectGroup) {
		protectObjectGroup.setGroupId(id);
		protectObjectGroupService.updateProtectObjectGroup(protectObjectGroup);
		return new ResultBean();
	}

	/**
	 * 删除分组id 为用逗号分隔的 id集合 如1,2,3
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/group/delete/{id}", method = RequestMethod.POST)
    public ResultBean deleteProtectObjectGroup(@PathVariable String id) {
		String[] ids = id.split(",");
		try{
			for(String s:ids)
				Integer.parseInt(s);
		}catch(Exception e){
			throw new RestfulException(ErrCode.PARAM_ERROR,"id :" + id);
		}
		protectObjectGroupService.deleteProtectObjectGroup(ids);
		return new ResultBean();
	}

    /**
     * 根据分组ID查询保护对象
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/group/select/{id}", method = RequestMethod.GET)
    public ResultBean getProtectObjectByGroupById(@PathVariable int id) {
        List<ProtectObject> list = protectObjectMapper.getByGroupId(id);
        Map<String, List<ProtectObject>> map = new HashMap<>();
        map.put("protectObjects", list);
        return new ResultBean<>(map);
    }
}
