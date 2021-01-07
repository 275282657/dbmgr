package com.hzmc.dbmgr.dbenum;

/**
* Created by chengp on 2020年10月27日
*/
public enum AssetDbType {

    schema("schema", 1), table("table", 2), column("column", 3), columnType("columnType", 4),
    schemaAndTable("schemaAndTable", 5);
    private String text;
    private Integer number;

    AssetDbType(String text, Integer number) {
        this.text = text;
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public Integer getNumber() {
        return number;
    }

    public static AssetDbType getAssetDbTypeEnum(int number) {
        for (AssetDbType assetDbType : AssetDbType.values()) {
            if (assetDbType.getNumber().intValue() == number) {
                return assetDbType;
            }
        }
        return null;
    }

    public static String getAssetDbTypeEnumText(int number) {
        for (AssetDbType assetDbType : AssetDbType.values()) {
            if (assetDbType.getNumber().intValue() == number) {
                return assetDbType.getText();
            }
        }
        return null;
    }

}
