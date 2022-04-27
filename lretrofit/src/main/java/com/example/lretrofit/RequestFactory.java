package com.example.lretrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lretrofit.http.Field;
import com.example.lretrofit.http.FormUrlEncoded;
import com.example.lretrofit.http.GET;
import com.example.lretrofit.http.HttpMethod;
import com.example.lretrofit.http.POST;

import okhttp3.HttpUrl;

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
  @NonNull
  private HttpUrl mBaseUrl;
  // 相对地址(不包括baseUrl的部分)
  @NonNull
  private String mRelativeUrl;

  // 是否有请求体
  private boolean mHasBody;
  private boolean isFormEncode;

  // 参数处理器
  private ParameterHandler<?>[] mParameterHandlers;

  static RequestFactory parseAnnotations(@NonNull LRetrofit retrofit, @NonNull Method method) {
    return new Builder(retrofit, method).build();
  }

  RequestFactory(@NonNull Builder builder) {
    this.mMethod = builder.mMethod;

    mBaseUrl = builder.mRetrofit.mBaseUrl;
    mHttpMethod = builder.mHttpMethod;
    mRelativeUrl = builder.mRelativeUrl;
    mHasBody = builder.mHasBody;
    isFormEncode = builder.mIsFormEncoded;
    mParameterHandlers = builder.mParameterHandlers;
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
    @HttpMethod
    private String mHttpMethod;
    // 相对地址(不包括baseUrl的部分)
    private String mRelativeUrl;

    // 是否有请求体
    private boolean mHasBody;
    private boolean mIsFormEncoded;

    // 是否有field参数(@Field)
    private boolean mHasField;

    // 参数处理器
    private ParameterHandler<?>[] mParameterHandlers;

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
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                " ,Http method annotation is required (@GET, @POST ...)");
      }
      // 检查表单encode
      if (!mHasBody && mIsFormEncoded) {
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod + " ");
      }

      // 参数的数量
      int parameterCount = mParameterAnnotations.length;
      mParameterHandlers = new ParameterHandler<?>[parameterCount];
      for (int p = 0; p < parameterCount; ++p) {
        mParameterHandlers[p] =
            parseParameter(p, mParameterTypes[p], mParameterAnnotations[p]);
      }

      if (mRelativeUrl == null) {
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                " ,url is required");
      }

      if (mIsFormEncoded && !mHasField) {
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                " ,Form-encoded method must contain at least one @Field.");
      }

      return new RequestFactory(this);
    }

    /**
     * 获取解析参数注解的处理器
     *
     * @param p             参数位置
     * @param parameterType 类型
     * @param annotations   参数的注解
     */
    @NonNull
    private ParameterHandler<?> parseParameter(
        int p, @NonNull Type parameterType, @Nullable Annotation[] annotations) {
      ParameterHandler<?> parameterHandler = null;
      if (annotations != null) {
        for (Annotation annotation : annotations) {
          ParameterHandler<?> handler =
              parseParameterAnnotation(p, parameterType, annotations, annotation);
          if (handler == null) {
            continue;
          }
          // 参数被对个retrofit-annotation注解时，error
          if (parameterHandler != null) {
            throw new IllegalArgumentException(
                "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                    ", only one retrofit annotation is allowed");
          }
          parameterHandler = handler;
        }
      }
      if (parameterHandler == null) {
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                ", only one retrofit annotation is allowed");
      }
      return parameterHandler;
    }

    /**
     * 获取解析参数注解的处理器
     *
     * @param p             参数位置
     * @param parameterType 类型
     * @param annotations   参数的所有注解
     * @param annotation    当前遍历到的该参数的注解
     */
    @Nullable
    private ParameterHandler<?> parseParameterAnnotation(
        int p, Type parameterType, Annotation[] annotations, Annotation annotation) {
      if (annotation instanceof Field) {
        // 检查参数的类型
        validateParameterResolvableType(p, parameterType);
        // 表单参数必须要encode
        if (!mIsFormEncoded) {
          throw new IllegalArgumentException(
              "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                  " ,@Field parameter must be used with form encoding");
        }

        Field field = (Field) annotation;
        String name = field.value();
        boolean encoded = field.encoded();

        mHasField = true;
        Class<?> rawParameterType = Utils.getRawType(parameterType);
        if (Iterable.class.isAssignableFrom(rawParameterType)) {
          // 必须有具体的类型
          if (!(parameterType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                    parameterType + " must has generic type (e.g.," +
                    rawParameterType.getSimpleName() + "<String>)");
          }
          ParameterizedType parameterizedType = (ParameterizedType) parameterType;
          // 拿到element的类型
          Type elementType = Utils.getParameterUpperBound(0, parameterizedType);
          Converter<?, String> converter = mRetrofit.stringConverter(elementType, annotations);
          return new ParameterHandler<Object>() {}
        } else if (rawParameterType.isArray()) {

        } else {

        }

        // TODO: 2022/4/24 继续
        // 该参数没有被retrofit的annotation注解时
        return null;
      }

      return null;
    }

    /**
     * 解析方法注解
     */
    private void parseMethodAnnotation(@NonNull Annotation annotation) {
      if (annotation instanceof GET) {
        parseHttpMethodAndPath(HttpMethod.GET, ((GET) annotation).value(), false);
      } else if (annotation instanceof POST) {
        parseHttpMethodAndPath(HttpMethod.POST, ((POST) annotation).value(), true);
      } else if (annotation instanceof FormUrlEncoded) {
        mIsFormEncoded = true;
      }
      // TODO: 2022/4/23 header
    }

    /**
     * 解析请求类型和地址
     *
     * @param httpMethod 请求类型
     * @param path       地址
     */
    private void parseHttpMethodAndPath(@HttpMethod String httpMethod, String path,
        boolean hasForm) {
      if (mHttpMethod != null) {
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                " ,only one http method is allowed");
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

    /**
     * 检查参数的类型
     *
     * @param p    参数的位置
     * @param type 参数的类型
     */
    private void validateParameterResolvableType(int p, Type type) {
      if (Utils.hasUnresolvableType(type)) {
        throw new IllegalArgumentException(
            "class:" + mMethod.getDeclaringClass().getName() + ", method:" + mMethod +
                " ,Parameter type must not include a type variable or wildcard:" + type);
      }
    }

  }

}
