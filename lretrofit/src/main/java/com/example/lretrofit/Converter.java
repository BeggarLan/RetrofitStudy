package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.ResponseBody;

/**
 * author: lanweihua
 * created on: 2022/4/27 12:48 下午
 * description: 转换器
 * 1. 将请求的参数处理
 * 2. 将接口的回执处理
 */
public interface Converter<T, R> {

    @Nullable
    R convert(T value);

    abstract class Factory {

        /**
         * 获得stringConverter，如果无法处理type那么返回null
         * 用在@Field注解上
         *
         * @param type        对象的类型
         * @param annotations 对象的所有注解
         */
        @Nullable
        public Converter<?, String> stringConverter(
                @NonNull Type type, @NonNull Annotation[] annotations, @NonNull LRetrofit retrofit) {
            return null;
        }

        /**
         * 请求回执数据转换
         *
         * @param responseType 请求的回执类型
         */
        @Nullable
        public Converter<ResponseBody, ?> responseBodyConverter(Type responseType, Annotation[] annotations, LRetrofit retrofit) {
            return null;
        }

    }

}
