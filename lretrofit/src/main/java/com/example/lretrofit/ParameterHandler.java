package com.example.lretrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: lanweihua
 * created on: 2022/4/24 12:47 下午
 * description: 参数处理
 */
public abstract class ParameterHandler<T> {

  abstract void apply(RequestBuilder requestBuilder, @Nullable T value);

  ParameterHandler<Iterable<T>> iterable() {
    return new ParameterHandler<Iterable<T>>() {
      @Override
      void apply(RequestBuilder requestBuilder, @Nullable Iterable<T> values) {
        if (values == null) {
          return;
        }
        for (T value : values) {
          ParameterHandler.this.apply(requestBuilder, value);
        }
      }
    };
  }


  static final class Field<T> extends ParameterHandler<T> {

    // 字段名
    private final String mFieldName;
    // 转换器
    @NonNull
    private final Converter<T, String> mConverter;
    // 是否已经编码了
    private final boolean mEncoded;

    public Field(String fieldName, @NonNull Converter<T, String> converter, boolean encoded) {
      mFieldName = fieldName;
      mConverter = converter;
      mEncoded = encoded;
    }

    @Override
    void apply(RequestBuilder requestBuilder, @Nullable T value) {
      if (value == null) {
        return;
      }
      String resultValue = mConverter.convert(value);
      if (resultValue == null) {
        return;
      }
      requestBuilder.addFormField(mFieldName, resultValue, mEncoded);
    }
  }

}
