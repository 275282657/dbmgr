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

import com.hzmc.dbmgr.bean.AssetSql;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.common.exceptions.RestfulException;


public interface SensitiveQueryService {

    List getSchemaList(AssetSql assetSql, ProtectObject protectObject) throws RestfulException;
		
    List getTableList(AssetSql assetSql, ProtectObject protectObject) throws RestfulException;
	
    List getTableColum(AssetSql assetSql, ProtectObject protectObject) throws RestfulException;

	/**
	 * 获取同义词列信息
	 * @param schema schema名
	 * @param synonymsName 同义词名
	 * @return
	 * @throws RestfulException
	 */
    List getSynonymsColum(AssetSql assetSql, ProtectObject protectObject) throws RestfulException;

	
	/**
	 * 获取表的列名称和列的数据类型
	 * @return
	 * @throws RestfulException
	 */
    List<Map<String, String>> getTableColumAndColType(AssetSql assetSql, ProtectObject protectObject)
            throws RestfulException;
	
	/**
	 * 获取同义词的列名称和列的数据类型
	 * @return
	 * @throws RestfulException
	 */
    List<Map<String, String>> getSynonymsColumAndColType(AssetSql assetSql, ProtectObject protectObject)
            throws RestfulException;
	
    List getSchemaListByDbid(AssetSql assetSql, ProtectObject protectObject);
	
    List getTableListByDbid(AssetSql assetSql, ProtectObject protectObject);
	
	/**
	 * 获取schema与表(敏感发现)
	 * 
	 * @param protectObject
	 * @param dbuser
	 * @param dbpass
	 * @param sjdbc
	 * @param sname
	 * @return
	 */
    List<SchemaTables> queryAllSchemaTables(AssetSql assetSql, ProtectObject protectObject);
}
