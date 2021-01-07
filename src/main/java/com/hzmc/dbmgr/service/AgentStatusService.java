package com.hzmc.dbmgr.service;

import java.util.List;

import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月15日 下午2:47:18
 */
public interface AgentStatusService {


	/**
	 * 根据保护对象id删除准入
	 * 
	 * @param objId
	 * @return
	 */
	boolean deleteAgentStatusByObjId(Integer objId);

	/**
	 * 启动或停止准入
	 * 
	 * @return
	 */
	boolean startAndStopAgentStatus(Integer objId, Integer status, String poxyIp,String haIp,String haServiceHost);

	/**
	 * 修改模式之修改DB探针状态(仅仅修改生产库数据)
	 * 
	 * @return
	 */
	void updateDBStatus(Integer status);

	/**
	 * 同步DB探针状态,检查DB探针状态
	 * 
	 * @return
	 */
	void synchronousAgentStatus();

	/**
	 * 获取准入分页
	 * 
	 * @param page
	 *            分页信息
	 * @return
	 */
	ResultBean<Page> selectPage(Page page);

	/**
	 * 根据保护对象Id查询准入信息
	 * 
	 * @param objId
	 * @return
	 */
	AgentStatus selectByObjId(Integer objId);

	/**
	 * 新增准入
	 * 
	 * @param agentStatus
	 * @return
	 */
	boolean insertAgentStatus(AgentStatus agentStatus);

	/**
	 * 根据保护对象修改准入状态
	 * 
	 * @param agentStatus
	 * @return
	 */
	boolean updateAgentStatusByObjId(AgentStatus agentStatus);

	/**
	 * 查询所有准入记录
	 * 
	 * @return
	 */
	List<AgentStatus> selectAllAgentStatus();

	/**
	 * 查询所有启用或者模拟启用的准入记录
	 * 
	 * @return
	 */
	List<AgentStatus> selectRunningAgentStatus();

	/**
	 * 卸载Db探针
	 */
	void removeAgentStatus(Integer objId);

}
