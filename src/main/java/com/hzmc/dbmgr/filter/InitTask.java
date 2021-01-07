package com.hzmc.dbmgr.filter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.dbenum.AgentStatusEnum;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.service.AgentStatusService;
import com.hzmc.dbmgr.service.ProtectObjectService;
import com.hzmc.dbmgr.util.DruidDataSourceUtil;
import com.hzmc.dbmgr.util.JasyptEncryptor;

/**
 * 版权所有：美创科技
 * 创建者: gpchen
 * 创建日期: 2019年10月24日 下午2:08:30
 */

@Component
public class InitTask implements CommandLineRunner {

	private final static Logger logger = LoggerFactory.getLogger(InitTask.class);

	@Autowired
	private AgentStatusService agentStatusService;

	@Autowired
	ProtectObjectService protectObjectService;

	@Override
	public void run(String... arg0) throws Exception {
		try {
			// 初始化准入数据库连接池
			initAgentStatus();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 初始化准入的数据库连接池
	 */
	private void initAgentStatus() {
		List<AgentStatus> list = agentStatusService.selectAllAgentStatus();
		List<AgentStatus> errorList = new ArrayList<AgentStatus>();
		for (AgentStatus agentStatus : list) {
			ProtectObject protectObject = protectObjectService.getProtectObjectById(agentStatus.getObjId(), false);
			protectObject.setDbUser(agentStatus.getDbUser());
			protectObject.setDbPassword(JasyptEncryptor.decoder(agentStatus.getDbPassword()));
			if (protectObject.getDbType() != DBEnum.ORACLE.getNumber()) {
				protectObject.setServiceName(agentStatus.getServiceName());
			}
			try {
				DruidDataSourceUtil.getInstance().addDruidDataBase(protectObject);
			} catch (SQLException e) {
				errorList.add(agentStatus);
				logger.error("init  database fail,objid and objName is [{}]",
						agentStatus.getObjId() + "," + protectObject.getObjName());
				e.printStackTrace();
			}
		}

		// 循环将有问题的准入信息填入
		for (AgentStatus agentStatus : errorList) {
			agentStatus.setStatus(AgentStatusEnum.ERROR.getNumber());
			agentStatus.setMessage(ErrCode.ERROR_MSG.getMessage());
			agentStatusService.updateAgentStatusByObjId(agentStatus);
		}
	}

	public static void main(String[] args) {
		System.out.println(JasyptEncryptor.encoder("123"));
	}
}
