package com.example.reggie.common;

import com.example.reggie.common.R;
import com.example.reggie.exception.CategoryBizException;
import com.example.reggie.exception.ExceptionDef;
import com.example.reggie.exception.SetmealException;
import com.example.reggie.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.info(e.getMessage());
        if (e.getMessage().contains("Duplicate entry")) {
            return R.error(ExceptionDef.EMPLOYEE_CREATE_DUPLICATE_ERR.getErrMsg());
        }

        return R.error(ExceptionDef.UNKNOWN_ERR.getErrMsg());
    }

    @ExceptionHandler(CategoryBizException.class)
    public R<String> handleCategoryBizException(CategoryBizException e) {
        return R.error(e.getMessage());
    }

    @ExceptionHandler(SetmealException.class)
    public R<String> handleSetmealException(SetmealException e) { return R.error(e.getMessage()); }

    @ExceptionHandler(UserException.class)
    public R<String> handleUserException(UserException e) { return R.error(e.getMessage()); }

    @ExceptionHandler(Exception.class)
    public R<String> defaultHandleException(Exception e) {
        log.error(e.getMessage());
        return R.error("未知错误");
    }
}
