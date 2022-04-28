package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: lanweihua
 * created on: 2022/4/27 12:48 下午
 * description: 转换器
 * 1. 将请求的参数处理
 * 2. 将接口的回执处理
 */
public interface Converter<T, R> {

  @Nullable
  R convert(R value);

  abstract class Factory {

    /**
     * 获得stringConverter，如果无法处理type那么返回null
     * 用在@Field注解上
     * @param type        对象的类型
     * @param annotations 对象的所有注解
     */
    @Nullable
    public Converter<?, String> stringConverter(
        @NonNull Type type, @NonNull Annotation[] annotations, @NonNull LRetrofit retrofit) {
      return null;
    }

  }

}