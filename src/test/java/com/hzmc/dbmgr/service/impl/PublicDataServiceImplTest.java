package com.hzmc.dbmgr.service.impl;

import com.hzmc.dbmgr.service.PublicDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * @author: taozr
 * @date: 2019/4/1 15:43
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@SpringBootTest
@WebAppConfiguration
public class PublicDataServiceImplTest {

    @Autowired
    PublicDataService publicDataService;

    @Test
    public void getDBTypeList() {
        publicDataService.getDBTypeList();
    }

    @Test
    public void getRunModesList() {
        publicDataService.getRunModesList();
    }

    @Test
    public void getStatusList() {
        publicDataService.getStatusList();
    }
}