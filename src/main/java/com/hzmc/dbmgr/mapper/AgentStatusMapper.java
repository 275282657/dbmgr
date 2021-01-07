package com.hzmc.dbmgr.mapper;

import java.util.List;

import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.common.bean.Page;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月15日 下午1:49:21
 */
public interface AgentStatusMapper {

	int insertAgentStatus(AgentStatus agentStatus);

	int deleteAgentStatus(Integer objId);

	int updateAgentStatusByObjId(AgentStatus agentStatus);

	int updateAllStatus(AgentStatus agentStatus);

	List<AgentStatus> selectPage(Page page);

	int selectPageCount(Page page);

	AgentStatus selectByObjId(Integer objId);

	List<AgentStatus> selectAllAgentStatus();

	List<AgentStatus> selectRunningAgentStatus();

}
