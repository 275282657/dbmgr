/**
 * 版权所有：美创科技
 * 项目名称:capaa-setupdbs
 * 创建者: cq
 * 创建日期: 2012-7-5
 * 文件说明:
 * 最近修改者：cq
 * 最近修改日期：2012-7-5
 */
package com.hzmc.dbmgr.datasync;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.log4j.Logger;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;

import com.hzmc.dbmgr.bean.AssetSql;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.util.DataBaseUtil;

import redis.clients.jedis.Jedis;


/**
 * @author cq
 */
public class Jdbc {

	private static final Logger	LOG		= Logger.getLogger(Jdbc.class);
	private Connection			con		= null;
	private Statement			st;
	public ResultSet			rs		= null;
	private boolean				status	= false;
    private String errormsg = null;
	private String unuser = "mc$unuser";
	
    private RestHighLevelClient restHighLevelClient;

    private org.apache.hadoop.hbase.client.Connection hbaseClient;

    private Jedis jedis;

    public String getErrormsg() {
        return errormsg;
    }

    /**
     * 初始化
     * 
     * @param dbUrl
     * @param user
     * @param password
     * @param driver
     */
    public Jdbc(AssetSql assetSql, ProtectObject protectObject) {
		long start = System.currentTimeMillis();
        String dbUrl = null;
        DBEnum dbEnum = DBEnum.getDbtypeByDbTypeNum(protectObject.getDbType());
		try {
            switch (dbEnum) {
                case REDIS:
                    setRedisClient(assetSql, protectObject);
                    break;
                case HBASE:
                    setHbaseClient(assetSql, protectObject);
                    break;
                case ELASTICSEARCH:
                    setEsClinet(assetSql, protectObject);
                    break;
                default:
                    setPulicConnection(assetSql, protectObject);
                    break;
            }
			status=true;
		} catch (Exception e) {
            errormsg = e.getMessage();
            LOG.error("jdbc url:" + dbUrl);
			LOG.error("jdbc:" + e.getMessage(), e);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Get jdbc connnection cost:" + (System.currentTimeMillis() - start) + "ms");
		}
	}

    /**
     * 获取reids连接
     */
    private void setRedisClient(AssetSql assetSql, ProtectObject protectObject) throws Exception {
        this.jedis = new Jedis(protectObject.getIp(), protectObject.getPort());
        if (StringUtils.isNotBlank(assetSql.getDbPassword())) {
            jedis.auth(assetSql.getDbPassword());
        }
    }

    /**
     * 获取Hbase连接
     */
    private void setHbaseClient(AssetSql assetSql, ProtectObject protectObject) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        if (protectObject.getZkProtectObject() == null) {
            LOG.error("hbase数据库并关联zookeeper数据库不存在");
            return;
        }
        ProtectObject zkProtectObject = protectObject.getZkProtectObject();
        conf.set("hbase.zookeeper.quorum", zkProtectObject.getIp());
        conf.set("hbase.zookeeper.property.clientPort", String.valueOf(zkProtectObject.getPort()));
        this.hbaseClient = ConnectionFactory.createConnection(conf);
    }

    /**
     * 获取ES连接
     * 
     * @param assetSql
     * @param protectObject
     */
    private void setEsClinet(AssetSql assetSql, ProtectObject protectObject) throws Exception {
        String userName = assetSql.getDbUser();
        String passWord = assetSql.getDbPassword();
        HttpHost host = new HttpHost(protectObject.getIp(), protectObject.getPort(), "http");
        RestClientBuilder builder = RestClient.builder(host);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (StringUtils.isBlank(assetSql.getDbUser())) {
            userName = "";
        }
        if (StringUtils.isBlank(assetSql.getDbPassword())) {
            passWord = "";
        }
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passWord));
        builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder f) {
                return f.setDefaultCredentialsProvider(credentialsProvider);
            }
        });
        this.restHighLevelClient = new RestHighLevelClient(builder);
        MainResponse response = restHighLevelClient.info(RequestOptions.DEFAULT);
        LOG.info(String.format("The Es clusterName is [{%s}],version is [{%s}]", response.getClusterName(),
            response.getVersion()));
    }

    /**
     * 获取关系型数据库连接
     * 
     * @param assetSql
     * @param protectObject
     * @throws ClassNotFoundException
     */
    private void setPulicConnection(AssetSql assetSql, ProtectObject protectObject) throws Exception {
        String dbUrl = DataBaseUtil.getUrl(protectObject);
        // 常规jdbc
        Class.forName("com.facebook.presto.jdbc.PrestoDriver");
        Class.forName("dm.jdbc.driver.DmDriver"); // 达梦6支持
        Class.forName("com.vertica.jdbc.Driver");// vertica支持
        Properties properties = assetSql.getProperties();
        properties.setProperty("user", assetSql.getDbUser());
        properties.setProperty("password", assetSql.getDbPassword());
        con = DriverManager.getConnection(dbUrl, assetSql.getProperties());
        st = con.createStatement();
    }


	
	public Connection getConn(){
		return this.con;
	}
	
	
    public RestHighLevelClient getEsClient() {
        return this.restHighLevelClient;
    }

    public org.apache.hadoop.hbase.client.Connection getHbaseClient() {
        return this.hbaseClient;
    }

    public Jedis getRedisClient() {
        return this.jedis;
    }

	
	

	/**
	 * @param sql
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		rs = null;
		try {
			rs = st.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
            LOG.error("executeQuery:" + e.getMessage());
			throw new SQLException();
		}
	}

	public ResultSet executeQuery(String sql, Connection con) throws SQLException {
		Statement statement = con.createStatement();
		return statement.executeQuery(sql);
	}

	/**
	 * @param sql
	 * @return ResultSet
	 * @throws SQLException
	 */
	public void execute(String sql) throws SQLException {
		try {
			st.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.error("execute:" + e);
			throw new SQLException();
		}
	}
	


	/**
	 * 关闭连接
	 */
	public void close() {
		try {
            if (rs != null)
				rs.close();
			if(con!=null && !con.isClosed())
				con.close();
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
            if (hbaseClient != null) {
                hbaseClient.close();
            }
            if (jedis != null) {
                jedis.close();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close(Integer dbtype) {
		try {
			if(rs!=null && !rs.isClosed())
				rs.close();
			if(con!=null && !con.isClosed())
				con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 检查连接初始化是否成功
	 * @return boolean
	 */
	public boolean checkJdbc(){
		return status;
	}

	public int queryTableExist(String table,int dbType) {
		// 拼接SQL
		String sql = null;
		int result = -1;
		try {
			ResultSet rs = executeQuery(sql);
			// 表存在
			result = 0;
			while (rs.next()) {
				if (StringUtils.isNotBlank(rs.getString(1))) {
					// 表存在且有数据
					result = 1;
				}
				break;
			}
		} catch (SQLException e) {
			// 解析表是否存在
			e.printStackTrace();
			return result;
		}
		return result;

	}

	
    public static void main(String args[]) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "zkproxy02");
        conf.set("hbase.zookeeper.property.clientPort", "21810");
        Connection connection = (Connection)ConnectionFactory.createConnection(conf);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("scan 'hbase:Student1'");
        while (resultSet.next()) {
            System.out.println(resultSet.getObject(1));
        }
    }

}
