package com.hzmc.dbmgr.service;

import java.util.List;

import com.hzmc.dbmgr.dto.LoginAudit;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月28日 下午3:41:06
 */
public interface LoginAuditService {

	List<LoginAudit> getLoginAudit(Integer objId, String dbName);

}
