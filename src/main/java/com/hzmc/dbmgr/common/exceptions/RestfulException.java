package com.hzmc.dbmgr.common.exceptions;

import com.hzmc.dbmgr.dbenum.ErrCode;

public class RestfulException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    // 异常code
    private String errorCode;
    // 异常信息
    private String errorMessage;

    public RestfulException() {
    }

    public RestfulException(ErrCode errCode) {
        this.errorCode = errCode.getValue();
        this.errorMessage = errCode.getMessage();
    }

    public RestfulException(ErrCode errCode, String customMessage) {
        this.errorCode = errCode.getValue();
        this.errorMessage = customMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }


    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
