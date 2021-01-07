package com.hzmc.dbmgr.bean;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年11月4日 下午2:59:55
 */
public class AgentAppName {
	
	private String id;
	
	/**
	 * 要copy的id
	 */
	private String oldId;

	/**
	 * 保护对象id
	 */
	private Integer objId;
	
	/**
	 * 应用名称
	 */
	private String appName;
	
	/**
	 * 状态,判断是否真实添加
	 */
	private Integer status;
	
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	/**
	 * 类型：新增,修改,复制
	 */
	private String type;

    /**
     * 备注
     */
    private String remark;

	/**
	 * 多个应用名称
	 */
	@JSONField(name = "appNameList")
	private List<String> appNameList;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOldId() {
		return oldId;
	}

	public void setOldId(String oldId) {
		this.oldId = oldId;
	}

	public Integer getObjId() {
		return objId;
	}

	public void setObjId(Integer objId) {
		this.objId = objId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getAppNameList() {
		return appNameList;
	}

	public void setAppNameList(List<String> appNameList) {
		this.appNameList = appNameList;
	}

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
