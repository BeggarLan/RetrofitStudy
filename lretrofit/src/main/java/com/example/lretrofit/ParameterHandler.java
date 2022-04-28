package com.example.lretrofit;

import androidx.annotation.Nullable;

/**
 * author: lanweihua
 * created on: 2022/4/24 12:47 下午
 * description: 参数处理
 */
public abstract class ParameterHandler<T> {

  abstract void apply(RequestBuilder requestBuilder, @Nullable T value);

  static final class Field<T> extends ParameterHandler<T> {

    // 字段名
    private final String mName;
    // 转换器
    private final Converter<T, String> mConverter;
    // 是否编码
    private final boolean mEncoded;

    public Field(String name, Converter<T, String> converter, boolean encoded) {
      mName = name;
      mConverter = converter;
      mEncoded = encoded;
    }

    @Override
    void apply(RequestBuilder requestBuilder, @Nullable T value) {

    }
  }

}
