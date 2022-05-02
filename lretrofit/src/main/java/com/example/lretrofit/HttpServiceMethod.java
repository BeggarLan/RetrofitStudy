package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.ResponseBody;

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

        // 适配器
        CallAdapter<ResponseT, ReturnT> callAdapter =
                createCallAdapter(retrofit, method, returnType, annotations);

        Type responseType = callAdapter.responseType();
        if (responseType == Response.class) {
            throw new IllegalArgumentException("class:" + method.getDeclaringClass().getName() + ", method:" + method + " ,Response must include generic type (e.g., Response<String>)");
        }

        //
        Converter<ResponseBody, ResponseT> responseConverter = createResponseConverter(retrofit, method, responseType, annotations);


    }

    static <ResponseT, ReturnT> CallAdapter<ResponseT, ReturnT> createCallAdapter(
            @NonNull LRetrofit retrofit,
            @NonNull Method method,
            @NonNull Type returnType,
            @Nullable Annotation[] annotations) {
        try {
            return (CallAdapter<ResponseT, ReturnT>) retrofit.callAdapter(returnType, annotations);
        } catch (Throwable e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create call adapter for %s", returnType));
        }
    }

    /**
     * 请求回执转换器
     * @param retrofit
     * @param method
     * @param responseType 返回的类型
     * @return
     */
    private static <ResponseT> Converter<ResponseBody, ResponseT> createResponseConverter(
            @NonNull LRetrofit retrofit, @NonNull Method method, @NonNull Type responseType, @NonNull Annotation[] annotations) {
        try {
            return retrofit.responseBodyConverter(responseType, annotations);
        } catch (Throwable e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create response converter for %s", responseType));
        }
    }
}
