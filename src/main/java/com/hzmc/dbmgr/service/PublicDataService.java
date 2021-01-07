package com.hzmc.dbmgr.service;

import java.util.List;

import com.hzmc.dbmgr.bean.PublicData;

public interface PublicDataService {

	List<PublicData> getDBTypeList();

	List<PublicData> getRunModesList();

	List<PublicData> getStatusList();
}
