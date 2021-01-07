/**
 * hive类型生产库敏感资产查询处理(schema,table,column)
 */
package com.hzmc.dbmgr.business.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.business.DoSensitiveQueryManager;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;
import com.hzmc.dbmgr.dbenum.ErrCode;

@Service("doPrestoSensitiveQueryManagerImpl")
public class DoPrestoSensitiveQueryManagerImpl implements DoSensitiveQueryManager {

    private static final Logger logger = Logger.getLogger(DoPrestoSensitiveQueryManagerImpl.class);

    /**
     * @param sname
     * @param db
     * @return list
     */
    @Override
    public List<Map> getSchemaList(String sname, ProtectObject protectObject, String username, String dbpass,
                                   Jdbc sjdbc) throws RestfulException {
        List list = new ArrayList();
        // 创建根据类型查找结果的sql
        String sql = "show schemas from " + protectObject.getServiceName();
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(protectObject.getServiceName() + "." + rs.getString("Schema"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(sql);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "无法获取database列表");
        }
        return list;
    }

    /**
     * @param sname  类型
     * @param schema db的用户
     * @return list
     */
    @Override
    public List<Object> getTableList(String sname, ProtectObject protectObject, String schema, String username,
                                     String dbpass, Jdbc sjdbc) throws RestfulException {
        List list = new ArrayList();
        String sql = "show tables from " + schema;
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                String tablename = rs.getString("Table");
                list.add(tablename);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_COLLECTION_ERROR, "无法获取collection列表");
        }
        return list;
    }

    /**
     * @param sname
     * @param schema
     * @param tName
     * @return list
     */
    @Override
    public List<Object> getTableColum(String sname, String schema, ProtectObject protectObject, String tName,
                                      String username, String dbpass, Jdbc sjdbc) throws RestfulException {
        List list = new ArrayList();
        String sql = "show columns from " + schema + "." + tName;
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_COLUMS_ERROR, "无法获取colums列信息");
        }
        return list;
    }

    /**
     * 获取同义词列信息
     *
     * @param sname
     * @param schema        schema名
     * @param protectObject 生产库对象
     * @param synonymsName  同义词名
     * @param username
     * @param dbpass
     * @param sjdbc
     * @return
     * @throws RestfulException
     */
    @Override
    public List<Object> getSynonymsColum(String sname, String schema, ProtectObject protectObject, String synonymsName,
                                         String username, String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, String>> getTableColumAndColType(String sname, ProtectObject protectObject, String schema,
                                                             String tableName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        List list = new ArrayList();
        String sql = "show columns from " + schema + "." + tableName;
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("COLUMN_NAME", rs.getString("Column"));
                map.put("DATA_TYPE", rs.getString("Type"));
                map.put("NULLABLE", "Y");
                list.add(map);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_COLUMS_ERROR, "无法获取colums列信息");
        }
        return list;
    }

    /**
     * 根据资产类型，生产库信息，同义词名 获取列名称和列类型
     *
     * @param sname
     * @param protectObject 生产库对象
     * @param schema        schema名
     * @param synonymsName  同义词名
     * @param dbuser
     * @param dbpass
     * @param sjdbc
     * @return
     * @throws RestfulException
     */
    @Override
    public List<Map<String, String>> getSynonymsColumAndColType(String sname, ProtectObject protectObject,
                                                                String schema, String synonymsName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList<>();
    }

    /**
     * JDBC获取表格
     *
     * @param schema
     * @return list
     */
    @Override
    public List getTableList(ProtectObject protectObject, String schema, Jdbc sjdbc) {
        String sql = String.format("show tables in `%s`", schema);

        List list = null;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(schema + "." + rs.getString("tab_name"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }


	@Override
    public List<SchemaTables> queryAllSchemaTables(ProtectObject protectObject, String dbuser, String dbpass,
        Jdbc sjdbc, String type, String sname) {
        return new ArrayList<>();
	}

}
