package com.example.lretrofit;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * author: BeggarLan
 * created on: 2022/4/22 22:12
 * description:
 */
public class LRetrofit {

    public <T> T create(@NonNull Class<T> service) {
        validService(service);
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        if(method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        return loadServiceMethod(method);
                    }
                });
    }

    private void validService(@NonNull Class<?> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("server must be interface");
        }
    }

    private Object loadServiceMethod(@NonNull Method method) {

        return null;
    }

}
