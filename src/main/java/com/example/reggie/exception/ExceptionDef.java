package com.example.reggie.exception;


public enum ExceptionDef {
    /***
     * EMPLOYEE相关异常，错误码范围：[100000~101000)
     */
    EMPLOYEE_CREATE_DUPLICATE_ERR(10_00_00, "员工用户名重复错误"),
    EMPLOYEE_NOT_EXIST_ERR(10_00_01, "员工不存在"),

    /***
     * Category相关异常，错误码范围：[101000~102000)
     */
    CATEGORY_CREATE_DUPLICATE_ERR(10_10_00, "员工用户名重复错误"),
    CATEGORY_DELETE_WITH_DISH_ERR(10_10_01, "删除分类时含有相关联菜品"),
    CATEGORY_DELETE_WITH_SETMEAL_ERR(10_10_02, "删除发呢类时含有相关联套餐"),

    /***
     * Setmeal相关异常，错误码范围：[102000~103000)
     */
    DELETE_LIVING_SETMEAL_ERR(102000, "删除正在售卖的套餐，请先停售"),

    /***
     * User相关异常，错误码范围：[103000~104000)
     */
    VALIDATE_CODE_DIFFER_ERR(103000, "验证码错误"),
    VALIDATE_CODE_EXPIRE_ERR(103001, "验证码已过期"),

    UNKNOWN_ERR(-1, "未知错误");

    ExceptionDef(Integer errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private Integer errCode;
    private String errMsg;

    public Integer getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
