package com.hzmc.dbmgr.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.hzmc.dbmgr.bean.ProtectObject;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月11日 下午6:31:09
 */
public class DruidDataSourceUtil {

	private static final Logger logger = LoggerFactory.getLogger(DruidDataSourceUtil.class);

	public static Map<Integer, DruidDataSource> druidMap = null;

	private static DruidDataSourceUtil single;

	/**
	 * 获取实例
	 */
	public synchronized static DruidDataSourceUtil getInstance() {
		if (single == null) {
			single = new DruidDataSourceUtil();
			druidMap = new HashMap<>();
		}
		return single;
	}

	/**
	 * 新增数据库连接
	 * 
	 * @param protectObject
	 * @throws SQLException
	 */
	public void addDruidDataBase(ProtectObject protectObject) throws SQLException {
		DruidDataSource dataSource = new DruidDataSource();
		String url = DataBaseUtil.getUrl(protectObject);
		String driverClassName = DataBaseUtil.getDriverClassName(protectObject);
		String testSql = DataBaseUtil.getTestSql(protectObject);
		dataSource.setUrl(url);
		dataSource.setUsername(protectObject.getDbUser());
		dataSource.setPassword(protectObject.getDbPassword());
		// 连接池可以自动识别,但最好填上
		dataSource.setDriverClassName(driverClassName);
		// 测试sql
		dataSource.setValidationQuery(testSql);
		// 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
		dataSource.setFilters("stat,wall,slf4j");
		// 初始化连接数
		dataSource.setInitialSize(2);
		// 最小连接池数量
		dataSource.setMinIdle(2);
		//最大连接池数量
		dataSource.setMaxActive(5);
		// 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
		dataSource.setMaxWait(10000);
		// 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		dataSource.setTestOnBorrow(true);
		// 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
		dataSource.setTestOnReturn(false);
		// 配置是否失败重连
		// dataSource.setBreakAfterAcquireFailure(true);
		// 失败重连次数
		// dataSource.setConnectionErrorRetryAttempts(1);
		// 超时回收
		dataSource.setRemoveAbandoned(true);
		// 超时回收时间(单位s)
		dataSource.setRemoveAbandonedTimeout(180);
		// <!-- 关闭abanded连接时输出错误日志 -->
		dataSource.setLogAbandoned(true);
		// 获取连接
		// 如果获取失败则清除dataSource,防止妨碍druid monitor的信息统计
		DruidPooledConnection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (Exception e) {
			dataSource.close();
			dataSource = null;
			throw e;
		} finally {
			// 关闭连接
			closeConnection(connection);
		}
		druidMap.put(protectObject.getObjId(), dataSource);
	}

	/**
	 * 根据保护对象id获取数据库连接
	 * 
	 * @param objId
	 * @return
	 * @throws SQLException
	 */
	public DruidPooledConnection getConnection(Integer objId) throws SQLException {
		DruidDataSource source = null;
		if (druidMap.containsKey(objId)) {
			source = druidMap.get(objId);
			if (source == null) {
				druidMap.remove(objId);
				return null;
			}
			return source.getConnection();
		} else {
			return null;
		}
	}

	/**
	 * 根据保护对象id获取DruidDataSource
	 * 
	 * @param objId
	 */
	public DruidDataSource getDruidDataSource(Integer objId) {
		DruidDataSource source = null;
		if (druidMap.containsKey(objId)) {
			source = druidMap.get(objId);
		}
		return source;
	}

	/**
	 * 根据保护对象id移除数据库连接
	 * 
	 * @param objId
	 */
	public void removeConnection(Integer objId) {
		if (druidMap.containsKey(objId)) {
			DruidDataSource source = druidMap.get(objId);
			if (source != null) {
				source.close();
				source.dump();
				source = null;
			}
			druidMap.remove(objId);
		}
	}


	public void closeConnection(DruidPooledConnection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public static void main(String[] args) {
		ProtectObject protectObject = new ProtectObject();
		protectObject.setIp("192.168.61.15");
		protectObject.setInstanceName("MSSQLServer");
		protectObject.setIsUdp(1);
		protectObject.setDbType(2);
		protectObject.setDbUser("sa");
		protectObject.setDbPassword("123");
		try {
			DruidDataSourceUtil.getInstance().addDruidDataBase(protectObject);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
