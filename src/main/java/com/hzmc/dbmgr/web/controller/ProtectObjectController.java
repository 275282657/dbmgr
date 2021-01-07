package com.hzmc.dbmgr.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import com.hzmc.dbmgr.service.ProtectObjectService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class ProtectObjectController {

	private final static Logger logger = LoggerFactory.getLogger(ProtectObjectController.class);

	@Autowired
	ProtectObjectService protectObjectService;

	@Autowired
	ProtectObjectGroupService protectObjectGroupService;

	/**
	 * 获取保护对象列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject", method = RequestMethod.GET)
	public ResultBean<Page> getProtectObjectList(Page page, ProtectObject protectObject) {
		if (StringUtils.isNotBlank(protectObject.getObjName()))
			page.addSearchParameter("objName", protectObject.getObjName());
		page.addSearchParameter("dbType", protectObject.getDbType());
		if (StringUtils.isNotBlank(protectObject.getIp()))
			page.addSearchParameter("ip", protectObject.getIp());
		page.addSearchParameter("status", protectObject.getStatus());
		page.addSearchParameter("groupId", protectObject.getGroupId());
		if (StringUtils.isBlank(page.getOrderField()))
			page.setOrderField(null);
		page = protectObjectService.getProtectObjectListPaged(page);
		return new ResultBean<>(page);
	}

	/**
	 * 按id获取单个保护对象
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/get/{id}", method = RequestMethod.GET)
	public ResultBean<ProtectObject> getProtectObjectById(@PathVariable int id) {
		return new ResultBean<>(protectObjectService.getProtectObjectById(id, false));
	}

	/**
	 * 新增保护对象
	 * 
	 * @param protectObject
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/create", method = RequestMethod.POST)
	public ResultBean<ProtectObject> createProtectObject(@RequestBody ProtectObject protectObject) {
		protectObjectService.createProtectObject(protectObject);
		return new ResultBean<>(protectObject);
	}

	/**
	 * 保护对象更新
	 * 
	 * @param id
	 * @param protectObject
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/update/{id}", method = RequestMethod.POST)
	public ResultBean<ProtectObject> updateProtectObject(@PathVariable int id,
			@RequestBody ProtectObject protectObject) {
		protectObject.setObjId(id);
		protectObjectService.updateProtectObject(protectObject);
		return new ResultBean<>(protectObject);
	}

	/**
	 * 删除保护对象
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/delete/{id}", method = RequestMethod.POST)
	public ResultBean deleteProtectObject(@PathVariable int id) {
		protectObjectService.deleteProtectObject(id);
		return new ResultBean();
	}

	/**
	 * 保护对象分组接口
	 * 
	 * @param body
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/group", method = RequestMethod.POST)
	public ResultBean groupProtectObject(@RequestBody JSONObject body) {
		int[] ids = null;
		Integer groupId = null;
		try {
			JSONArray a = body.getJSONArray("ids");
			ids = new int[a.size()];
			for (int i = 0; i < a.size(); i++) {
				ids[i] = a.getInt(i);
			}
			groupId = body.getInt("groupId");
		} catch (Exception e) {
			throw new RestfulException(ErrCode.PARAM_ERROR, "body:" + body);
		}

		if (Objects.isNull(ids) || Objects.isNull(groupId) || groupId < 1) {
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "ids:" + ids + " groupId:" + groupId);
		}
		protectObjectService.groupProtectObject(ids, groupId);
		return new ResultBean();
	}

    /**
     * 保护对象分组接口/全量新增（在队列里的加入不再的删除）
     * 
     * @param body
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/group/all", method = RequestMethod.POST)
    public ResultBean groupProtectObjectNew(@RequestBody JSONObject body) {
        int[] ids = null;
        Integer groupId = null;
        String description = null;
        String groupName = null;
        Integer dbType = null;
        try {
            JSONArray a = body.getJSONArray("ids");
            ids = new int[a.size()];
            for (int i = 0; i < a.size(); i++) {
                ids[i] = a.getInt(i);
            }
            if (body.containsKey("groupId")) {
                groupId = body.getInt("groupId");
            }
            if (body.containsKey("description")) {
                description = body.getString("description");
            }
            groupName = body.getString("groupName");
            dbType = body.getInt("dbType");
        } catch (Exception e) {
            throw new RestfulException(ErrCode.PARAM_ERROR, "body:" + body);
        }
        if (groupId == null) {
            ProtectObjectGroup group = new ProtectObjectGroup();
            group.setGroupName(groupName);
            group.setDescription(description);
            group.setDbType(dbType);
            group.setCreateTime(new Date());
            protectObjectGroupService.createProtectObjectGroup(group);
            groupId = group.getGroupId();
        } else {
            ProtectObjectGroup group = protectObjectGroupService.getProtectObjectGroupById(groupId);
            group.setGroupName(groupName);
            group.setDescription(description);
            group.setUpdateTime(new Date());
            if (group == null)
                throw new RestfulException(ErrCode.UNKNOWN_ID, "分组不存在");
            protectObjectGroupService.updateProtectObjectGroup(group);
        }
        protectObjectService.groupProtectObjectNew(ids, groupId);
        return new ResultBean();
    }


	/**
	 * 保护对象退组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/exitGroup/{id}", method = RequestMethod.POST)
	public ResultBean exitGroupProtectObject(@PathVariable int id) {
		protectObjectService.exitGroup(id);
		return new ResultBean();
	}

	/**
	 * 保护对象导入
	 * 
	 * @param file
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/importProtectObject", method = RequestMethod.POST)
	public ResultBean<Map<String, List<ProtectObject>>> importProtectObject(@RequestParam("file") MultipartFile file) {
		return new ResultBean<>(protectObjectService.importProtectObject(file));
	}

	/**
	 * 保护对象导出
	 * 
	 * @param list
	 * @return 导出结果
	 */
	@RequestMapping(value = "/assetProtectObject/exportProtectObject", method = RequestMethod.POST)
	public ResultBean<Map<String, String>> exportProtectObject(@RequestBody List<ProtectObject> list) {
		String uuid = protectObjectService.exportProtectObject(list);
		Map<String, String> map = new HashMap<>();
		map.put("uuid", uuid);
		return new ResultBean<>(map);
	}

	/**
	 * 下载导出的文件
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/exportProtectObject/download", method = RequestMethod.GET)
	public ResponseEntity downloadProtectObject(@RequestParam("uuid") String uuid) {
		return protectObjectService.downloadProtectObject(uuid);
	}

	/**
	 * 保护对象批量启停
	 * 
	 * @param body
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/startOrStop", method = RequestMethod.POST)
	public ResultBean startOrStopProtectObject(@RequestBody JSONObject body) {
		int[] ids = null;
		String cmd = null;
		try {
			JSONArray a = body.getJSONArray("ids");
			ids = new int[a.size()];
			for (int i = 0; i < a.size(); i++) {
				ids[i] = a.getInt(i);
			}
			cmd = body.getString("cmd");
		} catch (Exception e) {
			throw new RestfulException(ErrCode.PARAM_ERROR, "body:" + body);
		}

		if (ids == null || cmd == null || (!"start".equals(cmd) && !"stop".equals(cmd))) {
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "ids:" + ids + " cmd:" + cmd);
		}
		protectObjectService.startOrStopProtectObject(ids, cmd);
		return new ResultBean();

	}

	/**
	 * 测试服务
	 */
	@RequestMapping(value = "/assetProtectObject/connectTest", method = RequestMethod.POST)
	public ResultBean connectTest(@RequestBody ProtectObject protectObject) {
		protectObjectService.connectTest(protectObject);
		return new ResultBean<>();
	}

	/**
	 * 获取账户密码不为空且符合数据库类型的保护对象(数据脱敏专用)
	 * 
	 * @param dbType
	 * @return
	 */
	@RequestMapping(value = "/assetProtectObject/query/{dbType}", method = RequestMethod.GET)
	public ResultBean<Map<String, List<ProtectObject>>> getProtectObjectByDbType(@PathVariable int dbType) {
		Map<String, List<ProtectObject>> map = new HashMap<>();
		map.put("result", protectObjectService.queryProtectObjectByDbType(dbType));
		return new ResultBean<>(map);
	}

	/**
	 * 同步获取sqlserver动态端口
	 * 
	 * @param dbType
	 * @return
	 */
	@RequestMapping(value = "/sycSqlserverTcpPort/query/{dbType}", method = RequestMethod.GET)
	public ResultBean<Map<String, List<ProtectObject>>> sycSqlserverTcpPort(@PathVariable int dbType) {
		Map<String, List<ProtectObject>> map = new HashMap<>();
		map.put("protectObjects", protectObjectService.sycSqlserverTcpPort(dbType));
		return new ResultBean<>(map);
	}
}
