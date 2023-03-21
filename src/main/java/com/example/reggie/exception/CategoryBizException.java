package com.example.reggie.exception;

public class CategoryBizException extends BaseBizException{
    public CategoryBizException() {
        super();
    }

    public CategoryBizException(String errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public CategoryBizException(ExceptionDef def) {
        super(def);
    }
}
