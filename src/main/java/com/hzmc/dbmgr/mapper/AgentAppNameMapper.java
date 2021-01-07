package com.hzmc.dbmgr.mapper;

import java.util.List;

import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.common.bean.Page;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年11月4日 下午3:06:18
 */
public interface AgentAppNameMapper {

	int insertAgentAppName(AgentAppName agentAppName);

	int deleteAgentAppNameById(String id);

	int updateAgentAppName(AgentAppName agentAppName);

	List<AgentAppName> selectAgentAppNameByObjId(Integer objId);

	AgentAppName selectAgentAppNameById(String id);

	List<AgentAppName> pageAgentAppName(Page page);

	int pageAgentAppNameCount(Page page);

	String selectAgentAppNameByName(AgentAppName agentAppName);

	List<String> selectAppNameByObjId(Integer objId);

}
