package com.TJS.reggie.common;

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void set(Long id){
        threadLocal.set(id);
    }
    public static long get(){
        return threadLocal.get();
    }
}
