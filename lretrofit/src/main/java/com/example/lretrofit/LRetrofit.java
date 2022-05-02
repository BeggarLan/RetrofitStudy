package com.example.lretrofit;

import static com.example.lretrofit.Utils.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;

/**
 * author: BeggarLan
 * created on: 2022/4/22 22:12
 * description:
 */
public class LRetrofit {

    @NonNull
    private final Map<Method, ServiceMethod<?>> mServiceMethodMap = new HashMap<>();

    @NonNull
    final HttpUrl mBaseUrl;

    @NonNull
    final List<Converter.Factory> mConverterFactories;
    @NonNull
    final List<CallAdapter.Factory> mCallAdapterFactories;

    public LRetrofit(@NonNull HttpUrl mBaseUrl) {
        this.mBaseUrl = mBaseUrl;
    }

    /**
     * 创建service的代理对象
     */
    public <T> T create(@NonNull Class<T> service) {
        validService(service);
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        return loadServiceMethod(method).invoke(args);
                    }
                });
    }

    /**
     * service合法性检查
     */
    private void validService(@NonNull Class<?> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("server must be interface");
        }
    }

    private ServiceMethod<?> loadServiceMethod(@NonNull Method method) {
        ServiceMethod<?> result = mServiceMethodMap.get(method);
        if (result != null) {
            return result;
        }
        synchronized (mServiceMethodMap) {
            // 在检查一遍
            result = mServiceMethodMap.get(method);
            if (result == null) {
                result = ServiceMethod.parseAnnotations(this, method);
                mServiceMethodMap.put(method, result);
            }
        }
        return result;
    }

    public Converter<?, String> stringConverter(
            @Nullable Type type, @Nullable Annotation[] annotations) {
        Objects.requireNonNull(type, "type == null");
        Objects.requireNonNull(annotations, "annotations == null");

        for (Converter.Factory factory : mConverterFactories) {
            Converter<?, String> stringConverter = factory.stringConverter(type, annotations, this);
            if (stringConverter != null) {
                return stringConverter;
            }
        }
        // TODO: 2022/4/27 default
        return null;
    }

    /**
     * 根据返回类型找到合适的adapter
     */
    public CallAdapter<?, ?> callAdapter(
            @NonNull Type returnType, @Nullable Annotation[] annotations) {
        Objects.requireNonNull(returnType, "returnType == null");
        Objects.requireNonNull(annotations, "annotations == null");
        for (int i = 0, size = mCallAdapterFactories.size(); i < size; ++i) {
            CallAdapter<?, ?> callAdapter =
                    mCallAdapterFactories.get(i).get(this, returnType, annotations);
            if (callAdapter != null) {
                return callAdapter;
            }
        }

        StringBuilder stringBuilder = new StringBuilder("could not find callAdapter for ")
                .append(returnType)
                .append(".\n");
        stringBuilder.append("  Tried:");
        for (int i = 0, size = mCallAdapterFactories.size(); i < size; ++i) {
            stringBuilder.append("\n   * ").append(mCallAdapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    /**
     * 请求回执的结果转换器
     *
     * @param responseType 如Resp
     * @param <ResponseT>  如Call<Resp>
     * @return
     */
    public <ResponseT> Converter<ResponseBody, ResponseT> responseBodyConverter(Type responseType, Annotation[] annotations) {
        Objects.requireNonNull(responseType, "responseType == null");
        Objects.requireNonNull(annotations, "annotations == null");
        for (Converter.Factory factory : mConverterFactories) {
            Converter<ResponseBody, ?> converter = factory.responseBodyConverter(responseType, annotations, this);
            if (converter != null) {
                return (Converter<ResponseBody, ResponseT>) converter;
            }
        }
        StringBuilder stringBuilder = new StringBuilder("could not find ResponseBody converter for ")
                .append(responseType)
                .append(".\n");
        stringBuilder.append("  Tried:");
        for (int i = 0, size = mConverterFactories.size(); i < size; ++i) {
            stringBuilder.append("\n   * ").append(mConverterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
