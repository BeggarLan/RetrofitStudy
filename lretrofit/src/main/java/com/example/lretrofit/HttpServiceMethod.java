package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;

/**
 * author: lanweihua
 * created on: 2022/4/29 12:45 下午
 * description: 接口方法调用发起网络请求
 */
abstract class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

  public static <ResponseT, ReturnT> HttpServiceMethod<ResponseT, ReturnT> parseAnnotations(
      @NonNull LRetrofit retrofit, @NonNull Method method, @NonNull RequestFactory requestFactory) {
    Annotation[] annotations = method.getAnnotations();
    // 返回类型
    method.getGenericReturnType();

  }

}
