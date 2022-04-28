package com.example.lretrofit;

import androidx.annotation.Nullable;

import okhttp3.FormBody;

/**
 * author: lanweihua
 * created on: 2022/4/27 10:04 下午
 * description: 构造请求的参数
 */
public class RequestBuilder {

  // 表单
  @Nullable
  private FormBody.Builder mFormBuilder;


  public void addFormField(String fieldName, String value, boolean encoded) {
    if (encoded) {
      mFormBuilder.addEncoded(fieldName, value);
    } else {
      mFormBuilder.add(fieldName, value);
    }
  }
}
