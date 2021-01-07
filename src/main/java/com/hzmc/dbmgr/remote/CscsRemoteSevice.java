package com.hzmc.dbmgr.remote;

import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.hzmc.dbmgr.dto.UuidToken;

/**
* Created by chengp on 2020年9月9日
*/
@FeignClient(name = "security-client-server")
public interface CscsRemoteSevice {

    /**
     * 根据用户id获取详细信息
     * 
     * @param id
     *            用户id
     * @return ResponseBean
     */
    @GetMapping(value = "/cscs/v1/remotIp/list")
    public Map<String, UuidToken> getRemoteIpList_V1();

}
