package com.hzmc.dbmgr.common.bean;

import com.hzmc.dbmgr.common.exceptions.RestfulException;
import com.hzmc.dbmgr.dbenum.ErrCode;

import java.io.Serializable;

/**
 * 这个自定义Bean相当于capaa里面的ResponseBean
 * @param <T>
 */
public class ResultBean<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean success = true;
    private String code = ErrCode.SUCCESS.getValue();
    private String message = ErrCode.SUCCESS.getMessage();
    private T data;

    public ResultBean() {
        super();
    }

    public ResultBean(T data) {
        super();
        this.data = data;
    }

    //自定义异常
    public ResultBean(RestfulException e) {
        super();
        this.success = false;
        this.code = e.getErrorCode();
        this.message = e.getErrorMessage();
    }

    //其他异常
    public ResultBean(Throwable e) {
        super();
        this.success = false;
        this.code = ErrCode.UNKNOW_ERROR.getValue();
        this.message = ErrCode.UNKNOW_ERROR.getMessage();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
