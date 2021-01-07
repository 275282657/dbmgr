package com.hzmc.dbmgr.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.bean.LoginWhiteList;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.dbenum.IpTypeEnum;
import com.hzmc.dbmgr.dto.UuidToken;
import com.hzmc.dbmgr.mapper.AgentAppNameMapper;
import com.hzmc.dbmgr.mapper.LoginWhiteListMapper;
import com.hzmc.dbmgr.service.AgentStatusService;
import com.hzmc.dbmgr.service.LoginWhiteListSevice;
import com.hzmc.dbmgr.util.DruidDataSourceUtil;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 上午10:46:24
 */
@Service
public class LoginWhiteListSeviceImpl implements LoginWhiteListSevice {

	private static final Logger logger = LoggerFactory.getLogger(LoginWhiteListSeviceImpl.class);

	@Autowired
	private LoginWhiteListMapper loginWhiteListMapper;

	@Autowired
	private AgentAppNameMapper agentAppNameMapper;

	@Autowired
	private AgentStatusService agentStatusService;

	private final static String INSERT_WHITE_LIST_SQL = "insert into mc$login_whitelist(id,ip_mode,ip,ip_end,app_name,poxy_logo,token_id) values(?,?,?,?,?,?,?)";

	private final static String UPDATE_WHITE_LIST_SQL = "update mc$login_whitelist set ip_mode=?,ip=?,ip_end=?,app_name=?  where id=?";

	private final static String DELETE_WHITE_LIST_SQL = "delete from mc$login_whitelist where id=?";

	private final static String DELETE_WHITE_LIST_SQL_BY_APPNAME = "delete from mc$login_whitelist where app_name = ? and ip = '%' and poxy_logo is null ";

	private final static String DELETE_WHITE_LIST_APP_NAME_SQL = "delete from mc$login_whitelist where app_name=? and poxy_logo is null ";

    private final static String QUERY_APP_NAME_MAX_AFTER = "select name from mc$app_names where name like '%s'";

	private final static String QUERY_POXY_IP = "select ip from mc$login_whitelist where poxy_logo = 'POXY'";

	private final static String UPDATE_POXY_IP = "update mc$login_whitelist set ip=?  where poxy_logo='POXY'";

    private final static String QUERY_HA_IP = "select ip from mc$login_whitelist where poxy_logo = '%s'";

    private final static String UPDATE_HA_IP = "update mc$login_whitelist set ip=?  where poxy_logo='%s'";

	private final static String QUERY_ALL_LOGIN_WHITE = "select id from mc$login_whitelist where poxy_logo is null";

    private final static String DELETE_WHITE_LIST_CSCS_SQL = "delete from mc$login_whitelist where  token_id = '%s' ";
    

	@Override
	@Transactional
	public void inserOrUpdatePoxyIp(LoginWhiteList loginWhiteList) {
		if (loginWhiteList.getObjId() == null) {
			throw new RestfulException(ErrCode.OBJ_ID, ErrCode.OBJ_ID.getMessage());
		}
		DruidPooledConnection connection = null;
		try {
			connection = DruidDataSourceUtil.getInstance().getConnection(loginWhiteList.getObjId());
		} catch (SQLException e) {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
			throw new RestfulException(ErrCode.GET_CONNECTION_ERROR, ErrCode.GET_CONNECTION_ERROR.getMessage());
		}
		if (connection == null) {
			throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
		}
		try {
			if (existPoxyIp(connection)) {
				updatePoxyIp(connection, loginWhiteList.getIp());
			} else {
                loginWhiteList.setPoxyLogo("POXY");
				insertLoginWhite(connection, loginWhiteList);
			}
			
            if (StringUtils.isNotBlank(loginWhiteList.getHaIp())) {
                if (existHaIp(connection, "HA")) {
                    updateHaIp(connection, loginWhiteList.getHaIp(), "HA");
                } else {
                    loginWhiteList.setPoxyLogo("HA");
                    insertHaLoginWhite(connection, loginWhiteList);
                }
            }
            if (StringUtils.isNotBlank(loginWhiteList.getHaServiceHost())) {
                if (existHaIp(connection, "HASERVICEHOST")) {
                    updateHaIp(connection, loginWhiteList.getHaServiceHost(), "HASERVICEHOST");
                } else {
                    loginWhiteList.setPoxyLogo("HASERVICEHOST");
                    insertHaServicehostLoginWhite(connection, loginWhiteList);
                }
            }
		} catch (Exception e) {
			logger.error("update poxyIp  fail:" + e.getMessage());
			throw new RestfulException(ErrCode.UPDATE_POXY_IP_FAIL, ErrCode.UPDATE_POXY_IP_FAIL.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
	}

	@Override
	@Transactional
	public void updatePoxyIp(LoginWhiteList loginWhiteList) {
		List<AgentStatus> list = agentStatusService.selectAllAgentStatus();
        for (AgentStatus agentStatus : list) {
            loginWhiteList.setObjId(agentStatus.getObjId());
            loginWhiteList.setId(-1);
            loginWhiteList.setAppName("%");
            loginWhiteList.setIpMode(IpTypeEnum.ALONE_IP.getIpMode());
            inserOrUpdatePoxyIp(loginWhiteList);
        }
	}

	@Transactional
	@Override
	public boolean copyLoginWhite(AgentAppName agentAppName) {
		boolean result = false;
		if (agentAppName == null) {
			throw new RestfulException(ErrCode.APP_NAME_NULL, ErrCode.APP_NAME_NULL.getMessage());
		}
		try {
			// 平台库添加白名单
			loginWhiteListMapper.copyLoginWhite(agentAppName);
		} catch (Exception e) {
			result = false;
			logger.error("Copy LoginWhite error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return result;
	}

	@Transactional
	@Override
	public Integer insertLoginWhite(LoginWhiteList loginWhiteList, DruidPooledConnection connection, Integer type) {
		Integer id = null;
		AgentAppName agentAppName = null;
		if (type == 0) {
			String checkResult = checkLoginWhite(loginWhiteList);
            if (StringUtils.isNotBlank(checkResult)) {
				throw new RestfulException(ErrCode.PARAM_ERROR, checkResult);
			}
		}
		try {
			if (type == 0) {
				agentAppName = agentAppNameMapper.selectAgentAppNameById(loginWhiteList.getAppId());
				loginWhiteList.setCreateTime(new Date());
				loginWhiteList.setUpdateTime(new Date());
				loginWhiteListMapper.insertLoginWhite(loginWhiteList);
			}
			if (type == 1) {
				deleteLoginWhiteByAppNameAndIp(connection, loginWhiteList.getAppName());
				insertLoginWhite(connection, loginWhiteList);
			}
			if (agentAppName != null && agentAppName.getStatus() == null) {
				loginWhiteList.setAppName(agentAppName.getAppName());
				if (connection == null) {
					throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
				}
				deleteLoginWhiteByAppNameAndIp(connection, loginWhiteList.getAppName());
				insertLoginWhite(connection, loginWhiteList);
			}
			id = loginWhiteList.getId();
		} catch (Exception e) {
			logger.error("insert LoginWhite error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return id;
	}

	@Transactional
	@Override
	public boolean deleteLoginWhiteById(Integer id) {
		boolean result = false;
		DruidPooledConnection connection = null;
		LoginWhiteList loginWhiteList = loginWhiteListMapper.selecLoginWhitetById(id);
		try {
			loginWhiteListMapper.deleteLoginWhiteById(id);
			if (loginWhiteList != null) {
				connection = DruidDataSourceUtil.getInstance().getConnection(loginWhiteList.getObjId());
				if (connection == null) {
					throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
				}
				deleteLoginWhite(connection, id);
				inserAppNameOrdeleteAppName(connection, loginWhiteList);
			}
			result = true;
		} catch (Exception e) {
			logger.error("delete  LoginWhite id" + id + " error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		return result;
	}


	@Transactional
	@Override
	public boolean deleteLoginWhiteByAppName(String appId) {
		boolean result = false;
		DruidPooledConnection connection = null;
		try {
			AgentAppName agentAppName = agentAppNameMapper.selectAgentAppNameById(appId);
			connection = DruidDataSourceUtil.getInstance().getConnection(agentAppName.getObjId());
			if (connection == null) {
				throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
			}
			loginWhiteListMapper.deleteLoginWhiteByAppNameId(appId);
			agentAppNameMapper.deleteAgentAppNameById(appId);
			deleteLoginWhiteByAppName(connection, agentAppName.getAppName());
			result = true;
		} catch (Exception e) {
			logger.error("delete  LoginWhite AppName  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		return result;
	}

	@Transactional
	@Override
	public boolean updateLoginWhite(LoginWhiteList loginWhiteList) {
		boolean result = false;
		String checkResult = checkLoginWhite(loginWhiteList);
		if (StringUtils.isBlank(checkResult)) {
			result = true;
		} else {
			throw new RestfulException(ErrCode.PARAM_ERROR, checkResult);
		}
		DruidPooledConnection connection = null;
		try {
			AgentAppName agentAppName = agentAppNameMapper.selectAgentAppNameById(loginWhiteList.getAppId());
            if (agentAppName != null) {
                loginWhiteList.setAppName(agentAppName.getAppName());
            }
			loginWhiteList.setUpdateTime(new Date());
			loginWhiteListMapper.updateLoginWhite(loginWhiteList);
			connection = DruidDataSourceUtil.getInstance().getConnection(loginWhiteList.getObjId());
			if (connection == null) {
				throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
			}
			updateLoginWhite(connection, loginWhiteList);
		} catch (Exception e) {
			result = false;
			logger.error("insert LoginWhite error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		return result;
	}


	@Override
	public ResultBean selectLoginWhiteByAppName(Page page) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<LoginWhiteList> plist = loginWhiteListMapper.selectLoginWhiteByAppName(page);
			String appaId = (String) page.getSearchParameters().get("appId");
			AgentAppName agentAppName = agentAppNameMapper.selectAgentAppNameById(appaId);
			map.put("items", plist);
			map.put("appId", appaId);
			map.put("type", page.getSearchParameters().get("type"));
			if (StringUtils.equals((String) map.get("type"), "copy")) {
				map.put("appName", null);
			} else {
				map.put("appName", agentAppName.getAppName());
			}
            map.put("remark", agentAppName.getRemark());
		} catch (Exception e) {
			logger.error("selectLoginWhiteByAppName error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return new ResultBean<>(map);
	}

	@Override
    public List<String> queryAppName(Integer objId, String type, String name) {
		List<String> allList = null;
		DruidPooledConnection connection = null;
		List<String> result = new ArrayList<>();
		Integer id = 0;
		try {
			connection = DruidDataSourceUtil.getInstance().getConnection(objId);
			if (connection == null) {
				throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
			}
            allList = queryAppName(connection, id, name);
		} catch (SQLException e) {
			logger.error("queryAppName error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		allList.add("*");
		if (StringUtils.isBlank(type)) {
			List<String> list = agentAppNameMapper.selectAppNameByObjId(objId);
			Map<String, String> map = new HashMap<>();
			for (String appName : list) {
				map.put(appName, null);
			}
			for (String appName : allList) {
				if (!map.containsKey(appName)) {
					result.add(appName);
				}
			}
		} else {
			result = allList;
		}
		return result;
	}

	/**
	 * 如果删除应用程序下所有ip,则新增应用名称保护所有ip
	 * 
	 * @param connection
	 * @param loginWhiteList
	 */
	@Override
	public void inserAppNameOrdeleteAppName(DruidPooledConnection connection, LoginWhiteList loginWhiteList) {
		List<LoginWhiteList> list = loginWhiteListMapper.selectLoginWhiteByAppNameId(loginWhiteList.getAppId());
		if (list == null || list.size() == 0) {
			LoginWhiteList loginWhiteList1 = new LoginWhiteList();
			loginWhiteList1.setAppId(loginWhiteList.getAppId());
			loginWhiteList1.setAppName(loginWhiteList.getAppName());
			loginWhiteList1.setIp("*");
			loginWhiteList1.setIpMode(IpTypeEnum.BLURRY_IP.getIpMode());
			loginWhiteListMapper.insertLoginWhite(loginWhiteList1);
			loginWhiteListMapper.deleteLoginWhiteById(loginWhiteList1.getId());
			insertLoginWhite(connection, loginWhiteList1);
		} else {
			deleteLoginWhiteByAppNameAndIp(connection, loginWhiteList.getAppName());
		}
	}

	@Override
	public void sycLoginWhiteList(Integer objId) {
		List<LoginWhiteList> loginWhiteLists = loginWhiteListMapper.selectLoginWhiteByObjId(objId);
		Map<Integer, Integer> remoteIds = new HashMap<Integer, Integer>();
		DruidPooledConnection connection = null;
		try {
			connection = DruidDataSourceUtil.getInstance().getConnection(objId);
			if (connection == null) {
				throw new RestfulException(ErrCode.CONNECTION_NULL, ErrCode.CONNECTION_NULL.getMessage());
			}
			remoteIds = queryAllId(connection);
			for (LoginWhiteList loginWhiteList : loginWhiteLists) {
				if (!remoteIds.containsKey(loginWhiteList.getId())) {
					insertLoginWhite(connection, loginWhiteList);
				}
			}
		} catch (SQLException e) {
			logger.error("sycLoginAudit error:" + e.getMessage(), e);
		} finally {
			loginWhiteLists.clear();
			remoteIds.clear();
			remoteIds = null;
			loginWhiteLists = null;
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
	}

    @Override
    public void sycCscsWhiteList(Integer objId, UuidToken uuidToken, String appId) {
        List<String> list = uuidToken.getIp();
        Map<String, String> ipMap = new HashMap<>();
        DruidPooledConnection connection = null;
        LoginWhiteList loginWhiteList = new LoginWhiteList();
        loginWhiteList.setIpMode(0);
        loginWhiteList.setAppName("*");
        loginWhiteList.setTokenId(uuidToken.getUuid());
        loginWhiteList.setAppId(appId);
        try {
            connection = DruidDataSourceUtil.getInstance().getConnection(objId);
            if (list == null || list.size() == 0) {
                loginWhiteListMapper.deleteLoginWhiteByTokenId(uuidToken.getUuid(), appId);
                deleteLoginWhiteByPoxyLog(connection, uuidToken.getUuid());
                return;
            } else {
                Map<String, Object> map = new HashMap<>();
                String[] ipArrays = list.toArray(new String[list.size()]);
                map.put("list", ipArrays);
                map.put("objId", objId);
                List<String> existIPs = loginWhiteListMapper.selectLoginWhiteByTokenIdAndIp(map);
                for (String existIP : existIPs) {
                    ipMap.put(existIP, null);
                }
            }
            if (ipMap == null || ipMap.size() == 0) {
                loginWhiteListMapper.deleteLoginWhiteByTokenId(uuidToken.getUuid(), appId);
                deleteLoginWhiteByPoxyLog(connection, uuidToken.getUuid());
            }
            for (String ip : list) {
                if (ipMap.containsKey(ip)) {
                    continue;
                }
                loginWhiteList.setIp(ip);
                loginWhiteList.setCreateTime(new Date());
                loginWhiteList.setUpdateTime(new Date());
                loginWhiteListMapper.insertLoginWhite(loginWhiteList);
                insertLoginWhite(connection, loginWhiteList);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ipMap.clear();
            DruidDataSourceUtil.getInstance().closeConnection(connection);
        }
    }

	/**
	 * 检查参数
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	private String checkLoginWhite(LoginWhiteList loginWhiteList) {
		if (loginWhiteList.getAppId() == null) {
			return ErrCode.APP_NAME_ID_NULL.getMessage();
		}

		if (loginWhiteList.getObjId() == null) {
			return ErrCode.OBJ_ID.getMessage();
		}

		if (StringUtils.isBlank(loginWhiteList.getIp())) {
			return ErrCode.IP_NULL.getMessage();
		}

		if (loginWhiteList.getIpMode() == IpTypeEnum.SEGMENTATION_IP.getIpMode()) {
			if (StringUtils.isBlank(loginWhiteList.getIpEnd())) {
				return ErrCode.IP_END_NULL.getMessage();
			}
			if (StringUtils.equals(loginWhiteList.getIp(), loginWhiteList.getIpEnd())) {
				return ErrCode.IP_EQUAL_IP_END.getMessage();
			}
		} else {
			if (StringUtils.isNotBlank(loginWhiteList.getIpEnd())) {
				return ErrCode.IP_END_NOT_NULL.getMessage();
			}
		}

		// 查重
		List<LoginWhiteList> list = loginWhiteListMapper.selectLoginWhiteByIp(loginWhiteList);
		if (loginWhiteList.getId() == null) {
			// 新增
			if (list != null && list.size() > 0) {
				return ErrCode.IP_REPEAT.getMessage();
			}
		} else {
			if (list != null && list.size() > 2) {
				return ErrCode.IP_REPEAT.getMessage();
			}
            if (list != null && list.size() == 1
                && list.get(0).getId().intValue() != loginWhiteList.getId().intValue()) {
				return ErrCode.IP_REPEAT.getMessage();
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
	 * 插入生产库白名单
	 * 
	 * @param connection
	 * @param loginWhiteList
	 * @return
	 */
	public boolean insertLoginWhite(DruidPooledConnection connection, LoginWhiteList loginWhiteList) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(INSERT_WHITE_LIST_SQL);
			preparedStatement.setInt(1, loginWhiteList.getId());
			preparedStatement.setInt(2, loginWhiteList.getIpMode());
			preparedStatement.setString(3, StringUtils.replace(loginWhiteList.getIp(), "*", "%"));
			preparedStatement.setString(4, StringUtils.replace(loginWhiteList.getIpEnd(), "*", "%"));
			preparedStatement.setString(5, StringUtils.replace(loginWhiteList.getAppName(), "*", "%"));
			preparedStatement.setString(6, loginWhiteList.getPoxyLogo());
            preparedStatement.setString(7, loginWhiteList.getTokenId());
			preparedStatement.execute();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("insert LoginWhite  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}
	

    /**
     * 插入Ha
     * 
     * @param connection
     * @param loginWhiteList
     * @return
     */
    public boolean insertHaLoginWhite(DruidPooledConnection connection, LoginWhiteList loginWhiteList) {
        boolean result = false;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(INSERT_WHITE_LIST_SQL);
            preparedStatement.setInt(1, loginWhiteList.getId());
            preparedStatement.setInt(2, loginWhiteList.getIpMode());
            preparedStatement.setString(3, StringUtils.replace(loginWhiteList.getHaIp(), "*", "%"));
            preparedStatement.setString(4, StringUtils.replace(loginWhiteList.getIpEnd(), "*", "%"));
            preparedStatement.setString(5, StringUtils.replace(loginWhiteList.getAppName(), "*", "%"));
            preparedStatement.setString(6, loginWhiteList.getPoxyLogo());
            preparedStatement.setString(7, loginWhiteList.getTokenId());
            preparedStatement.execute();
            result = true;
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            logger.error("insert LoginWhite  error:" + e.getMessage(), e);
            throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
        }
        return result;
    }

    /**
     * 插入Ha
     * 
     * @param connection
     * @param loginWhiteList
     * @return
     */
    public boolean insertHaServicehostLoginWhite(DruidPooledConnection connection, LoginWhiteList loginWhiteList) {
        boolean result = false;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(INSERT_WHITE_LIST_SQL);
            preparedStatement.setInt(1, loginWhiteList.getId());
            preparedStatement.setInt(2, loginWhiteList.getIpMode());
            preparedStatement.setString(3, StringUtils.replace(loginWhiteList.getHaServiceHost(), "*", "%"));
            preparedStatement.setString(4, StringUtils.replace(loginWhiteList.getIpEnd(), "*", "%"));
            preparedStatement.setString(5, StringUtils.replace(loginWhiteList.getAppName(), "*", "%"));
            preparedStatement.setString(6, loginWhiteList.getPoxyLogo());
            preparedStatement.setString(7, loginWhiteList.getTokenId());
            preparedStatement.execute();
            result = true;
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            logger.error("insert LoginWhite  error:" + e.getMessage(), e);
            throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
        }
        return result;
    }

	/**
	 * 修改生产库白名单
	 * 
	 * @param connection
	 * @param loginWhiteList
	 * @return
	 */
	private boolean updateLoginWhite(DruidPooledConnection connection, LoginWhiteList loginWhiteList) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(UPDATE_WHITE_LIST_SQL);
			preparedStatement.setInt(5, loginWhiteList.getId());
			preparedStatement.setInt(1, loginWhiteList.getIpMode());
			preparedStatement.setString(2, StringUtils.replace(loginWhiteList.getIp(), "*", "%"));
			preparedStatement.setString(3, StringUtils.replace(loginWhiteList.getIpEnd(), "*", "%"));
			preparedStatement.setString(4, StringUtils.replace(loginWhiteList.getAppName(), "*", "%"));
			preparedStatement.executeUpdate();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("update LoginWhite  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

	/**
	 * 删除生产库白名单根据ID
	 * 
	 * @param connection
	 * @param loginWhiteList
	 * @return
	 */
	private boolean deleteLoginWhite(DruidPooledConnection connection, Integer id) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(DELETE_WHITE_LIST_SQL);
			preparedStatement.setInt(1, id);
			preparedStatement.execute();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("delete LoginWhite  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

	/**
	 * 删除生产库白名单根据应用名称
	 * 
	 * @param connection
	 * @param loginWhiteList
	 * @return
	 */
	private boolean deleteLoginWhiteByAppName(DruidPooledConnection connection, String appName) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(DELETE_WHITE_LIST_APP_NAME_SQL);
			preparedStatement.setString(1, StringUtils.replace(appName, "*", "%"));
			preparedStatement.execute();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("delete LoginWhite  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

    public static void main(String[] args) {
        System.out.println(String.format(QUERY_APP_NAME_MAX_AFTER, "%aa%"));
    }

    /**
     * 删除生产库白名单根据uuid
     * 
     * @param connection
     * @param loginWhiteList
     * @return
     */
    private boolean deleteLoginWhiteByPoxyLog(DruidPooledConnection connection, String uuid) {
        boolean result = false;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(String.format(DELETE_WHITE_LIST_CSCS_SQL, uuid));
            preparedStatement.execute();
            result = true;
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            logger.error("delete  LoginWhiteByPoxyLog:" + e.getMessage(), e);
            throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
        }
        return result;
    }


	/**
	 * 查询生产库应用程序名(如果查全部就将id设置为0)
	 * 
	 * @param connection
	 * @return
	 */
    private List<String> queryAppName(DruidPooledConnection connection, Integer id, String name) {
		List<String> result = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
            resultSet = statement.executeQuery(String.format(QUERY_APP_NAME_MAX_AFTER, "%" + name + "%"));
			while (resultSet.next()) {
				result.add(resultSet.getString("NAME"));
			}
			closeResultSetAndStatement(statement, resultSet);
		} catch (Exception e) {
			logger.error("query AppName  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

	/**
	 * 判断代理ip是否存在
	 * 
	 * @param connection
	 * @return
	 */
	private boolean existPoxyIp(DruidPooledConnection connection) {
		boolean result = false;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(QUERY_POXY_IP);
			while (resultSet.next()) {
				result = true;
				break;
			}
			closeResultSetAndStatement(statement, resultSet);
		} catch (Exception e) {
			logger.error("query PoxyIp  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

    /**
     * 判断代理Haip是否存在
     * 
     * @param connection
     * @return
     */
    private boolean existHaIp(DruidPooledConnection connection, String sign) {
        boolean result = false;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(String.format(QUERY_HA_IP, sign));
            while (resultSet.next()) {
                result = true;
                break;
            }
            closeResultSetAndStatement(statement, resultSet);
        } catch (Exception e) {
            logger.error("query PoxyIp  error:" + e.getMessage(), e);
            throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
        }
        return result;
    }

	/**
	 * 修改代理ip
	 * 
	 * @param connection
	 * @param loginWhiteList
	 * @return
	 */
	private boolean updatePoxyIp(DruidPooledConnection connection, String ip) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(UPDATE_POXY_IP);
			preparedStatement.setString(1, ip);
			preparedStatement.executeUpdate();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("update PoxyIp  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

    /**
     * 修改代理ip
     * 
     * @param connection
     * @param loginWhiteList
     * @return
     */
    private boolean updateHaIp(DruidPooledConnection connection, String ip, String sign) {
        boolean result = false;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(String.format(UPDATE_HA_IP, sign));
            preparedStatement.setString(1, ip);
            preparedStatement.executeUpdate();
            result = true;
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            logger.error("update PoxyIp  error:" + e.getMessage(), e);
            throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
        }
        return result;
    }


	/**
	 * 删除应用名称下所有ip的策略
	 * 
	 * @param connection
	 * @param appName
	 * @return
	 */
	private boolean deleteLoginWhiteByAppNameAndIp(DruidPooledConnection connection, String appName) {
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(DELETE_WHITE_LIST_SQL_BY_APPNAME);
			preparedStatement.setString(1, StringUtils.replace(appName, "*", "%"));
			preparedStatement.executeUpdate();
			result = true;
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			logger.error("delete LoginWhiteByAppNameAndIp  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

	/**
	 * 查询生产库所有白名单id
	 * 
	 * @param connection
	 * @return
	 */
	private Map<Integer, Integer> queryAllId(DruidPooledConnection connection) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(QUERY_ALL_LOGIN_WHITE);
			while (resultSet.next()) {
				result.put(resultSet.getInt("id"), null);
			}
			closeResultSetAndStatement(statement, resultSet);
		} catch (Exception e) {
			logger.error("query All Id  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}


	/**
	 * 关闭sql句柄
	 * 
	 * @param statement
	 * @param resultSet
	 * @throws SQLException
	 */
	private void closeResultSetAndStatement(Statement statement, ResultSet resultSet) throws SQLException {
		if (resultSet != null) {
			resultSet.close();
		}
		if (statement != null) {
			statement.close();
		}
	}



}
