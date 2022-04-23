package com.example.lretrofit.http;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * author: BeggarLan
 * created on: 2022/4/23 22:21
 * description:
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({HttpMethod.GET, HttpMethod.POST})
public @interface HttpMethod {
    String GET = "get";
    String POST = "post";
}
