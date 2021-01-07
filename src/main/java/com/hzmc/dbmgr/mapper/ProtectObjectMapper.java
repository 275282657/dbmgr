package com.hzmc.dbmgr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.common.bean.Page;

public interface ProtectObjectMapper {
    int deleteByPrimaryKey(Integer objId);

    int insert(ProtectObject record);

    int insertSelective(ProtectObject record);

    int insertSelectiveWithObjId(ProtectObject record);

    ProtectObject selectByPrimaryKey(Integer objId);

    int updateByPrimaryKeySelective(ProtectObject record);

    int updateByPrimaryKey(ProtectObject record);

    int updateByGroupId(Integer groupId);

    /**
     * 从表mc$asset_db_objects中获取objName为#{objName}的记录
     * @param objName
     * @return
     */
    ProtectObject getProtectObjectByName(String objName);

    List<ProtectObject> getAll();

    /**
     * 获取分组的组员
     * @param groupId
     * @return
     */
    List<ProtectObject> getByGroupId(Integer groupId);

    /**
     * 获取集群模式下的子节点
     * @param parentId
     * @return
     */
    List<ProtectObject> getByParentId(Integer parentId);

    /**
     * 从表mc$asset_db_objects中按page获取记录数目
     * @param page
     * @return
     */
    List<ProtectObject> getProtectObjectListPaged(Page page);

    /**
     * 根据分组，批量启停
     * @param status
     * @param groupId
     * @return
     */
    int updateStatusByGroupId(@Param("status") Integer status, @Param("groupId") Integer groupId);

    /**
     * 获取保护对象列表,包括单库和集群
     * @return
     */
    List<ProtectObject> getProtectObjectListPublic();

    /**
     * 根据查询条件，获取保护对象列表，包括单库和集群
     * 状态为启用的
     * @param protectObject
     * @return
     */
    List<ProtectObject> getAllDatabases(ProtectObject protectObject);

    /**
     * 根据条件查询保护对象
     * @param ip
     * @param port
     * @param instanceName
     * @param serviceName
     * @return
     */
    ProtectObject selectByIPPortInstanceNameAndServiceName(@Param("ip") String ip, @Param("port") Integer port, @Param("instanceName") String instanceName, @Param("serviceName") String serviceName);

    /**
     * 根据父节点删除子节点
     * @param parentId
     * @return
     */
    int deleteByParentId(Integer parentId);

    ProtectObject getByObjName(@Param("objName") String objName);

    int updateDbUserAndPassword(ProtectObject protectObject);

    List<Integer> getPortsByDbtype(@Param("dbType") Integer dbType);

    /**
     * 更新状态，如果有子节点，子节点也一并更新
     * @param objId
     * @param status
     * @return
     */
    int updateStatus(@Param("objId") Integer objId, @Param("status") Integer status);

    /**
     * 更新运行模式，如果有子节点，子节点也一并更新
     * @param objId
     * @param runMode
     * @return
     */
    int updateRunMode(@Param("objId") Integer objId, @Param("runMode") Integer runMode);

	/**
	 * 获取账户密码不为空且符合数据库类型的保护对象(数据脱敏专用)
	 * 
	 * @param parentId
	 * @return
	 */
	List<ProtectObject> getProjectByDbtype(Integer dbType);

	/**
	 * 获取符合数据库类型的保护对象
	 * 
	 * @param parentId
	 * @return
	 */
	List<ProtectObject> getAllProjectByDbtype(Integer dbType);

}