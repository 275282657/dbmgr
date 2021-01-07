package com.hzmc.dbmgr.service.impl;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.mapper.ProtectObjectGroupMapper;
import com.hzmc.dbmgr.mapper.ProtectObjectMapper;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import com.hzmc.dbmgr.service.ProtectObjectService;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: taozr
 * @date: 2018/7/20 9:07
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest
@WebAppConfiguration
public class ProtectObjectGroupServiceImplTest {

    @Autowired
    private ProtectObjectGroupService protectObjectGroupService;

    @Autowired
    private ProtectObjectService protectObjectService;

    @Autowired
    private ProtectObjectGroupMapper protectObjectGroupMapper;

    @Autowired
    private ProtectObjectMapper protectObjectMapper;

    public static ProtectObjectGroup getOracleGroup() {
        ProtectObjectGroup protectObjectGroup = new ProtectObjectGroup();
        protectObjectGroup.setGroupName("junit test Oracle group");
        protectObjectGroup.setDbType(DBEnum.ORACLE.getNumber());
        protectObjectGroup.setDescription("description for junit test Oracle group");
        return protectObjectGroup;
    }

    public static ProtectObjectGroup getSQLServerGroup() {
        ProtectObjectGroup protectObjectGroup = new ProtectObjectGroup();
        protectObjectGroup.setGroupName("junit test SQLServer group");
        protectObjectGroup.setDbType(DBEnum.SQLSERVER.getNumber());
        protectObjectGroup.setDescription("description for junit test SQLServer group");
        return protectObjectGroup;
    }

    @Test
    public void getProtectObjectGroupListPageNormally() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        Page page = new Page();
        page.addSearchParameter("groupName", protectObjectGroup.getGroupName());
        page.addSearchParameter("dbType", protectObjectGroup.getDbType());
        protectObjectGroupService.getProtectObjectGroupList(page);
        Assert.assertEquals(1, page.getItems().size());
    }

    /**
     * 正常添加
     */
    @Test
    public void createNormalProtectObjectGroup() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        Assert.assertNotNull(protectObjectGroup.getGroupId());
        ProtectObjectGroup groupTemp = protectObjectGroupMapper.selectByPrimaryKey(protectObjectGroup.getGroupId());
        Assert.assertNotNull(groupTemp);
        Assert.assertNotNull(groupTemp.getCreateTime());
        Assert.assertNotNull(groupTemp.getUpdateTime());
        Assert.assertEquals(protectObjectGroup.getGroupName(), groupTemp.getGroupName());
        Assert.assertEquals(protectObjectGroup.getDbType().intValue(), groupTemp.getDbType().intValue());
        Assert.assertEquals(protectObjectGroup.getDescription(), groupTemp.getDescription());
    }

    /**
     * 分组名称为空
     */
    @Test
    public void whenCreatingProtectObjectGroupAndGroupNameIsNullThenExceptionIsThrown() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroup.setGroupName(null);
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("分组名称为空", restfulException.getErrorMessage());
    }

    /**
     * 数据库类型为空
     */
    @Test
    public void whenCreatingProtectObjectGroupAndDbTypeIsNullThenExceptionIsThrown() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroup.setDbType(null);
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("数据库类型为空", restfulException.getErrorMessage());
    }

    /**
     * 数据库类型错误
     */
    @Test
    public void whenCreatingProtectObjectGroupWithErrorDbTypeThenExceptionIsThrown() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroup.setDbType(0);
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("数据库类型错误"));
    }

    /**
     * 分组名称重复
     */
    @Test
    public void whenCreatingProtectObjectGroupWithDuplicatedGroupNameThenExceptionIsThrown() {
        createNormalProtectObjectGroup();
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertEquals("分组名称已存在", restfulException.getErrorMessage());
    }


    /**
     * 正常更新
     */
    @Test
    public void updateProtectGroupNormally() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        String name = protectObjectGroup.getGroupName() + "" + RandomUtils.nextInt(100);
        String description = protectObjectGroup.getDescription() + "" + RandomUtils.nextInt(100);
        protectObjectGroup.setGroupName(name);
        protectObjectGroup.setDbType(DBEnum.SQLSERVER.getNumber());
        protectObjectGroup.setDescription(description);
        protectObjectGroupService.updateProtectObjectGroup(protectObjectGroup);
        ProtectObjectGroup groupTemp = protectObjectGroupMapper.selectByPrimaryKey(protectObjectGroup.getGroupId());
        Assert.assertEquals(name, groupTemp.getGroupName());
        Assert.assertEquals(DBEnum.SQLSERVER.getNumber().intValue(), groupTemp.getDbType().intValue());
        Assert.assertEquals(description, groupTemp.getDescription());
    }

    /**
     * 正常更新，不更新分组数据库类型
     */
    @Test
    public void updateProtectGroupWithSameDbTypeNormally() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        String name = protectObjectGroup.getGroupName() + "" + RandomUtils.nextInt(100);
        String description = protectObjectGroup.getDescription() + "" + RandomUtils.nextInt(100);
        protectObjectGroup.setGroupName(name);
        protectObjectGroup.setDescription(description);
        protectObjectGroupService.updateProtectObjectGroup(protectObjectGroup);
        ProtectObjectGroup groupTemp = protectObjectGroupMapper.selectByPrimaryKey(protectObjectGroup.getGroupId());
        Assert.assertEquals(name, groupTemp.getGroupName());
        Assert.assertEquals(description, groupTemp.getDescription());
    }

    /**
     * 找不到分组id
     */
    @Test
    public void whenUpdatingProtectObjectGroupWithErrorIdThenExceptionIsThrown() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        protectObjectGroup.setGroupId(-3);
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.updateProtectObjectGroup(protectObjectGroup);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains(ErrCode.UNKNOWN_ID.getMessage()));
    }

    /**
     * 新分组名称重复
     */
    @Test
    public void whenUpdatingProtectObjectGroupWithDuplicatedGroupNameThenExceptionIsThrown() {
        ProtectObjectGroup group1 = getOracleGroup();
        ProtectObjectGroup group2 = getSQLServerGroup();
        protectObjectGroupService.createProtectObjectGroup(group1);
        protectObjectGroupService.createProtectObjectGroup(group2);
        group2.setGroupName(group1.getGroupName());
        RestfulException restfulException = null;
        try {
             protectObjectGroupService.updateProtectObjectGroup(group2);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("分组名称已存在"));
    }
    /**
     * 分组内有成员不能修改数据库类型
     */
    @Test
    public void whenUpdatingGroupDbTypeWithGroupMemberThenExceptionIsThrown() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        ProtectObject protectObject = ProtectObjectServiceImplTest.getProtectObject(DBEnum.ORACLE);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        protectObjectService.createProtectObject(protectObject);
        protectObjectGroup.setDbType(DBEnum.SQLSERVER.getNumber());
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.updateProtectObjectGroup(protectObjectGroup);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains("分组内已有成员，无法修改数据库类型"));
    }

    /**
     * 正常删除，并退组
     */
    @Test
    public void deleteProtectObjectGroupNormally() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        ProtectObject protectObject = ProtectObjectServiceImplTest.getProtectObject(DBEnum.ORACLE);
        protectObject.setGroupId(protectObjectGroup.getGroupId());
        protectObjectService.createProtectObject(protectObject);
        protectObjectGroupService.deleteProtectObjectGroup(protectObjectGroup.getGroupId());
        ProtectObjectGroup groupTemp = protectObjectGroupMapper.selectByPrimaryKey(protectObjectGroup.getGroupId());
        Assert.assertNull(groupTemp);
        int GroupId = protectObjectMapper.selectByPrimaryKey(protectObject.getObjId()).getGroupId();
        Assert.assertEquals(0, GroupId);
    }

    /**
     * 批量删除
     */
    @Test
    public void deleteBatchProtectObjectGroupNormally() {
        ProtectObjectGroup group1 = getOracleGroup();
        ProtectObjectGroup group2 = getSQLServerGroup();
        protectObjectGroupService.createProtectObjectGroup(group1);
        protectObjectGroupService.createProtectObjectGroup(group2);
        String[] ids = {group1.getGroupId().toString(), group2.getGroupId().toString()};
        protectObjectGroupService.deleteProtectObjectGroup(ids);
        ProtectObjectGroup groupTemp = protectObjectGroupMapper.selectByPrimaryKey(group1.getGroupId());
        Assert.assertNull(groupTemp);
        ProtectObjectGroup groupTemp2 = protectObjectGroupMapper.selectByPrimaryKey(group2.getGroupId());
        Assert.assertNull(groupTemp2);
    }

    /**
     * 分组id不存在
     */
    @Test
    public void WhenDeletingProtectObjectGroupWithErrorGroupIdThenExceptionIsThrown() {
        RestfulException restfulException = null;
        try {
            protectObjectGroupService.deleteProtectObjectGroup(-1);
        } catch (RestfulException e) {
            restfulException = e;
        }
        Assert.assertNotNull(restfulException);
        Assert.assertTrue(restfulException.getErrorMessage().contains(ErrCode.UNKNOWN_ID.getMessage()));
    }

    /**
     * 正常查找
     */
    @Test
    public void getProtectObjectGroupByIdNormally() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        ProtectObjectGroup groupTemp = protectObjectGroupService.getProtectObjectGroupById(protectObjectGroup.getGroupId());
        Assert.assertNotNull(groupTemp);
    }

    @Test
    public void getProtectObjectGroupListPublicNormally() {
        ProtectObjectGroup protectObjectGroup = getOracleGroup();
        protectObjectGroupService.createProtectObjectGroup(protectObjectGroup);
        List<ProtectObjectGroup> list = protectObjectGroupService.getProtectObjectGroupListPublic();
        Assert.assertEquals(1, list.size());
    }
}