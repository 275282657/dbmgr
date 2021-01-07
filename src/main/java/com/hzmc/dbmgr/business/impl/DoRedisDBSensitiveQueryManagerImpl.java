/**
 * hive类型生产库敏感资产查询处理(schema,table,column)
 */
package com.hzmc.dbmgr.business.impl;

import java.util.ArrayList;
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

import redis.clients.jedis.Jedis;

@Service("doRedisDBSensitiveQueryManagerImpl")
public class DoRedisDBSensitiveQueryManagerImpl implements DoSensitiveQueryManager {

    private static final Logger logger = Logger.getLogger(DoRedisDBSensitiveQueryManagerImpl.class);

    @Override
    public List getSchemaList(String sname, ProtectObject protectObject, String dbuser, String dbpass, Jdbc jdbc)
        throws RestfulException {
        List result = new ArrayList();
        boolean isconn = true;
        try {
            Jedis jedis = jdbc.getRedisClient();
            List<String> list = jedis.configGet("DATABASES");
            Integer num = Integer.valueOf(list.get(1));
            for (int i = 0; i < num; i++) {
                result.add("db" + i);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }

        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "无法获取database列表");
        }
        return result;
    }

    @Override
    public List<Object> getTableList(String sname, ProtectObject protectObject, String schema, String dbuser,
        String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList();

    }

    @Override
    public List<Object> getTableColum(String sname, String schema, ProtectObject protectObject, String tName,
        String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList<>();
    }

    @Override
    public List<Object> getSynonymsColum(String sname, String schema, ProtectObject protectObject, String synonymsName,
        String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, String>> getTableColumAndColType(String sname, ProtectObject protectObject, String schema,
        String tableName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, String>> getSynonymsColumAndColType(String sname, ProtectObject protectObject,
        String schema, String synonymsName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        return new ArrayList<>();
    }

    @Override
    public List getTableList(ProtectObject protectObject, String schema, Jdbc sjdbc) {
        return new ArrayList<>();
    }

    @Override
    public List<SchemaTables> queryAllSchemaTables(ProtectObject protectObject, String dbuser, String dbpass,
        Jdbc sjdbc, String type, String sname) {
        return new ArrayList<>();
    }

}
