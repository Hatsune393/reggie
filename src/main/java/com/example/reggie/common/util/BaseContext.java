package com.example.reggie.common.util;

/**
 * 工具类，使用ThreadLocal存储每一个线程独有的上下文信息
 */
public class BaseContext {
    private static ThreadLocal<Long> sessionUserId = new ThreadLocal<>();

    public static void setSessionUserId(Long id) {
        sessionUserId.set(id);
    }

    public static Long getSessionUserId() {
        return sessionUserId.get();
    }
}
