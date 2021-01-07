/**
 * 版权所有：美创科技
 * 项目名称:capaa-web
 * 创建者: fg
 * 创建日期: 2017-8-2
 * 文件说明:敏感资产查询处理(schema,table,column)
 * 最近修改者：fg
 * 最近修改日期：2017-8-2
 */
package com.hzmc.dbmgr.business.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.AssetSql;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.business.DoSensitiveQueryManager;
import com.hzmc.dbmgr.business.SensitiveQueryService;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;
import com.hzmc.dbmgr.dbenum.AssetDbType;
import com.hzmc.dbmgr.dbenum.DBEnum;

@Service
public class SensitiveQueryServiceImpl implements SensitiveQueryService{
	private static final Logger logger = Logger.getLogger(SensitiveQueryService.class);
			
	@Autowired
    @Qualifier("doEsDBSensitiveQueryManagerImpl")
    private DoSensitiveQueryManager doEsDBSensitiveQueryManagerImpl;
	
    @Autowired
    @Qualifier("doPrestoSensitiveQueryManagerImpl")
    private DoSensitiveQueryManager doPrestoSensitiveQueryManagerImpl;

    @Autowired
    @Qualifier("doHbaseSensitiveQueryManagerImpl")
    private DoSensitiveQueryManager doHbaseSensitiveQueryManagerImpl;

    @Autowired
    @Qualifier("doRedisDBSensitiveQueryManagerImpl")
    private DoSensitiveQueryManager doRedisDBSensitiveQueryManagerImpl;

    @Autowired
    @Qualifier("doDm6SensitiveQueryManagerImpl")
    private DoSensitiveQueryManager doDm6SensitiveQueryManagerImpl;

    @Autowired
    @Qualifier("doVerticaSensitiveQueryManagerImpl")
    private DoSensitiveQueryManager doVerticaSensitiveQueryManagerImpl;

	private DoSensitiveQueryManager switchDoSensitiveQueryManager(Integer dbtype) {
        switch (DBEnum.getDbtypeByDbTypeNum(dbtype)) {
            case ELASTICSEARCH:
                return doEsDBSensitiveQueryManagerImpl;
            case PRESTO:
                return doPrestoSensitiveQueryManagerImpl;
            case HBASE:
                return doHbaseSensitiveQueryManagerImpl;
            case REDIS:
                return doRedisDBSensitiveQueryManagerImpl;
            case DM6:
                return doDm6SensitiveQueryManagerImpl;
            case HPVertica:
                return doVerticaSensitiveQueryManagerImpl;
		default:
			return null;

		}

	}
	
	
	/**
	 * 查询敏感schmea
	 * @param sname
	 * @param dbid
	 * @param rtnmap
	 * @return
	 */
    public List getSchemaList(AssetSql assetSql, ProtectObject protectObject) throws RestfulException {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
		//根据资产的的类型,和资产的db主键 查找 结果集
        List schemas = doSensQueryManager.getSchemaList(AssetDbType.schema.getText(), protectObject,
            assetSql.getDbUser(),
            assetSql.getDbPassword(), jdbc);
        jdbc.close();
		return schemas;
	}
	
	/**
	 * 获取敏感表	
	 * @param sname
	 * @param dbid
	 * @param schema
	 * @param rtnmap
	 * @return
	 */	
    public List getTableList(AssetSql assetSql, ProtectObject protectObject) throws RestfulException {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
		//根据资产的的类型,和资产的db主键 查找 结果集
        List tableNames = doSensQueryManager.getTableList(AssetDbType.table.getText(), protectObject,
            assetSql.getSchemaName(),
            assetSql.getDbUser(),
            assetSql.getDbPassword(), jdbc);
        jdbc.close();
		return tableNames;
	}
	
	
	/**
	 * 查询敏感列
	 * @param sname
	 * @param schema
	 * @param dbid
	 * @param tName
	 * @param rtnmap
	 * @return
	 */	
    public List getTableColum(AssetSql assetSql, ProtectObject protectObject) throws RestfulException {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
		//根据资产的的类型,和资产的db主键 查找 结果集
        List columnNames = doSensQueryManager.getTableColum(AssetDbType.column.getText(), assetSql.getSchemaName(),
            protectObject, assetSql.getTableName(), assetSql.getDbUser(), assetSql.getDbPassword(), jdbc);
		// 未查询到列信息时，尝试作为同义词进行进一步处理
		if (columnNames == null || columnNames.isEmpty()) {
			// 获取同义词列信息
            columnNames = getSynonymsColum(assetSql, protectObject);
		}
        jdbc.close();
		return columnNames;
	}
	
	/**
	 * 获取同义词列信息
	 * @param schema schema名
	 * @param synonymsName 同义词名
	 * @return
	 * @throws RestfulException
	 */
	@Override
    public List getSynonymsColum(AssetSql assetSql, ProtectObject protectObject) throws RestfulException {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
		
		//根据资产的的类型,和资产的db主键 查找 结果集
        List columnNames = doSensQueryManager.getSynonymsColum(AssetDbType.column.getText(), assetSql.getSchemaName(),
            protectObject, assetSql.getTableName(), assetSql.getDbUser(), assetSql.getDbPassword(), jdbc);
        jdbc.close();
		return columnNames;
	}

	/**
	 * 根据资产类型，protectObject,tableName 获取列名称和列类型
	 * @param sname
	 * @param protectObject
	 * @param schema
	 * @param tableName
	 * @param dbuser
	 * @param dbpass
	 * @param jdbc
	 * @return
	 * @throws RestfulException
	 */
	@Override
    public List<Map<String, String>> getTableColumAndColType(AssetSql assetSql, ProtectObject protectObject)
        throws RestfulException {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
        List columnList = doSensQueryManager.getTableColumAndColType(AssetDbType.columnType.getText(), protectObject,
            assetSql.getSchemaName(), assetSql.getTableName(), assetSql.getDbUser(), assetSql.getDbPassword(), jdbc);
		// 未查询到列信息时，尝试作为同义词进行进一步处理
		if (columnList == null || columnList.isEmpty()) {
			// 获取同义词列信息
            columnList = getSynonymsColumAndColType(assetSql, protectObject);
		}
        jdbc.close();
		return columnList;
	}
	
	/**
	 * 根据资产类型，生产库信息，同义词名  获取列名称和列类型
	 * @param sname
	 * @param protectObject
	 * @param schema schema名
	 * @param synonymsName 同义词名
	 * @param dbuser
	 * @param dbpass
	 * @param jdbc
	 * @return
	 * @throws RestfulException
	 */
	@Override
    public List<Map<String, String>> getSynonymsColumAndColType(AssetSql assetSql, ProtectObject protectObject)
        throws RestfulException {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
        List columnList = doSensQueryManager.getSynonymsColumAndColType(AssetDbType.columnType.getText(), protectObject,
            assetSql.getSchemaName(), assetSql.getTableName(), assetSql.getDbUser(), assetSql.getDbPassword(), jdbc);
        jdbc.close();
		return columnList;
	}
	
	@Override
    public List<SchemaTables> queryAllSchemaTables(AssetSql assetSql, ProtectObject protectObject) {
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        DoSensitiveQueryManager doSensQueryManager = switchDoSensitiveQueryManager(protectObject.getDbType());
        List<SchemaTables> list = doSensQueryManager.queryAllSchemaTables(protectObject, assetSql.getDbUser(),
            assetSql.getDbPassword(), jdbc, AssetDbType.getAssetDbTypeEnumText(assetSql.getAssetType()),
            assetSql.getTableName());
        jdbc.close();
		return list;
	}


    @Override
    public List getSchemaListByDbid(AssetSql assetSql, ProtectObject protectObject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List getTableListByDbid(AssetSql assetSql, ProtectObject protectObject) {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

}
