package com.example.lretrofit.call;

import java.io.IOException;

import androidx.annotation.NonNull;

import com.example.lretrofit.Response;

/**
 * author: lanweihua
 * created on: 2022/4/30 2:49 下午
 * description: 对请求的封装
 */
public interface Call<T> {

  /**
   * 同步的请求
   */
  Response<T> execute() throws IOException;

  /**
   * 异步请求
   *
   * @param callBack 请求的回调
   */
  void enqueue(@NonNull CallBack<T> callBack);

  /**
   * 请求是否已经执行过。
   * execute和enqueue只能执行一次
   */
  boolean isExecuted();

  /**
   * 取消请求
   */
  void cancel();

  /**
   * @return {@code }
   */
  boolean isCanceled();

}
