package com.example.lretrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lretrofit.http.HttpMethod;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * author: lanweihua
 * created on: 2022/4/27 10:04 下午
 * description: 构造请求的参数
 */
public class RequestBuilder {

    private final Request.Builder requestBuilder;
    private final Headers.Builder headersBuilder;

    // 表单
    @Nullable
    private FormBody.Builder mFormBuilder;

    private @Nullable HttpUrl.Builder mUrlBuilder;

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
    private boolean mIsFormEncoded;

    public RequestBuilder(
            @Nullable String httpMethod,
            @NonNull HttpUrl baseUrl,
            @NonNull String relativeUrl,
            boolean hasBody,
            boolean isFormEncoded) {
        this.mHttpMethod = mHttpMethod;
        this.mBaseUrl = mBaseUrl;
        this.mRelativeUrl = mRelativeUrl;
        this.mHasBody = mHasBody;
        this.mIsFormEncoded = mIsFormEncoded;

        this.requestBuilder = new Request.Builder();
        if (isFormEncoded) {
            mFormBuilder = new FormBody.Builder();
        }
//        if (headers != null) {
//            headersBuilder = headers.newBuilder();
//        } else {
            headersBuilder = new Headers.Builder();
//        }
    }

    public void addFormField(String fieldName, String value, boolean encoded) {
        if (encoded) {
            mFormBuilder.addEncoded(fieldName, value);
        } else {
            mFormBuilder.add(fieldName, value);
        }
    }

    Request.Builder get() {
        HttpUrl url;
        HttpUrl.Builder urlBuilder = mUrlBuilder;
        if (urlBuilder != null) {
            url = urlBuilder.build();
        } else {
            // No query parameters triggered builder creation, just combine the relative URL and base URL.
            //noinspection ConstantConditions Non-null if urlBuilder is null.
            url = mBaseUrl.resolve(mRelativeUrl);
            if (url == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + mBaseUrl + ", Relative: " + mRelativeUrl);
            }
        }

        RequestBody body = null;
            // Try to pull from one of the builders.
            if (mFormBuilder != null) {
                body = mFormBuilder.build();
            } else if (mHasBody) {
                // Body is absent, make an empty body.
                body = RequestBody.create(null, new byte[0]);
            }

        MediaType contentType = this.contentType;
        if (contentType != null) {
            if (body != null) {
                body = new ContentTypeOverridingRequestBody(body, contentType);
            } else {
                headersBuilder.add("Content-Type", contentType.toString());
            }
        }

        return requestBuilder.url(url).headers(headersBuilder.build()).method(method, body);
    }

}
