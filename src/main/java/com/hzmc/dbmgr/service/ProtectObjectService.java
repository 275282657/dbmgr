package com.hzmc.dbmgr.service;

import java.util.List;
import java.util.Map;

//import com.hzmc.dbmgr.bean.LogicalDatabase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.common.bean.Page;

public interface ProtectObjectService {

	/**
	 * 获取ProtectObject分页
	 * @param page 分页信息
	 * @return
	 */
	Page getProtectObjectListPaged(Page page);

	/**
	 * 按id获取单个protectObject,包括节点
	 * @param id
	 * @param withPassword  true 有密码 false没密码
	 * @return
	 */
	ProtectObject getProtectObjectById(int id, Boolean withPassword);

	/**
	 * 创建保护对象
	 * @param protectObject
	 * @return
	 */
	Boolean createProtectObject(ProtectObject protectObject);

	/**
	 * 更新保护对象记录
	 * 对象中为空的字段不会被更新
	 * @param protectObject
	 */
	Boolean updateProtectObject(ProtectObject protectObject);

	/**
	 * 删除保护对象
	 * @param id
	 * @return
	 */
	Boolean deleteProtectObject(int id);

	/**
	 * 保护对象分组
	 * @param ids
	 * @param group_id
	 * @return 
	 */
	Boolean groupProtectObject(int[] ids, Integer group_id);

    /**
     * 保护对象分组
     * 
     * @param ids
     * @param group_id
     * @return
     */
    Boolean groupProtectObjectNew(int[] ids, Integer group_id);

	/**
	 * 保护对象退组
	 * @param id
	 * @return
	 */
	Boolean exitGroup(int id);

	/**
	 * 保护对象导入
	 * @param file
	 * @return map有两个key，key为success，key为fail；value为保护对象列表
	 */
	Map<String,List<ProtectObject>> importProtectObject(MultipartFile file);

	/**
	 * 把xls或csv文件解析成保护对象列表
	 * @param filePath 文件路径
	 * @return
	 */
	List<ProtectObject> parseFromFile(String filePath);

	/**
	 * 保护对象导出
	 * @param list
	 * @return 导出的文件的路径
	 */
	String exportProtectObject(List<ProtectObject> list);

	/**
	 * 导出的保护对象文件下载
	 * @param uuid
	 * @return
	 */
	ResponseEntity downloadProtectObject(String uuid);

	/**
	 * 获取所有的保护对象
	 * @param withStructure true表示集群模式下，子节点套在父节点里面返回，false 子节点和父节点同级返回
	 * @return
	 */
	List<ProtectObject> getProtectObjectListPublic(Boolean withStructure);

	/**
	 * 保护对象批量启用停止
	 * @param ids
	 * @param cmd
	 */
	void startOrStopProtectObject(int[] ids, String cmd);

	/**
	 * 连接测试
	 * @param protectObject
	 * @return
	 */
	Boolean connectTest(ProtectObject protectObject);

	/**
	 * 根据保护对象名称获取保护对象
	 * @param objName
	 * @return
	 */
    ProtectObject getProtectObjectByObjName(String objName);

	/**
	 * 更新数据库账户密码
	 * @param protectObject
	 * @return
	 */
	Boolean updateDbUserAndPassword(ProtectObject protectObject);

	/**
	 * 获取某种数据库类型使用的所有端口
	 * @param dbType
	 * @return
	 */
	List<Integer> getPortsByDbtype(Integer dbType);

	/**
	 * 更新运行模式接口
	 * @param protectObject
	 * @return
	 */
	Boolean updateRunMode(ProtectObject protectObject);

	/**
	 * 获取账户密码不为空且符合数据库类型的保护对象(数据脱敏专用)
	 * 
	 * @param dbType
	 * @return
	 */
	List<ProtectObject> queryProtectObjectByDbType(Integer dbType);

	/**
	 * 同步获取sqlserver动态端口改变得保护对象
	 * 
	 * @param dbType
	 * @return
	 */
	List<ProtectObject> sycSqlserverTcpPort(Integer dbType);
}
