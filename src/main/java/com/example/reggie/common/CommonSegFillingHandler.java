package com.example.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.reggie.common.util.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommonSegFillingHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 怎么为不同的实体自动填充不同的字段？
        // 自动填充时间
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        // 自动填充操作用户
        Long operateUserId = BaseContext.getSessionUserId();
        metaObject.setValue("createUser", operateUserId);
        metaObject.setValue("updateUser", operateUserId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        Long operateUserId = BaseContext.getSessionUserId();
        metaObject.setValue("updateUser", operateUserId);
    }
}
