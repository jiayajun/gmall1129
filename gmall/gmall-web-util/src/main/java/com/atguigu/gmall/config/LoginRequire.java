package com.atguigu.gmall.config;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {

    boolean autoRedirect() default true;

    //设定默认登录用户id为1号
    String debugUser() default "0";

}

