package com.hzmc.dbmgr.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hzmc.dbmgr.bean.AgentStatus;
import com.hzmc.dbmgr.bean.CamDevice;
import com.hzmc.dbmgr.bean.CamProxy;
import com.hzmc.dbmgr.bean.ProtectObject;
import com.hzmc.dbmgr.bean.ProtectObjectGroup;
import com.hzmc.dbmgr.common.bean.Page;
import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.DBEnum;
import com.hzmc.dbmgr.dbenum.ErrCode;
import com.hzmc.dbmgr.dbenum.ObjTypeEnum;
import com.hzmc.dbmgr.dbenum.RunModeEnum;
import com.hzmc.dbmgr.dbenum.StatusTypeEnum;
import com.hzmc.dbmgr.mapper.CamDeviceMapper;
import com.hzmc.dbmgr.mapper.CamProxyMapper;
import com.hzmc.dbmgr.mapper.ProtectObjectMapper;
import com.hzmc.dbmgr.service.AgentStatusService;
import com.hzmc.dbmgr.service.ProtectObjectGroupService;
import com.hzmc.dbmgr.service.ProtectObjectService;
import com.hzmc.dbmgr.util.FileUtil;
import com.hzmc.dbmgr.util.IpUtils;
import com.hzmc.dbmgr.util.JasyptEncryptor;
import com.hzmc.dbmgr.util.ReadExcelFile;
import com.hzmc.dbmgr.util.UdpUtil;

@Service
public class ProtectObjectServiceImpl implements ProtectObjectService {

    private final static Logger logger = LoggerFactory.getLogger(ProtectObjectServiceImpl.class);

	@Autowired
	private ProtectObjectMapper protectObjectMapper;
	@Autowired
	private ProtectObjectGroupService protectObjectGroupService;
	@Autowired
	private CamProxyMapper camProxyMapper;
	@Autowired
	private CamDeviceMapper camDeviceMapper;
	@Autowired
	private AgentStatusService agentStatusService;

	@Value("${tempFileDir}")
	private String tempFileDir;

	@Value("${SYS_PORT}")
	private String sysPort;

	@Override
	public Page getProtectObjectListPaged(Page page) {
		//这里查询出来的有，单库、集群，不包括节点
		List<ProtectObject> plist = protectObjectMapper.getProtectObjectListPaged(page);
		//补全节点信息
		for (ProtectObject p : plist) {
			setNodes(p);
			removePassword(p);
		}
		//如果有ip,要特殊处理
		//对于单库，不符合ip的话，去掉
		//对于集群，如果集群内有一条ip符合，则保留整个集群
		String ip = (String) page.getSearchParameters().get("ip");
		if (ip != null) {
			Iterator<ProtectObject> iterator = plist.iterator();
			while (iterator.hasNext()) {
				ProtectObject protectObject = iterator.next();
				if (protectObject.getObjectType() == ObjTypeEnum.SINGLE.getNumber().intValue())
					if (!protectObject.getIp().contains(ip))
						iterator.remove();
				if (protectObject.getObjectType() == ObjTypeEnum.CLUSTER.getNumber().intValue()) {
					boolean find = false;
					if (protectObject.getIp().contains(ip))
						find = true;
					for (ProtectObject node : protectObject.getNodes()) {
						if (node.getIp().contains(ip)) {
							find = true;
							break;
						}
					}
					if (!find)
						iterator.remove();
				}
			}
		}
		setProxyIpAndProxyPort(plist);
		//计算数据总数和分页
		int count = plist.size();
		int fromIndex = page.getLimitNumber();
		if (fromIndex > count)
			throw new RestfulException(ErrCode.PARAM_ERROR, "页码越界");
		int toIndex = fromIndex + page.getPageSize();
		List<ProtectObject> subList;
		if (toIndex > count)
			subList = plist.subList(fromIndex, count);
		else
			subList = plist.subList(fromIndex, toIndex);
		page.setTotalCount(count);
		page.setItems((List) subList);
		return page;
	}


	@Override
	public ProtectObject getProtectObjectById(int id, Boolean withPassword) {
		ProtectObject p = protectObjectMapper.selectByPrimaryKey(id);
		if (p == null)
			throw new RestfulException(ErrCode.UNKNOWN_ID, "找不到数据库");
		setNodes(p);
		setProxyIpAndProxyPort(p);
		if (withPassword)
			decodePassword(p);
		else
			removePassword(p);
		return p;
	}

	@Transactional
	@Override
	public Boolean createProtectObject(ProtectObject protectObject) {
		logger.info("add protectObject begin");
		protectObject.setObjId(null);
		checkAndSetDefaultValue(protectObject);
		logger.info("add protectObject:" + protectObject);
		protectObjectMapper.insertSelective(protectObject);
		//处理节点信息
		handleNodes(protectObject);
		//处理代理IP和代理端口信息
		handleProxyIpAndProxyPort(protectObject);
		logger.info("add protectObject successfully, id:{}", protectObject.getObjId());
		logger.info("add protectObject end");
		return true;
	}



	@Transactional
	@Override
	public Boolean updateProtectObject(ProtectObject protectObject) {
		logger.info("update protectObject begin");
		//判断id是否合法
		ProtectObject oldProtectObject = getProtectObjectById(protectObject.getObjId(),false);
		if(oldProtectObject==null)
			throw new RestfulException(ErrCode.UNKNOWN_ID, "找不到数据库");
		if(oldProtectObject.getObjectType() == ObjTypeEnum.NODE.getNumber().intValue())
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点不能单独修改");
		//先找出被删除的子节点，并删除
		deleteOldNodes(protectObject.getNodes(),oldProtectObject.getNodes());
		checkAndSetDefaultValue(protectObject);
		//更新的时候，不改变数据库的状态
		protectObject.setStatus(oldProtectObject.getStatus());
		for (ProtectObject node : protectObject.getNodes())
			node.setStatus(oldProtectObject.getStatus());
		logger.info("update protectObject:" + protectObject);
		protectObjectMapper.updateByPrimaryKeySelective(protectObject);
		//处理节点信息
		handleNodes(protectObject);
		//处理代理IP和代理端口信息
		handleProxyIpAndProxyPort(protectObject);
		logger.info("update protectObject successfully, id:{}", protectObject.getObjId());
		logger.info("update protectObject end");
		return true;
	}

	
	@Override
	@Transactional
	public Boolean deleteProtectObject(int id) {
		// 删除准入
		AgentStatus agentStatus = agentStatusService.selectByObjId(id);
		if (agentStatus != null) {
			agentStatusService.deleteAgentStatusByObjId(id);
		}
		//删除的可能是单库和集群
		ProtectObject p = getProtectObjectById(id,false);
		if (p.getObjectType().intValue() == ObjTypeEnum.NODE.getNumber().intValue())
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点不能单独删除");
		protectObjectMapper.deleteByPrimaryKey(id);
		protectObjectMapper.deleteByParentId(id);
		//删除代理IP和代理端口信息
		camProxyMapper.deleteByDbid((long) id);
		for (ProtectObject node : p.getNodes())
			camProxyMapper.deleteByDbid(Long.valueOf(node.getObjId()));
		logger.info("delete protectObject successfully, id:{}", id);
		for (ProtectObject node : p.getNodes())
			logger.info("delete protectObject node successfully, id:{}", node.getObjId());
		return true;
	}


	@Override
	@Transactional
	public Boolean groupProtectObject(int[] ids, Integer group_id) {
		ProtectObjectGroup group = protectObjectGroupService.getProtectObjectGroupById(group_id);
		if (group == null)
			throw new RestfulException(ErrCode.UNKNOWN_ID, "分组不存在");
		//只有单库且数据库类型相同才能被分组
		for (int id : ids) {
			ProtectObject protectObject = protectObjectMapper.selectByPrimaryKey(id);
			if (protectObject == null)
				throw new RestfulException(ErrCode.UNKNOWN_ID, "找不到数据库");
			protectObject.setGroupId(group_id);
			checkGroupId(protectObject);
			protectObjectMapper.updateByPrimaryKeySelective(protectObject);
		}
		return true;
	}

    @Override
    @Transactional
    public Boolean groupProtectObjectNew(int[] ids, Integer group_id) {
        ProtectObjectGroup group = protectObjectGroupService.getProtectObjectGroupById(group_id);
        if (group == null)
            throw new RestfulException(ErrCode.UNKNOWN_ID, "分组不存在");
        // 先清空分组ip
        protectObjectMapper.updateByGroupId(group_id);
        // 只有单库且数据库类型相同才能被分组
        for (int id : ids) {
            ProtectObject protectObject = protectObjectMapper.selectByPrimaryKey(id);
            if (protectObject == null)
                throw new RestfulException(ErrCode.UNKNOWN_ID, "找不到数据库");
            protectObject.setGroupId(group_id);
            checkGroupId(protectObject);
            protectObjectMapper.updateByPrimaryKeySelective(protectObject);
        }
        return true;
    }

	@Override
	@Transactional
	public Boolean exitGroup(int id) {
		ProtectObject protectObject = protectObjectMapper.selectByPrimaryKey(id);
		if (protectObject == null)
			throw new RestfulException(ErrCode.UNKNOWN_ID, "找不到数据库");
		if (protectObject.getObjectType().intValue() == ObjTypeEnum.NODE.getNumber().intValue())
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点不能单独退组");
		protectObject.setGroupId(0);
		protectObjectMapper.updateByPrimaryKeySelective(protectObject);
		return true;
	}

	@Override
	@Transactional
	public Map<String,List<ProtectObject>> importProtectObject(MultipartFile file) {
		if (Objects.isNull(file)) {
			throw new RestfulException(ErrCode.PARAM_ERROR, "没有文件上传");
		}
		//处理文件名
		String fileType;
		if (file.getOriginalFilename().endsWith(".xls")) {
			fileType = ".xls";
		} else if (file.getOriginalFilename().endsWith(".csv")) {
			fileType = ".csv";
		} else
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "不支持的文件类型");
		String filePath = tempFileDir + File.separator + "import" + UUID.randomUUID() + fileType;
		File tempFile = new File(filePath);
		//解析文件
		try {
			file.transferTo(tempFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "文件转换出错");
		}
		List<ProtectObject> plist = parseFromFile(filePath);
		tempFile.delete();
		//插入到数据库
		List<ProtectObject> successList = new ArrayList<>();
		List<ProtectObject> failList = new ArrayList<>();
		Map<String,List<ProtectObject>> data= new HashMap<>();
		for(ProtectObject p:plist){
			try {
				createProtectObject(p);
				//对于保存成功的记录，如果填写了密码，显示"*",没填写显示空
				removePassword(p);
				successList.add(p);
			} catch (RestfulException e) {
				p.setErrMsg(e.getErrorMessage());
				//对于保存失败的记录，不管有没有填写密码，都显示空
				p.setDbPassword("");
				failList.add(p);
			}
		}
		data.put("success",successList);
		data.put("fail",failList);
		return data;
	}

	@Override
	public List<ProtectObject> parseFromFile(String filePath) {
		String fileType;
		if (StringUtils.isBlank(filePath))
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "没有文件上传");
		if (filePath.endsWith(".xls"))
			fileType = ".xls";
		else if (filePath.endsWith(".csv"))
			fileType = ".csv";
		else
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "不支持的文件类型");
		List<ProtectObject> plist;
		try {
			switch (fileType) {
				case ".xls":
					plist = readXlsFile(filePath);
					break;
				case ".csv":
					plist = readCsvFile(filePath);
					break;
				default:
					plist = readXlsFile(filePath);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "文件解析错误");
		}
		if (plist.size() == 0)
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "未找到任何记录");
		//可能出现xls看过去是空的，但是被解析成一个保护对象的情况,这种要去除
		plist.removeIf(this::isInvalidRecord);
		for (ProtectObject p :plist) {
			p.setStatus(StatusTypeEnum.STOP.getNumber());
			p.setRunMode(RunModeEnum.NORMAL.getNumber());
			p.setObjectType(ObjTypeEnum.SINGLE.getNumber());
		}
		return plist;
	}

	@Override
	public List<ProtectObject> getProtectObjectListPublic(Boolean withStructure) {
		List<ProtectObject> plist;
		if(withStructure)
			plist = protectObjectMapper.getProtectObjectListPublic();
		else
			plist = protectObjectMapper.getAll();
		if(withStructure) {
			//如果是有结构的，补全节点信息
			for(ProtectObject p:plist){
				setNodes(p);
			}
		}
		//解密密码
		for(ProtectObject p:plist){
			decodePassword(p);
		}
		setProxyIpAndProxyPort(plist);
		return plist;
	}

	@Transactional
	@Override
	public void startOrStopProtectObject(int[] ids, String cmd) {
		int status = 0;
		if ("start".equals(cmd))
			status = StatusTypeEnum.START.getNumber();
		else
			status = StatusTypeEnum.STOP.getNumber();
		for (int id : ids) {
			ProtectObject protectObject = protectObjectMapper.selectByPrimaryKey(id);
			if (protectObject == null)
				throw new RestfulException(ErrCode.UNKNOWN_ID);
			if (protectObject.getObjectType().intValue() == ObjTypeEnum.NODE.getNumber().intValue())
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点状态不能单独修改");
			//如果是集群模式，子节点的状态变化逻辑已经包含在sql语句中了
			protectObjectMapper.updateStatus(id,status);
		}
		logger.info(cmd + " protectObject successfully, ids:{}", ids);
	}

	@Override
	public String exportProtectObject(List<ProtectObject> list) {
		if(Objects.isNull(list)){
			throw new RestfulException(ErrCode.PARAM_ERROR, "数据库保护对象为null");
		}
		String uuid = UUID.randomUUID().toString();
		String fileName = tempFileDir + File.separator + "export" + uuid + ".csv";
		String title = "数据库名称,数据库类型,数据库地址,数据库端口,服务名/库名,实例名,数据库用户名,数据库密码,代理IP,代理端口,版本,";
		StringBuffer writeStr = new StringBuffer();
		writeStr.append(title).append("\n");
		String delimiter = ",";
		for(ProtectObject p:list){
			String line = "";
			if (p.getObjName() != null)
				line += p.getObjName();
			line += delimiter;
			//把int类型的数据库，转成string
			if (p.getDbType() != null)
				line += DBEnum.getTextByDbNumber(p.getDbType());
			line += delimiter;
			if (p.getIp() != null)
				line += p.getIp();
			line += delimiter;
			if (p.getPort() != null)
				line += p.getPort();
			line += delimiter;
			if (p.getServiceName() != null)
				line += p.getServiceName();
			line += delimiter;
			if (p.getInstanceName() != null)
				line += p.getInstanceName();
			line += delimiter;
			if (p.getDbUser() != null)
				line += p.getDbUser();
			line += delimiter;
			if (p.getDbPassword() != null)
				line += p.getDbPassword();
			line += delimiter;
			if (p.getProxyIp()!= null)
				line += p.getProxyIp();
			line += delimiter;
			if (p.getProxyPort() != null)
				line += + p.getProxyPort();
			line += delimiter;
			if (p.getVersion() != null)
				line += p.getVersion();
			line += delimiter;
			writeStr.append(line).append("\n");
		}
		Boolean result = false;
		result = FileUtil.write(fileName, "gbk", writeStr.toString());
		if(!result)
			throw new RestfulException(ErrCode.TRANSFER_ERROR, "导出失败");
		return uuid;
	}

	@Override
	public ResponseEntity downloadProtectObject(String uuid) {
		ResultBean resultBean = new ResultBean();
		resultBean.setSuccess(false);
		//检查uuid是否符合格式
		try {
			UUID.fromString(uuid);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resultBean.setCode(ErrCode.PARAM_CHECK_ERROR.getValue());
			resultBean.setMessage("uuid格式错误");
			return new ResponseEntity(resultBean, HttpStatus.NOT_FOUND);
		}
		String fileName = tempFileDir + File.separator + "export" + uuid + ".csv";
		File file = new File(fileName);
		InputStream in = null;
		ResponseEntity<byte[]> responseEntity = null;
		try {
			in = new FileInputStream(file);
			byte[] data = new byte[in.available()];
			in.read(data);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.set("Content-Disposition", "attachment; filename=Protect.csv");
			responseEntity = new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
			return responseEntity;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			resultBean.setCode(ErrCode.PARAM_CHECK_ERROR.getValue());
			resultBean.setMessage("找不到文件");
			return new ResponseEntity(resultBean, HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			resultBean.setCode(ErrCode.PARAM_CHECK_ERROR.getValue());
			resultBean.setMessage("IO错误");
			return new ResponseEntity(resultBean, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public Boolean connectTest(ProtectObject protectObject) {
		String ip = protectObject.getIp();
		Integer port = protectObject.getPort();
		if(StringUtils.isBlank(ip) || !IpUtils.isIp(ip) || port == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR,"IP或端口为空或格式错误");
		if(System.getProperty("os.name").toLowerCase().contains("windows")){
			throw new RestfulException(ErrCode.DIY_ERROR, "测试服务仅限于linux环境下执行");
		}
		//代码执行超时设置
    	Integer flag=200;
	    final ExecutorService exec = Executors.newFixedThreadPool(1);
	    Callable<Integer> call = new Callable<Integer>() {//使用Callable接口作为构造参数    
	        public Integer call() throws Exception {  
	        	//真正的任务在这里执行，这里的返回值类型为Integer，可以为任意类型    
	        	return doTestProdbService(ip,port);
	        }  
	    };  
	    try {  
	        Future<Integer> future = exec.submit(call);  
	        flag = future.get(1000 * 10, TimeUnit.MILLISECONDS);   
	    } catch (Exception e) {  
	    	logger.error("测试服务连接超时：",e);
			throw new RestfulException(ErrCode.TIME_OUT, "测试服务连接超时");
	    }finally{
		    //关闭线程池  
		    exec.shutdown();  
	    }
	    if(flag == 200)
	    	return true;
	    else
			throw new RestfulException(ErrCode.DIY_ERROR, "测试服务连接失败");
	}

	@Override
	public ProtectObject getProtectObjectByObjName(String objName) {
		ProtectObject protectObject = protectObjectMapper.getProtectObjectByName(objName);
		if (protectObject == null)
			throw new RestfulException(ErrCode.UNKNOWN_ID, "找不到数据库");
		setNodes(protectObject);
		decodePassword(protectObject);
		setProxyIpAndProxyPort(protectObject);
		return protectObject;
	}

	@Override
	@Transactional
	public Boolean updateDbUserAndPassword(ProtectObject protectObject) {
		//加密密码
		setNewPassword(protectObject);
		protectObjectMapper.updateDbUserAndPassword(protectObject);
		return true;
	}

	@Override
	public List<Integer> getPortsByDbtype(Integer dbType) {
		return protectObjectMapper.getPortsByDbtype(dbType);
	}

	@Override
	@Transactional
	public Boolean updateRunMode(ProtectObject protectObject) {
		//if (!RunModeEnum.isValidRunMode(protectObject.getRunMode()))
		//	throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "运行模式错误:" + protectObject.getRunMode());
		protectObjectMapper.updateRunMode(protectObject.getObjId(), protectObject.getRunMode());
		logger.info("update protectObject id:{}, runMode:{} successfully", protectObject.getObjId(), protectObject.getRunMode());
		return true;
	}

	private Integer doTestProdbService(String ip, Integer port){
		Integer flag = 200;
    	Process p = null;
    	BufferedReader br=null;
    	try {    		
    		//执行shell脚本
    		String filename = System.getProperty("user.dir") + File.separator + "testProdbService.sh";
    		filename = filename.replace("/tomcat", "");
     		String cmd = "sh " + filename + " " + ip + " " + port;
     		logger.info("testProdbService cmd=========="+cmd);
			p = Runtime.getRuntime().exec(cmd);
			
			//读取ErrorStream很关键，这个解决了挂起的问题。
			String line = null;
			br =  new BufferedReader(new InputStreamReader(p.getErrorStream(),"GBK"));
			while ((line = br.readLine()) != null){
				logger.error("testProdbService error info=========="+line);
				flag = 442;
            }	
			p.waitFor();//一直等到Process执行完成
		} catch (Exception e) {
			flag=442;
			logger.error("测试服务：" + ip + "/" + port,e);
		}finally {
			try {
				if(null != br){
					br.close();
				}
				if(null != p){
					p.destroy();
				}
			} catch (Exception e) {} 
		}
    	logger.error("testProdbService result=========="+flag);
    	return flag;
	}

	/**
	 *  去除返回信息中的密码
	 * @param protectObject
	 */
	private void removePassword(ProtectObject protectObject) {
		if (!StringUtils.isBlank(protectObject.getDbPassword()))
			protectObject.setDbPassword("********");
		else
			protectObject.setDbPassword("");
	}

	/**
	 * 检查保护对象参数，并设置一些默认值
	 * @param protectObject
	 */
	private void checkAndSetDefaultValue(ProtectObject protectObject) {
		//给父节点设置默认值
		setDefaultValues(protectObject);
		//检查父节点参数
		checkObjectParam(protectObject);
		//给子节点设置默认值
		setNodeDefaultValues(protectObject);
		//检查子节点参数
		checkNodeParam(protectObject);
		//检查不能修改的参数
		checkCanNotChangeParam(protectObject);
		//检查分组
		checkGroupId(protectObject);
		//检查数据库名称是否重复
		checkObjName(protectObject);
		//检查是否重复配置数据库
		checkDb(protectObject);
		//检查代理IP和代理端口
		checkProxyIpAndProxyPort(protectObject);
		//处理密码
		setNewPassword(protectObject);
	}




	/**
	 * 处理子节点信息
	 * @param protectObject
	 */
	private void handleNodes(ProtectObject protectObject) {
		List<ProtectObject> newNodes = protectObject.getNodes();
		for (ProtectObject node : newNodes) {
			node.setParentId(protectObject.getObjId());
			if (node.getObjId() == null)
				protectObjectMapper.insertSelective(node);
			else { //更新子节点
				ProtectObject temp = protectObjectMapper.selectByPrimaryKey(node.getObjId());
				if (temp == null) //capaa更新数据库，回滚被删除的子节点的时候，才会出现节点objId不等于null，但是数据库没有这条记录
					protectObjectMapper.insertSelectiveWithObjId(node);
				else  //正常更新
					protectObjectMapper.updateByPrimaryKeySelective(node);
			}
		}
	}

	/**
	 * 处理密码
	 * @param protectObject
	 */
	private void setNewPassword(ProtectObject protectObject) {
		//如用密码是空串或null,则把密码置为空串,如果填写的是"********",表示不修改密码，其他情况把密码加密
		if(StringUtils.isBlank(protectObject.getDbPassword()))
			protectObject.setDbPassword("");
		else if(protectObject.getDbPassword().equals("********"))
			protectObject.setDbPassword(null);
		else
			protectObject.setDbPassword(JasyptEncryptor.encoder(protectObject.getDbPassword()));
	}

	/**
	 * 校验保护对象父节点参数填写是否合法
	 * @param p
	 */
	private void checkObjectParam(ProtectObject p) {
        if (p.getObjectType() == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "objectType为空");
		else if (p.getObjectType() != ObjTypeEnum.SINGLE.getNumber().intValue() && p.getObjectType() != ObjTypeEnum.CLUSTER.getNumber().intValue())
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "objectType错误:" + p.getObjectType());
		else if (p.getObjectType() == ObjTypeEnum.SINGLE.getNumber().intValue() && p.getNodes().size() > 0)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "非集群不能有子节点");
		if (StringUtils.isBlank(p.getObjName()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库名称为空");
        else if (p.getObjName().length() > 60)
            throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库名称最多只能输入60个字符");
		if (!isValidObjName(p.getObjName()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库名称可以输入中文、字母、数字、下划线且不能以数字开头");
		if (p.getDbType() == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库类型为空");
		else if (!DBEnum.isValidDbType(p.getDbType()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库类型错误:" + p.getDbType());
		//达梦不支持集群
//		if (p.getDbType() == DBEnum.DAMENG.getNumber().intValue() && p.getObjectType() == ObjTypeEnum.CLUSTER.getNumber().intValue())
//			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, String.format("%s类型的数据库不支持集群", DBEnum.DAMENG.getText()));
		//Hive不支持集群
//		if (p.getDbType() == DBEnum.HIVE.getNumber().intValue() && p.getObjectType() == ObjTypeEnum.CLUSTER.getNumber().intValue())
//			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, String.format("%s类型的数据库不支持集群", DBEnum.HIVE.getText()));
		if (StringUtils.isBlank(p.getIp()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库地址为空");
		else if (!IpUtils.isIp(p.getIp()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库地址格式错误:" + p.getIp());
		if (p.getPort() == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库端口为空");
        else if (p.getPort() <= 0 || p.getPort() > 65535) {
	          if (p.getDbType().intValue() != DBEnum.ODPS.getNumber().intValue()) {
	                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库端口范围不正确:" + p.getPort());
	            }
	        }
		//对于SQL Server来说，只需要IP+PORT; 子节点：IP+PORT
		//对于MySQL，只需要IP+PORT；子节点：IP+PORT
		//对于单个Oracle，只需要IP+PORT+服务名+实例名；
		//对于Oracle集群，父节点：IP+PORT+服务名；子节点：IP+PORT+实例名(可选);
		if (p.getDbType().intValue() == DBEnum.ORACLE.getNumber().intValue()) {
			//单库
			if (p.getObjectType().intValue() == ObjTypeEnum.SINGLE.getNumber().intValue()) {
				if (StringUtils.isBlank(p.getServiceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务名为空");
				if (StringUtils.isBlank(p.getInstanceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库实例名为空");
				else if (!isValidInstanceName(p.getInstanceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "数据库实例名格式错误");
			}
			if (p.getObjectType().intValue() == ObjTypeEnum.CLUSTER.getNumber().intValue()) {
				if (StringUtils.isBlank(p.getServiceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务名为空");
			}
			if (!isValidServiceName(p.getServiceName()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务名格式不正确，多个服务名请用英文逗号隔开");
            if (!isValidServiceName1(p.getServiceName())) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务名/库名格式不正确");
            }
		}
		//对于db2，单库的时候需要填写库名，不能填写实例名
		// PostgreSQL,UX,GP,Cache 必须填写库名
		if (p.getDbType().intValue() == DBEnum.DB2.getNumber()
				|| p.getDbType().intValue() == DBEnum.POSTGRESQL.getNumber()
				|| p.getDbType().intValue() == DBEnum.UXDB.getNumber()
				|| p.getDbType().intValue() == DBEnum.GREENPLUM.getNumber()
				|| p.getDbType().intValue() == DBEnum.CACHE.getNumber()
				|| p.getDbType().intValue() == DBEnum.OSCDB.getNumber()) {
			if (StringUtils.isBlank(p.getServiceName())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "库名为空");
			}
            if (!isValidServiceName1(p.getServiceName())) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务名/库名格式不正确");
            }
			if (StringUtils.isNotBlank(p.getInstanceName())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "该类型的数据库不能填写实例名");
			}
		}
	      if (p.getDbType().intValue() == DBEnum.ODPS.getNumber()) {
	            if (StringUtils.isBlank(p.getIp())) {
	                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务的访问链接为空");
	            }
	            if (StringUtils.isBlank(p.getServiceName())) {
	                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "项目空间名称为空");
	            }
	        }
		// KINGBASE 必须填写库名
		if (p.getDbType().intValue() == DBEnum.KINGBASE.getNumber()) {
			// 人大金仓版本
			if (StringUtils.isBlank(p.getVersion())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "该数据库版本不能为空");
			}
			if (StringUtils.isBlank(p.getServiceName())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "库名为空");
			}
			if (StringUtils.isNotBlank(p.getInstanceName())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "该类型的数据库不能填写实例名");
			}
		}
        // GBASE_S87
        if (p.getDbType().intValue() == DBEnum.GBASE_S87.getNumber()) {
			if (StringUtils.isBlank(p.getServiceName())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "库名为空");
			}
		}
		//如果是sql server或mysql或gbase 8.3 或hive 或 达梦并且填写了实例名或服务名，返回错误信息
        if (p.getDbType().intValue() == DBEnum.MYSQL.getNumber()
				|| p.getDbType().intValue() == DBEnum.DAMENG.getNumber()
				|| p.getDbType().intValue() == DBEnum.GBASE_S83.getNumber()
				|| p.getDbType().intValue() == DBEnum.MONGODB.getNumber()
				|| p.getDbType().intValue() == DBEnum.SYBASE.getNumber()
				|| p.getDbType().intValue() == DBEnum.OCEANBASE.getNumber()
				|| p.getDbType().intValue() == DBEnum.HANA.getNumber()
				||p.getDbType().intValue() == DBEnum.HOTDB.getNumber()
            || p.getDbType().intValue() == DBEnum.MARIADB.getNumber()
				||p.getDbType().intValue() == DBEnum.TIDB.getNumber()
				||p.getDbType().intValue() == DBEnum.SEQUOIADBMYSQL.getNumber()) {
			if (StringUtils.isNotBlank(p.getInstanceName()) || StringUtils.isNotBlank(p.getServiceName()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "该类型的数据库不能填写服务名和实例名");
		}
		if (p.getDbType().intValue() == DBEnum.SQLSERVER.getNumber()) {
			if (p.getIsUdp() == Integer.valueOf(1) && StringUtils.isBlank(p.getInstanceName())) {
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "动态端口需要填写实例名");
			}
		}
        //
        if (p.getDbType().intValue() == DBEnum.HIVE.getNumber() && p.getIsKrbs() == 1) {
            if (StringUtils.isBlank(p.getKeyTab())) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "客户端keyTab为空");
            }
            if (StringUtils.isBlank(p.getKrbConf())) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "krb5.conf为空");
            }
            if (StringUtils.isBlank(p.getKeyTabServer())) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "服务端keyTab为空");
            }
        }
		if (p.getRunMode() == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "运行模式为空");
		else if (!RunModeEnum.isValidRunMode(p.getRunMode()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "运行模式错误:" + p.getRunMode());
		if (StringUtils.isBlank(p.getProxyIp()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "代理IP为空");
		else if (!IpUtils.isIp(p.getProxyIp()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "代理IP格式错误:" + p.getProxyIp());
		if (p.getProxyPort() == null)
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "代理端口为空");
		else if (p.getProxyPort() <= 1024 || p.getProxyPort() > 65535)
            if (p.getDbType().intValue() != DBEnum.ODPS.getNumber().intValue()) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "代理端口范围应为1025-65535");
            }
		else if (isProxyPortConflictWithSysPort(p.getProxyPort()))
                if (p.getDbType().intValue() != DBEnum.ODPS.getNumber().intValue()) {
                    throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "代理端口已被系统占用");
                }
        if (p.getDbType().intValue() == DBEnum.ODPS.getNumber()) {
            List<ProtectObject> list = protectObjectMapper.getAllProjectByDbtype(DBEnum.ODPS.getNumber());
            if (list != null && list.size() > 0) {
                if (p.getObjId() == null) {
                    throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "MaxConpute数据库类型只能创建一个");
                } else {
                    if (p.getObjId().intValue() != list.get(0).getObjId().intValue()) {
                        throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "MaxConpute数据库类型只能创建一个");
                    }
                }
            }
        }
        if (p.getDbType().intValue() != DBEnum.ODPS.getNumber() && StringUtils.isNotBlank(p.getTunnelUrl())) {
            throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "Tunnel服务的访问链接不为空");
        }
        if (p.getDbType().intValue() == DBEnum.HBASE.getNumber()) {
            if (p.getZkObjId() == null) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "zookeeper为空");
            }
            ProtectObject p1 = protectObjectMapper.selectByPrimaryKey(p.getZkObjId());
            if (p1 == null) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "zookeeper为空");
            }
            if (StringUtils.isBlank(p.getProxyDomainName())) {
                throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "代理域名为空");
            }
        }
	}

	/**
	 * 给保护对象设置默认值
	 * @param protectObject
	 */
	private void setDefaultValues(ProtectObject protectObject) {
		if(protectObject.getGroupId() == null)
			protectObject.setGroupId(0);
		protectObject.setParentId(0);
		protectObject.setStatus(StatusTypeEnum.STOP.getNumber());
		if (protectObject.getNodes() == null)
			protectObject.setNodes(new ArrayList<>());
	}
	/**
	 * 给子节点设置默认值
	 * @param parent 父节点
	 */
	private void setNodeDefaultValues(ProtectObject parent) {
		List<ProtectObject> nodes = parent.getNodes();
		for (ProtectObject node : nodes) {
			node.setGroupId(0);
			node.setObjectType(ObjTypeEnum.NODE.getNumber());
			//子节点状态、数据库类型、运行模式与父节点同步
			node.setStatus(parent.getStatus());
			node.setDbType(parent.getDbType());
			node.setRunMode(parent.getRunMode());
		}
	}

	/**
	 * 如果选择了分组，判断分组是否合法
	 * @param protectObject
	 */
	private void checkGroupId(ProtectObject protectObject) {
		if (protectObject.getGroupId() == null || protectObject.getGroupId() == 0) {
			return;
		}
		//集群不能被分组
        if (protectObject.getObjectType().intValue() == ObjTypeEnum.NODE.getNumber().intValue())
            throw new RestfulException(ErrCode.PARAM_ERROR, "子节点不能被分组");
		ProtectObjectGroup group = protectObjectGroupService.getProtectObjectGroupById(protectObject.getGroupId());
		if (group == null)
			throw new RestfulException(ErrCode.DIY_ERROR, "找不到分组");
		else if (protectObject.getDbType().intValue() != group.getDbType().intValue())
            throw new RestfulException(ErrCode.PARAM_ERROR, "分组数据库类型与数据库的类型不同，请重新选择");
	}

	/**
	 * 判断保护对象名称是否重复(父节点和子节点)
	 * @param protectObject
	 */
	private void checkObjName(ProtectObject protectObject) {
		//判断保护对象父节点和子节点本身有没有重复
		Map<String, ProtectObject> objNameMap = new LinkedHashMap<>();
		objNameMap.put(protectObject.getObjName().toUpperCase(), protectObject);
		for (ProtectObject node : protectObject.getNodes())
			objNameMap.put(node.getObjName().toUpperCase(), node);
		if (objNameMap.values().size() != protectObject.getNodes().size() + 1)
			throw new RestfulException(ErrCode.EXIST_OBJECT, "数据库名称和子节点名称重复");
		Map<Integer, Boolean> idMap = new LinkedHashMap<>();
		for (ProtectObject p : objNameMap.values()) {
			if (p.getObjId() != null)
				idMap.put(p.getObjId(), true);
		}
		for (ProtectObject p : objNameMap.values()) {
			ProtectObject object = protectObjectMapper.getProtectObjectByName(p.getObjName());
			//如果数据库里有重复的，并且重复的这条记录不属于这个保护对象（父节点和子节点），则认为重复
			if (object != null && idMap.get(object.getObjId()) == null)
				throw new RestfulException(ErrCode.EXIST_OBJECT, "数据库名称已存在" + p.getObjName());
		}
	}

	/**
	 * 检查生产库是否已经被注册(父节点和子节点)
	 * 即，校验IP+端口+服务名+实例名是否重复
	 * @param protectObject
	 */
	private void checkDb(ProtectObject protectObject) {
		//父节点和子节点本身有没有重复
		checkDbItSelf(protectObject);
		List<ProtectObject> allList = new ArrayList<>();
		allList.add(protectObject);
		allList.addAll(protectObject.getNodes());
		Map<Integer, Boolean> idMap = new LinkedHashMap<>();
		for (ProtectObject p : allList) {
			if (p.getObjId() != null)
				idMap.put(p.getObjId(), true);
		}
		//只填写IP+PORT的情况下，IP+PORT唯一
		//如果还填写了实例名，IP+PORT+实例名唯一
		//如果还填写了服务名，IP+PORT+服务名唯一
		for (ProtectObject p : allList) {
	          if (p.getDbType() == DBEnum.ODPS.getNumber()) {
	                continue;
	            }
			//只填写IP+PORT的情况下
			if (StringUtils.isBlank(p.getServiceName()) && StringUtils.isBlank(p.getInstanceName())) {
				ProtectObject object = protectObjectMapper.selectByIPPortInstanceNameAndServiceName(p.getIp(), p.getPort(), null, null);
				if (object != null && idMap.get(object.getObjId()) == null)
					throw new RestfulException(ErrCode.EXIST_OBJECT, "数据库已存在" + p.getIp() + ":" +p.getPort());
			} else {
				if (StringUtils.isNotBlank(p.getInstanceName())) {
					ProtectObject object = protectObjectMapper.selectByIPPortInstanceNameAndServiceName(p.getIp(), p.getPort(), p.getInstanceName(), null);
					if (object != null && idMap.get(object.getObjId()) == null)
						throw new RestfulException(ErrCode.EXIST_OBJECT, "数据库已存在" + p.getIp() + ":" + p.getPort() + "/" + p.getInstanceName());
				}
				if (StringUtils.isNotBlank(p.getServiceName())) {
					ProtectObject object = protectObjectMapper.selectByIPPortInstanceNameAndServiceName(p.getIp(), p.getPort(), null, p.getServiceName());
					if (object != null && idMap.get(object.getObjId()) == null)
						throw new RestfulException(ErrCode.EXIST_OBJECT, "数据库已存在" + p.getIp() + ":" + p.getPort() + "/" + p.getServiceName());
				}
			}
		}
	}

	/**
	 * 检查父节点子节点之间生产库配置是否重复
	 * @param protectObject
	 */
	private void checkDbItSelf(ProtectObject protectObject) {
		List<ProtectObject> tempList = new ArrayList<>();
		//先把父节点加入tempList
		tempList.add(protectObject);
		//如果有子节点，再检查子节点和父节点，以及子节点之间有没有重复
		for (ProtectObject node : protectObject.getNodes()) {
			for (ProtectObject temp : tempList) {
				if (StringUtils.isBlank(node.getServiceName()) && StringUtils.isBlank(node.getInstanceName())) {
					if (temp.getIp().equals(node.getIp()) && temp.getPort().intValue() == node.getPort())
						throw new RestfulException(ErrCode.EXIST_OBJECT, "父节点和子节点配置重复" + node.getIp() + ":" + node.getPort());
				} else {
					if (StringUtils.isNotBlank(node.getInstanceName())) {
						if (temp.getIp().equals(node.getIp()) && temp.getPort().intValue() == node.getPort()
								&& (StringUtils.isBlank(temp.getInstanceName()) || temp.getInstanceName().equals(node.getInstanceName()))) {
							throw new RestfulException(ErrCode.EXIST_OBJECT, "父节点和子节点配置重复" + node.getIp() + ":" + node.getPort() + "/" + node.getInstanceName());
						}
					}
					if (StringUtils.isNotBlank(node.getServiceName())) {
						if (temp.getIp().equals(node.getIp()) && temp.getPort().intValue() == node.getPort()
								&& (StringUtils.isBlank(temp.getServiceName()) || temp.getServiceName().equals(node.getServiceName()))) {
							throw new RestfulException(ErrCode.EXIST_OBJECT, "父节点和子节点配置重复" + node.getIp() + ":" + node.getPort() + "/" + node.getServiceName());
						}
					}
				}
			}
			//都没冲突加到tempList里面
			tempList.add(node);
		}
	}

	/**
	 * 检查代理端口是否重复(父节点和子节点)
	 * @param protectObject
	 */
	private void checkProxyIpAndProxyPort(ProtectObject protectObject) {
		//注：原IP+PORT不能重复的逻辑，改成只判断PORT有没有重复
		//判断父节点和子节点本身有没有重复
	       if (protectObject.getDbType()==DBEnum.ODPS.getNumber()) {
	            return;
	        }
		Map<String, ProtectObject> map = new LinkedHashMap<>();
		String key = String.valueOf(protectObject.getProxyPort());
		map.put(key, protectObject);
		for (ProtectObject node : protectObject.getNodes()) {
			key = String.valueOf(node.getProxyPort());
			if (map.containsKey(key))
				throw new RestfulException(ErrCode.EXIST_OBJECT, String.format("代理端口%s已被使用，请重新输入", node.getProxyPort()));
			map.put(key, node);
		}
		//判断与数据库里的是否重复
		Map<Integer, Boolean> idMap = new LinkedHashMap<>();
		for (ProtectObject p : map.values()) {
			if (p.getObjId() != null)
				idMap.put(p.getObjId(), true);
		}
		for (ProtectObject p : map.values()) {
			CamProxy camProxy = camProxyMapper.getByProxyPort(String.valueOf(p.getProxyPort()));
			//如果数据库里有重复的，并且重复的这条记录不属于这个保护对象（父节点和子节点），则认为重复
			if (camProxy != null && idMap.get(camProxy.getDbid().intValue()) == null)
				throw new RestfulException(ErrCode.EXIST_OBJECT, String.format("代理端口%s已被使用，请重新输入", p.getProxyPort()));
		}
	}


	/**
	 * 判断节点参数是否合法
	 * @param parent 父节点
	 */
	private void checkNodeParam(ProtectObject parent) {
		List<ProtectObject> nodes = parent.getNodes();
		//节点名称 IP地址 端口 代理IP 代理端口 都是必填的
		//对于Oracle节点，还可以填写实例名（可选）
		for (ProtectObject node : nodes) {
			if (StringUtils.isBlank(node.getObjName()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点名称为空");
			else if (node.getObjName().length() > 14)
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点名称最多只能输入14位字符");
			if (StringUtils.isBlank(node.getIp()) || !IpUtils.isIp(node.getIp()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点IP为空或格式不正确");
			if (node.getPort() == null || (node.getPort() <= 0 || node.getPort() > 65535))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点端口为空或范围不正确");
			//Oracle 无需特殊判断
			//sql server或mysql gbase 8.3 并且填写了实例名，返回错误信息
			if (parent.getDbType() == DBEnum.MYSQL.getNumber().intValue()
					|| parent.getDbType() == DBEnum.GBASE_S83.getNumber().intValue()
					|| parent.getDbType() == DBEnum.MONGODB.getNumber().intValue()
					|| parent.getDbType() == DBEnum.SYBASE.getNumber().intValue()) {
				if (StringUtils.isNotBlank(node.getInstanceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "Mongodb或SQL Server或MySQL类型的子节点不能填写实例名");
			}
			//DB2的子节点需要填写库名，不能填写实例名
			if (parent.getDbType() == DBEnum.DB2.getNumber().intValue()) {
				if (StringUtils.isBlank(node.getServiceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点库名为空");
				if (StringUtils.isNotBlank(node.getInstanceName()))
					throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "DB2类型的子节点不能填写实例名");
			}
			if (StringUtils.isNotBlank(node.getInstanceName()) && !isValidInstanceName(node.getInstanceName()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点实例名格式错误");
			if (StringUtils.isBlank(node.getProxyIp()) || !IpUtils.isIp(node.getProxyIp()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点代理IP为空或格式不正确");
			if (node.getProxyPort() == null || (node.getProxyPort() <= 1024 || node.getProxyPort() > 65535))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点代理端口范围应为1025-65535");
			else if (isProxyPortConflictWithSysPort(node.getProxyPort()))
				throw new RestfulException(ErrCode.PARAM_CHECK_ERROR, "子节点代理端口已被系统占用");
		}
	}

	/**
	 * 给保护对象补充子节点信息
	 * @param p
	 */
	private void setNodes(ProtectObject p) {
		List<ProtectObject> nodes = protectObjectMapper.getByParentId(p.getObjId());
		p.setNodes(nodes);
	}

	/**
	 * 处理节点被删除的情况
	 * @param newNodes
	 * @param oldNodes
	 */
	private void deleteOldNodes(List<ProtectObject> newNodes, List<ProtectObject> oldNodes) {
		// 前端传的nodes可能为null,所以这里要处理一下
		if (newNodes == null)
			newNodes = new ArrayList<>();
		for (ProtectObject oldNode : oldNodes) {
			boolean find = false;
			for (ProtectObject newNode : newNodes) {
				if (newNode.getObjId() == null)
					continue;
				if (newNode.getObjId().intValue() == oldNode.getObjId().intValue()) {
					find = true;
					break;
				}
			}
			if (!find) {
				protectObjectMapper.deleteByPrimaryKey(oldNode.getObjId());
				camProxyMapper.deleteByDbid(Long.valueOf(oldNode.getObjId()));
				logger.info("delete protectObject oldNode in update successfully, id:{}", oldNode.getObjId());
			}
		}
	}

	/**
	 * 读取csv格式的模板，解析成保护对象数组
	 * @param fileName
	 * @return
	 */
	private List<ProtectObject> readCsvFile(String fileName) {
		String str = FileUtil.read(fileName);
		String[] astr = str.split("\n");
		List<ProtectObject> plist = new ArrayList<>();
		boolean first = true;
		for (String as : astr) {
			if (first) {
				first = false;
				continue;
			}
			ProtectObject p = new ProtectObject();
			plist.add(p);
			int column = 0;
			for (String s : as.split(",")) {
				convertToObject(column, p, s);
				column++;
			}
		}
		return plist;
	}

	/**
	 * 读取xls格式的模板，解析成保护对象数组
	 * @param fileName
	 * @return
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private List<ProtectObject> readXlsFile(String fileName) {
		List<String> list = ReadExcelFile.readExcel(fileName);
        int totalColumn = 12;
		int column = 0;
		List<ProtectObject> plist = new ArrayList<>();
		ProtectObject p = null;
		for (String s : list) {
			if (column == 0) {
				p = new ProtectObject();
				plist.add(p);
			}
			convertToObject(column, p, s);
			column++;
			column = column % totalColumn;
		}
		return plist;
	}

	/**
	 * 根据第几列，将s设置到保护对象对应的属性上
	 * @param column
	 * @param p
	 * @param s
	 */
	private void convertToObject(int column, ProtectObject p, String s) {
		switch (column) {
			case 0:
				p.setObjName(s);
				break;
			case 1:
				if (StringUtils.isBlank(s))
					break;
				int dbType = DBEnum.getNumberByDbText(s);
				p.setDbType(dbType);
				break;
			case 2:
                p.setIp(s.trim());
				break;
			case 3:
				if (StringUtils.isBlank(s))
					break;
				try {
					p.setPort(Integer.parseInt(s));
				} catch (NumberFormatException e) {
					p.setPort(-1);
				}
				break;
			case 4:
				p.setServiceName(s);
				break;
			case 5:
				p.setInstanceName(s);
				break;
			case 6:
				p.setDbUser(s);
				break;
			case 7:
				p.setDbPassword(s);
				break;
			case 8:
                p.setProxyIp(s.trim());
				break;
			case 9:
				if (StringUtils.isBlank(s))
					break;
				try {
                    p.setProxyPort(Integer.parseInt(s.trim()));
				} catch (NumberFormatException e) {
					p.setProxyPort(-1);
				}
				break;
			case 10:
                p.setVersion(s.trim());
				break;
            case 11:
                p.setTunnelUrl(s);
                break;
		}
	}

	/**
	 * 更新的时候，检查不能修改的字段
	 * 保护对象名称不能修改
	 * 数据库类型不能修改
	 * @param protectObject
	 */
	private void checkCanNotChangeParam(ProtectObject protectObject) {
		//有id的是更新，没有的是新增
		if(protectObject.getObjId() == null)
			return;
		ProtectObject oldProtectObject = getProtectObjectById(protectObject.getObjId(),false);
		if(!protectObject.getObjName().equals(oldProtectObject.getObjName()))
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR,"数据库名称不能修改");
		if(protectObject.getDbType().intValue() != oldProtectObject.getDbType().intValue())
			throw new RestfulException(ErrCode.PARAM_CHECK_ERROR,"数据库类型不能修改");
	}

	/**
	 * 解密密码
	 * @param protectObject
	 */
	private void decodePassword(ProtectObject protectObject) {
		//密码只有父节点才有
		if(StringUtils.isNotBlank(protectObject.getDbPassword()))
			protectObject.setDbPassword(JasyptEncryptor.decoder(protectObject.getDbPassword()));
	}

	/**
	 * 判断保护对象名称是否合法
	 * @param objName
	 * @return
	 */
	private Boolean isValidObjName(String objName) {
		//不能以数字和下划线开头
		String strPattern = "^[A-Za-z_\\$\u4e00-\u9fa5][A-Za-z0-9_\\$\u4e00-\u9fa5]*$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(objName);
		return m.matches();
	}

	/**
	 * 检查实例名是否合法
	 * @param instanceName
	 * @return
	 */
	private Boolean isValidInstanceName(String instanceName) {
		//数字英文_#$
//		String pattern = "[a-zA-Z0-9_#$]+";
//		Pattern p = Pattern.compile(pattern);
//		Matcher m = p.matcher(instanceName);
//		return m.matches();
	    return true;
	}


	/**
	 * 检查服务是否合法
	 * @param serviceName
	 * @return
	 */
	private boolean isValidServiceName(String serviceName) {
		try {
            if (StringUtils.isNotBlank(serviceName)) {
                serviceName.split(",");
            }
		} catch (Exception e) {
			return false;
		}
		return true;
	}

    /**
     * 检查服务是否合法
     * 
     * @param serviceName
     * @return
     */
    private boolean isValidServiceName1(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return true;
        }
        String pattern = "[a-zA-Z0-9_#$-,，]+";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(serviceName);
        return m.matches();
    }


	private Boolean isInvalidRecord(ProtectObject protectObject) {
		if (StringUtils.isNotBlank(protectObject.getObjName()))
			return false;
		if (protectObject.getDbType() != null)
			return false;
		if (StringUtils.isNotBlank(protectObject.getIp()))
			return false;
		if (protectObject.getPort() != null)
			return false;
		if (StringUtils.isNotBlank(protectObject.getServiceName()))
			return false;
		if (StringUtils.isNotBlank(protectObject.getInstanceName()))
			return false;
		if (StringUtils.isNotBlank(protectObject.getDbUser()))
			return false;
		if (StringUtils.isNotBlank(protectObject.getDbPassword()))
			return false;
		if (StringUtils.isNotBlank(protectObject.getProxyIp()))
			return false;
		if (protectObject.getProxyPort() != null)
			return false;
		return true;
	}


	/**
	 * 往数据库插入代理IP和代理端口信息（父节点和子节点的）
	 * @param protectObject
	 */
	private void handleProxyIpAndProxyPort(ProtectObject protectObject) {
		handleOneProxyIpAndProxyPort(protectObject.getObjId(), protectObject.getProxyIp(), protectObject.getProxyPort());
		if (protectObject.getNodes() != null) {
			for (ProtectObject p : protectObject.getNodes())
				handleOneProxyIpAndProxyPort(p.getObjId(), p.getProxyIp(), p.getProxyPort());
		}
	}

	/**
	 * 处理一条代理IP和代理端口信息
	 * @param objId
	 * @param proxyIp
	 * @param proxyPort
	 */
	private void handleOneProxyIpAndProxyPort(Integer objId, String proxyIp, Integer proxyPort) {
		CamProxy camProxy = new CamProxy();
		camProxy.setDbid(Long.valueOf(objId));
		camProxy.setProxyPort(String.valueOf(proxyPort));
		camProxy.setDeviceId(getDeviceId(proxyIp));
		if (camProxyMapper.getByDbid(camProxy.getDbid()) == null)
			camProxyMapper.insertSelective(camProxy);
		else
			camProxyMapper.updateByDbid(camProxy);
	}

	private Integer getDeviceId(String proxyIp) {
		CamDevice camDevice = camDeviceMapper.getByProxyIp(proxyIp);
		if (camDevice != null) {
			return camDevice.getId();
		} else {
			Date date = new Date();
			CamDevice temp = new CamDevice();
			temp.setDeviceId(1);
			temp.setProxyIp(proxyIp);
			temp.setCreateTime(date);
			temp.setUpdateTime(date);
			camDeviceMapper.insertSelective(temp);
			return temp.getId();
		}
	}

	/**
	 * 给保护对象数组补充代理IP、代理端口
	 * @param list
	 */
	private void setProxyIpAndProxyPort(List<ProtectObject> list) {
		list.forEach(this::setProxyIpAndProxyPort);
	}

	/**
	 * 给单个保护对象补充代理IP、代理端口
	 * @param protectObject
	 */
	private void setProxyIpAndProxyPort(ProtectObject protectObject) {
		CamProxy camProxy = getCamProxy(protectObject.getObjId());
		if (camProxy != null) {
			protectObject.setProxyIp(camProxy.getProxyIp());
			protectObject.setProxyPort(Integer.valueOf(camProxy.getProxyPort()));
		}
		if (protectObject.getNodes() == null)
			return;
		for (ProtectObject p : protectObject.getNodes()) {
			CamProxy temp = getCamProxy(p.getObjId());
			if (temp != null) {
				p.setProxyIp(temp.getProxyIp());
				p.setProxyPort(Integer.valueOf(temp.getProxyPort()));
			}
		}
	}

	private CamProxy getCamProxy(Integer objId) {
		return camProxyMapper.getByDbid(Long.valueOf(objId));
	}

	/**
	 * 检查代理端口是否和系统保留端口冲突
	 * @param proxyPort
	 * @return true 冲突 false不冲突
	 */
	private Boolean isProxyPortConflictWithSysPort(Integer proxyPort) {
		return sysPort.contains("," + proxyPort + ",");
	}

	/**
	 * 获取账户密码不为空且符合数据库类型的保护对象(数据脱敏专用)
	 * 
	 * @param dbType
	 * @return
	 */
	@Override
	public List<ProtectObject> queryProtectObjectByDbType(Integer dbType) {
		List<ProtectObject> list = protectObjectMapper.getProjectByDbtype(dbType);
		if (list != null) {
			for (ProtectObject protectObject : list) {
				removePassword(protectObject);
			}
		}
		return list;
	}

	@Override
	public List<ProtectObject> sycSqlserverTcpPort(Integer dbType) {
		List<ProtectObject> list = protectObjectMapper.getAllProjectByDbtype(DBEnum.SQLSERVER.getNumber());
		Map<Integer, ProtectObject> map = new HashMap<>();
		for (ProtectObject protectObject : list) {
			map.put(protectObject.getObjId(), protectObject);
		}
		List<ProtectObject> result = new ArrayList<>();
		for (ProtectObject protectObject : list) {
			if (protectObject.getDbType() != DBEnum.SQLSERVER.getNumber()) {
				continue;
			}
			// 判断是否是sqlserver子节点,如果是子节点补充UDP端口和动态端口标志
			if (protectObject.getParentId() != null && protectObject.getParentId() != Integer.valueOf(0)) {
				if (map != null && map.containsKey(protectObject.getParentId())) {
					ProtectObject parent_protect = map.get(protectObject.getParentId());
					if (StringUtils.isNotBlank(protectObject.getInstanceName())) {
						protectObject.setIsUdp(parent_protect.getIsUdp());
						protectObject.setUdpPort(parent_protect.getUdpPort());
					}
				}
			}
			if (protectObject.getIsUdp() != Integer.valueOf(1)) {
				continue;
			}
			String msg = null;
			try {
				msg = UdpUtil.getInstance(protectObject.getIp(), protectObject.getUdpPort())
						.sendUpdReq(getUdpMsg(protectObject), true);
				if (StringUtils.isBlank(msg) || (Integer.valueOf(msg) != null
						&& Integer.valueOf(msg).intValue() == protectObject.getPort().intValue())) {
					continue;
				}
				protectObject.setPort(Integer.valueOf(msg));
				result.add(protectObject);
			} catch (Exception e) {
				logger.error("The sqlserver istance: " + protectObject.getInstanceName() + ",UDP port :"
						+ protectObject.getUdpPort());
				logger.error(e.getMessage(), e);
				continue;
			}
		}
		return result;
	}

	private String getUdpMsg(ProtectObject protectObject) {
		String msg = null;
		if (StringUtils.isNotBlank(protectObject.getUdpMsgHead())) {
			msg = ((char) Integer.parseInt(protectObject.getUdpMsgHead(), 16)) + protectObject.getInstanceName();
		} else {
			msg = ((char) Integer.parseInt("04", 16)) + protectObject.getInstanceName();
		}
		return msg;
	}

    public static void main(String[] args) {
        System.out.println(JasyptEncryptor.decoder("Zu5mIJN5AjWHpFv3jAiCPBOJzKnq0baiZ"));
    }

}
