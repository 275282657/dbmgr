package com.hzmc.dbmgr.bean;

import java.util.Properties;

/**
* Created by chengp on 2020年10月27日
*/
public class AssetSql {

    /**
     * 保护对象id
     */
    private Integer objId;

    /**
     * 数据库用户名
     */
    private String dbUser;

    /**
     * 数据库密码
     */
    private String dbPassword;

    /**
     * 资产类型
     */
    private Integer assetType;

    private String schemaName;

    private String tableName;

    private Properties properties;

    public Integer getObjId() {
        return objId;
    }

    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public Integer getAssetType() {
        return assetType;
    }

    public void setAssetType(Integer assetType) {
        this.assetType = assetType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "AssetSql [objId=" + objId + ", dbUser=" + dbUser + ", dbPassword=" + dbPassword + ", assetType="
            + assetType + ", schemaName=" + schemaName + ", tableName=" + tableName + ", properties=" + properties
            + "]";
    }

}
