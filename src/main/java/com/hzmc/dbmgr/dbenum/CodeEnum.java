package com.hzmc.dbmgr.dbenum;
/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月17日 上午10:54:47
 */
public enum CodeEnum {

	SUCCESS(0, "操作成功");


	private Integer code;

	private String value;

	private CodeEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}

}
