package com.hzmc.dbmgr.dbenum;

import java.util.ArrayList;
import java.util.List;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年8月22日 上午10:04:33
 */
/**
 * 数据库版本
 * 
 * @author chengp
 *
 */
public enum DataBaseVersionEnum {
	KINGBASE_V7("kingbaseV7", "V7", 9), // 人大金仓
	KINGBASE_V8("kingbaseV8", "V8", 9),;

	private String dbName; // 数据库类型英文值
	private String dbVersion; // 数据库版本标识
	private int dbTypeNum; // 数据库类型int值

	DataBaseVersionEnum(String dbName, String dbVersion, int dbTypeNum) {
		this.dbName = dbName;
		this.dbVersion = dbVersion;
		this.dbTypeNum = dbTypeNum;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbVersion() {
		return dbVersion;
	}

	public int getDbTypeNum() {
		return dbTypeNum;
	}

	/**
	 * 通过数据库类型获取版本号
	 * 
	 * @param dbTypeNum
	 * @return
	 */
	public static List<DataBaseVersionEnum> getListByNum(int dbTypeNum) {
		List<DataBaseVersionEnum> list = new ArrayList<>();
		for (DataBaseVersionEnum dataBaseVersionEnum : DataBaseVersionEnum.values()) {
			if (dataBaseVersionEnum.getDbTypeNum() == dbTypeNum) {
				list.add(dataBaseVersionEnum);
			}
		}
		return list;
	}

}
