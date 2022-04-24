package com.example.lretrofit;

import androidx.annotation.Nullable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * author: BeggarLan
 * created on: 2022/4/22 22:39
 * description:
 */
public class Utils {

    /**
     * 是否有无法解析的类型
     * 无法解析如下
     * 1. T
     * 2. <?> <? extends XX> <? super XX>
     */
    static boolean hasUnresolvableType(@Nullable Type type) {
        if (type instanceof Class) {
            return false;
        }
        // XX<XX>
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type arguType : parameterizedType.getActualTypeArguments()) {
                if (hasUnresolvableType(arguType)) {
                    return true;
                }
            }
            return false;
        }
        // T[]
        if (type instanceof GenericArrayType) {
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        }
        // T
        if (type instanceof TypeVariable) {
            return true;
        }
        // 通配符 <?> <? extends XX> <? super XX>
        if (type instanceof WildcardType) {
            return true;
        }
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("expect a Class, ParameterizedType, or GenericArrayType," +
                " but type is  " + type + " , class is " + className);
    }

}
