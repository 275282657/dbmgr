/**
 * hive类型生产库敏感资产查询处理(schema,table,column)
 */
package com.hzmc.dbmgr.business.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.business.DoSensitiveQueryManager;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;
import com.hzmc.dbmgr.dbenum.ErrCode;

@Service("doEsDBSensitiveQueryManagerImpl")
public class DoEsDBSensitiveQueryManagerImpl implements DoSensitiveQueryManager {

    private static final Logger logger = Logger.getLogger(DoEsDBSensitiveQueryManagerImpl.class);



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
        try {
            RestHighLevelClient restHighLevelClient = sjdbc.getEsClient();
            GetAliasesRequest request = new GetAliasesRequest();
            GetAliasesResponse getAliasesResponse =
                restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
            Map<String, Set<AliasMetaData>> map = getAliasesResponse.getAliases();
            Set<String> indices = map.keySet();
            list.add("ES");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
        boolean isconn = true;
        try {
            RestHighLevelClient restHighLevelClient = sjdbc.getEsClient();
            GetAliasesRequest request = new GetAliasesRequest();
            GetAliasesResponse getAliasesResponse =
                restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
            Map<String, Set<AliasMetaData>> map = getAliasesResponse.getAliases();
            Set<String> indices = map.keySet();
            for (String key : indices) {
                list.add(key);
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
    	//todo
		List<Object> list = new ArrayList();
        boolean isconn = true;
		try {
            RestHighLevelClient restHighLevelClient = sjdbc.getEsClient();
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(tName);
            GetMappingsResponse getMappingResponse =
                restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings =
                getMappingResponse.mappings();
            ImmutableOpenMap<String, MappingMetaData> indexMapping = allMappings.get(tName);
            MappingMetaData data = indexMapping.get("properties");
            Map<String, Object> mapping = data.getSourceAsMap();
            for (String key : mapping.keySet()) {
                list.add(key);
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
        // todo
        List<Map<String, String>> list = new ArrayList();
        boolean isconn = true;
        try {
            RestHighLevelClient restHighLevelClient = sjdbc.getEsClient();
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(tableName);
            GetMappingsResponse getMappingResponse =
                restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings =
                getMappingResponse.mappings();
            ImmutableOpenMap<String, MappingMetaData> indexMapping = allMappings.get(tableName);
            MappingMetaData data = indexMapping.get("properties");
            Map<String, Object> mapping = data.getSourceAsMap();
            for (String key : mapping.keySet()) {
                Map<String, String> map = (Map<String, String>)mapping.get(key);
                Map<String, String> columnMap = new HashMap<String, String>();
                columnMap.put("COLUMN_NAME", key);
                if (StringUtils.isBlank(map.get("type"))) {
                    columnMap.put("DATA_TYPE", "json");
                } else {
                    columnMap.put("DATA_TYPE", map.get("type"));
                }
                columnMap.put("NULLABLE", "Y");
                list.add(columnMap);
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

	private String getType(Object columValue) {
		if (columValue instanceof String) {
			return "string";
		}
		if (columValue instanceof Timestamp) {
			return "timestamp";
		}
		if (columValue instanceof Date) {
			return "date";
		}
		if (columValue instanceof Integer) {
            return "integer";
		}
		if (columValue instanceof Double) {
			return "double";
		}
		if (columValue instanceof Arrays) {
			return "arrays";
		}
		if (columValue instanceof Boolean) {
			return "boolean";
		}
		if (columValue instanceof Collection) {
			return "collection";
		}
		return "unkown";
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
    	 List list = new ArrayList();
         try {

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
