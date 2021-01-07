package com.hzmc.dbmgr.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.bean.LoginWhiteList;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.AgentStatusEnum;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.dbenum.IpTypeEnum;
import com.hzmc.dbmgr.dto.UuidToken;
import com.hzmc.dbmgr.mapper.AgentAppNameMapper;
import com.hzmc.dbmgr.mapper.AgentStatusMapper;
import com.hzmc.dbmgr.mapper.LoginWhiteListMapper;
import com.hzmc.dbmgr.remote.CscsRemoteSevice;
import com.hzmc.dbmgr.service.AgentStatusService;
import com.hzmc.dbmgr.service.LoginWhiteListSevice;
import com.hzmc.dbmgr.service.ProtectObjectService;
import com.hzmc.dbmgr.util.DataBaseUtil;
import com.hzmc.dbmgr.util.DruidDataSourceUtil;
import com.hzmc.dbmgr.util.JasyptEncryptor;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月15日 下午3:30:27
 */
@Service
public class AgentStatusServiceImpl implements AgentStatusService {

	private static final Logger logger = LoggerFactory.getLogger(AgentStatusServiceImpl.class);

	private final static String STATUS = "status";

	private final static String QUERY_AGENT_STATUS = "select status from mc$agent_status";

	private final static String UPDATE_AGENT_STATUS = "update  mc$agent_status set status='%S'";

	private final static String DELETE_ALL_WHITE_LIST = "delete from mc$login_whitelist where poxy_logo is null";

	@Autowired
	private AgentStatusMapper agentStatusMapper;

	@Autowired
	private ProtectObjectService protectObjectService;

	@Autowired
	private LoginWhiteListSevice loginWhiteListSevice;

	@Autowired
	private LoginWhiteListMapper loginWhiteListMapper;

	@Autowired
	private AgentAppNameMapper agentAppNameMapper;

    @Autowired
    private CscsRemoteSevice cscsRemoteSevice;

	@Value("${agentDb.userName}")
	private String USER_NAME;

	@Value("${agentDb.userName1}")
	private String USER_NAME1;

	@Value("${agentDb.passWord}")
	private String PASS_WORD;

	@Value("${agentDb.serviceName}")
	private String SERVICE_NAME;

	@Transactional
	@Override
	public boolean deleteAgentStatusByObjId(Integer objId) {
		boolean result = false;
		DruidPooledConnection druidPooledConnection = null;
		try {
			// 停止DB探针
            startAndStopAgentStatus(objId, AgentStatusEnum.STOP.getNumber(), null, null, null);
			agentStatusMapper.deleteAgentStatus(objId);
			List<AgentAppName> list = agentAppNameMapper.selectAgentAppNameByObjId(objId);
            if (list != null && list.size() > 0) {
                for (AgentAppName agentAppName : list) {
                    loginWhiteListMapper.deleteLoginWhiteByAppNameId(agentAppName.getId());
                    agentAppNameMapper.deleteAgentAppNameById(agentAppName.getId());
                }
            }
			// 清空生产库白名单
			druidPooledConnection = DruidDataSourceUtil.getInstance().getConnection(objId);
			truncateWhileList(druidPooledConnection);
			DruidDataSourceUtil.getInstance().removeConnection(objId);
			result = true;
		} catch (Exception e) {
			logger.error("Delete AgentStatus fail:" + e.getMessage());
			throw new RestfulException(ErrCode.DELETE_AGENTSTATUS_FAIL, ErrCode.DELETE_AGENTSTATUS_FAIL.getMessage());
		}finally {
			DruidDataSourceUtil.getInstance().closeConnection(druidPooledConnection);
		}
		return result;
	}

	@Transactional
	@Override
    public boolean startAndStopAgentStatus(Integer objId, Integer status, String poxyIp, String haIp,
        String haServiceHost) {
		boolean result = false;
		AgentStatus agentStatus = agentStatusMapper.selectByObjId(objId);
		if (agentStatus == null && status == null) {
			status = AgentStatusEnum.SIMULATION.getNumber();
		} else {
			if (status == null) {
				status = agentStatus.getStatus();
			}
		}
		ProtectObject protectObject = protectObjectService.getProtectObjectById(objId, false);
		if (protectObject == null) {
			throw new RestfulException(ErrCode.PROTECTOBJECT_NULL, ErrCode.PROTECTOBJECT_NULL.getMessage());
		}
		// 生成
		protectObject.setDbUser(USER_NAME);
		protectObject.setDbPassword(PASS_WORD);
		if (protectObject.getDbType() == DBEnum.SQLSERVER.getNumber()) {
			protectObject.setServiceName(SERVICE_NAME);
		}
		DruidPooledConnection connection = null;
		if (agentStatus == null) {
			// 第一次
			// 生成准入信息
			agentStatus = createAgentStatus(objId, status);
			// 初始化连接池
			try {
				DruidDataSourceUtil.getInstance().addDruidDataBase(protectObject);
				connection = DruidDataSourceUtil.getInstance().getConnection(objId);
			} catch (SQLException e) {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
				logger.error("Add Once DruidDataBase error " + e.getMessage(), e);
			}
			// 区别CDB与PDB账户
			protectObject.setDbUser(USER_NAME1);
			if (connection == null) {
				try {
					DruidDataSourceUtil.getInstance().addDruidDataBase(protectObject);
					connection = DruidDataSourceUtil.getInstance().getConnection(objId);
					agentStatus.setDbUser(USER_NAME1);
				} catch (SQLException e) {
					DruidDataSourceUtil.getInstance().closeConnection(connection);
					logger.error("Add Second  DruidDataBase error " + e.getMessage(), e);
					throw new RestfulException(ErrCode.AGENT_SQL_NO_RUN,
							protectObject.getObjName() + ErrCode.AGENT_SQL_NO_RUN.getMessage());
				}
			}
			// 平台库插入准入信息
			agentStatusMapper.insertAgentStatus(agentStatus);
			// 插入代理ip信息
		} else {
			// 清空msg
			agentStatus.setMessage(null);
			agentStatus.setStatus(status);
			// 初始化连接池
			try {
				connection = DruidDataSourceUtil.getInstance().getConnection(objId);
				if (connection == null) {
					DruidDataSourceUtil.getInstance().addDruidDataBase(protectObject);
					connection = DruidDataSourceUtil.getInstance().getConnection(objId);
				}
			} catch (SQLException e) {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
				logger.error("Add Once DruidDataBase error " + e.getMessage(), e);
			}
			try {
				if (connection == null) {
					// 区别CDB与PDB账户
					protectObject.setDbUser(USER_NAME1);
					DruidDataSourceUtil.getInstance().addDruidDataBase(protectObject);
					connection = DruidDataSourceUtil.getInstance().getConnection(objId);
				}
				if (status == null || status == AgentStatusEnum.ERROR.getNumber()) {
					agentStatus.setStatus(queryAgentStatus(connection));
				}
				// 同步生产库DB探针状态
				String checkAgentStatusSql = DataBaseUtil.getDbAgentStatusSql(protectObject.getDbType());
				sycAgentStatus(connection, agentStatus, checkAgentStatusSql);
			} catch (Exception e) {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
				agentStatus.setStatus(AgentStatusEnum.ERROR.getNumber());
				agentStatus.setMessage(ErrCode.ERROR_MSG.getMessage());
				logger.error("Add DruidDataBase error " + e.getMessage(), e);
				throw new RestfulException(ErrCode.ERROR_MSG,
						protectObject.getObjName() + ErrCode.ERROR_MSG.getMessage());
			}
			agentStatusMapper.updateAgentStatusByObjId(agentStatus);
		}
		// 生产库插入准入信息
		if (agentStatus.getStatus() != AgentStatusEnum.ERROR.getNumber()) {
			try {
				updateAgentStatus(connection, agentStatus);
			} catch (RestfulException e) {
				throw e;
			} finally {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
			}
		}
		// 代理ip
		if (StringUtils.isNotBlank(poxyIp)) {
			LoginWhiteList loginWhiteList = new LoginWhiteList();
			loginWhiteList.setId(-1);
			loginWhiteList.setObjId(objId);
			loginWhiteList.setPoxyLogo("POXY");
			loginWhiteList.setIp(poxyIp);
			loginWhiteList.setAppName("%");
            loginWhiteList.setHaIp(haIp);
            loginWhiteList.setHaServiceHost(haServiceHost);
			loginWhiteList.setIpMode(IpTypeEnum.ALONE_IP.getIpMode());
			loginWhiteListSevice.inserOrUpdatePoxyIp(loginWhiteList);
		}
        // 同步CSCS
        try {
            // 插入
            Map<String, UuidToken> map = cscsRemoteSevice.getRemoteIpList_V1();
            List<UuidToken> list = new ArrayList<UuidToken>();
            if (map != null && map.size() > 0) {
                AgentAppName agentAppName = getAgentAppName(objId);
                String appId = agentAppNameMapper.selectAgentAppNameByName(agentAppName);
                if (StringUtils.isBlank(appId)) {
                    agentAppName.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                    agentAppNameMapper.insertAgentAppName(agentAppName);
                    appId = agentAppName.getId();
                }
                Iterator uuidTokens = map.keySet().iterator();
                String key = null;
                while (uuidTokens.hasNext()) {
                    key = (String)uuidTokens.next();
                    loginWhiteListSevice.sycCscsWhiteList(objId, map.get(key), appId);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
		result = true;
		return result;
	}

    private AgentAppName getAgentAppName(Integer objId) {
        AgentAppName agentAppName = new AgentAppName();
        agentAppName.setAppName("*");
        agentAppName.setCreateTime(new Date());
        agentAppName.setUpdateTime(new Date());
        agentAppName.setObjId(objId);
        return agentAppName;
    }

	@Override
	public void updateDBStatus(Integer status) {
		List<AgentStatus> list = agentStatusMapper.selectAllAgentStatus();
        if (list == null || list.size() == 0) {
            return;
        }
		for (AgentStatus agentStatus : list) {
			DruidPooledConnection connection = null;
			try {
				connection = DruidDataSourceUtil.getInstance().getConnection(agentStatus.getObjId());
				if (status == AgentStatusEnum.STOP.getNumber()) {
					stopDbAgent(agentStatus, connection);
				} else {
					if (status != AgentStatusEnum.ERROR.getNumber()) {
						updateAgentStatus(connection, agentStatus);
					}
				}
			} catch (Exception e) {
				logger.error("updateDBStatus error:" + e.getMessage());
			} finally {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
			}
		}
	}

	@Override
	public void synchronousAgentStatus() {
		List<AgentStatus> list = agentStatusMapper.selectAllAgentStatus();
        if (list == null || list.size() == 0) {
            return;
        }
		for (AgentStatus agentStatus : list) {
			DruidPooledConnection connection = null;
			try {
				connection = DruidDataSourceUtil.getInstance().getConnection(agentStatus.getObjId());
				if (connection == null) {
					continue;
				}
				String checkAgentStatusSql = DataBaseUtil.getDbAgentStatusSql(agentStatus.getDbType());
				sycAgentStatus(connection, agentStatus, checkAgentStatusSql);
				if (agentStatus.getStatus() == AgentStatusEnum.ERROR.getNumber()) {
					agentStatusMapper.updateAgentStatusByObjId(agentStatus);
				}
			} catch (Exception e) {
				logger.error("Synchronous AgentStatus error:" + e.getMessage());
			} finally {
				DruidDataSourceUtil.getInstance().closeConnection(connection);
			}
		}
	}

	@Override
	public ResultBean<Page> selectPage(Page page) {
		try {
			int totalCount = agentStatusMapper.selectPageCount(page);
			setPageTotalPage(page, totalCount);
			List<AgentStatus> plist = agentStatusMapper.selectPage(page);
			page.setItems((List) plist);
			page.setTotalCount(totalCount);
		} catch (Exception e) {
			logger.error("Query AgentStatus page error:" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, ErrCode.UNKNOW_ERROR.getMessage());
		}
		return new ResultBean<>(page);
	}

	@Override
	public AgentStatus selectByObjId(Integer objId) {
		AgentStatus agentStatus = null;
		try {
			agentStatus = agentStatusMapper.selectByObjId(objId);
		} catch (Exception e) {
			logger.error("DownLoadsqlScript error :" + e.getMessage());
			throw new RestfulException(ErrCode.UNKNOW_ERROR, ErrCode.UNKNOW_ERROR.getMessage());
		}
		if (agentStatus != null) {
			// 解密
			decodePassword(agentStatus);
		}
		return agentStatus;
	}

	@Transactional
	@Override
	public boolean insertAgentStatus(AgentStatus agentStatus) {
		boolean result = false;
		try {
			agentStatus.setDbPassword(JasyptEncryptor.encoder(agentStatus.getDbPassword()));
			agentStatus.setCreateTime(new Date());
			agentStatus.setUpdateTime(new Date());
			agentStatusMapper.insertAgentStatus(agentStatus);
			result = true;
		} catch (Exception e) {
			logger.error("insert AgentStatus error :" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UNKNOW_ERROR, e.getMessage());
		}
		return result;
	}

	@Transactional
	@Override
	public boolean updateAgentStatusByObjId(AgentStatus agentStatus) {
		boolean result=false;
		agentStatus.setUpdateTime(new Date());
		DruidPooledConnection connection = null;
		try {
			connection = DruidDataSourceUtil.getInstance().getConnection(agentStatus.getObjId());
			agentStatusMapper.updateAgentStatusByObjId(agentStatus);
			// 生产库插入准入信息
			if (agentStatus.getStatus() != AgentStatusEnum.ERROR.getNumber()) {
				updateAgentStatus(connection, agentStatus);
			}
			result = true;
		} catch (Exception e) {
			logger.error("updateAgentStatusByObjId error :" + e.getMessage(), e);
			throw new RestfulException(ErrCode.UPDATE_AGENT_STATUS, e.getMessage());
		} finally {
			DruidDataSourceUtil.getInstance().closeConnection(connection);
		}
		return result;
	}

	@Override
	public List<AgentStatus> selectAllAgentStatus() {
		return agentStatusMapper.selectAllAgentStatus();
	}

	@Override
	public List<AgentStatus> selectRunningAgentStatus() {
		return agentStatusMapper.selectRunningAgentStatus();
	}

	@Transactional
	@Override
	public void removeAgentStatus(Integer objId) {
		AgentStatus agentStatus = agentStatusMapper.selectByObjId(objId);
		// 断开前校验
		String result = checkBeforeRemoveAgentStatus(agentStatus);
		if (StringUtils.isNotBlank(result)) {
			throw new RestfulException(ErrCode.UPDATE_AGENT_STATUS, result);
		}
		//

	}

	private String checkBeforeRemoveAgentStatus(AgentStatus agentStatus) {
		String result = null;
		if (agentStatus == null) {
			return ErrCode.AGENT_SQL_NO_RUN.getMessage();
		}
		if (agentStatus.getStatus() != AgentStatusEnum.STOP.getNumber()) {
			return ErrCode.DB_STATUS_NOT_STOP.getMessage();
		}
		return result;
	}

	private AgentStatus createAgentStatus(Integer objId, Integer status) {
		AgentStatus agentStatus = new AgentStatus();
		agentStatus.setObjId(objId);
		agentStatus.setStatus(status);
		agentStatus.setDbUser(USER_NAME);
		agentStatus.setDbPassword(JasyptEncryptor.encoder(PASS_WORD));
		agentStatus.setServiceName(SERVICE_NAME);
		agentStatus.setCreateTime(new Date());
		agentStatus.setUpdateTime(new Date());
		return agentStatus;
	}

	/**
	 * 解密密码
	 * 
	 * @param agentStatus
	 */
	private void decodePassword(AgentStatus agentStatus) {
		if (StringUtils.isNotBlank(agentStatus.getDbPassword()))
			agentStatus.setDbPassword(JasyptEncryptor.decoder(agentStatus.getDbPassword()));
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
	 * 修改生产库准入状态
	 * 
	 * @param connection
	 * @param agentStatus
	 * @return
	 */
	private boolean updateAgentStatus(DruidPooledConnection connection, AgentStatus agentStatus) {
		boolean result = false;
		Statement statement = null;
		Integer status = agentStatus.getStatus();
		String sql = UPDATE_AGENT_STATUS.format(UPDATE_AGENT_STATUS, status);
		try {
			statement = connection.createStatement();
			statement.execute(sql);
			result = true;
			closeResultSetAndStatement(statement, null);
		} catch (Exception e) {
			logger.error("update AgentStatus sql:" + sql + ", error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;
	}

	/**
	 * 查询生产库准入状态
	 * 
	 * @param connection
	 * @param agentStatus
	 * @return
	 */
	private Integer queryAgentStatus(DruidPooledConnection connection) {
		Statement statement = null;
		ResultSet resultSet = null;
		Integer status = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(QUERY_AGENT_STATUS);
			while (resultSet.next()) {
				status = resultSet.getInt(STATUS);
				break;
			}
			closeResultSetAndStatement(statement, resultSet);
		} catch (Exception e) {
			logger.error("query AgentStatus  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return status;
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

	/**
	 * 关闭DB探针
	 * 
	 * @param agentStatus
	 * @param connection
	 */
	private void stopDbAgent(AgentStatus agentStatus, DruidPooledConnection connection) {
		agentStatus.setStatus(AgentStatusEnum.STOP.getNumber());
		if (AgentStatusEnum.STOP.getNumber() != queryAgentStatus(connection)) {
			updateAgentStatus(connection, agentStatus);
		}
	}

	private void sycAgentStatus(DruidPooledConnection connection, AgentStatus agentStatus, String sql) {
		Statement statement = null;
		ResultSet resultSet = null;
		String status = null;
		String message = null;
		String result = null;
		try {
			statement = connection.createStatement();
			// {"status": "running","version": "v1.0","message": ""}
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				result = resultSet.getString(1);
				break;
			}
			closeResultSetAndStatement(statement, resultSet);
			JSONObject jSONObject = JSON.parseObject(result);
			message = jSONObject.getString("message");
			status = jSONObject.getString("status");
			if (StringUtils.equalsIgnoreCase(status, "running")) {
				return;
			} else {
				agentStatus.setStatus(AgentStatusEnum.ERROR.getNumber());
				agentStatus.setMessage(message);
			}
		} catch (Exception e) {
			agentStatus.setStatus(AgentStatusEnum.ERROR.getNumber());
			agentStatus.setMessage(e.getMessage());
			logger.error("query AgentStatus  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, e.getMessage());
		}
	}

	private boolean truncateWhileList(DruidPooledConnection connection) {
		boolean result = false;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(DELETE_ALL_WHITE_LIST);
			result = true;
			if (statement != null) {
				statement.close();
			}
		} catch (Exception e) {
			logger.error("Truncate WhileList  error:" + e.getMessage(), e);
			throw new RestfulException(ErrCode.SQL_EXCUTE_ERROR, ErrCode.SQL_EXCUTE_ERROR.getMessage());
		}
		return result;

	}
}
