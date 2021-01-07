package com.hzmc.dbmgr.web.controller;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.bean.LoginWhiteList;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.AgentStatusEnum;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.dto.LoginAudit;
import com.hzmc.dbmgr.dto.SqlScript;
import com.hzmc.dbmgr.dto.UuidToken;
import com.hzmc.dbmgr.mapper.AgentAppNameMapper;
import com.hzmc.dbmgr.service.AgentAppNameService;
import com.hzmc.dbmgr.service.AgentStatusService;
import com.hzmc.dbmgr.service.LoginAuditService;
import com.hzmc.dbmgr.service.LoginWhiteListSevice;
import com.hzmc.dbmgr.util.DruidDataSourceUtil;

/**
 * 准入
 * 
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 下午2:38:37
 */
@RestController
public class AgentStatusController {

	private static final Logger logger = LoggerFactory.getLogger(AgentStatusController.class);

	@Autowired
	private AgentStatusService agentStatusService;

	@Autowired
	private LoginWhiteListSevice loginWhiteListSevice;

	@Autowired
	private LoginAuditService loginAuditService;

	@Autowired
	private AgentAppNameService agentAppNameService;

    @Autowired
    private AgentAppNameMapper agentAppNameMapper;

	@Value("${agentDb.fileUrl}")
	private String file_url;

	@Value("${agentDb.fileAllUrl}")
	private String fileAllUrl;

	/**
	 * 获取准入列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentStatus/all", method = RequestMethod.GET)
	public ResultBean<Page> getPageAgentStatus(Page page, AgentStatus agentStatus) {
		if (agentStatus.getObjId() != null)
			page.addSearchParameter("objId", agentStatus.getObjId());
		if (StringUtils.isBlank(page.getOrderField()))
			page.setOrderField(null);
		ResultBean<Page> result = agentStatusService.selectPage(page);
		return result;
	}

	/**
	 * 获取启用或者模拟启用的准入列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentStatus/run", method = RequestMethod.GET)
	public ResultBean getRunningAgentStatus() {
		List<AgentStatus> list = agentStatusService.selectRunningAgentStatus();
		Map<String, List<AgentStatus>> map = new HashMap<>();
		map.put("RunningAgentStatus", list);
		return new ResultBean(map);
	}

	/**
	 * 启用,模拟启用,停用,检查连接
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentStatus/set", method = RequestMethod.POST)
	public ResultBean setAgentStatus(@RequestBody AgentStatus agentStatus) {
		List<Integer> objIds = JSON.parseArray(agentStatus.getObjIds(), Integer.class);
		for (Integer objId : objIds) {
            agentStatusService.startAndStopAgentStatus(objId, agentStatus.getStatus(), agentStatus.getPoxyIp(),
                agentStatus.getHaIp(), agentStatus.getHaServiceHost());
		}
		return new ResultBean();
	}

	/**
	 * 断开连接
	 * 
	 * @param agentStatus
	 * @return
	 */
	@RequestMapping(value = "/agentStatus/removeConnect/{objId}", method = RequestMethod.POST)
	public ResultBean agentStatusRemovConnect(@PathVariable int objId) {
		AgentStatus agentStatus = agentStatusService.selectByObjId(objId);
		if (agentStatus == null) {
			throw new RestfulException(ErrCode.AGENT_SQL_NO_RUN, ErrCode.AGENT_SQL_NO_RUN.getMessage());
		}
		agentStatusService.deleteAgentStatusByObjId(objId);
		return new ResultBean<>();
	}


	/**
	 * 新增修改复制应用名称确认按钮
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentAppName/confirm", method = RequestMethod.POST)
	public ResultBean confirmAgentAppName(@RequestBody AgentAppName agentAppName) {
		// 新增
		if (StringUtils.equalsIgnoreCase(agentAppName.getType(), "insert")
				|| StringUtils.equalsIgnoreCase(agentAppName.getType(), "copy")) {
			List<String> appNames = agentAppName.getAppNameList();
			if (appNames == null || appNames.size() == 0) {
				throw new RestfulException(ErrCode.APP_NAME_NULL, ErrCode.APP_NAME_NULL.getMessage());
			}
			for (String appName : appNames) {
				String appId = UUID.randomUUID().toString().replaceAll("-", "");
				agentAppName.setAppName(appName);
				agentAppNameService.copyAgentAppName(agentAppName, appId, "insert");
				agentAppName.setId(appId);
				agentAppName.setStatus(null);
				agentAppName.setType("copy");
				agentAppNameService.updateAgentAppName(agentAppName);
			}
		} else if (StringUtils.equalsIgnoreCase(agentAppName.getType(), "update")) {
			agentAppName.setStatus(null);
			agentAppNameService.updateAgentAppName(agentAppName);
		} else {
			throw new RestfulException(ErrCode.OPERATE_NULL, ErrCode.OPERATE_NULL.getMessage());
		}
		return new ResultBean();
	}

	/**
	 * 复制白名单
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentAppName/copy", method = RequestMethod.POST)
	public ResultBean copyAgentAppName(@RequestBody AgentAppName agentAppName) {
		String appId = UUID.randomUUID().toString().replaceAll("-", "");
		agentAppNameService.copyAgentAppName(agentAppName, appId, "copy");
		Map<String, String> map = new HashMap<>();
		map.put("appId", appId);
		return new ResultBean(map);
	}

	/**
	 * 修改应用名称
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentAppName/update", method = RequestMethod.POST)
	public ResultBean updateAgentAppName(@RequestBody AgentAppName agentAppName) {
		agentAppNameService.updateAgentAppName(agentAppName);
		return new ResultBean();
	}

	/**
	 * 根据id删除应用名称
	 * 
	 * @return
	 */
	@RequestMapping(value = "/agentAppName/delete", method = RequestMethod.POST)
	public ResultBean deleteLoginWhiteByAppName(@RequestBody LoginWhiteList loginWhiteList) {
		loginWhiteListSevice.deleteLoginWhiteByAppName(loginWhiteList.getAppId());
		return new ResultBean();
	}


	/**
	 * 新增白名单
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginWhiteList/insert", method = RequestMethod.POST)
	public ResultBean insertLoginWhite(@RequestBody LoginWhiteList loginWhiteList) {
		DruidPooledConnection connection = null;
		try {
			connection = DruidDataSourceUtil.getInstance()
					.getConnection(loginWhiteList.getObjId());
		} catch (SQLException e) {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
			throw new RestfulException(ErrCode.GET_CONNECTION_ERROR, ErrCode.GET_CONNECTION_ERROR.getMessage());
		}
		Integer id = loginWhiteListSevice.insertLoginWhite(loginWhiteList, connection, 0);
		Map<String, Integer> map = new HashMap<>();
		map.put("id", id);
		DruidDataSourceUtil.getInstance().closeConnection(connection);
		return new ResultBean(map);
	}


	/**
	 * 根据id删除白名单
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginWhiteList/delete/{id}", method = RequestMethod.POST)
	public ResultBean deleteLoginWhiteById(@PathVariable int id) {
		loginWhiteListSevice.deleteLoginWhiteById(id);
		return new ResultBean();
	}

	/**
	 * 修改白名单
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginWhiteList/update", method = RequestMethod.POST)
	public ResultBean updateLoginWhite(@RequestBody LoginWhiteList loginWhiteList) {
		loginWhiteListSevice.updateLoginWhite(loginWhiteList);
		return new ResultBean();
	}

	/**
	 * 分页查询应用程序名
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginWhiteList/page/appName", method = RequestMethod.GET)
	public ResultBean getLoginWhiteListPage(Page page, AgentAppName agentAppName) {
		if (agentAppName.getObjId() == null) {
			throw new RestfulException(ErrCode.APP_NAME_ID_NULL, ErrCode.APP_NAME_ID_NULL.getMessage());
		} else {
			page.addSearchParameter("objId", agentAppName.getObjId());
		}
		if (StringUtils.isNotBlank(agentAppName.getAppName()))
			page.addSearchParameter("appName", agentAppName.getAppName());
		if (StringUtils.isBlank(page.getOrderField()))
			page.setOrderField(null);
		ResultBean result = agentAppNameService.pageAgentAppName(page);
		return result;
	}

	/**
	 * 根据应用程序名查询ip
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginWhiteList/page/ip", method = RequestMethod.GET)
	public ResultBean<Page> getLoginWhiteListPageIp(Page page, LoginWhiteList loginWhiteList) {
		page.setCurrentPage(1);
		page.setPageSize(100);
		String type = loginWhiteList.getType();
		if (StringUtils.isBlank(type)) {
			throw new RestfulException(ErrCode.OPERATE_NULL, ErrCode.OPERATE_NULL.getMessage());
		}
		if (loginWhiteList.getObjId() == null) {
			throw new RestfulException(ErrCode.PARAM_ERROR, ErrCode.OBJ_ID.getMessage());
		}
		// 新增
		if (StringUtils.equalsIgnoreCase(type, "insert")) {
			Map<String, Object> map = new HashMap<>();
			map.put("appId", UUID.randomUUID().toString().replaceAll("-", ""));
			map.put("type", type);
			map.put("appName", null);
			map.put("items", page.getItems());
			return new ResultBean(map);
		}
		if (loginWhiteList.getAppId() == null) {
			throw new RestfulException(ErrCode.APP_NAME_ID_NULL, ErrCode.APP_NAME_ID_NULL.getMessage());
		}
		// 修改
		if (StringUtils.equalsIgnoreCase(type, "update")) {
			page.addSearchParameter("appId", loginWhiteList.getAppId());
			page.addSearchParameter("type", loginWhiteList.getType());
			if (StringUtils.isBlank(page.getOrderField()))
				page.setOrderField(null);
			return loginWhiteListSevice.selectLoginWhiteByAppName(page);
		} else if (StringUtils.equalsIgnoreCase(type, "copy")) {
			AgentAppName agentAppName = new AgentAppName();
			String appId = UUID.randomUUID().toString().replaceAll("-", "");
			agentAppName.setId(loginWhiteList.getAppId());
			agentAppName.setObjId(loginWhiteList.getObjId());
			// 先复制
			agentAppNameService.copyAgentAppName(agentAppName, appId, "copy");
			page.addSearchParameter("appId", appId);
			page.addSearchParameter("type", loginWhiteList.getType());
			return loginWhiteListSevice.selectLoginWhiteByAppName(page);
		} else {
			throw new RestfulException(ErrCode.OPERATE_NULL, ErrCode.OPERATE_NULL.getMessage());
		}
	}

	/**
	 * 查询生产库所有应用名称
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loginWhiteList/db/appName", method = RequestMethod.GET)
    public ResultBean getDbAllAppName(Integer objId, String type, String name) {
		if (objId == null) {
			throw new RestfulException(ErrCode.OBJ_ID, ErrCode.OBJ_ID.getMessage());
		}
        List<String> result = loginWhiteListSevice.queryAppName(objId, type, name);
		Map<String, Object> result_map = new HashMap<>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int tmp = 1;
		for (String appName : result) {
            if (tmp > 50) {
                break;
            }
			Map<String, Object> map = new HashMap<>();
			map.put("label", appName);
			map.put("value", appName);
			list.add(map);
            tmp++;
		}
		result_map.put("appName", list);
		return new ResultBean<>(result_map);
	}

	/**
	 * 获取登录审计
	 * 
	 * @param objId
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "/loginAudit/db", method = RequestMethod.GET)
	public ResultBean getLoginAudit(Integer objId, String dbName) {
		List<LoginAudit> list = loginAuditService.getLoginAudit(objId, dbName);
		Map<String, List<LoginAudit>> map=new HashMap<>();
		map.put("LoginAudit", list);
		return new ResultBean<>(map);
	}

	/**
	 * 修改代理ip
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	@RequestMapping(value = "/poxyIp/update", method = RequestMethod.POST)
	public ResultBean updatePoxyIp(@RequestBody LoginWhiteList loginWhiteList) {
		loginWhiteListSevice.updatePoxyIp(loginWhiteList);
		return new ResultBean<>();
	}

	/**
	 * 获取DB探针脚本详细信息
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	@RequestMapping(value = "/agentStatus/script/{objType}", method = RequestMethod.GET)
	public ResultBean getSriptDeatail(@PathVariable Integer objType) {
		SqlScript sqlScript = new SqlScript();
		sqlScript.setDescription("文件下载后参考readme安装脚本");
		sqlScript.setVersion("v1.0");
		sqlScript.setDbType(objType);
		if (DBEnum.SQLSERVER.getNumber() == objType) {
			File file = new File(fileAllUrl + "sqlserver.zip");
			if (!file.exists()) {
				throw new RestfulException(ErrCode.TRANSFER_ERROR, ErrCode.TRANSFER_ERROR.getMessage());
			}
			sqlScript.setName("sqlserver.zip");
			sqlScript.setFileCapacity(getPrintSize(file.length()));
			sqlScript.setUrl(file_url + "sqlserver.zip");
		}
		if (DBEnum.ORACLE.getNumber() == objType) {
			File file = new File(fileAllUrl + "oracle.zip");
			if (!file.exists()) {
				throw new RestfulException(ErrCode.FILE_NULL, ErrCode.FILE_NULL.getMessage());
			}
			sqlScript.setName("oracle.zip");
			sqlScript.setFileCapacity(getPrintSize(file.length()));
			sqlScript.setUrl(file_url + "oracle.zip");
		}
		return new ResultBean<>(sqlScript);
	}

	/**
	 * 非反向代理关闭DB探针
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	@RequestMapping(value = "/dbStatus/update/{status}", method = RequestMethod.POST)
	public ResultBean updateDBStatus(@PathVariable Integer status) {
		agentStatusService.updateDBStatus(status);
		return new ResultBean<>();
	}

	/**
	 * 同步DB探针是否报错
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	@RequestMapping(value = "/dbStatus/synchronous/", method = RequestMethod.POST)
	public ResultBean synchronousAgentStatus() {
		// 同步DB探针状态
		agentStatusService.synchronousAgentStatus();
		// 同步丢失的白名单
		List<AgentStatus> list = agentStatusService.selectRunningAgentStatus();
		for (AgentStatus agentStatus : list) {
			loginWhiteListSevice.sycLoginWhiteList(agentStatus.getObjId());
		}
		return new ResultBean<>();
	}

    /**
     * 同步CSCS白名单入DB探针
     * 
     * @param loginWhiteList
     * @return
     */
    @RequestMapping(value = "/agent/cscs/synchronous/", method = RequestMethod.POST)
    public void synchronousCscsAgentStatus(@RequestBody UuidToken uuidToken) {
        List<AgentStatus> list = agentStatusService.selectRunningAgentStatus();
        AgentAppName agentAppName = new AgentAppName();
        agentAppName.setAppName("*");
        String appId = null;
        for (AgentStatus agentStatus : list) {
            if (agentStatus.getStatus() == AgentStatusEnum.ERROR.getNumber()) {
                continue;
            }
            agentAppName.setObjId(agentStatus.getObjId());
            appId = agentAppNameMapper.selectAgentAppNameByName(agentAppName);
            if (StringUtils.isBlank(appId)) {
                agentAppName = getAgentAppName(agentStatus.getObjId());
                agentAppNameMapper.insertAgentAppName(agentAppName);
                appId = agentAppName.getId();
            }
            loginWhiteListSevice.sycCscsWhiteList(agentStatus.getObjId(), uuidToken, appId);
        }
    }

    public static void main(String[] args) {
        UuidToken token = new UuidToken();
        token.setUuid("1");
        List<String> ips = new ArrayList<>();
        ips.add("1.2.3.4");
        ips.add("2.3.4.5");
        token.setIp(ips);
        System.out.println(JSONObject.toJSONString(token));
    }


    private AgentAppName getAgentAppName(Integer objId) {
        AgentAppName agentAppName = new AgentAppName();
        agentAppName.setAppName("*");
        agentAppName.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        agentAppName.setCreateTime(new Date());
        agentAppName.setUpdateTime(new Date());
        agentAppName.setObjId(objId);
        return agentAppName;
    }


	private static String getPrintSize(long size) {
		// 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
		double value = (double) size;
		if (value < 1024) {
			return String.valueOf(value) + "B";
		} else {
			value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
		}
		// 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
		// 因为还没有到达要使用另一个单位的时候
		// 接下去以此类推
		if (value < 1024) {
			return String.valueOf(value) + "KB";
		} else {
			value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
		}
		if (value < 1024) {
			return String.valueOf(value) + "MB";
		} else {
			// 否则如果要以GB为单位的，先除于1024再作同样的处理
			value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
			return String.valueOf(value) + "GB";
		}
	}

}
