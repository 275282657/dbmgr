package com.hzmc.dbmgr.service;

import java.util.List;

import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年11月4日 下午4:01:13
 */
public interface AgentAppNameService {

	/**
	 * 插入应用名称
	 * 
	 * @param agentAppName
	 * @return
	 */
	boolean insertAgentAppName(AgentAppName agentAppName);

	/**
	 * 复制应用名称
	 * 
	 * @param agentAppName
	 * @return
	 */
	boolean copyAgentAppName(AgentAppName agentAppName, String id, String type);

	/**
	 * 修改应用名称
	 * 
	 * @return
	 */
	boolean updateAgentAppName(AgentAppName agentAppName);

	/**
	 * 根据id删除应用名称
	 * 
	 * @param id
	 * @return
	 */
	boolean deleteAgentAppNameById(String id);

	/**
	 * 根据保护对象id查询应用名称
	 * 
	 * @param objId
	 * @return
	 */
	List<AgentAppName> selectAgentAppNameByObjId(Integer objId);

	/**
	 * 根据id查询应用名称
	 * 
	 * @param id
	 * @return
	 */
	AgentAppName selectAgentAppNameById(String id);

	/**
	 * 分页查询应用名称
	 * 
	 * @param page
	 * @return
	 */
	ResultBean pageAgentAppName(Page page);

}
