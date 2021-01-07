 /**
 * 版权所有：美创科技
 * 项目名称：capaa-web
 * 创建者：Mark Gan(ganzg)
 * 创建日期：2017年7月21日
 * 文件说明：
 * 最近修改者：Mark Gan(ganzg)
 * 最近修改日期：2017年7月21日
 *
 */
package com.hzmc.dbmgr.common.aop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.hzmc.dbmgr.util.IPRequest;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;





/**
 * 类说明：
 * Web log 日志统一处理类
 * @author Mark Gan(ganzg)
 *
 */
@Aspect
@Component
@Order(-5)
public class WebLogAspect {

	private final static Logger logger = Logger.getLogger(WebLogAspect.class);

	ThreadLocal<Long> startTime = new ThreadLocal<Long>();

	/**
	 * 定义一个切入点. 解释下：
	 *
	 * web包或者子包中的所有public 方法，返回类型任意，方法参数任意。
	 *
	 */
	@Pointcut("execution(public * com.hzmc.dbmgr.web.controller..*.*(..))")
	public void webLog() {
	}

	@Before("webLog()")
	public void doBefore(JoinPoint joinPoint) {
		startTime.set(System.currentTimeMillis());
		
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attributes == null) {
			return;
		}
		HttpServletRequest request = attributes.getRequest();
		
		logger.info("---- request from " + IPRequest.getIpAddr(request) + ", Method:" + request.getMethod()
			+ ", URL:" + request.getRequestURI() + "?" + reqParams2String(request));
		
		logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "."
				+ joinPoint.getSignature().getName());
		logger.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
	}

	@AfterReturning("webLog()")
	public void doAfterReturning(JoinPoint joinPoint) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attributes == null) {
			return;
		}
		HttpServletRequest request = attributes.getRequest();
		
        logger.info("---- End request:" + request.getRequestURI() + " cost: " + (System.currentTimeMillis() - startTime.get()) + " ms");
	}
	
	protected String reqParams2String(HttpServletRequest request) {
		Enumeration paramsNames = request.getParameterNames();
		List<String> paramKeys = new ArrayList<String>();
		while(paramsNames.hasMoreElements()) {
			paramKeys.add((String)paramsNames.nextElement());
		}
		
		return reqParams2String(request, paramKeys);
	}
	
	protected String reqParams2String(HttpServletRequest request, List<String> paramNames) {
		StringBuilder build = new StringBuilder();
		
		boolean firstKey = true;
		for(String paramName: paramNames) {
			String[] vals = request.getParameterValues(paramName);
			if(vals.length > 1) {
				List<String> valList = new ArrayList<String>();
				for(String val: vals) {
					valList.add(val);
				}
				Collections.sort(valList, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o1.compareTo(o2);
					}
				});
				for(String val: valList) {
					if(firstKey) {
						firstKey = false;
					}else {
						build.append("&");
					}
					build.append(paramName).append("=").append(val);
				}
			}else {
				for(String val: vals) {
					if(firstKey) {
						firstKey = false;
					}else {
						build.append("&");
					}
					build.append(paramName).append("=").append(val);
				}
			}
		}
		return build.toString();
	}

}
