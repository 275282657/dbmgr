package com.hzmc.dbmgr.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hzmc.dbmgr.bean.PublicData;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.RunModeEnum;
import com.hzmc.dbmgr.dbenum.StatusTypeEnum;
import com.hzmc.dbmgr.service.PublicDataService;

@Service
public class PublicDataServiceImpl implements PublicDataService {

    private final static Logger logger = LoggerFactory.getLogger(PublicDataServiceImpl.class);

    @Override
    public List<PublicData> getDBTypeList() {
        List<PublicData> publicDataList = new ArrayList<>();
        for (DBEnum a : DBEnum.values()) {
            PublicData publicData = new PublicData();
            publicData.setName(a.getText());
            publicData.setValue(a.getNumber().toString());
            publicDataList.add(publicData);
        }
        return publicDataList;
    }

    @Override
    public List<PublicData> getRunModesList() {
        List<PublicData> publicDataList = new ArrayList<>();
        for (RunModeEnum a : RunModeEnum.values()) {
            PublicData publicData = new PublicData();
            publicData.setName(a.getText());
            publicData.setValue(a.getNumber().toString());
            publicDataList.add(publicData);
        }
        return publicDataList;
    }

    @Override
    public List<PublicData> getStatusList() {
        List<PublicData> publicDataList = new ArrayList<>();
        for (StatusTypeEnum a : StatusTypeEnum.values()) {
            PublicData publicData = new PublicData();
            publicData.setName(a.getText());
            publicData.setValue(a.getNumber().toString());
            publicDataList.add(publicData);
        }
        return publicDataList;
    }

}
