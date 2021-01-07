package com.hzmc.dbmgr.dbenum;

/**
 * @author: taozr
 * @date: 2018/8/6 14:47
 */
public enum RunModeEnum {
    //原先capaa逻辑中 0，1，2，3分别表示不激活、激活、模拟、学习
    //如果license过期，则给状态+4存入数据库，相当于4，5，6，7
    NORMAL("正式", 1),   //相当于原先的激活
    SIMULATION("模拟", 2),
    STUDY("学习", 3),
    //PASS("放行",0); //相当于原先的不激活
    ;

    private String text;

    private Integer number;

    public String getText() {
        return text;
    }

    public Integer getNumber() {
        return number;
    }

    RunModeEnum(String text, Integer number) {
        this.text = text;
        this.number = number;
    }

    /**
     * 添加保护对象和修改保护对象的时候使用
     * @param number
     * @return
     */
    public static Boolean isValidRunMode(Integer number) {
        for (RunModeEnum a : values()) {
            if (number.intValue() == a.number) {
                return true;
            }
        }
        return false;
    }

    public static String getRunModeCn(Integer runMode) {
        for (RunModeEnum a : values()) {
            if (runMode.intValue() == a.number.intValue()) {
                return a.text;
            }
        }
        if (runMode > 7)
            return "旁路";
        else
            return "过期";
    }

}
