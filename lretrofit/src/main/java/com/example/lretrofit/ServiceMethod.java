package com.example.lretrofit;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: BeggarLan
 * created on: 2022/4/22 22:27
 * description: 将接口的method转化为实际请求的method
 */
abstract class ServiceMethod<ReturnT> {

  static <T> ServiceMethod<T> parseAnnotations(@NonNull LRetrofit retrofit, @NonNull Method method) {
    RequestFactory requestFactory = RequestFactory.parseAnnotations(retrofit, method);
    // 返回类型
    Type returnType = method.getGenericReturnType();
    if (Utils.hasUnresolvableType(returnType)) {
      throw new IllegalArgumentException(
          "class:" + method.getDeclaringClass().getName() + ", method:" + method +
              " ,returnType error: " + returnType);
    }
    if (returnType == Void.TYPE) {
      throw new IllegalArgumentException(
          "class:" + method.getDeclaringClass().getName() + ", method:" + method +
              " cannot return void");
    }
    return HttpServiceMethod.parseAnnotations(retrofit, method, requestFactory);
  }

  @Nullable
  abstract ReturnT invoke(@Nullable Object[] args);

}
