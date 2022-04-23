package com.example.lretrofit;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lretrofit.http.FormUrlEncoded;
import com.example.lretrofit.http.Get;
import com.example.lretrofit.http.HttpMethod;
import com.example.lretrofit.http.POST;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * author: BeggarLan
 * created on: 2022/4/23 21:53
 * description: 解析出请求需要的参数(请求类型、url、表单等)
 */
// TODO: 2022/4/23 文件上传
public class RequestFactory {

    @NonNull
    private Method mMethod;

    // 区分请求类型的(get、post等)
    @Nullable
    @HttpMethod
    private String mHttpMethod;
    // 相对地址(不包括baseUrl的部分)
    @Nullable
    private String mRelativeUrl;

    // 是否有请求体
    private boolean mHasBody;
    private boolean isFormEncode;

    static RequestFactory parseAnnotations(@NonNull LRetrofit retrofit, @NonNull Method method) {
        return new Builder(retrofit, method).build();
    }

    RequestFactory(@NonNull Builder builder) {
        this.mMethod = builder.mMethod;
        mHttpMethod = builder.mHttpMethod;
        // TODO: 2022/4/23 baseUrl
        mRelativeUrl = builder.mRelativeUrl;
        mHasBody = builder.mHasBody;
        isFormEncode = builder.isFormEncode;
    }


    static final class Builder {
        @NonNull
        private LRetrofit mRetrofit;
        @NonNull
        private Method mMethod;
        // 方法的注解
        @NonNull
        private Annotation[] mMethodAnnotations;
        // 各个参数的类型
        @NonNull
        private Type[] mParameterTypes;
        // 各个参数的注解
        @NonNull
        private Annotation[][] mParameterAnnotations;

        // 区分请求类型的(get、post等)
        @Nullable
        @HttpMethod
        private String mHttpMethod;
        // 相对地址(不包括baseUrl的部分)
        @Nullable
        private String mRelativeUrl;

        // 是否有请求体
        private boolean mHasBody;
        private boolean isFormEncode;

        public Builder(@NonNull LRetrofit retrofit, @NonNull Method method) {
            this.mRetrofit = retrofit;
            this.mMethod = method;
            mMethodAnnotations = method.getAnnotations();
            mParameterTypes = method.getGenericParameterTypes();
            mParameterAnnotations = method.getParameterAnnotations();
        }

        public RequestFactory build() {
            // 解析方法的注解
            for (Annotation annotation : mMethodAnnotations) {
                parseMethodAnnotation(annotation);
            }
            if (mHttpMethod == null) {
                throw new IllegalArgumentException("class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod + " ,Http method annotation is required (@GET, @POST ...)");
            }
            // 检查表单encode
            if (!mHasBody && isFormEncode) {
                throw new IllegalArgumentException("class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod + " ");
            }

            // 参数的数量(只统计带注解的)
            int parameterCount = mParameterAnnotations.length;
            // TODO: 2022/4/23 参数处理

            if (TextUtils.isEmpty(mRelativeUrl)) {
                throw new IllegalArgumentException("class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod + " ,url is required");
            }

            return new RequestFactory(this);
        }

        /**
         * 解析方法注解
         */
        private void parseMethodAnnotation(@NonNull Annotation annotation) {
            if (annotation instanceof Get) {
                parseHttpMethodAndPath(HttpMethod.GET, ((Get) annotation).value(), false);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath(HttpMethod.POST, ((POST) annotation).value(), true);
            } else if (annotation instanceof FormUrlEncoded) {
                isFormEncode = true;
            }
            // TODO: 2022/4/23 header
        }

        /**
         * 解析请求类型和地址
         *
         * @param httpMethod 请求类型
         * @param path       地址
         */
        private void parseHttpMethodAndPath(@HttpMethod String httpMethod, String path, boolean hasForm) {
            if (mHttpMethod != null) {
                throw new IllegalArgumentException("class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod + " ,only one http method is allowed");
            }
            mHttpMethod = httpMethod;
            mHasBody = hasForm;
            if (TextUtils.isEmpty(path)) {
                return;
            }

            // 将参数取出来
            int question = path.indexOf("?");
            if (question != -1 && question < path.length() - 1) {
                String queryParams = path.substring(question + 1);
                // TODO: 2022/4/23 url中的参数是否做处理
            }
            mRelativeUrl = path;
        }

    }

}
