package com.hzmc.dbmgr.dbenum;

/**
 * 保护对象类型枚举
 */
public enum ObjTypeEnum {
    //FOLDER("分组", 0),//分组不会放到保护对象表里面了
    SINGLE("单库", 1),
    CLUSTER("集群", 2),
    NODE("节点",3);

    private String text;

    private Integer number;

    public String getText() {
        return text;
    }

    public Integer getNumber() {
        return number;
    }

    ObjTypeEnum(String text, Integer number) {
        this.text = text;
        this.number = number;
    }
}
