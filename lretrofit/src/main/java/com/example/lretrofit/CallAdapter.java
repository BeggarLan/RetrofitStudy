package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lretrofit.call.Call;

/**
 * author: lanweihua
 * created on: 2022/4/29 12:54 下午
 * description: 结果适配器
 */
public interface CallAdapter<T, R> {

  /**
   * 请求返回的类型，
   * e.g. the responseType for Call<Repo> is Repo
   */
  Type responseType();

  /**
   * 请求默认是返回Call。
   * 通过该方法可以适配自己的返回类型
   */
  R adapt(Call<T> call);

  abstract class Factory {

    /**
     * 根据返回类型获得对应的adapter。
     * 找不到能处理的adapter的话，返回null
     */
    @Nullable
    abstract CallAdapter<?, ?> get(
        @NonNull LRetrofit retrofit, @NonNull Type returnType, @NonNull Annotation[] annotations);

  }

}
