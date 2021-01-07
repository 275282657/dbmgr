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

@Service("doDm6SensitiveQueryManagerImpl")
public class DoDm6SensitiveQueryManagerImpl implements DoSensitiveQueryManager {

    private static final Logger logger = Logger.getLogger(DoDm6SensitiveQueryManagerImpl.class);

    /**
     * @param sname
     * @param db
     * @return list
     */
    @Override
    public List<Map> getSchemaList(String sname, ProtectObject protectObject, String username, String dbpass,
                                   Jdbc sjdbc) throws RestfulException {
        List list = new ArrayList();

        boolean isconn = true;
        String sql = getSchemaListSql();
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(protectObject.getServiceName() + "." + rs.getString("NAME"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "请给用户授予SYSDBA.SYSSCHEMAS表的查询权限");
        }
        return list;
    }

    private String getSchemaListSql() {
        String sql =
            "SELECT  NAME FROM SYSDBA.SYSSCHEMAS WHERE NAME NOT IN ('INFORMATION_SCHEMA','SYSDBA','INFO_SCHEM','SYSAUDITOR','SYSSSO')";
        return sql;
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
        String sql = getTableListSql(sname, schema);

        // 若查找视图同义词，使用相应的sql
        String sqlvs = getViweListSql(schema);

        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME"));
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
            throw new RestfulException(ErrCode.GET_COLLECTION_ERROR, "请给用户授予all_tables表的查询权限");
        }

        return list;
    }

    private String getTableListSql(String sname, String schema) {
        return "SELECT  B.NAME TABLE_NAME FROM SYSDBA.SYSSCHEMAS A INNER JOIN SYSDBA.SYSTABLES B ON A.SCHID=B.SCHID WHERE  B.TYPE<>'S' AND A.NAME='"
            + schema + "'";
    }

    private String getViweListSql(String schema) {
        return "select B.NAME TABLE_NAME from SYSDBA.SYSSCHEMAS A INNER JOIN SYSDBA.SYSVIEWS B on  A.SCHID=B.SCHID where B.TYPE<>'S' AND A.NAME='"
            + schema + "'";
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
            throw new RestfulException(ErrCode.GET_COLUMS_ERROR, "请给用户授予all_tab_columns表的查询权限");
        }
        return list;
    }

    private String getTableColumSql(String sname, String schema, String tName) {
        return "SELECT  C.NAME COLUMN_NAME FROM SYSDBA.SYSSCHEMAS A, SYSDBA.SYSTABLES B , SYSDBA.SYSCOLUMNS C WHERE A.SCHID=B.SCHID AND B.ID=C.ID AND"
            + " A.NAME='" + schema + "' AND B.NAME='" + tName + "'";
    }

    private String getViewColumSql(String sname, String schema, String tName) {
        return "SELECT  C.NAME COLUMN_NAME FROM SYSDBA.SYSSCHEMAS A, SYSDBA.SYSVIEWS B , SYSDBA.SYSCOLUMNS C WHERE A.SCHID=B.SCHID AND B.ID=C.ID AND"
            + " A.NAME='" + schema + "' AND B.NAME='" + tName + "'";
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
        List list = new ArrayList();
        if (StringUtils.contains(schema, '.')) {
            schema = StringUtils.split(schema, '.')[1];
        }
        String tableOwner = null;
        String tableName = null;
        Map<String, String> tableInfoMap = new HashMap<String, String>();
        tableInfoMap = getSynonymsTableInfo(sname, schema, protectObject, synonymsName, username, dbpass, sjdbc);
        tableOwner = tableInfoMap.get("tableOwner");
        tableName = tableInfoMap.get("tableName");
        // 未查询到表格信息时，直接返回
        if (tableOwner == null || tableName == null) {
            return list;
        }
        // 查询到表格信息时，继续查询列信息
        list = getTableColum(sname, tableOwner, protectObject, tableName, username, dbpass, sjdbc);
        return list;
    }

    /**
     * 获取同义词对应表格信息
     *
     * @param sname
     * @param schema
     *            schema名
     * @param protectObject
     *            生产库对象
     * @param synonymsName
     *            同义词名
     * @param username
     * @param dbpass
     * @param sjdbc
     * @return Map<String, String> 表格信息map
     * @throws RestfulException
     */
    private Map<String, String> getSynonymsTableInfo(String sname, String schema, ProtectObject protectObject,
        String synonymsName, String username, String dbpass, Jdbc sjdbc) throws RestfulException {
        HashMap<String, String> tableInfoMap = new HashMap<String, String>();
        String sql = "";
        // 获取同义词对应表格信息
        sql = getSynonymsTableInfoSql(sname, schema, synonymsName);
        boolean isconn = true;
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                tableInfoMap.put("tableOwner", rs.getString("TABLE_OWNER"));
                tableInfoMap.put("tableName", rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_ALL_SYNONYMS_ERROR, "请给用户授予all_synonyms表的查询权限");
        }
        return tableInfoMap;
    }

    /**
     * 获取同义词对应表格信息sql
     *
     * @param sname
     * @param schema
     *            schema名
     * @param synonymsName
     *            同义词名
     * @return
     */
    private String getSynonymsTableInfoSql(String sname, String schema, String synonymsName) {
        return "SELECT A.NAME TABLE_OWNER,B.NAME TABLE_NAME FROM SYSDBA.SYSSCHEMAS A INNER JOIN SYSDBA.SYSPRIVATESYNONYMS B ON A.SCHID=B.SCHID WHERE A.SCHID=B.SCHID AND B.UOBJSCHNAME='"
            + schema + "' AND B.UOBJNAME ='"
            + synonymsName + "'";
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
                map.put("NULLABLE", rs.getString("NULLABLE"));
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
            throw new RestfulException(ErrCode.GET_ALL_SYNONYMS_ERROR, "请给用户授予all_tab_columns表的查询权限");
        }
        return list;
    }

    private String getTableColumAndColTypeSql(String sname, String schema, String tName) {
        return "SELECT  C.NAME COLUMN_NAME,C.TYPE DATA_TYPE,C.NULLABLE NULLABLE FROM SYSDBA.SYSSCHEMAS A, SYSDBA.SYSTABLES B , SYSDBA.SYSCOLUMNS C WHERE A.SCHID=B.SCHID AND B.ID=C.ID AND"
            + " A.NAME='" + schema + "' and B.NAME='" + tName + "'" + " ORDER BY COLID";
    }

    private String getViewColumAndColTypeSql(String sname, String schema, String tName) {
        return "SELECT  C.NAME COLUMN_NAME,C.TYPE DATA_TYPE,C.NULLABLE NULLABLE FROM SYSDBA.SYSSCHEMAS A, SYSDBA.SYSVIEWS B , SYSDBA.SYSCOLUMNS C WHERE A.SCHID=B.SCHID AND B.ID=C.ID AND"
            + " A.NAME='" + schema + "' and B.NAME='" + tName + "'" + " ORDER BY COLID";
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
        List list = new ArrayList();
        if (StringUtils.contains(schema, '.')) {
            schema = StringUtils.split(schema, '.')[1];
        }
        String tableOwner = null;
        String tableName = null;
        Map<String, String> tableInfoMap = new HashMap<String, String>();
        tableInfoMap = getSynonymsTableInfo(sname, schema, protectObject, synonymsName, dbuser, dbpass, sjdbc);
        tableOwner = tableInfoMap.get("tableOwner");
        tableName = tableInfoMap.get("tableName");
        // 未查询到表格信息时，直接返回
        if (tableOwner == null || tableName == null) {
            return list;
        }
        // 查询到表格信息时，继续查询列信息
        list = getTableColumAndColType(sname, protectObject, tableOwner, tableName, dbuser, dbpass, sjdbc);
        return list;
    }

    /**
     * JDBC获取表格
     *
     * @param schema
     * @return list
     */
    @Override
    public List getTableList(ProtectObject protectObject, String schema, Jdbc sjdbc) {
        return new ArrayList<>();
    }


	@Override
    public List<SchemaTables> queryAllSchemaTables(ProtectObject protectObject, String dbuser, String dbpass,
        Jdbc sjdbc, String type, String sname) {
        String sql = null;
        if (StringUtils.equalsIgnoreCase("table", sname)) {
            sql =
                "SELECT  A.NAME TABLE_SCHEMA ,B.NAME TABLE_NAME FROM SYSDBA.SYSSCHEMAS A INNER JOIN SYSDBA.SYSTABLES B ON A.SCHID=B.SCHID WHERE A.NAME NOT IN('INFORMATION_SCHEMA','SYSDBA','INFO_SCHEM','SYSAUDITOR','SYSSSO')";
        } else {
            sql =
                "SELECT  NAME TABLE_SCHEMA FROM SYSDBA.SYSSCHEMAS WHERE NAME NOT IN ('INFORMATION_SCHEMA','SYSDBA','INFO_SCHEM','SYSAUDITOR','SYSSSO')";
        }
        List<SchemaTables> list = new ArrayList<>();
        try {
            ResultSet rs = sjdbc.executeQuery(sql);
            while (rs.next()) {
                SchemaTables schemaTables = new SchemaTables();
                schemaTables.setSchemaName(rs.getString("TABLE_SCHEMA"));
                if (StringUtils.equalsIgnoreCase("table", sname)) {
                    schemaTables.setTableName(rs.getString("TABLE_NAME"));
                }
                list.add(schemaTables);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

}
