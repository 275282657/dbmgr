package com.hzmc.dbmgr.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.hzmc.dbmgr.bean.AgentAppName;
import com.hzmc.dbmgr.bean.LoginWhiteList;
import com.hzmc.dbmgr.common.bean.Page;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 上午8:50:54
 */
public interface LoginWhiteListMapper {

	/**
	 * 插入准入白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	int insertLoginWhite(LoginWhiteList loginWhiteList);
	

	/**
	 * copy白名单
	 * 
	 * @param agentAppName
	 * @return
	 */
	int copyLoginWhite(AgentAppName agentAppName);

	/**
	 * 根据id删除准入白名单
	 * 
	 * @param id
	 * @return
	 */
	int deleteLoginWhiteById(Integer id);

	/**
	 * 根据应用名称Id删除准入白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	int deleteLoginWhiteByAppNameId(String appId);
	
	/**
     * 根据tokenId删除准入白名单
     * 
     * @param loginWhiteList
     * @return
     */
        int deleteLoginWhiteByTokenId(@Param("tokenId") String tokenId, @Param("appId") String appId);

	/**
	 * 修改准入白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	int updateLoginWhite(LoginWhiteList loginWhiteList);

	/**
	 * 根据id查询白名单
	 * 
	 * @param id
	 * @return
	 */
	LoginWhiteList selecLoginWhitetById(Integer id);

	/**
	 * 根据应用名称分页查询ip
	 * 
	 * @param page
	 * @return
	 */
	List<LoginWhiteList> selectLoginWhiteByAppName(Page page);

	/**
	 * 根据应用名称分页查询ip数据量
	 * 
	 * @param page
	 * @return
	 */
	int selectLoginWhiteByAppNameCount(Page page);

	/**
	 * 根据应用程序id查询白名单
	 * 
	 * @param appId
	 * @return
	 */
	List<LoginWhiteList> selectLoginWhiteByAppNameId(String appId);

	/**
	 * 查询改DB探针的代理ip
	 * 
	 * @param objId
	 * @return
	 */
	LoginWhiteList selectPoxyIpByObjId(Integer objId);

	/**
	 * 根据IP查询查询白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	List<LoginWhiteList> selectLoginWhiteByIp(LoginWhiteList loginWhiteList);

	/**
	 * 根据保护对象ID获取白名单
	 * 
	 * @param loginWhiteList
	 * @return
	 */
	List<LoginWhiteList> selectLoginWhiteByObjId(Integer objId);
	
	/**
     * 根据tokenid与ip查询白名单
     * 
     * @param tokenId
     * @param ip
     * @return
     */
	List<String> selectLoginWhiteByTokenIdAndIp(Map<String, Object> map);

}
