/**
 * 版权所有：美创科技
 * 项目名称:capaa-web
 * 创建者: fg
 * 创建日期: 2017-8-2
 * 文件说明:敏感资产查询处理(schema,table,column)
 * 最近修改者：fg
 * 最近修改日期：2017-8-2
 */
package com.hzmc.dbmgr.business;

import java.util.List;
import java.util.Map;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;

public interface DoSensitiveQueryManager {
	
	/**
	 * 
	 * @param sname 
	 * @param dbid 
	 * @return list 
	 * @throws Exception 
	 */
	List getSchemaList(String sname, ProtectObject protectObject, String dbuser, String dbpass, Jdbc jdbc) throws RestfulException;
	/**
	 * 
	 * @param sname  
	 * @param dbid 
	 * @param schema 
	 * @return list
	 */
	List<Object> getTableList(String sname, ProtectObject protectObject, String schema,String dbuser,String dbpass,Jdbc sjdbc) throws RestfulException;
	/**
	 * 
	 * @param sname 
	 * @param dbid 
	 * @param schema  
	 * @param tName 
	 * @return list 
	 */
	List<Object> getTableColum(String sname, String schema, ProtectObject protectObject, String tName,String dbuser,String dbpass,Jdbc sjdbc) throws RestfulException;


	/**
	 * 获取同义词列信息
	 * @param sname
	 * @param schema schema名
	 * @param protectObject 生产库对象
	 * @param synonymsName 同义词名
	 * @param dbuser
	 * @param dbpass
	 * @param sjdbc
	 * @return
	 * @throws RestfulException
	 */
	List<Object> getSynonymsColum(String sname, String schema, ProtectObject protectObject, String synonymsName, String dbuser,String dbpass,Jdbc sjdbc) throws RestfulException;
	
	/**
	 * 根据资产类型，db,tableName 获取列名称和列类型
	 * @param sname
	 * @param protectObject 生产库对象
	 * @param schema schema名
	 * @param tableName 表格名
	 * @param dbuser
	 * @param dbpass
	 * @param sjdbc
	 * @return
	 * @throws RestfulException
	 */
	List<Map<String,String >> getTableColumAndColType(String sname, ProtectObject protectObject, String schema, String tableName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException;

	/**
	 * 根据资产类型，生产库信息，同义词名  获取列名称和列类型
	 * @param sname
	 * @param protectObject 生产库对象
	 * @param schema schema名
	 * @param synonymsName 同义词名
	 * @param dbuser
	 * @param dbpass
	 * @param sjdbc
	 * @return
	 * @throws RestfulException
	 */
	List<Map<String,String>> getSynonymsColumAndColType(String sname, ProtectObject protectObject, String schema, String synonymsName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException;

	/**
	 * 
	 * JDBC获取表格
	 * @param dbid  
	 * @param schema 
	 * @return list 
	 */
	List getTableList(ProtectObject protectObject, String schema,Jdbc sjdbc);


	/**
	 * 获取所有schema与表名
	 * 
	 * @param sname
	 * @param protectObject
	 * @param schema
	 * @param dbuser
	 * @param dbpass
	 * @param sjdbc
	 * @return
	 */
    List<SchemaTables> queryAllSchemaTables(ProtectObject protectObject, String dbuser, String dbpass, Jdbc sjdbc,
        String type, String sname);

}
