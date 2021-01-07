/**
 * hive类型生产库敏感资产查询处理(schema,table,column)
 */
package com.hzmc.dbmgr.business.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.business.DoSensitiveQueryManager;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;
import com.hzmc.dbmgr.dbenum.ErrCode;

@Service("doVerticaSensitiveQueryManagerImpl")
public class DoVerticaSensitiveQueryManagerImpl implements DoSensitiveQueryManager {

    private static final Logger logger = Logger.getLogger(DoVerticaSensitiveQueryManagerImpl.class);

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
        String sql = getSchemaListSql();
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(protectObject.getServiceName() + "." + rs.getString("SCHEMA_NAME"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(sql);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "无法获取schemata列表");
        }
        return list;
    }

    private String getSchemaListSql() {
        return "SELECT  SCHEMA_NAME FROM SCHEMATA WHERE IS_SYSTEM_SCHEMA='FALSE'";
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
        if (StringUtils.contains(schema, '.')) {
            schema = StringUtils.split(schema, '.')[1];
        }
        String sql = getTableListSql(schema);
        // 若查找视图同义词，使用相应的sql
        String sqlvs = getViweListSql(schema);
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add("TABLE_NAME");
            }
            rs = sjdbc.executeQuery(sqlvs);
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_COLLECTION_ERROR, "无法获取TABLES列表");
        }
        return list;
    }

    private String getTableListSql(String schema) {
        return "SELECT TABLE_NAME FROM TABLES WHERE TABLE_SCHEMA='" + schema + "'";
    }

    private String getViweListSql(String schema) {
        return "SELECT TABLE_NAME FROM VIEWS WHERE TABLE_SCHEMA='" + schema + "'";
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
        if (StringUtils.contains(schema, '.')) {
            schema = StringUtils.split(schema, '.')[1];
        }
        String sql = getTableColumSql(sname, schema, tName);
        String viewSql = getViewColumSql(sname, schema, tName);
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
            }
            if (list.size() == 0) {
                rs = sjdbc.executeQuery(viewSql);
                while (rs.next()) {
                    list.add(rs.getString("COLUMN_NAME"));
                }
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

    private String getTableColumSql(String sname, String schema, String tName) {
        return "SELECT COLUMN_NAME FROM COLUMNS WHERE TABLE_SCHEMA='" + schema + "' AND TABLE_NAME='" + tName
            + "' ORDER BY ORDINAL_POSITION";
    }

    private String getViewColumSql(String sname, String schema, String tName) {
        return "SELECT COLUMN_NAME FROM VIEW_COLUMNS WHERE TABLE_SCHEMA='" + schema + "' AND TABLE_NAME='" + tName
            + "' ORDER BY ORDINAL_POSITION";
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
        if (StringUtils.contains(schema, '.')) {
            schema = StringUtils.split(schema, '.')[1];
        }
        String sql = getTableColumAndColTypeSql(sname, schema, tableName);
        String viewSql = getViewColumAndColTypeSql(sname, schema, tableName);
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                map.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                map.put("NULLABLE", "NULLABLE");
                list.add(map);
            }
            if (list.size() == 0) {
                rs = sjdbc.executeQuery(viewSql);
                while (rs.next()) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    map.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                    map.put("NULLABLE", rs.getString("NULLABLE"));
                    list.add(map);
                }
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

    private String getTableColumAndColTypeSql(String sname, String schema, String tName) {
        return "SELECT COLUMN_NAME,DATA_TYPE,(CASE WHEN IS_NULLABLE = 'TRUE' THEN 'Y' ELSE 'N' END) AS NULLABLE FROM COLUMNS WHERE "
            + " TABLE_SCHEMA='" + schema + "' AND TABLE_NAME='" + tName + "' ORDER BY ORDINAL_POSITION";
    }

    private String getViewColumAndColTypeSql(String sname, String schema, String tName) {
        return "SELECT COLUMN_NAME,DATA_TYPE,'N' NULLABLE FROM VIEW_COLUMNS WHERE "
            + " TABLE_SCHEMA='" + schema + "' AND TABLE_NAME='" + tName + "' ORDER BY ORDINAL_POSITION";
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
