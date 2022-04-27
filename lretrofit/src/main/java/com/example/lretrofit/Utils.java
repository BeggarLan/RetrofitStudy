package com.example.lretrofit;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

  /**
   * 获取最外层类型
   *
   * @param type 如List<XXX<xx>>，那么返回List
   */
  static Class<?> getRawType(@NonNull Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      // 这里没懂为啥还能拿到不是Class<?>的。。。。。
      if (!(rawType instanceof Class<?>)) {
        throw new IllegalArgumentException();
      }
      return (Class<?>) rawType;
    }

    // List<String>[]-->List[]
    if (type instanceof GenericArrayType) {
      // 获取element类型
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return Array.newInstance(getRawType(componentType), 0).getClass();
    }

    if (type instanceof TypeVariable) {
      return Object.class;
    }

    // 通配符
    if (type instanceof WildcardType) {
      return getRawType(((WildcardType) type).getUpperBounds()[0]);
    }

    throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
        + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
  }

  /**
   * 获取ParameterizedType的第index个类型的上限
   * 例如:List<Set<Queue<String>>> ---> Set<Queue<String>>
   * 例如:List<? extends String> ---> String
   */
  static Type getParameterUpperBound(int index, ParameterizedType type) {
    Type[] types = type.getActualTypeArguments();
    if (index < 0 || index >= types.length) {
      throw new IllegalArgumentException(
          "Index " + index + " not in range [0," + types.length + ") for " + type);
    }
    Type resultType = types[index];
    if (resultType instanceof WildcardType) {
      return ((WildcardType) resultType).getUpperBounds()[0];
    }
    return resultType;
  }

  static void checkNotNull(@Nullable Object object, String msg) {
    if (object == null) {
      throw new NullPointerException(msg);
    }
  }

}
