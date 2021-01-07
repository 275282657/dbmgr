package com.hzmc.dbmgr.service;

import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.bean.LoginWhiteList;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.dto.UuidToken;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 上午10:45:34
 */
public interface LoginWhiteListSevice {

	/**
	 * 新增代理ip
	 * 
	 * @param ip
	 * @return
	 */
	void inserOrUpdatePoxyIp(LoginWhiteList loginWhiteList);
	
	/**
	 * 修改代理ip
	 * 
	 * @param ip
	 * @return
	 */
	void updatePoxyIp(LoginWhiteList loginWhiteList);

	/**
	 * 复制白名单
	 * 
	 * @return
	 */
	boolean copyLoginWhite(AgentAppName agentAppName);

	/**
	 * 插入准入白名单
	 * 
	 * @param loginWhiteList
	 * @param connection
	 * @param type
	 *            0时插入平台库与生产库,1仅插入生产库
	 * @return
	 */
	Integer insertLoginWhite(LoginWhiteList loginWhiteList, DruidPooledConnection connection, Integer type);

	/**
	 * 根据id删除准入白名单
	 * 
	 * @param id
	 * @return
	 */
	boolean deleteLoginWhiteById(Integer id);

	/**
	 * 根据应用名称Id删除准入白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	boolean deleteLoginWhiteByAppName(String appId);

	/**
	 * 修改准入白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	boolean updateLoginWhite(LoginWhiteList loginWhiteList);


	/**
	 * 根据应用名称分页查询ip
	 * 
	 * @param page
	 * @return
	 */
	ResultBean selectLoginWhiteByAppName(Page page);

	/**
	 * 获取应用名称
	 * 
	 * @param objId
	 * @return
	 */
    List<String> queryAppName(Integer objId, String type, String name);

	/**
	 * 如果删除应用程序下所有ip,则新增应用名称保护所有ip
	 * 
	 * @param connection
	 * @param loginWhiteList
	 */
	void inserAppNameOrdeleteAppName(DruidPooledConnection connection, LoginWhiteList loginWhiteList);

	/**
	 * 同步白名单
	 * 
	 * @param objId
	 */
	void sycLoginWhiteList(Integer objId);
	
    /**
     * 同步安全客户端IP白名单
     * 
     * @param objId
     */
    void sycCscsWhiteList(Integer objId,UuidToken uuidToken,String appId);

}
