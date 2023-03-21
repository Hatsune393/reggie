package com.example.reggie.exception;

public class BaseBizException extends RuntimeException{
    /**
     * 错误码
     */
    private String errCode;

    /**
     * 错误信息
     */
    private String errMsg;

    public BaseBizException() {
        super();
    }

    public BaseBizException(String errCode, String errMsg) {
        super(errCode + " " + errMsg);
    }

    public BaseBizException(ExceptionDef def) {
        super(def.getErrCode() + " " + def.getErrMsg());
    }
}
