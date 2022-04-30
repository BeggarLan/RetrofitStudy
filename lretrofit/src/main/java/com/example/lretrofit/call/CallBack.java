package com.example.lretrofit.call;

import androidx.annotation.NonNull;

import com.example.lretrofit.Response;

/**
 * author: lanweihua
 * created on: 2022/4/30 2:51 下午
 * description: 请求的回调
 */
public interface CallBack<T> {

  /**
   * 成功
   */
  void onResponse(@NonNull Call<T> call, @NonNull Response<T> response);

  /**
   * 失败
   */
  void onFailure(@NonNull Call<T> call, @NonNull Throwable throwable);

}
