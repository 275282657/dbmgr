package com.hzmc.dbmgr.dbenum;

public enum DBEnum {
	ORACLE("Oracle",1),
	SQLSERVER("SQL Server",2),
	MYSQL("MySQL",3),
	SYBASE("Sybase",4),
	DB2("DB2",5),
	POSTGRESQL("PostgreSQL",6),
	GBASE_S83("GBase 8s 8.3", 7),
	DAMENG("DM",8),
	KINGBASE("KingBase",9),
	INFORMIX("Informix",10),
	MARIADB("Mariadb",11),
	GBASE_S87("Gbase 8s 8.7", 12),
	HIVE("Hive",13),
	UXDB("Uxdb",14),//优炫
	MONGODB("Mongodb",15),
	CACHE("Cache", 16), // CACHE
	OCEANBASE("OceanBase", 17), // oceanbase
	HANA("Hana", 18), // hana
	GREENPLUM("Greenplum", 19),// greenplum
	OSCDB("oscar", 20),
    // RDSMYSQL("RDS(MySQL)", 21),
    // RDSPOSTGRESQL("RDS(PostgreSQL)", 22),
     ODPS("MaxCompute", 23),
    HOTDB("hotdb", 24),
    TIDB("tidb", 25),
    SEQUOIADBMYSQL("sequoiadbmysql", 26),
    TRANSWARP("transwarp",27),
    ENTERPRISEDB("enterprisedb",28),
    ELASTICSEARCH("elasticsearch",29),
    PRESTO("presto",30),
    HBASE("hbase", 31),
    REDIS("redis", 32),
    ZOOKEEPER("zookeeper", 33),
    DM6("dm6", 34),
    HPVertica("vertica", 35),
	;

	private String text;

	private Integer number;
	
	DBEnum(String text,Integer number){
		this.text = text;
		this.number = number;
	}

	public String getText() {
		return text;
	}

	public Integer getNumber() {
		return number;
	}

	public static Boolean isValidDbType(Integer number) {
		for (DBEnum a : values()) {
			if (number.intValue() == a.number.intValue()) {
				return true;
			}
		}
		return false;
	}

	public static int getNumberByDbText(String text) {
		for (DBEnum a : values()) {
			if (a.text.equalsIgnoreCase(text)) {
				return a.number;
			}
		}
		return 0;
	}

	public static String getTextByDbNumber(Integer number) {
		for (DBEnum a : values()) {
			if (a.number.intValue() == number.intValue()) {
				return a.text;
			}
		}
		return "数据库类型错误";
	}

	public static DBEnum getDbtypeByDbTypeNum(Integer dbTypeNum) {
		if (null == dbTypeNum) {
			return null;
		}
		for (DBEnum dBEnum : DBEnum.values()) {
			if (dBEnum.getNumber() == dbTypeNum) {
				return dBEnum;
			}
		}
		return null;
	}

}
