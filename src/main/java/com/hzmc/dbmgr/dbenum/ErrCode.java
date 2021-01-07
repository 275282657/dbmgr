package com.hzmc.dbmgr.dbenum;

public enum ErrCode {

	SUCCESS("0","操作成功"),
	UNKNOWN_ID("610","未知的ID"),
	DIY_ERROR("660","自定义错误消息"),
	UNKNOW_ERROR("666","数据库服务未知错误"),
	PARAM_ERROR("669","参数错误"),
	PARAM_CHECK_ERROR("670","参数校验错误"),
	EXIST_OBJECT("671","内容已存在"),
	RESULT_NOT_FOUND("672","未找到任何记录"),
	SERVER_NO_RESPONSE("673","服务器没有响应"), 
	TRANSFER_ERROR("674","文件处理中出现错误"), 
	TIME_OUT("675", "操作超时"),
	SQL_EXCUTE_ERROR("999", "SQL执行失败"),
	ERROR_MSG("777", "数据库连接初始化失败"),
	OPERATE_NULL("233", "操作类型错误"),
	FILE_NULL("234", "文件为空"),

	OBJ_ID("1","保护对象id为空"),
	IP_NULL("2","ip为空"),
	APP_NAME_NULL("3", "应用程序名为空"),
	IP_END_NULL("4", "ip地址段结束为空"),
	IP_END_NOT_NULL("5", "ip地址段结束不应该空"),
	IP_EQUAL_IP_END("6", "ip与ip地址段结束不应该相同"),
	PROTECTOBJECT_NULL("7", "保护对象为空"),
	AGENT_SQL_NO_RUN("8", "准入脚本未安装"),
	AGENT_ID_NULL("9", "准入id为空"),
	UPDATE_AGENT_STATUS("10", "修改准入失败"),
	APP_NAME_ID_NULL("11","应用名称id为空"),
	APP_NAME_REPEAT("12", "应用名称重复"),
	CONNECTION_NULL("13", "缺少数据库连接"),
	GET_CONNECTION_ERROR("14", "获取数据库连接错误"),
	UPDATE_POXY_IP_FAIL("15", "修改代理ip失败"),
	DB_AGNET_FILE_NULL("16", "DB探针下载包不存在"),
	LOGIN_WHITE_NULL("17", "应用白名单不存在"),
	IP_REPEAT("18", "ip重复"),
	DELETE_AGENTSTATUS_FAIL("19", "删除准入信息失败"),
	APP_NAME_NO_EXIST("20", "应用程序不存在"),
	DB_STATUS_NOT_STOP("21", "DB探针状态不为停用"),

    GET_DATABASE_ERROR("22", "无法获取database列表"),
    GET_COLLECTION_ERROR("23", "无法获取collection列表"),
    GET_COLUMS_ERROR("24", "无法获取colums列信息"),
    GET_ALL_SYNONYMS_ERROR("26", "无法获取all_synonyms表的查询权限"),

    CONNECT_DB_ERROR("25", "数据库连接失败"),
	;

	private String value;
	private String message;
	
	private ErrCode(String value,String message){
		this.value = value;
		this.message = message;
	}
	
	public String toString() {
		return value;
	}
	public String getMessage() {
		return message;
	}
	public String getValue() {
		return value;
	}
	
	public static ErrCode getEnum(String value) {
		for (ErrCode a : values()) {
			if (value.equals(a.value)) {
				return a;
			}
		}
		throw new IllegalArgumentException("error value");
	}
}
