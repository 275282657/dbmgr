package com.hzmc.dbmgr.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hzmc.dbmgr.bean.AssetSql;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.SchemaTables;
import com.hzmc.dbmgr.business.SensitiveQueryService;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.datasync.Jdbc;
import com.hzmc.dbmgr.dbenum.AssetDbType;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.service.ProtectObjectService;
import com.hzmc.dbmgr.util.DataBaseUtil;

/**
* Created by chengp on 2020年10月26日
*/
@RestController
@RequestMapping(value = "/assetSql")
public class ResultSetController {

    private static final Logger logger = LoggerFactory.getLogger(ResultSetController.class);

    @Autowired
    private SensitiveQueryService sensitiveQueryServiceImpl;

    @Autowired
    ProtectObjectService protectObjectService;

    @Value("${presto.ssl}")
    private String PRESTO_SSL;
    @Value("${presto.SSLKeyStorePath}")
    private String PRESTO_SSLKEYSTOREPATH;
    @Value("${presto.SSLKeyStorePassword}")
    private String PRESTO_SSLKEYSTOREPASSWORD;


    @ResponseBody
    @ExceptionHandler({Exception.class})
    public Object exception(Exception e) {
        ResultBean responseBean = null;
        if (e instanceof RestfulException) {
            responseBean = new ResultBean((RestfulException)e);
        } else {
            responseBean = new ResultBean(e);
        }
        return responseBean;
    }

    /**
     * 查询数据库资产信息
     * 
     * @param assetSql
     * @return
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResultBean queryAssetSql(@RequestBody AssetSql assetSql) {
        logger.info("assetSql" + assetSql.toString());
        AssetDbType assetDbType = AssetDbType.getAssetDbTypeEnum(assetSql.getAssetType());
        ProtectObject protectObject = protectObjectService.getProtectObjectById(assetSql.getObjId(), true);
        // 如果注册的hbase 则IP和端口用 关联的zookeeper的ip 端口
        setHbaseIP(protectObject);
        ResultBean bean = new ResultBean();
        Map<String, List> map = new HashMap<String, List>();
        assetSql.setProperties(getPropeties(assetSql, protectObject));
        switch (assetDbType) {
            case schema:
                List schemaList = sensitiveQueryServiceImpl.getSchemaList(assetSql, protectObject);
                map.put("result", schemaList);
                break;
            case table:
                List tableList = sensitiveQueryServiceImpl.getTableList(assetSql, protectObject);
                map.put("result", tableList);
                break;
            case column:
                List columnList = sensitiveQueryServiceImpl.getTableColum(assetSql, protectObject);
                map.put("result", columnList);
                break;
            case columnType:
                List<Map<String, String>> columnTypeList =
                    sensitiveQueryServiceImpl.getTableColumAndColType(assetSql, protectObject);
                map.put("result", columnTypeList);
                break;
            case schemaAndTable:
                List<SchemaTables> schemaTableList =
                    sensitiveQueryServiceImpl.queryAllSchemaTables(assetSql, protectObject);
                map.put("result", schemaTableList);
                break;
            default:
                throw new RestfulException(ErrCode.CONNECT_DB_ERROR, ErrCode.CONNECT_DB_ERROR.getMessage());
        }
        bean.setData(map);
        return bean;
    }

    /**
     * 查询数据库资产信息
     * 
     * @param assetSql
     * @return
     */
    @RequestMapping(value = "/check/dbConnection", method = RequestMethod.POST)
    public ResultBean checkdbConnect(@RequestBody AssetSql assetSql) {
        ProtectObject protectObject = protectObjectService.getProtectObjectById(assetSql.getObjId(), true);
        // 如果注册的hbase 则IP和端口用 关联的zookeeper的ip 端口
        setHbaseIP(protectObject);
        assetSql.setProperties(getPropeties(assetSql, protectObject));
        Jdbc jdbc = new Jdbc(assetSql, protectObject);
        jdbc.close();
        if (!jdbc.checkJdbc()) {
            throw new RestfulException(ErrCode.CONNECT_DB_ERROR, ErrCode.CONNECT_DB_ERROR.getMessage());
        }
        return new ResultBean();
    }

    /**
     * 根据DBid查询数据库url
     * 
     * @param objId
     * @return
     */
    @RequestMapping(value = "/query/dburl/{objId}", method = RequestMethod.GET)
    public ResultBean getDbUrl(@PathVariable Integer objId) {
        ProtectObject protectObject = protectObjectService.getProtectObjectById(objId, true);
        if (protectObject == null) {
            return new ResultBean();
        }
        // 如果注册的hbase 则IP和端口用 关联的zookeeper的ip 端口
        setHbaseIP(protectObject);
        ResultBean bean = new ResultBean();
        Map<String, String> map = new HashMap<>();
        map.put("url", DataBaseUtil.getUrl(protectObject));
        bean.setData(map);
        return bean;
    }

    private Properties getPropeties(AssetSql assetSql, ProtectObject protectObject) {
        Properties properties = new Properties();
        if (protectObject.getDbType().intValue() == DBEnum.PRESTO.getNumber()) {
            if (StringUtils.equals(PRESTO_SSL, "true")) {
                properties.setProperty("SSL", PRESTO_SSL);
                properties.setProperty("SSLKeyStorePath", PRESTO_SSLKEYSTOREPATH);
                properties.setProperty("SSLKeyStorePassword", PRESTO_SSLKEYSTOREPASSWORD);
            }
        }
        return properties;
    }

    private void setHbaseIP(ProtectObject protectObject) {
        if (protectObject.getDbType().intValue() == DBEnum.HBASE.getNumber().intValue()) {
            ProtectObject zkProtectObject = protectObjectService.getProtectObjectById(protectObject.getZkObjId(), true);
            protectObject.setZkProtectObject(zkProtectObject);
        }
    }


}
