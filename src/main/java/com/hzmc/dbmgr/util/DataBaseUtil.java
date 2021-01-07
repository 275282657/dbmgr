package com.hzmc.dbmgr.util;

import org.apache.commons.lang.StringUtils;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.DataBaseVersionEnum;

import dm.jdbc.util.IPAddressUtil;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月12日 上午9:32:12
 */
public class DataBaseUtil {

	public static String getUrl(ProtectObject protectObject) {
		String ip = protectObject.getIp();
		Integer port = protectObject.getPort();
		String dataBase = protectObject.getServiceName();
        if (StringUtils.isBlank(dataBase)) {
            dataBase = "";
        }
		String server = protectObject.getInstanceName();
        if (StringUtils.isBlank(server)) {
            server = "";
        }
		Integer dbType = protectObject.getDbType();
		String version = protectObject.getVersion();
        if (IPAddressUtil.isIPv6LiteralAddress(ip)) {
            return getIpv6Url(ip, port, dbType, dataBase, server, version, protectObject);
        }
		String url = "jdbc:oracle:thin:@" + ip + ":" + port + "/" + dataBase;
		switch (DBEnum.getDbtypeByDbTypeNum(dbType)) {
		case ORACLE:
			 url = "jdbc:oracle:thin:@" + ip + ":" + port + "/" + dataBase;
			break;
		case SQLSERVER:
			if (protectObject.getIsUdp() == Integer.valueOf(1)) {
				url = "jdbc:sqlserver://" + ip + "\\" + protectObject.getInstanceName();
			} else {
				url = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";DatabaseName=" + dataBase;
			}
			break;
		case MYSQL:
			url="jdbc:mysql://" + ip + ":" + port + "/" + dataBase+"?useSSL=false&useUnicode=true&characterEncoding=utf-8";
			break;
		case GBASE_S83:
			url = "jdbc:gbase://" + ip + ":" + port + "/" + dataBase;
			break;
		case DB2:
			url = "jdbc:db2://" + ip + ":" + port + "/" + dataBase;
			break;
		case SYBASE:
			url = "jdbc:sybase:Tds:" + ip + ":" + port + "/" + dataBase;
			break;
		case POSTGRESQL:
			url = "jdbc:postgresql://" + ip + ":" + port + "/" + dataBase;
			break;
		case DAMENG:
			url = "jdbc:dm://" + ip + ":" + port + "/" + dataBase;
			break;
		case HIVE:
			url = "jdbc:hive2://" + ip + ":" + port + "/" + dataBase;
			break;
		case UXDB:
			url = "jdbc:postgresql://" + ip + ":" + port + "/" + dataBase;
			break;
		case KINGBASE:
			if (StringUtils.equals(DataBaseVersionEnum.KINGBASE_V7.getDbVersion(), version)) {
				// 人大金仓V7
				url = "jdbc:kingbase://" + ip + ":" + port + "/" + dataBase;
			}
			if (StringUtils.equals(DataBaseVersionEnum.KINGBASE_V8.getDbVersion(), version)) {
				// 人大金仓V8
				url = "jdbc:kingbase8://" + ip + ":" + port + "/" + dataBase;
			}
			break;
		case MONGODB:
			url = "mongodb://%s" + ip + ":" + port;
			break;
		case GBASE_S87:
			url = "jdbc:informix-sqli://" + ip + ":" + port + "/" + dataBase + ":INFORMIXSERVER=" + server;
			break;
		case PRESTO:
            url = "jdbc:presto://" + ip + ":" + port + "/" + dataBase;
            break;
        case DM6:
            url = "jdbc:dm://" + ip + ":" + port + "/" + dataBase;
            break;
        case HPVertica:
            url = "jdbc:vertica://" + ip + ":" + port + "/" + dataBase;
            break;
		default:
			break;
		}
		return url;
	}
	
    public static String getIpv6Url(String ip, Integer port, int dbType, String dataBase, String server,
        String version,ProtectObject protectObject) {
        String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=[" + ip + "])(PORT=" + port
            + "))(CONNECT_DATA=(SERVICE_NAME=" + dataBase + ")))"; // Oracle Service Name
        switch (DBEnum.getDbtypeByDbTypeNum(dbType)) {
            case ORACLE:
                url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=[" + ip + "])(PORT=" + port
                + "))(CONNECT_DATA=(SERVICE_NAME=" + dataBase + ")))"; // Oracle Service Name
                break;
            case SQLSERVER:
                if (protectObject.getIsUdp() == Integer.valueOf(1)) {
                    url = "jdbc:sqlserver://" + ip + "\\" + protectObject.getInstanceName();
                } else {
                    url = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";DatabaseName=" + dataBase;
                }
                break;
            case MYSQL:
                url = "jdbc:mysql://address=(protocol=tcp)(host=" + ip + ")(port=" + port + ")/" + dataBase;
                break;
            case GBASE_S83:
                url = "jdbc:gbase://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case DB2:
                url = "jdbc:db2://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case SYBASE:
                url = "jdbc:jtds:sybase://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case POSTGRESQL:
                url = "jdbc:postgresql://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case DAMENG:
                url = "jdbc:dm://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case HIVE:
                url = "jdbc:hive2://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case UXDB:
                url = "jdbc:postgresql://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case KINGBASE:
                if (StringUtils.equals(DataBaseVersionEnum.KINGBASE_V7.getDbVersion(), version)) {
                    // 人大金仓V7
                    url = "jdbc:kingbase://[" + ip + "]:" + port + "/" + dataBase;
                }
                if (StringUtils.equals(DataBaseVersionEnum.KINGBASE_V8.getDbVersion(), version)) {
                    // 人大金仓V8
                    url = "jdbc:kingbase8://[" + ip + "]:" + port + "/" + dataBase;
                }
                break;
            case MONGODB:
                url = "mongodb://[" + ip + "]:" + port;
                break;
            case GBASE_S87:
                url = "jdbc:gbasedbt-sqli://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case PRESTO:
                url = "jdbc:presto://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case DM6:
                url = "jdbc:dm://[" + ip + "]:" + port + "/" + dataBase;
                break;
            case HPVertica:
                url = "jdbc:vertica://[" + ip + "]:" + port + "/" + dataBase;
                break;
            default:
                break;
        }
        return url;
    }
    

	public static String getDriverClassName(ProtectObject protectObject) {
		Integer dbType = protectObject.getDbType();
		String version = protectObject.getVersion();
		String driverClassName = null;
		switch (DBEnum.getDbtypeByDbTypeNum(dbType)) {
		case ORACLE:
			driverClassName = "oracle.jdbc.OracleDriver";
			break;
		case SQLSERVER:
			if (protectObject.getIsUdp() == Integer.valueOf(1)) {
				driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			} else {
				driverClassName = "net.sourceforge.jtds.jdbc.Driver";
			}
			break;
		case MYSQL:
			driverClassName = "com.mysql.jdbc.Driver";
			break;
		case GBASE_S83:
			driverClassName = "com.gbase.jdbc.Driver";
			break;
		case DB2:
			driverClassName = "com.ibm.db2.jcc.DB2Driver";
			break;
		case POSTGRESQL:
			driverClassName = "org.postgresql.Driver";
			break;
		case DAMENG:
			driverClassName = "dm.jdbc.driver.DmDriver";
			break;
		case HIVE:
			driverClassName = "org.apache.hive.jdbc.HiveDriver";
			break;
		case UXDB:
			driverClassName = "org.postgresql.Driver";
			break;
		case KINGBASE:
			if (StringUtils.equals(DataBaseVersionEnum.KINGBASE_V7.getDbVersion(), version)) {
				// 人大金仓V7
				driverClassName = "com.kingbase.Driver";
			}
			if (StringUtils.equals(DataBaseVersionEnum.KINGBASE_V8.getDbVersion(), version)) {
				// 人大金仓V8
				driverClassName = "com.kingbase8.Driver";
			}
			break;
		case GBASE_S87:
			driverClassName = "com.gbase.jdbc.Driver";
			break;
	    case DM6:
	        driverClassName = "dm.jdbc.driver.DmDriver";
	        break;
		default:
			break;

		}
		return driverClassName;
	}

	public static String getTestSql(ProtectObject protectObject) {
		Integer dbType = protectObject.getDbType();
		String testSql = null;
		if (DBEnum.ORACLE.getNumber() == dbType || DBEnum.DAMENG.getNumber() == dbType) {
			testSql = "select 1 from dual";
		}else {
			testSql="select 1";
		}
		return testSql;
	}

	public static String getDbAgentStatusSql(Integer dbType) {
		String sql = null;
		switch (DBEnum.getDbtypeByDbTypeNum(dbType)) {
		case ORACLE:
			sql = "select dbagent_status from dual";
			break;
		case SQLSERVER:
			sql = "select dbo.dbagent_status()";
			break;
		default:
			break;
		}
		return sql;
	}

}
