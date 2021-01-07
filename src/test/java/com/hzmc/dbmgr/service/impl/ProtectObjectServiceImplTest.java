package com.hzmc.dbmgr.service.impl;

import static com.hzmc.dbmgr.service.impl.ProtectObjectGroupServiceImplTest.getOracleGroup;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.hzmc.dbmgr.DbMgrServiceApplication;
import com.hzmc.dbmgr.bean.CamProxy;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ObjTypeEnum;
import com.hzmc.dbmgr.dbenum.RunModeEnum;
import com.hzmc.dbmgr.dbenum.StatusTypeEnum;
import com.hzmc.dbmgr.mapper.CamProxyMapper;
import com.hzmc.dbmgr.mapper.ProtectObjectGroupMapper;
import com.hzmc.dbmgr.mapper.ProtectObjectMapper;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import com.hzmc.dbmgr.service.ProtectObjectService;

/**
 * @author: taozr
 * @date: 2018/7/20 10:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest(classes = DbMgrServiceApplication.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath*:/mybatis/mapper/*.xml" })
public class ProtectObjectServiceImplTest {

    private static final Integer ORACLE_PORT = 1521;
    private static final Integer SQLSERVER_PORT = 1433;
    private static final Integer MYSQL_PORT = 3306;
    private static final Integer DB2_PORT = 50000;
    private static final Integer GBASE8s_PORT = 5258;
    private static final Integer HIVE_PORT = 10000;
    private static final Integer DM_PORT = 5236;
	private static final Integer POSTGRES_PORT = 5432;
	private static final Integer UX_PORT = 5432;
	private static final Integer KINGBASE_PORT = 54321;
	private static final Integer GBASE_S87_PORT = 9088;
	private static final Integer MONGO_PORT = 27017;
	private static final Integer SYBASE_PORT = 7000;
    private static final String PROXY_IP = "127.0.0.1";
    private static final String ORACLE_INSTANCE_NAME = "orcl_instance";
    private static final String ORACLE_SERVICE_NAME = "orcl_service";
    private static final String DB2_SERVICE_NAME = "DB2_dbName";
    private static final Map<Integer, Integer> PORT_MAP = new HashMap<>();
    private static final Map<Integer, String> IP_MAP = new HashMap<>();

    static {
        PORT_MAP.put(DBEnum.ORACLE.getNumber(), ORACLE_PORT);
        PORT_MAP.put(DBEnum.SQLSERVER.getNumber(), SQLSERVER_PORT);
        PORT_MAP.put(DBEnum.MYSQL.getNumber(), MYSQL_PORT);
        PORT_MAP.put(DBEnum.DB2.getNumber(), DB2_PORT);
        PORT_MAP.put(DBEnum.GBASE_S83.getNumber(), GBASE8s_PORT);
        PORT_MAP.put(DBEnum.HIVE.getNumber(), HIVE_PORT);
        PORT_MAP.put(DBEnum.DAMENG.getNumber(), DM_PORT);
		PORT_MAP.put(DBEnum.POSTGRESQL.getNumber(), POSTGRES_PORT);
		PORT_MAP.put(DBEnum.UXDB.getNumber(), UX_PORT);
		PORT_MAP.put(DBEnum.KINGBASE.getNumber(), KINGBASE_PORT);
		PORT_MAP.put(DBEnum.GBASE_S87.getNumber(), GBASE_S87_PORT);
		PORT_MAP.put(DBEnum.MONGODB.getNumber(), MONGO_PORT);
		PORT_MAP.put(DBEnum.SYBASE.getNumber(), SYBASE_PORT);
        IP_MAP.put(DBEnum.ORACLE.getNumber(), "192.168.1.1");
        IP_MAP.put(DBEnum.SQLSERVER.getNumber(), "192.168.2.1");
        IP_MAP.put(DBEnum.MYSQL.getNumber(), "192.168.3.1");
        IP_MAP.put(DBEnum.DB2.getNumber(), "192.168.4.1");
        IP_MAP.put(DBEnum.GBASE_S83.getNumber(), "192.168.5.1");
        IP_MAP.put(DBEnum.HIVE.getNumber(), "192.168.6.1");
        IP_MAP.put(DBEnum.DAMENG.getNumber(), "192.168.7.1");
		IP_MAP.put(DBEnum.POSTGRESQL.getNumber(), "192.168.8.1");
		IP_MAP.put(DBEnum.UXDB.getNumber(), "192.168.9.1");
		IP_MAP.put(DBEnum.KINGBASE.getNumber(), "192.168.10.1");
		IP_MAP.put(DBEnum.GBASE_S87.getNumber(), "192.168.11.1");
		IP_MAP.put(DBEnum.MONGODB.getNumber(), "192.168.12.1");
		IP_MAP.put(DBEnum.SYBASE.getNumber(), "192.168.13.1");

    }

    @Autowired
    private ProtectObjectService protectObjectService;

    @Autowired
    private ProtectObjectGroupService protectObjectGroupService;

    @Autowired
    private ProtectObjectGroupMapper protectObjectGroupMapper;

    @Autowired
    private ProtectObjectMapper protectObjectMapper;

    @Autowired
    private CamProxyMapper camProxyMapper;
    
	@Mock
	private ProtectObjectMapper protectObjectMapperMock;

	@InjectMocks
	private ProtectObjectServiceImpl protectObjectServiceMock;

	@Before
	public void initMock() {
		MockitoAnnotations.initMocks(this);
	}

    private ProtectObjectGroup protectObjectGroup;

    private static void setProxyPort(ProtectObject protectObject) {
        String ip = protectObject.getIp();
        Integer port = protectObject.getPort();
        ip = ip.substring(ip.indexOf(".") + 1);
        ip = ip.substring(ip.indexOf(".") + 1);
        Integer value1 = Integer.valueOf(ip.substring(0, ip.indexOf(".")));
        Integer value2 = Integer.valueOf(ip.substring(ip.indexOf(".") + 1));
        protectObject.setProxyPort(port + value1 * 10 + value2);
    }

    public static ProtectObject getProtectObject(DBEnum dbEnum) {
        return getProtectObject(dbEnum, IP_MAP.get(dbEnum.getNumber()));
    }

    public static ProtectObject getProtectObject(DBEnum dbEnum, Boolean isCluster) {
        return getProtectObject(dbEnum, IP_MAP.get(dbEnum.getNumber()), isCluster);
    }

    public static ProtectObject getProtectObject(DBEnum dbEnum, String ip) {
        return getProtectObject(dbEnum, ip, false);
    }

    public static ProtectObject getProtectObject(DBEnum dbEnum, String ip, Boolean isCluster) {
        return getProtectObject(dbEnum, ip, PORT_MAP.get(dbEnum.getNumber()), isCluster);
    }

    public static ProtectObject getProtectObject(DBEnum dbEnum, String ip, Integer port, Boolean isCluster) {
        ProtectObject protectObject = new ProtectObject();
        Integer dbType = dbEnum.getNumber();
        protectObject.setDbType(dbType);
        protectObject.setObjName("single_" + protectObject.getDbType());
        protectObject.setIp(ip);
        protectObject.setPort(port);
        if (dbType == DBEnum.ORACLE.getNumber().intValue()) {
            protectObject.setInstanceName(ORACLE_INSTANCE_NAME);
            protectObject.setServiceName(ORACLE_SERVICE_NAME);
        }
        if (dbType == DBEnum.DB2.getNumber().intValue()) {
            protectObject.setServiceName(DB2_SERVICE_NAME);
        }
        protectObject.setDbUser("hzmc");
        protectObject.setDbPassword("hzmc321#");
        protectObject.setRunMode(RunModeEnum.NORMAL.getNumber());
        protectObject.setProxyIp(PROXY_IP);
        setProxyPort(protectObject);
        protectObject.setObjectType(ObjTypeEnum.SINGLE.getNumber());
        protectObject.setNodes(new ArrayList<>());
        if (isCluster) {
            protectObject.setObjName("cluster_" + protectObject.getDbType());
            protectObject.setObjectType(ObjTypeEnum.CLUSTER.getNumber());
            String ipPrefix = ip.substring(0, ip.lastIndexOf(".") + 1);
            String lastIp = ip.substring(ip.lastIndexOf(".") + 1);
            ProtectObject nodeA = new ProtectObject();
            nodeA.setObjName("nodeA_" + protectObject.getDbType());
            nodeA.setIp(ipPrefix + (Integer.valueOf(lastIp) + 1));
            nodeA.setPort(protectObject.getPort());
            nodeA.setProxyIp(PROXY_IP);
            setProxyPort(nodeA);
            ProtectObject nodeB = new ProtectObject();
            nodeB.setObjName("nodeB_" + protectObject.getDbType());
            nodeB.setIp(ipPrefix + (Integer.valueOf(lastIp) + 2));
            nodeB.setPort(protectObject.getPort());
            nodeB.setProxyIp(PROXY_IP);
            setProxyPort(nodeB);
            if (dbType == DBEnum.ORACLE.getNumber().intValue()) {
                protectObject.setInstanceName(null);
                nodeA.setInstanceName("nodeA_" + ORACLE_INSTANCE_NAME);
                nodeB.setInstanceName("nodeB_" + ORACLE_INSTANCE_NAME);
            }
            if (dbType == DBEnum.DB2.getNumber().intValue()) {
                nodeA.setServiceName("nodeA_" + DB2_SERVICE_NAME);
                nodeB.setServiceName("nodeB_" + DB2_SERVICE_NAME);
            }
            protectObject.getNodes().add(nodeA);
            protectObject.getNodes().add(nodeB);
        }
        return protectObject;
    }

    private void createGroup() {
        protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        ProtectObjectGroup temp = protectObjectGroupMapper.selectByPrimaryKey(protectObjectGroup.getGroupId());
        Assert.assertNotNull(temp);
    }

    @Before
    public void setUp() {
        createGroup();
    }

    @Test
    public void getProtectObjectListPaged() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        protectObjectService.createProtectObject(protectObject);
        Page page = new Page();
        page.addSearchParameter("objName", protectObject.getObjName());
        page.addSearchParameter("dbType", protectObject.getDbType());
        page.addSearchParameter("ip", protectObject.getIp());
        page.addSearchParameter("status", protectObject.getStatus());
        page.addSearchParameter("groupId", protectObject.getGroupId());
        page.setOrderField("objName");
        page.setAscend(Boolean.valueOf("true"));
        protectObjectService.getProtectObjectListPaged(page);
        Assert.assertTrue(page.getItems().size() == 1);
    }

    /**
     * 集群的子节点ip符合搜索条件
     */
    @Test
    public void getProtectObjectListPaged2() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        Page page = new Page();
        page.addSearchParameter("objName", protectObject.getObjName());
        page.addSearchParameter("dbType", protectObject.getDbType());
        page.addSearchParameter("ip", protectObject.getNodes().get(0).getIp());
        page.addSearchParameter("status", protectObject.getStatus());
        page.addSearchParameter("groupId", protectObject.getGroupId());
        page.setOrderField("objName");
        page.setAscend(Boolean.valueOf("true"));
        protectObjectService.getProtectObjectListPaged(page);
        Assert.assertTrue(page.getItems().size() == 1);
    }

    /**
     * 页码越界
     */
    @Test
    public void getProtectObjectListPaged3() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        protectObjectService.createProtectObject(protectObject);
        Page page = new Page();
        page.setCurrentPage(2);
        boolean caught = false;
        try {
            protectObjectService.getProtectObjectListPaged(page);
        } catch (RestfulException e) {
            Assert.assertTrue(e.getErrorMessage().contains("页码越界"));
            caught = true;
        }
        Assert.assertTrue(caught);
    }

    /**
     * 正常搜索
     */
    @Test
    public void getProtectObjectListPaged4() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject protectObject2 = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        protectObject2.setObjName("jks");
        protectObjectService.createProtectObject(protectObject2);
        ProtectObject protectObject3 = getProtectObject(DBEnum.ORACLE, "192.168.3.1");
        protectObject3.setObjName("asdlk");
        protectObjectService.createProtectObject(protectObject3);
        Page page = new Page();
        page.addSearchParameter("ip", ".168.2.");
        page.setPageSize(1);
        protectObjectService.getProtectObjectListPaged(page);
        Assert.assertTrue(page.getItems().size() == 1);
    }

    @Test
    public void getProtectObjectListPaged5() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        Page page = new Page();
        page.addSearchParameter("ip", ".168.");
        page.setPageSize(1);
        protectObjectService.getProtectObjectListPaged(page);
        Assert.assertTrue(page.getItems().size() == 1);
    }

    @Test
    public void getProtectObjectByIdWithoutPassword() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(protectObject.getObjId(),false);
        Assert.assertEquals("********",temp.getDbPassword());
        Assert.assertNotNull(temp);
        Assert.assertEquals(protectObject.getProxyIp(),temp.getProxyIp());
        Assert.assertEquals(protectObject.getProxyPort(),temp.getProxyPort());
        List<ProtectObject> nodes = temp.getNodes();
        Assert.assertNotNull(nodes);
        Assert.assertTrue(nodes.size() ==2);
    }

    @Test
    public void getProtectObjectByIdWithPassword() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        String password = protectObject.getDbPassword();
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(protectObject.getObjId(), true);
        Assert.assertNotNull(temp);
        Assert.assertEquals(password, temp.getDbPassword());
    }

    /**
     * 正常添加，单节点被分组
     */
    @Test
    public void createNormalOracleSingleWithGroup() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        ProtectObject protectObjectCopy = getProtectObject(DBEnum.ORACLE);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(protectObjectGroup.getGroupId().intValue(), temp.getGroupId().intValue());
        Assert.assertEquals(protectObjectCopy.getDbType().intValue(), temp.getDbType().intValue());
        Assert.assertEquals(protectObjectCopy.getObjName(), temp.getObjName());
        Assert.assertEquals(protectObjectCopy.getIp(), temp.getIp());
        Assert.assertEquals(protectObjectCopy.getPort().intValue(), temp.getPort().intValue());
        Assert.assertEquals(protectObjectCopy.getInstanceName(), temp.getInstanceName());
        Assert.assertEquals(protectObjectCopy.getServiceName(), temp.getServiceName());
        Assert.assertEquals(protectObjectCopy.getDbUser(), temp.getDbUser());
        Assert.assertEquals(protectObjectCopy.getRunMode().intValue(), temp.getRunMode().intValue());
        Assert.assertEquals(ObjTypeEnum.SINGLE.getNumber().intValue(), temp.getObjectType().intValue());
        Assert.assertEquals(StatusTypeEnum.STOP.getNumber().intValue(), temp.getStatus().intValue());
        Assert.assertEquals(0, temp.getParentId().intValue());
    }

    /**
     * 正常添加，集群模式，不分组
     */
    @Test
    public void createNormalOracleClusterWithNoGroup() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(ObjTypeEnum.CLUSTER.getNumber().intValue(), temp.getObjectType().intValue());
        List<ProtectObject> tempNodes = protectObjectMapper.getByParentId(protectObject.getObjId());
        Assert.assertEquals(protectObject.getNodes().size(), tempNodes.size());
        Assert.assertEquals(ObjTypeEnum.NODE.getNumber().intValue(), tempNodes.get(0).getObjectType().intValue());
        Assert.assertEquals(ObjTypeEnum.NODE.getNumber().intValue(), tempNodes.get(1).getObjectType().intValue());
        //todo 子节点有几个值是拷贝父节点的
    }

    /**
     * 正常添加，Oracle集群没有填写实例名
     */
    @Test
    public void createNormalOracleClusterAndNodesHaveNoInstanceName() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setInstanceName(null);
        protectObject.getNodes().get(1).setInstanceName(null);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(ObjTypeEnum.CLUSTER.getNumber().intValue(), temp.getObjectType().intValue());
        List<ProtectObject> tempNodes = protectObjectMapper.getByParentId(protectObject.getObjId());
        Assert.assertEquals(protectObject.getNodes().size(), tempNodes.size());
        Assert.assertEquals(ObjTypeEnum.NODE.getNumber().intValue(), tempNodes.get(0).getObjectType().intValue());
        Assert.assertEquals(ObjTypeEnum.NODE.getNumber().intValue(), tempNodes.get(1).getObjectType().intValue());
    }

    /**
     * 正常情况
     * 集群，父节点实例名为null, 另一个单库IP+端口和集群的子节点的IP+PORT相同，但实例名不同
     */
    @Test
    public void createNormalOracle() {
        ProtectObject cluster = getProtectObject(DBEnum.ORACLE, "192.168.1.1", true);
        protectObjectService.createProtectObject(cluster);
        ProtectObject single = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        single.setIp(cluster.getNodes().get(0).getIp());
        single.setPort(cluster.getNodes().get(0).getPort());
        protectObjectService.createProtectObject(single);
    }

    /**
     * 正常情况
     * 集群，父节点实例名为null, 另一个单库IP+端口和集群的父节点的IP+PORT相同，但实例名、服务名都不同
     */
    @Test
    public void createNormalOracle2() {
        ProtectObject cluster = getProtectObject(DBEnum.ORACLE, "192.168.1.1", true);
        protectObjectService.createProtectObject(cluster);
        ProtectObject single = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        single.setIp(cluster.getIp());
        single.setPort(cluster.getPort());
        single.setServiceName(cluster.getServiceName() + "dfa");
        protectObjectService.createProtectObject(single);
    }

    @Test
    public void whenCreatingWithNoDbTypeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setDbType(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("数据库类型为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingWithNoObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setObjName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("数据库名称为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingWithNodeNoObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setObjName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("子节点名称为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithNoIpThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setIp(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("数据库地址为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithNodeErrorIpThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setIp("46.134");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("子节点IP为空或格式不正确", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithNodeErrorProxyIpThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setProxyIp("45.134");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("子节点代理IP为空或格式不正确", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithNoPortThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setPort(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("数据库端口为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithNodePortOutOfRangeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setPort(8888888);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("子节点端口为空或范围不正确", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithDifferentTypeOfGroupIdThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("分组数据库类型与数据库的数据库类型不同", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithErrorGroupIdThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER);
        protectObject.setGroupId(-10);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("找不到分组", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithPortOutOfRangeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setPort(777777);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库端口范围不正确"));
    }

    @Test
    public void whenCreateWithNoRunModeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setRunMode(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("运行模式为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithErrorRunModeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setRunMode(-10);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("运行模式错误"));
    }

    @Test
    public void whenCreateWithNoObjTypeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setObjectType(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("objectType为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreateWithErrorObjTypeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setObjectType(-10);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("objectType错误"));
    }

    @Test
    public void whenCreateSingleWithNodesThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.setObjectType(ObjTypeEnum.SINGLE.getNumber());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("非集群不能有子节点"));
    }

    @Test
    public void whenCreateWithInvalidObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setObjName("234_22");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库名称可以输入字母、数字、下划线且不能以数字开头"));
    }

    @Test
    public void whenCreateWithErrorDbTypeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setDbType(-10);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库类型错误"));
    }

    @Test
    public void whenCreateHiveClusterThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.MYSQL, true);
        protectObject.setDbType(DBEnum.HIVE.getNumber());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("类型的数据库不支持集群"));
    }

    @Test
    public void whenCreateDMClusterThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.MYSQL, true);
        protectObject.setDbType(DBEnum.DAMENG.getNumber());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("类型的数据库不支持集群"));
    }

    /**
     * Oracle实例名格式不正确
     */
    @Test
    public void whenCreatingOracleWithErrorInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setInstanceName("jdfl**&%39");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库实例名格式错误"));
    }

    /**
     * 子节点实例名格式不正确
     */
    @Test
    public void whenCreatingOracleClusterWithNodeHasErrorInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setInstanceName("jdfl**&%39");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点实例名格式错误"));
    }

    /**
     * 数据库名称超过14个字符
     */
    @Test
    public void whenCreatingWithTooLongObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setObjName("a123456789b1234");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库名称最多只能输入14位字符"));
    }

    /**
     * 子节点名称超过14个字符
     */
    @Test
    public void whenCreatingWithNodeHavingTooLongObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setObjName("a123456789b1234");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点名称最多只能输入14位字符"));
    }

    /**
     * oracle 单节点 没有服务名
     */
    @Test
    public void whenCreatingSingleWithNoServiceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setServiceName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("服务名为空", restfulException.getErrorMessage());
    }

    /**
     * oracle 单节点 没有服务名
     */
    @Test
    public void whenCreatingClusterWithNoServiceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.setServiceName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("服务名为空", restfulException.getErrorMessage());
    }

    /**
     * oracle 没有实例名
     */
    @Test
    public void whenCreatingWithNoInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setInstanceName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("数据库实例名为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingDB2WithNoServiceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.DB2);
        protectObject.setServiceName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("库名为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingDB2WithNodeNoServiceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.DB2, true);
        protectObject.getNodes().get(0).setServiceName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("子节点库名为空", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingDB2WithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.DB2);
        protectObject.setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("DB2类型的数据库不能填写实例名", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingSqlServerWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER);
        protectObject.setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("该类型的数据库不能填写服务名和实例名", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingMysqlWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.MYSQL);
        protectObject.setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("该类型的数据库不能填写服务名和实例名", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingHiveWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.HIVE);
        protectObject.setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("该类型的数据库不能填写服务名和实例名", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingDMWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.DAMENG);
        protectObject.setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("该类型的数据库不能填写服务名和实例名", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingGBASE_8WithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.GBASE_S83);
        protectObject.setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("该类型的数据库不能填写服务名和实例名", restfulException.getErrorMessage());
    }

    @Test
    public void whenCreatingGBASE_8ClusterWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.GBASE_S83, true);
        protectObject.getNodes().get(0).setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("类型的子节点不能填写实例名"));
    }

    @Test
    public void whenCreatingSqlServerClusterWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER, true);
        protectObject.getNodes().get(0).setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("类型的子节点不能填写实例名"));
    }

    @Test
    public void whenCreatingMySqlClusterWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.MYSQL, true);
        protectObject.getNodes().get(0).setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("类型的子节点不能填写实例名"));
    }

    @Test
    public void whenCreatingDb2ClusterWithInstanceNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.DB2, true);
        protectObject.getNodes().get(0).setInstanceName("instance");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("DB2类型的子节点不能填写实例名"));
    }

    @Test
    public void createNormalSqlServerSingle() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER);
        ProtectObject protectObjectCopy = getProtectObject(DBEnum.SQLSERVER);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(protectObjectCopy.getDbType().intValue(), temp.getDbType().intValue());
        Assert.assertEquals(protectObjectCopy.getObjName(), temp.getObjName());
        Assert.assertEquals(protectObjectCopy.getIp(), temp.getIp());
        Assert.assertEquals(protectObjectCopy.getPort().intValue(), temp.getPort().intValue());
        Assert.assertEquals(protectObjectCopy.getDbUser(), temp.getDbUser());
        Assert.assertEquals(protectObjectCopy.getRunMode().intValue(), temp.getRunMode().intValue());
        Assert.assertEquals(ObjTypeEnum.SINGLE.getNumber().intValue(), temp.getObjectType().intValue());
        Assert.assertEquals(StatusTypeEnum.STOP.getNumber().intValue(), temp.getStatus().intValue());
        Assert.assertEquals(0, temp.getParentId().intValue());
    }

    /**
     * SQL Server HA
     */
    @Test
    public void createNormalSqlServerCluster() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER, true);
        ProtectObject protectObjectCopy = getProtectObject(DBEnum.SQLSERVER, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(ObjTypeEnum.CLUSTER.getNumber().intValue(), temp.getObjectType().intValue());
        List<ProtectObject> tempNodes = protectObjectMapper.getByParentId(protectObject.getObjId());
        Assert.assertEquals(protectObjectCopy.getNodes().size(), tempNodes.size());
        Assert.assertEquals(ObjTypeEnum.NODE.getNumber().intValue(), tempNodes.get(0).getObjectType().intValue());
        Assert.assertEquals(ObjTypeEnum.NODE.getNumber().intValue(), tempNodes.get(1).getObjectType().intValue());
    }

    /**
     * 单节点保护对象名称重复
     */
    @Test
    public void whenCreatingWithDuplicatedObjNameThenExceptionIsThrown() {
        protectObjectService.createProtectObject(getProtectObject(DBEnum.ORACLE));
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库名称已存在"));
    }

    /**
     * 单节点ip+port+实例与ip+port+实例重复
     */
    @Test
    public void whenCreatingWithDuplicatedIPPortAndInstanceNameThenExceptionIsThrown() {
        ProtectObject exist = getProtectObject(DBEnum.ORACLE, "192.168.1.1");
        protectObjectService.createProtectObject(exist);
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        newProtectObject.setObjName(newProtectObject.getObjName() + "sdf");
        newProtectObject.setIp(exist.getIp());
        newProtectObject.setPort(exist.getPort());
        newProtectObject.setInstanceName(exist.getInstanceName());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库已存在"));
    }

    /**
     * 单节点ip+port+服务名与ip+port+服务名重复
     */
    @Test
    public void whenCreatingWithDuplicatedIPPortAndServiceNameThenExceptionIsThrown() {
        ProtectObject exist = getProtectObject(DBEnum.ORACLE, "192.168.1.1");
        protectObjectService.createProtectObject(exist);
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        newProtectObject.setObjName(newProtectObject.getObjName() + "sdf");
        newProtectObject.setIp(exist.getIp());
        newProtectObject.setPort(exist.getPort());
        newProtectObject.setInstanceName(newProtectObject.getInstanceName() + "dfadf");
        newProtectObject.setServiceName(exist.getServiceName());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库已存在"));
    }

    /**
     * 单节点代理IP不同，但是代理端口重复
     */
    @Test
    public void whenCreatingWithDifferentProxyIpButDuplicatedProxyPortThenExceptionIsThrown() {
        ProtectObject exist = getProtectObject(DBEnum.ORACLE, "192.168.1.1");
        exist.setObjName("adsf");
        protectObjectService.createProtectObject(exist);
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        newProtectObject.setProxyIp("127.0.0.2");
        newProtectObject.setProxyPort(exist.getProxyPort());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("代理端口" + exist.getProxyPort() + "已被使用，请重新输入"));
    }


    /**
     * 集群，父节点和子节点名称重复
     */
    @Test
    public void whenCreatingClusterWithSameObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObject.getNodes().get(0).setObjName(protectObject.getObjName());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库名称和子节点名称重复"));
    }

    /**
     * 集群，父节点和子节点ip+port+实例重复
     */
    @Test
    public void whenCreatingClusterWithDuplicatedIPPortAndInstanceNameThenExceptionIsThrown() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setIp(newProtectObject.getIp());
        newProtectObject.getNodes().get(0).setPort(newProtectObject.getPort());
        newProtectObject.getNodes().get(0).setInstanceName(newProtectObject.getInstanceName());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("父节点和子节点配置重复"));
    }

    /**
     * 集群，父节点填写IP+PORT+服务名，子节点填写IP+PORT+实例名，其中父节点和子节点的IP+PORT相同
     */
    @Test
    public void createProtectObjectClusterConflict() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setIp(newProtectObject.getIp());
        newProtectObject.getNodes().get(0).setPort(newProtectObject.getPort());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("父节点和子节点配置重复"));
    }

    /**
     * 集群，父节点填写IP+PORT+服务名，子节点填写IP+PORT，其中父节点和子节点的IP+PORT相同
     */
    @Test
    public void createProtectObjectClusterConflict2() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setIp(newProtectObject.getIp());
        newProtectObject.getNodes().get(0).setPort(newProtectObject.getPort());
        newProtectObject.getNodes().get(0).setInstanceName(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("父节点和子节点配置重复"));
    }

    /**
     * SQL Server HA 父节点IP+PORT和子节点IP+PORT重复
     */
    @Test
    public void createProtectObjectSQLServerHAConflict() {
        ProtectObject protectObject = getProtectObject(DBEnum.SQLSERVER, true);
        protectObject.setDbType(DBEnum.SQLSERVER.getNumber());
        protectObject.getNodes().get(0).setIp(protectObject.getIp());
        protectObject.getNodes().get(0).setPort(protectObject.getPort());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("父节点和子节点配置重复"));
    }

    /**
     * 集群，父节点和子节点代理端口重复
     */
    @Test
    public void whenCreatingProtectObjectClusterWithDuplicatedProxyPortThenExceptionIsThrown() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setProxyPort(newProtectObject.getProxyPort());
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("代理端口" + newProtectObject.getProxyPort() + "已被使用，请重新输入"));
    }

    /**
     * 单节点，代理端口和系统端口冲突
     */
    @Test
    public void whenCreatingProtectObjectWithConflictSystemPortThenExceptionIsThrown() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE);
        newProtectObject.setProxyPort(61615);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("代理端口已被系统占用"));
    }


    /**
     * 集群，子节点代理端口和系统端口冲突
     */
    @Test
    public void whenCreatingProtectObjectNodeConflictSystemPortThenExceptionIsThrown() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setProxyPort(61615);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点代理端口已被系统占用"));
    }

    /**
     * 单节点，代理端口小于1024
     */
    @Test
    public void whenCreatingProtectObjectWithErrorProxyPortThenExceptionIsThrown() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE);
        newProtectObject.setProxyPort(23);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("代理端口范围应为1025-65535"));
    }


    /**
     * 集群，子节点代理端口小于1024
     */
    @Test
    public void whenCreatingProtectObjectNodeWithErrorProxyPortThenExceptionIsThrown() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setProxyPort(23);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("代理端口范围应为1025-65535"));
    }

    /**
     * 单节点，占用系统端口
     */
    //@Test
    public void createProtectObject15() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE);
        newProtectObject.setProxyPort(8320);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("代理端口已被系统占用"));
    }


    /**
     * 集群，子节点占用系统端口
     */
    @Test
    public void createProtectObject16() {
        ProtectObject newProtectObject = getProtectObject(DBEnum.ORACLE, true);
        newProtectObject.getNodes().get(0).setProxyPort(13306);
        RestfulException restfulException = null;
        try {
            protectObjectService.createProtectObject(newProtectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点代理端口已被系统占用"));
    }

    /**
     * 正常更新 单节点
     */
    @Test
    public void updateProtectObjectNormally() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        protectObject.setIp("172.19.1.1");
        protectObject.setPort(3306);
        protectObjectService.updateProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(ObjTypeEnum.SINGLE.getNumber().intValue(), temp.getObjectType().intValue());
        Assert.assertEquals(protectObject.getStatus().intValue(), temp.getStatus().intValue());
        Assert.assertEquals(0, temp.getParentId().intValue());
    }

    /**
     * 正常更新 多节点的情况
     */
    @Test
    public void updateProtectObjectClusterNormally() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        protectObject.setIp("172.19.1.1");
        protectObject.setPort(3306);
        protectObject.getNodes().remove(1);
        protectObjectService.updateProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(ObjTypeEnum.CLUSTER.getNumber().intValue(), temp.getObjectType().intValue());
        Assert.assertEquals(protectObject.getStatus().intValue(), temp.getStatus().intValue());
        Assert.assertEquals(0, temp.getParentId().intValue());
        List<ProtectObject> nodes = protectObjectMapper.getByParentId(protectObject.getObjId());
        Assert.assertEquals(1, nodes.size());
    }

    /**
     * 正常修改，父节点和子节点的IP和端口调换
     */
    @Test
    public void updateProtectObjectCluster2() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        String nodeIP = protectObject.getNodes().get(0).getIp();
        Integer nodePort = protectObject.getNodes().get(0).getPort();
        protectObject.getNodes().get(0).setIp(protectObject.getIp());
        protectObject.getNodes().get(0).setPort(protectObject.getPort());
        protectObject.setIp(nodeIP);
        protectObject.setPort(nodePort);
        protectObjectService.updateProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNotNull(temp);
        Assert.assertEquals(nodeIP, temp.getIp());
    }

    /**
     * 正常修改，父节点和子节点的代理IP和代理端口调换
     */
    @Test
    public void updateProtectObjectCluster3() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        String nodeProxyIP = protectObject.getNodes().get(0).getProxyIp();
        Integer nodeProxyPort = protectObject.getNodes().get(0).getProxyPort();
        protectObject.getNodes().get(0).setProxyIp(protectObject.getProxyIp());
        protectObject.getNodes().get(0).setProxyPort(protectObject.getProxyPort());
        protectObject.setProxyIp(nodeProxyIP);
        protectObject.setProxyPort(nodeProxyPort);
        protectObjectService.updateProtectObject(protectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(protectObject.getObjId(), false);
        Assert.assertNotNull(temp);
        Assert.assertEquals(nodeProxyIP, temp.getProxyIp());
        Assert.assertEquals(nodeProxyPort, temp.getProxyPort());
    }

    /**
     * 正常修改，删除了一个子节点，又新增了一个子节点。新增的子节点信息和删除的子节点信息相同
     */
    @Test
    public void updateProtectObjectCluster4() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject oldNode = protectObject.getNodes().get(0);
        Integer oldNode2ObjId = protectObject.getNodes().get(1).getObjId();

        ProtectObject newNode = new ProtectObject();
        newNode.setObjName(oldNode.getObjName());
        newNode.setIp(oldNode.getIp());
        newNode.setPort(oldNode.getPort());
        newNode.setInstanceName(oldNode.getInstanceName());
        newNode.setProxyIp(oldNode.getProxyIp());
        newNode.setProxyPort(oldNode.getProxyPort());

        protectObject.getNodes().removeIf(p -> p.getObjId().intValue() == oldNode.getObjId().intValue());
        protectObject.getNodes().add(newNode);

        protectObjectService.updateProtectObject(protectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(protectObject.getObjId(), false);
        Assert.assertNotNull(temp);


        Assert.assertEquals(oldNode2ObjId, protectObject.getNodes().get(0).getObjId());
        Assert.assertTrue(protectObject.getNodes().get(1).getObjId() > oldNode2ObjId);
    }


    /**
     * 异常更新：修改了数据库类型
     */
    @Test
    public void whenUpdateProtectObjectWithDbTypeThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        protectObject.setInstanceName("");
        protectObject.setServiceName("");
        protectObject.setDbType(DBEnum.SQLSERVER.getNumber());
        RestfulException restfulException = null;
        try {
            protectObjectService.updateProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库类型不能修改"));
    }

    /**
     * 异常更新，修改了保护对象名称
     */
    @Test
    public void whenUpdateProtectObjectWithObjNameThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        protectObject.setObjName(protectObject.getObjName() + "adfd");
        RestfulException restfulException = null;
        try {
            protectObjectService.updateProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库名称不能修改"));
    }

    /**
     * 异常更新，id不存在
     */
    @Test
    public void whenUpdateProtectObjectWithErrorObjIdThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        protectObject.setObjId(-10);
        RestfulException restfulException = null;
        try {
            protectObjectService.updateProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("找不到数据库"));
    }

    /**
     * 异常更新，修改了子节点
     */
    @Test
    public void whenUpdateProtectObjectNodeAloneThenExceptionIsThrown() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject node = protectObject.getNodes().get(0);
        RestfulException restfulException = null;
        try {
            protectObjectService.updateProtectObject(node);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点不能单独修改"));
    }


    /**
     * ip+port修改冲突
     */
    @Test
    public void updateProtectObjectSingle3() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject protectObjectB = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        protectObjectB.setObjName(protectObjectB.getObjName() + "adfjl");
        protectObjectService.createProtectObject(protectObjectB);
        protectObject.setIp(protectObjectB.getIp());
        protectObject.setPort(protectObjectB.getPort());
        RestfulException restfulException = null;
        try {
            protectObjectService.updateProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库已存在"));
    }

    /**
     * ip+port+实例修改冲突
     */
    @Test
    public void updateProtectObjectSingle4() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject protectObjectB = getProtectObject(DBEnum.ORACLE, "192.168.2.1");
        protectObjectB.setObjName(protectObjectB.getObjName() + "adfjl");
        protectObjectService.createProtectObject(protectObjectB);
        protectObject.setIp(protectObjectB.getIp());
        protectObject.setPort(protectObjectB.getPort());
        protectObject.setInstanceName(protectObjectB.getInstanceName());
        RestfulException restfulException = null;
        try {
            protectObjectService.updateProtectObject(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库已存在"));
    }


    /**
     * 正常删除，单库
     */
    @Test
    public void deleteProtectObject() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        Assert.assertNotNull(protectObjectMapper.selectByPrimaryKey(protectObject.getObjId()));
        protectObjectService.deleteProtectObject(protectObject.getObjId());
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNull(temp);
        CamProxy camProxy = camProxyMapper.getByDbid(Long.valueOf(protectObject.getObjId()));
        Assert.assertNull(camProxy);
    }

    /**
     * 正常删除，集群
     */
    @Test
    public void deleteProtectObject2() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        Assert.assertNotNull(protectObjectMapper.selectByPrimaryKey(protectObject.getObjId()));
        Assert.assertEquals(2, protectObjectMapper.getByParentId(protectObject.getObjId()).size());
        protectObjectService.deleteProtectObject(protectObject.getObjId());
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertNull(temp);
        Assert.assertEquals(0, protectObjectMapper.getByParentId(protectObject.getObjId()).size());
        CamProxy camProxy = camProxyMapper.getByDbid(Long.valueOf(protectObject.getObjId()));
        Assert.assertNull(camProxy);
        for (ProtectObject node : protectObject.getNodes()) {
            CamProxy camProxy2 = camProxyMapper.getByDbid(Long.valueOf(node.getObjId()));
            Assert.assertNull(camProxy2);
        }
    }

    /**
     * 找不到删除的id
     */
    @Test
    public void deleteProtectObject3() {
        RestfulException restfulException = null;
        try {
            protectObjectService.deleteProtectObject(-1);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("找不到数据库"));
    }

    /**
     * 异常删除，单独删除子节点
     */
    @Test
    public void deleteProtectObjectNodeAlone() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        RestfulException restfulException = null;
        try {
            protectObjectService.deleteProtectObject(protectObject.getNodes().get(0).getObjId());
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点不能单独删除"));
    }

    /**
     * 正常分组
     */
    @Test
    public void groupProtectObject() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertEquals(0, temp.getGroupId().intValue());
        int[] ids = {protectObject.getObjId()};
        protectObjectService.groupProtectObject(ids, protectObjectGroup.getGroupId());
        ProtectObject temp2 = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertEquals(protectObjectGroup.getGroupId().intValue(), temp2.getGroupId().intValue());
    }

    /**
     * 分组id不存在
     */
    @Test
    public void groupProtectObjectWithErrorGroupId() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        int[] ids = {protectObject.getObjId()};
        RestfulException restfulException = null;
        try {
            protectObjectService.groupProtectObject(ids, -10);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("分组不存在"));
    }

    /**
     * 分组是，保护对象不存在
     */
    @Test
    public void groupProtectObjectWithErrorObjId() {
        int[] ids = {-12};
        RestfulException restfulException = null;
        try {
            protectObjectService.groupProtectObject(ids, protectObjectGroup.getGroupId());
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("找不到数据库"));
    }

    /**
     * 给集群分组
     */
    @Test
    public void groupProtectObject2() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        int[] ids = {protectObject.getObjId()};
        RestfulException restfulException = null;
        try {
            protectObjectService.groupProtectObject(ids, protectObjectGroup.getGroupId());
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("集群或子节点不能被分组"));
    }

    /**
     * 正常退分组
     */
    @Test
    public void exitGroup() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        protectObjectService.createProtectObject(protectObject);
        protectObjectService.exitGroup(protectObject.getObjId());
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertEquals(0, temp.getGroupId().intValue());
    }

    @Test
    public void getProtectObjectListPublic() {
        protectObjectService.createProtectObject(getProtectObject(DBEnum.ORACLE, true));
        List<ProtectObject> plist = protectObjectService.getProtectObjectListPublic(true);
        Assert.assertNotNull(plist);
        Assert.assertEquals(1, plist.size());
    }

    @Test
    public void getProtectObjectListPublic2() {
        protectObjectService.createProtectObject(getProtectObject(DBEnum.ORACLE, true));
        List<ProtectObject> plist = protectObjectService.getProtectObjectListPublic(false);
        Assert.assertNotNull(plist);
        Assert.assertEquals(3, plist.size());
    }

    @Test
    public void startOrStopProtectObject() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertEquals(0, temp.getStatus().intValue());
        int[] ids = {protectObject.getObjId()};
        protectObjectService.startOrStopProtectObject(ids, "start");
        ProtectObject temp2 = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertEquals(1, temp2.getStatus().intValue());
        List<ProtectObject> tempNodes = protectObjectMapper.getByParentId(protectObject.getObjId());
        Assert.assertEquals(1, tempNodes.get(0).getStatus().intValue());
        Assert.assertEquals(1, tempNodes.get(1).getStatus().intValue());
    }

    @Test
    public void startOrStopProtectObject2() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        int[] ids = {protectObject.getObjId()};
        protectObjectService.startOrStopProtectObject(ids, "stop");
        ProtectObject temp2 = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId());
        Assert.assertEquals(0, temp2.getStatus().intValue());
    }

    /**
     * 异常情况，单独停用子节点
     */
    @Test
    public void startOrStopProtectObject3() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        int[] ids = {protectObject.getNodes().get(0).getObjId()};
        RestfulException restfulException = null;
        try {
            protectObjectService.startOrStopProtectObject(ids, "stop");
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("子节点状态不能单独修改"));
    }

    @Test
    public void getProtectObjectByObjName() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE, true);
        protectObjectService.createProtectObject(protectObject);
        ProtectObject temp = protectObjectService.getProtectObjectByObjName(protectObject.getObjName());
        Assert.assertNotNull(temp);
        Assert.assertEquals(2, temp.getNodes().size());
    }

    @Test
    public void updateDbUserAndPassword() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        Integer objId = protectObject.getObjId();
        String user = "root";
        String password = "mysql";
        ProtectObject newProtectObject = new ProtectObject();
        newProtectObject.setObjId(objId);
        newProtectObject.setDbUser(user);
        newProtectObject.setDbPassword(password);
        protectObjectService.updateDbUserAndPassword(newProtectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(objId, true);
        Assert.assertNotNull(temp);
        Assert.assertEquals(user, temp.getDbUser());
        Assert.assertEquals(password, temp.getDbPassword());
    }

    @Test
    public void updateDbUserOnly() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        String originPass = protectObject.getDbPassword();
        protectObjectService.createProtectObject(protectObject);
        Integer objId = protectObject.getObjId();
        String user = "root";
        String password = "********";
        ProtectObject newProtectObject = new ProtectObject();
        newProtectObject.setObjId(objId);
        newProtectObject.setDbUser(user);
        newProtectObject.setDbPassword(password);
        protectObjectService.updateDbUserAndPassword(newProtectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(objId, true);
        Assert.assertNotNull(temp);
        Assert.assertEquals(user, temp.getDbUser());
        Assert.assertEquals(originPass, temp.getDbPassword());
    }

    @Test
    public void getPortsByDbtype() {
        List<Integer> list = protectObjectService.getPortsByDbtype(DBEnum.ORACLE.getNumber());
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void updateRunMode() {
        ProtectObject protectObject = getProtectObject(DBEnum.ORACLE);
        protectObjectService.createProtectObject(protectObject);
        Integer objId = protectObject.getObjId();
        ProtectObject newProtectObject = new ProtectObject();
        newProtectObject.setObjId(objId);
        newProtectObject.setRunMode(RunModeEnum.SIMULATION.getNumber());
        protectObjectService.updateRunMode(newProtectObject);
        ProtectObject temp = protectObjectService.getProtectObjectById(objId, true);
        Assert.assertNotNull(temp);
        Assert.assertEquals(RunModeEnum.SIMULATION.getNumber(), temp.getRunMode());
    }




    /**
     * 用于传递导入失败的保护对象列表
     */
    private List<ProtectObject> fail;

    private String path;

    private String uuid;

    /**
     * 文件导入测试object_import_junit_test.xls
     * 有7条正常测试用例，11条异常测试用例
     * 修改xls之后，测试用例需要做相应的修改
     */
    @Test
    public void importProtectObject() throws Exception {
        String fileName = System.getProperty("user.dir") + File.separator + "object_import_junit_test.xls";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, "", new FileInputStream(new File(fileName)));
        Map<String, List<ProtectObject>> resultMap = protectObjectService.importProtectObject(mockMultipartFile);
        List<ProtectObject> success = resultMap.get("success");
        fail = resultMap.get("fail");
        Assert.assertEquals(7, success.size());
        Assert.assertEquals(11, fail.size());
    }

    /**
     * 没有上传文件
     */
    @Test
    public void importProtectObjectWithNoFile() {
        RestfulException restfulException = null;
        try {
            protectObjectService.importProtectObject(null);
        } catch (RestfulException e) {
            restfulException = e;
        }
		Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("没有文件上传"));
    }

    /**
     * 没有上传文件
     */
    @Test
    public void importProtectObjectWithErrorType() throws IOException {
        RestfulException restfulException = null;
        try {
            String fileName = System.getProperty("user.dir") + File.separator + "errorTypeTest.txt";
            MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, "", new FileInputStream(new File(fileName)));
            Map<String, List<ProtectObject>> resultMap = protectObjectService.importProtectObject(mockMultipartFile);
            protectObjectService.importProtectObject(null);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("不支持的文件类型"));
    }


    /**
     * 导出测试，使用导入失败的数据
     */
    @Test
    public void exportProtectObject() throws Exception {
        importProtectObject();
        Assert.assertTrue(fail.size() == 11);
        uuid = protectObjectService.exportProtectObject(fail);
        path = "/tmp" + File.separator + "export" + uuid + ".csv";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            path = "c:" + path;
        }
    }

    @Test
    public void downloadProtectObject() throws Exception {
        exportProtectObject();
        ResponseEntity responseEntity = protectObjectService.downloadProtectObject(uuid);
        Assert.assertNotNull(responseEntity);
        Assert.assertNotNull(responseEntity.getBody());
        HttpHeaders httpHeaders = responseEntity.getHeaders();
        Assert.assertEquals(MediaType.APPLICATION_OCTET_STREAM, httpHeaders.getContentType());
    }

    @Test
    public void downloadProtectObjectWithErrorUUID() {
        ResponseEntity responseEntity = protectObjectService.downloadProtectObject("dfadfsad");
        Assert.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void downloadProtectObjectWithUnknownUUID() {
        ResponseEntity responseEntity = protectObjectService.downloadProtectObject(UUID.randomUUID().toString());
        Assert.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    /**
     * 导入测试，测试导出的csv文件能够被正常解析
     */
    @Test
    public void importProtectObject2() throws Exception {
        exportProtectObject();
        String fileName = path;
        MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, "", new FileInputStream(new File(fileName)));
        Map<String, List<ProtectObject>> resultMap = protectObjectService.importProtectObject(mockMultipartFile);
        List<ProtectObject> success = resultMap.get("success");
        List<ProtectObject> fail = resultMap.get("fail");
        Assert.assertEquals(0, success.size());
        Assert.assertEquals(11, fail.size());
        File tempFile = new File(fileName);
        Assert.assertTrue(tempFile.delete());
    }

    @Test
    public void connectTest() {
        ProtectObject protectObject = new ProtectObject();
        protectObject.setIp("192.168.1.1");
        protectObject.setPort(3306);
        RestfulException restfulException = null;
        try {
            protectObjectService.connectTest(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Assert.assertNotNull(restfulException);
            Assert.assertEquals("测试服务仅限于linux环境下执行", restfulException.getErrorMessage());
        }
    }

    @Test
    public void connectTestWithErrorIp() {
        ProtectObject protectObject = new ProtectObject();
        protectObject.setIp("1.1.1");
        protectObject.setPort(null);
        RestfulException restfulException = null;
        try {
            protectObjectService.connectTest(protectObject);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("IP或端口为空或格式错误"));
    }

	/**
	 * 测试修改保护对象之 找不到数据库
	 * 
	 */
	@Test
	public void connectTestNotFindDataBase() {
		ProtectObject protectObject = new ProtectObject();
		RestfulException restfulException = null;
		protectObject.setObjId(-1);
		try {
			protectObjectService.updateProtectObject(protectObject);
		} catch (RestfulException e) {
			restfulException = e;
		}
		Assert.assertNotNull(restfulException);
		Assert.assertTrue(restfulException.getErrorMessage().contains("找不到数据库"));
	}

	/**
	 * 子节点不能单独退组
	 * 
	 */
	@Test
	public void connectTestNodeAloneExitGroup() {
		ProtectObject protectObject = new ProtectObject();
		RestfulException restfulException = null;
		protectObject.setObjectType(ObjTypeEnum.NODE.getNumber());
		try {
			Mockito.when(protectObjectMapperMock.selectByPrimaryKey(-1)).thenReturn(protectObject);
			protectObjectServiceMock.exitGroup(-1);
			Mockito.verify(protectObjectMapperMock).selectByPrimaryKey(-1);
		} catch (RestfulException e) {
			restfulException = e;
		}
		Assert.assertNotNull(restfulException);
		Assert.assertTrue(restfulException.getErrorMessage().contains("子节点不能单独退组"));
	}

	/**
	 * 1.未知的ID 
	 * 2.子节点状态不能单独修改
	 * 
	 */
	@Test
	public void connectTestNotExsitId() {
		ProtectObject protectObject = new ProtectObject();
		RestfulException restfulException = null;
		int[] ids = { -1 };
		try {
			protectObjectService.startOrStopProtectObject(ids, null);
		} catch (RestfulException e) {
			restfulException = e;
		}
		Assert.assertNotNull(restfulException);
		Assert.assertTrue(restfulException.getErrorMessage().contains("未知的ID"));
	}

	/**
	 * 测试连接失败
	 * 
	 */
	@Test
	public void connectSucceceTest() {
		ProtectObject protectObject = new ProtectObject();
		// 连接失败未找到任何记录
		protectObject.setIp("192.168.0.1");
		protectObject.setPort(anyInt());
		System.setProperty("os.name", "linux");
		boolean result = false;
		try {
			result = protectObjectService.connectTest(protectObject);
		} catch (RestfulException e) {
		}
		Assert.assertFalse(result);
	}

	/**
	 * 测试读取csv格式的模板，解析成保护对象数组
	 * 
	 */
	@Test
	public void readCsvFileTest() throws Exception {
		String fileName = System.getProperty("user.dir") + File.separator + "object_import_junit_test.csv";
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, "",
				new FileInputStream(new File(fileName)));
		Map<String, List<ProtectObject>> resultMap = protectObjectService.importProtectObject(mockMultipartFile);
		List<ProtectObject> success = resultMap.get("success");
		fail = resultMap.get("fail");
		Assert.assertEquals(7, success.size());
		Assert.assertEquals(11, fail.size());
	}

	/**
	 * 解析上传文件 
	 * 1.没有文件上传
	 * 2.不支持的文件类型
	 * 3.文件解析错误
	 * 4.未找到任何记录
	 */
	@Test
	public void parseFromFileTest() throws Exception {
		RestfulException restfulException = null;
		// 没有文件上传
		try {
			protectObjectService.parseFromFile(null);
		} catch (RestfulException e) {
			restfulException = e;
		}
		Assert.assertNotNull(restfulException);
		Assert.assertTrue(restfulException.getErrorMessage().contains("没有文件上传"));
		// 不支持的文件类型
		restfulException = null;
		try {
			protectObjectService.parseFromFile(anyString() + ".txt");
		} catch (RestfulException e) {
			restfulException = e;
		}
		Assert.assertNotNull(restfulException);
		Assert.assertTrue(restfulException.getErrorMessage().contains("不支持的文件类型"));
		// 未找到任何记录
		restfulException = null;
		try {
			protectObjectService.parseFromFile("test.csv");
		} catch (RestfulException e) {
			restfulException = e;
		}
		Assert.assertNotNull(restfulException);
		Assert.assertTrue(restfulException.getErrorMessage().contains("未找到任何记录"));

	}

	@Test
	public void queryProtectObjectByDbTypeTest() throws Exception {
		List<ProtectObject> list = protectObjectService.queryProtectObjectByDbType(DBEnum.ORACLE.getNumber());
		Assert.assertNotNull(list);
	}

}