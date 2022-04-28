package com.example.lretrofit.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author: lanweihua
 * created on: 2022/4/24 1:07 下午
 * description: 参数注解
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

  String value();

  // 是否已经编码过
  boolean encoded() default false;

}
