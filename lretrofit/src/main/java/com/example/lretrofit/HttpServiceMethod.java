package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: lanweihua
 * created on: 2022/4/29 12:45 下午
 * description: 接口方法调用发起网络请求
 */
abstract class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

  public static <ResponseT, ReturnT> HttpServiceMethod<ResponseT, ReturnT> parseAnnotations(
      @NonNull LRetrofit retrofit, @NonNull Method method, @NonNull RequestFactory requestFactory) {
    // 返回类型
    Type returnType = method.getGenericReturnType();
    // 函数注解
    Annotation[] annotations = method.getAnnotations();
    CallAdapter<ResponseT, ReturnT> callAdapter =
        createCallAdapter(retrofit, method, returnType, annotations);


  }

  static <ResponseT, ReturnT> CallAdapter<ResponseT, ReturnT> createCallAdapter(
      @NonNull LRetrofit retrofit,
      @NonNull Method method,
      @NonNull Type returnType,
      @Nullable Annotation[] annotations) {
    return (CallAdapter<ResponseT, ReturnT>) retrofit.callAdapter(returnType, annotations);
  }


}
