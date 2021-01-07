package com.hzmc.dbmgr.business.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.business.DoSensitiveQueryManager;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;
import com.hzmc.dbmgr.dbenum.ErrCode;

/**
* Created by chengp on 2020年12月14日
*/

@Service("doHbaseSensitiveQueryManagerImpl")
public class DoHbaseSensitiveQueryManagerImpl implements DoSensitiveQueryManager {

    private static final Logger logger = Logger.getLogger(DoHbaseSensitiveQueryManagerImpl.class);

    @Override
    public List getSchemaList(String sname, ProtectObject protectObject, String dbuser, String dbpass, Jdbc jdbc)
        throws RestfulException {
        List list = new ArrayList();
        boolean isconn = true;
        try {
            Connection hbase = jdbc.getHbaseClient();
            NamespaceDescriptor[] nameSpaces = hbase.getAdmin().listNamespaceDescriptors();
            for (NamespaceDescriptor namespaceDescriptor : nameSpaces) {
                list.add(namespaceDescriptor.getName());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "无法获取namaspace列表");
        }
        return list;
    }

    @Override
    public List<Object> getTableList(String sname, ProtectObject protectObject, String schema, String dbuser,
        String dbpass, Jdbc sjdbc) throws RestfulException {

        List list = new ArrayList();
        boolean isconn = true;
        try {
            Connection hbase = sjdbc.getHbaseClient();
            TableName[] tables = hbase.getAdmin().listTableNamesByNamespace(schema);
            for (TableName tableName : tables) {
                list.add(tableName.getQualifierAsString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "无法获取tables列表");
        }
        return list;
    }

    @Override
    public List<Object> getTableColum(String sname, String schema, ProtectObject protectObject, String tName,
        String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {

        List list = new ArrayList();
        boolean isconn = true;
        try {
            Connection hbaseClient = sjdbc.getHbaseClient();
            tName = schema + ":" + tName;
            TableName tableName = TableName.valueOf(tName);
            HTableDescriptor descriptors = hbaseClient.getAdmin().getTableDescriptor(tableName);
            Set<byte[]> bs = descriptors.getColumnFamilyNames();
            for (byte[] bs2 : bs) {
                list.add(new String(bs2));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            isconn = false;
        }
        if (!isconn) {
            throw new RestfulException(ErrCode.GET_DATABASE_ERROR, "无法获取columns列表");
        }
        return list;
    }

    @Override
    public List<Object> getSynonymsColum(String sname, String schema, ProtectObject protectObject, String synonymsName,
        String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        List<Object> list = new ArrayList();
        return list;
    }

    @Override
    public List<Map<String, String>> getTableColumAndColType(String sname, ProtectObject protectObject, String schema,
        String tableName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        List list = new ArrayList();
        boolean isconn = true;
        try {
            Connection hbaseClient = sjdbc.getHbaseClient();
            tableName = schema + ":" + tableName;
            TableName tabN = TableName.valueOf(tableName);
            HTableDescriptor descriptors = hbaseClient.getAdmin().getTableDescriptor(tabN);
            Set<byte[]> bs = descriptors.getColumnFamilyNames();
            for (byte[] bs2 : bs) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("COLUMN_NAME", new String(bs2));
                map.put("DATA_TYPE", "string");
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

    @Override
    public List<Map<String, String>> getSynonymsColumAndColType(String sname, ProtectObject protectObject,
        String schema, String synonymsName, String dbuser, String dbpass, Jdbc sjdbc) throws RestfulException {
        return null;
    }

    @Override
    public List getTableList(ProtectObject protectObject, String schema, Jdbc sjdbc) {
        return null;
    }

    @Override
    public List<SchemaTables> queryAllSchemaTables(ProtectObject protectObject, String dbuser, String dbpass,
        Jdbc sjdbc, String type, String sname) {
        return new ArrayList<>();
    }

}
