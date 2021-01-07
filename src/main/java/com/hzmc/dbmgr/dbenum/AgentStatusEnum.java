package com.hzmc.dbmgr.dbenum;
/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月24日 下午2:41:23
 */
public enum AgentStatusEnum {

	NOT_INSTALL("未安装", 99),
    START("启用",1),
    SIMULATION("模拟启用",2),
	STOP("停用", 0),
    ERROR("异常",-1),
    ;

	private String text;

	private Integer number;

	public String getText() {
		return text;
	}

	public Integer getNumber() {
		return number;
	}

	AgentStatusEnum(String text, Integer number) {
        this.text = text;
        this.number = number;
    }


}
