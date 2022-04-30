package com.example.lretrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * author: lanweihua
 * created on: 2022/4/30 2:22 下午
 * description: 请求回执的封装
 */
public class Response<T> {

  // http请求的response
  @NonNull
  private final okhttp3.Response mRawResponse;

  // success时的数据
  @Nullable
  private final T mBody;

  // error
  @Nullable
  private final ResponseBody mErrorBody;

  public Response(
      @NonNull okhttp3.Response rawResponse, @Nullable T body, @Nullable ResponseBody errorBody) {
    mRawResponse = rawResponse;
    this.mBody = body;
    mErrorBody = errorBody;
  }

  /**
   * @param body
   * @param rawResponse
   */
  public static <T> Response<T> success(@Nullable T body, @NonNull okhttp3.Response rawResponse) {
    // 这里就是判断了code在[200,299]
    if (!rawResponse.isSuccessful()) {
      throw new IllegalArgumentException("rawResponse must be successful response");
    }
    return new Response<>(rawResponse, body, null);
  }

  public static <T> Response<T> error(
      @NonNull ResponseBody errorBody, @NonNull okhttp3.Response rawResponse) {
    if (rawResponse.isSuccessful()) {
      throw new IllegalArgumentException("rawResponse should not be successful response");
    }
    return new Response<>(rawResponse, null, errorBody);
  }

  public okhttp3.Response rawResponse() {
    return mRawResponse;
  }

  public boolean isSuccessful() {
    return mRawResponse.isSuccessful();
  }

  public int code() {
    return mRawResponse.code();
  }

  public String message() {
    return mRawResponse.message();
  }

  public Headers headers() {
    return mRawResponse.headers();
  }

  @Nullable
  public T body() {
    return mBody;
  }

  public ResponseBody errorBody() {
    return mErrorBody;
  }

  @Override
  public String toString() {
    return "Response{" +
        "mRawResponse=" + mRawResponse +
        ", mBody=" + mBody +
        ", mErrorBody=" + mErrorBody +
        '}';
  }

}
