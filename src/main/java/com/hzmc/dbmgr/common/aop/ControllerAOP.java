package com.hzmc.dbmgr.common.aop;

import com.hzmc.dbmgr.common.bean.ResultBean;
import com.hzmc.dbmgr.common.exceptions.RestfulException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 处理和包装异常
 */
@Aspect
@Component
@Order(-4)
public class ControllerAOP {

    private static Logger logger = LoggerFactory.getLogger(ControllerAOP.class);

    //controller包及子包中，public的，且返回类型为ResultBean的所有方法
    @Pointcut("execution(public com.hzmc.dbmgr.common.bean.ResultBean com.hzmc.dbmgr.web.controller..*.*(..))")
    public void webLog() {
    }

    @Around("webLog()")
    public Object handleAPI(ProceedingJoinPoint pjp) {
        ResultBean<?> result;
        try {
            result = (ResultBean<?>) pjp.proceed();
        } catch (RestfulException e) {
            result = handleRestException(pjp, e);
        } catch (Throwable e) {
            result = handleOtherException(pjp, e);
        }
        return result;
    }

    private ResultBean<?> handleRestException(ProceedingJoinPoint pjp, RestfulException e) {
        // 已知异常, result的信息已经在构造函数里面设置；
        ResultBean<?> result = new ResultBean(e);
        if (logger.isDebugEnabled())
            logger.debug(pjp.getSignature().toString(), e);
        //已知异常，不记录在日志里面
        return result;
    }

    private ResultBean<?> handleOtherException(ProceedingJoinPoint pjp, Throwable e) {
        //result的信息，已经在构造函数里面设置了的。
        ResultBean<?> result = new ResultBean(e);
        logger.error(pjp.getSignature() + " error ", e);
        return result;
    }
}
