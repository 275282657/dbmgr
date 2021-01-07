package com.hzmc.dbmgr.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hzmc.dbmgr.bean.CamDevice;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.bean.PublicData;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.mapper.CamDeviceMapper;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import com.hzmc.dbmgr.service.PublicDataService;

@RestController
public class PublicDataController {
	
	@Autowired
	PublicDataService publicDataService;

	@Autowired
	ProtectObjectGroupService protectObjectGroupService;

	@Autowired
	CamDeviceMapper camDeviceMapper;

	@RequestMapping(value = "/assetProtectObject/dbtypes", method = RequestMethod.GET)
    public ResultBean<Map<String,List<PublicData>>> getDBTypeList() {
		Map<String,List<PublicData>> map = new HashMap<>();
		map.put("dbtypes",publicDataService.getDBTypeList());
		return new ResultBean<>(map);
	}
	
	@RequestMapping(value = "/assetProtectObject/runmodes", method = RequestMethod.GET)
    public ResultBean<Map<String,List<PublicData>>> getRunModesList() {
		Map<String,List<PublicData>> map = new HashMap<>();
		map.put("runmodes",publicDataService.getRunModesList());
		return new ResultBean<>(map);
	}
	
	@RequestMapping(value = "/assetProtectObject/status", method = RequestMethod.GET)
    public ResultBean<Map<String,List<PublicData>>> getStatusList() {
		Map<String,List<PublicData>> map = new HashMap<>();
		map.put("status",publicDataService.getStatusList());
		return new ResultBean<>(map);
	}

	@RequestMapping(value = "/assetProtectObject/groups", method = RequestMethod.GET)
    public ResultBean<Map<String,List<ProtectObjectGroup>>> getGroupList() {
		Map<String,List<ProtectObjectGroup>> map = new HashMap<>();
		map.put("groups",protectObjectGroupService.getProtectObjectGroupListPublic());
		return new ResultBean<>(map);
	}

	@RequestMapping(value = "/assetProtectObject/devices", method = RequestMethod.GET)
	public ResultBean<Map<String, List<CamDevice>>> getCamDeviceList() {
		Map<String, List<CamDevice>> map = new HashMap<>();
		map.put("devices", camDeviceMapper.getAll());
		return new ResultBean<>(map);
	}
}
