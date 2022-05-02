package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lretrofit.call.Call;
import com.example.lretrofit.call.OkHttpCall;

import okhttp3.ResponseBody;

/**
 * author: lanweihua
 * created on: 2022/4/29 12:45 下午
 * description: 接口方法调用发起网络请求
 */
class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {


    @NonNull
    private final RequestFactory mRequestFactory;
    @NonNull
    private final okhttp3.Call.Factory mCallFactory;

    @NonNull
    private final CallAdapter<ResponseT, ReturnT> mCallAdapter;
    @NonNull
    private final Converter<ResponseBody, ResponseT> mResponseConverter;

    public HttpServiceMethod(
            @NonNull RequestFactory mRequestFactory,
            @NonNull okhttp3.Call.Factory mCallFactory,
            @NonNull CallAdapter<ResponseT, ReturnT> mCallAdapter,
            @NonNull Converter<ResponseBody, ResponseT> mResponseConverter) {
        this.mRequestFactory = mRequestFactory;
        this.mCallFactory = mCallFactory;
        this.mCallAdapter = mCallAdapter;
        this.mResponseConverter = mResponseConverter;
    }

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

        // 返回数据转换器
        Converter<ResponseBody, ResponseT> responseConverter = createResponseConverter(retrofit, method, responseType, annotations);

        return new HttpServiceMethod<ResponseT, ReturnT>(requestFactory, retrofit.mCallFactory, callAdapter, responseConverter);
    }


    @Nullable
    @Override
    ReturnT invoke(Object[] args) {
        Call<ResponseT> call = new OkHttpCall<>(mRequestFactory, args, mCallFactory, mResponseConverter);
        return adapt(call);
    }

    /**
     * 适配接口返回类型
     */
    private ReturnT adapt(@NonNull Call<ResponseT> call) {
        return mCallAdapter.adapt(call);
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
     *
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
