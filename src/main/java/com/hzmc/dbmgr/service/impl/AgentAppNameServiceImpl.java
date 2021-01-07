package com.hzmc.dbmgr.service.impl;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.bean.LoginWhiteList;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.mapper.AgentAppNameMapper;
import com.hzmc.dbmgr.mapper.LoginWhiteListMapper;
import com.hzmc.dbmgr.service.AgentAppNameService;
import com.hzmc.dbmgr.service.LoginWhiteListSevice;
import com.hzmc.dbmgr.util.DruidDataSourceUtil;

/**
 * 版权所有：美创科技 创建者: gpchen 创建日期: 2019年11月4日 下午4:03:05
 */
@Service
public class AgentAppNameServiceImpl implements AgentAppNameService {
    private static final Logger logger = LoggerFactory.getLogger(AgentAppNameServiceImpl.class);

	@Autowired
	private AgentAppNameMapper agentAppNameMapper;

	@Autowired
	private LoginWhiteListSevice loginWhiteListSevice;

	@Autowired
	private LoginWhiteListMapper loginWhiteListMapper;

	private static final String UPDATE_AGENTAPPNAME = "update mc$login_whitelist set app_name=?  where app_name=?";

	@Transactional
	@Override
	public boolean insertAgentAppName(AgentAppName agentAppName) {
		boolean result = false;
		String msg = checkAgentAppName(agentAppName);
		DruidPooledConnection connection = null;
		if (StringUtils.isNotBlank(msg)) {
			throw new RestfulException(ErrCode.PARAM_ERROR, msg);
		}
		try {
			agentAppName.setUpdateTime(new Date());
			agentAppName.setCreateTime(new Date());
			agentAppNameMapper.insertAgentAppName(agentAppName);
			connection = DruidDataSourceUtil.getInstance().getConnection(agentAppName.getObjId());
		} catch (Exception e) {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
			logger.error("insertAgentAppName error :" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		if (connection == null) {
			throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
		}
		List<LoginWhiteList> list = loginWhiteListMapper.selectLoginWhiteByAppNameId(agentAppName.getId());

		if (list == null || list.size() == 0) {
			if (agentAppName.getStatus() == null) {
				LoginWhiteList loginWhiteList = new LoginWhiteList();
				loginWhiteList.setAppId(agentAppName.getId());
				loginWhiteList.setAppName(agentAppName.getAppName());
				loginWhiteList.setObjId(agentAppName.getObjId());
				loginWhiteListSevice.inserAppNameOrdeleteAppName(connection, loginWhiteList);
			}
		} else {
			for (LoginWhiteList loginWhiteList : list) {
				try {
					loginWhiteList.setObjId(agentAppName.getObjId());
					loginWhiteListSevice.insertLoginWhite(loginWhiteList, connection, 1);
				} catch (RestfulException e) {
					throw e;
				}
			}
		}
		DruidDataSourceUtil.getInstance().closeConnection(connection);
		result = true;
		return result;
	}

	@Transactional
	@Override
	public boolean copyAgentAppName(AgentAppName agentAppName, String id, String type) {
		boolean result = false;
		String oldId = agentAppName.getId();
		if (StringUtils.isBlank(oldId)) {
			throw new RestfulException(ErrCode.APP_NAME_ID_NULL, ErrCode.APP_NAME_ID_NULL.getMessage());
		}
		try {
			AgentAppName newAgentAppName = agentAppNameMapper.selectAgentAppNameById(oldId);
			if (newAgentAppName == null) {
				if (StringUtils.equalsIgnoreCase(type, "copy")) {
					throw new RestfulException(ErrCode.APP_NAME_NULL, ErrCode.APP_NAME_NULL.getMessage());
				} else {
					newAgentAppName = agentAppName;
				}
			}
			newAgentAppName.setOldId(oldId);
			newAgentAppName.setId(id);
			newAgentAppName.setStatus(1);
			// 插入应用程序名
			result = insertAgentAppName(newAgentAppName);
			//
			result = loginWhiteListSevice.copyLoginWhite(newAgentAppName);
		} catch (Exception e) {
			logger.error("copyAgentAppName error :" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return result;
	}

	@Transactional
	@Override
	public boolean updateAgentAppName(AgentAppName agentAppName) {
		boolean result = false;
		String msg = checkAgentAppName(agentAppName);
		if (StringUtils.isNotBlank(msg)) {
			throw new RestfulException(ErrCode.PARAM_ERROR, msg);
		}
		String oldAppName = null;
		DruidPooledConnection connection = null;
		try {
			oldAppName = agentAppNameMapper.selectAgentAppNameById(agentAppName.getId()).getAppName();
			agentAppName.setUpdateTime(new Date());
			// 平台库修改应用名称
			agentAppNameMapper.updateAgentAppName(agentAppName);
			connection = DruidDataSourceUtil.getInstance().getConnection(agentAppName.getObjId());
			if (connection == null) {
				throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
			}
			// 生产库修改应用名称
			List<LoginWhiteList> list = loginWhiteListMapper.selectLoginWhiteByAppNameId(agentAppName.getId());
			if (list != null && list.size() > 0) {
				for (LoginWhiteList loginWhiteList : list) {
					loginWhiteList.setAppName(agentAppName.getAppName());
					if (StringUtils.equals("copy", agentAppName.getType())) {
						loginWhiteListSevice.insertLoginWhite(loginWhiteList, connection, 1);
					} else {
						loginWhiteListSevice.updateLoginWhite(loginWhiteList);
					}
				}
			} else {
				if (StringUtils.equals("copy", agentAppName.getType())) {
					LoginWhiteList loginWhiteList = new LoginWhiteList();
					loginWhiteList.setAppId(agentAppName.getId());
					loginWhiteList.setAppName(agentAppName.getAppName());
					loginWhiteList.setObjId(agentAppName.getObjId());
					loginWhiteListSevice.inserAppNameOrdeleteAppName(connection, loginWhiteList);
				}
			}
		} catch (Exception e) {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
            e.printStackTrace();
			logger.error("updateAgentAppName error :" + e.getMessage());
            throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		return result;
	}

	@Transactional
	@Override
	public boolean deleteAgentAppNameById(String id) {
		boolean result = false;
		try {
			if (id == null) {
				throw new RestfulException(ErrCode.PARAM_ERROR, ErrCode.UNKNOWN_ID.getMessage());
			}
			agentAppNameMapper.deleteAgentAppNameById(id);
			loginWhiteListSevice.deleteLoginWhiteByAppName(id);
			result = true;
		} catch (Exception e) {
			logger.error("deleteAgentAppNameById error :" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return result;
	}

	@Override
	public List<AgentAppName> selectAgentAppNameByObjId(Integer objId) {
		List<AgentAppName> list = null;
		try {
			list = agentAppNameMapper.selectAgentAppNameByObjId(objId);
		} catch (Exception e) {
			logger.error("selectAgentAppNameByObjId error :" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return list;
	}

	@Override
	public AgentAppName selectAgentAppNameById(String id) {
		AgentAppName agentAppName = null;
		try {
			agentAppName = agentAppNameMapper.selectAgentAppNameById(id);
		} catch (Exception e) {
			logger.error("selectAgentAppNameById error :" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return agentAppName;
	}

	@Override
	public ResultBean pageAgentAppName(Page page) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int totalCount = agentAppNameMapper.pageAgentAppNameCount(page);
			setPageTotalPage(page, totalCount);
			List<AgentAppName> plist = agentAppNameMapper.pageAgentAppName(page);
			page.setItems((List) plist);
			page.setTotalCount(totalCount);
		} catch (Exception e) {
			logger.error("Query AgentAppName page error:" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, ErrCode.UNKNOW_ERROR.getMessage());
		}
		return new ResultBean<>(page);
	}

	/**
	 * 检查参数
	 * 
	 * @param agentAppName
	 * @return
	 */
	private String checkAgentAppName(AgentAppName agentAppName) {
		if (agentAppName.getId() == null) {
			return ErrCode.APP_NAME_ID_NULL.getMessage();
		}
		if (agentAppName.getObjId() == null) {
			return ErrCode.OBJ_ID.getMessage();
		}
		if (StringUtils.isBlank(agentAppName.getAppName())) {
			return ErrCode.APP_NAME_NULL.getMessage();
		}
		if (agentAppName.getStatus() != null) {
			return null;
		}
		String id = agentAppNameMapper.selectAgentAppNameByName(agentAppName);
		if (StringUtils.isBlank(agentAppName.getId())) {
			if (!StringUtils.equals(agentAppName.getId(), id)) {
				return ErrCode.APP_NAME_REPEAT.getMessage();
			}
		} else {
			if (StringUtils.isBlank(id)) {
				return null;
			}
			if (!StringUtils.equals(agentAppName.getId(), id)) {
				return ErrCode.APP_NAME_REPEAT.getMessage();
			}
		}
		return null;
	}

	/**
	 * 分页
	 * 
	 * @param page
	 * @param totalCount
	 */
	private void setPageTotalPage(Page page, int totalCount) {
		if (totalCount == 0) {
			page.setTotalCount(0);
			page.setItems(Collections.emptyList());
			return;
		}
		// 分页
		int totalPage = (totalCount / page.getPageSize());
		if (totalCount % page.getPageSize() != 0) {
			totalPage++;
		}
		if (totalPage < page.getCurrentPage()) {
			page.setCurrentPage(totalPage);
		}
	}

	/**
	 * 修改应用名称
	 * 
	 * @param connection
	 * @param loginWhiteList
	 * @return
	 */
	private boolean updateAgentAppName(DruidPooledConnection connection, AgentAppName agentAppName, String oldAppName) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(UPDATE_AGENTAPPNAME);
			preparedStatement.setString(1, StringUtils.replace(agentAppName.getAppName(), "*", "%"));
			preparedStatement.setString(2, StringUtils.replace(oldAppName, "*", "%"));
			preparedStatement.executeUpdate();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("update AgentAppName  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}
}
