package com.hzmc.dbmgr.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @ProjectName: svc-search-biz
 * @description: 设置允许跨域
 * @author: lanhaifeng
 * @create: 2018-05-08 16:37
 * @UpdateUser:
 * @UpdateDate: 2018/5/8 16:37
 * @UpdateRemark:
 **/
//@Component
public class SimpleCORSFilter {//implements Filter {
//    private final static Logger logger = LoggerFactory.getLogger(SimpleCORSFilter.class);
//    @Value("${web.server}")
//    String webLocation;
//
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//        if(logger.isDebugEnabled()){
//            logger.info("api url:"+((HttpServletRequest)req).getRequestURI());
//        }
//        HttpServletResponse response = (HttpServletResponse) res;
////        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Origin", webLocation);
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, cache-control, if-modified-since, pragma");
//
//        chain.doFilter(req, res);
////        try {
////            if(loginValidate(req,res)){
////                chain.doFilter(req, res);
////            }else {
////                response.setContentType("application/json;charset=UTF-8");
////                response.getWriter().print("未登陆，请先登陆再访问该接口！");
////            }
////        } catch (Exception e) {
////            if( e instanceof BusinessException){
////                BusinessException businessException = (BusinessException) e;
////                logger.error("[" + businessException.getMessage() + "] {}" , StringUtil.getStackTrace(businessException.getSourceException()));
////            }else {
////                logger.error("[系统异常] {}",StringUtil.getStackTrace(e));
////            }
////
////            response.setContentType("application/json;charset=UTF-8");
////            response.getWriter().print("未知异常，api调用失败！");
////        }
//    }
//
//    public void init(FilterConfig filterConfig) {}
//
//    public void destroy() {}

}
