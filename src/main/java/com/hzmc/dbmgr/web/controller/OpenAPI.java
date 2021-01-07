package com.hzmc.dbmgr.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.common.bean.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import com.hzmc.dbmgr.service.ProtectObjectService;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class OpenAPI {

    @Autowired
    ProtectObjectService protectObjectService;
    @Autowired
    ProtectObjectGroupService protectObjectGroupService;

    /**
     * capaa内部获取所有的保护对象接口
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/assetProtectObject", method = RequestMethod.GET)
    public ResultBean<Map<String, List<ProtectObject>>> getProtectObjectList(@RequestParam(value="withStructure", required = false, defaultValue = "true") Boolean withStructure) {
        Map<String, List<ProtectObject>> map = new HashMap<>();
        map.put("protectObjects", protectObjectService.getProtectObjectListPublic(withStructure));
        return new ResultBean<>(map);
    }

    /**
     * 按id获取保护对象，包括加密的密码
     * @param id
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/assetProtectObject/{id}", method = RequestMethod.GET)
    public ResultBean<ProtectObject> getProtectObjectById(@PathVariable Integer id) {
        return new ResultBean<>(protectObjectService.getProtectObjectById(id,true));
    }

    /**
     * 根据名称查找保护对象
     * @param objName
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/assetProtectObject/objName/{objName}", method = RequestMethod.GET)
    public ResultBean<ProtectObject> getProtctObjectByName(@PathVariable String objName) {
        return new ResultBean<>(protectObjectService.getProtectObjectByObjName(objName));
    }

    /**
     * 更新数据库账户和密码
     * @param id
     * @param protectObject
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/assetProtectObject/update/{id}", method = RequestMethod.POST)
    public ResultBean updateDbUserAndPassword(@PathVariable Integer id, @RequestBody ProtectObject protectObject) {
        protectObject.setObjId(id);
        protectObjectService.updateDbUserAndPassword(protectObject);
        return new ResultBean();
    }

    /**
     * 查找某个类型的数据库使用的所有端口
     * @param dbType
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/assetProtectObject/{dbType}/ports", method = RequestMethod.GET)
    public ResultBean<Map<String, List<Integer>>> getPortsByDbtype(@PathVariable Integer dbType) {
        Map<String, List<Integer>> map = new HashMap<>();
        map.put("ports", protectObjectService.getPortsByDbtype(dbType));
        return new ResultBean<>(map);
    }

    /**
     * 更新运行模式接口
     * @param id
     * @param protectObject
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/updateRunMode/{id}", method = RequestMethod.POST)
    public ResultBean updateRunMode(@PathVariable Integer id, @RequestBody ProtectObject protectObject) {
        protectObject.setObjId(id);
        protectObjectService.updateRunMode(protectObject);
        return new ResultBean();
    }

    /**
     * 把xls或csv文件解析成保护对象列表
     * @param file
     * @return
     */
    @RequestMapping(value = "/assetProtectObject/public/parseFromFile", method = RequestMethod.GET)
    public ResultBean<Map<String, List<ProtectObject>>> parseFromFile(@RequestParam("filePath") String filePath) {
        Map<String, List<ProtectObject>> map = new HashMap<>();
        map.put("protectObjects", protectObjectService.parseFromFile(filePath));
        return new ResultBean<>(map);
    }

}
