package com.example.lretrofit;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * author: BeggarLan
 * created on: 2022/4/22 22:27
 * description:
 */
public class ServiceMethod {

    static ServiceMethod parseAnnotations(@NonNull LRetrofit retrofit, @NonNull Method metho    d) {

        // 返回类型
        Type returnType = method.getGenericReturnType();
        if(Utils.haUnresolvableType(returnType)) {
            throw new IllegalArgumentException("class:"+method.getDeclaringClass().getName()+", method:" + method+ " ,returnType error: " +returnType);
        }
        if(returnType == Void.TYPE) {
            throw new IllegalArgumentException("class:"+method.getDeclaringClass().getName()+", method:" + method+ " cannot return void");
        }
        return
    }

}
