package com.hzmc.dbmgr.dbenum;

/**
 * @author: taozr
 * @date: 2018/8/6 10:57
 */
public enum StatusTypeEnum {
    STOP("停止",0),
    START("启用",1);

    private String text;

    private Integer number;

    public String getText() {
        return text;
    }

    public Integer getNumber() {
        return number;
    }

    StatusTypeEnum(String text, Integer number) {
        this.text = text;
        this.number = number;
    }

    public static Boolean isValidStatusType(Integer number) {
        for (StatusTypeEnum a : values()) {
            if (number.intValue() == a.number.intValue()) {
                return true;
            }
        }
        return false;
    }
}
