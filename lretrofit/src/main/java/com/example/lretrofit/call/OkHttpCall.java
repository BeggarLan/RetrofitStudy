package com.example.lretrofit.call;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lretrofit.Converter;
import com.example.lretrofit.RequestFactory;
import com.example.lretrofit.Response;
import com.example.lretrofit.Utils;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import kotlin.internal.ProgressionUtilKt;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

/**
 * author: BeggarLan
 * created on: 2022/5/2 18:26
 * description: http请求实现类
 */
public class OkHttpCall<T> implements Call<T> {

    @NonNull
    private final RequestFactory mRequestFactory;
    @NonNull
    private final Object[] mArgs;
    @NonNull
    private final okhttp3.Call.Factory mCallFactory;
    @NonNull
    private final Converter<ResponseBody, T> mResponseConverter;

    // 请求调用实体
    @NonNull
    private okhttp3.Call mRawCall;

    // 注意多线程
    private volatile boolean mIsCanceled;
    private boolean mIsExecuted;


    public OkHttpCall(
            @NonNull RequestFactory mRequestFactory,
            @NonNull Object[] mArgs,
            @NonNull okhttp3.Call.Factory mCallFactory,
            @NonNull Converter<ResponseBody, T> mResponseConverter) {
        this.mRequestFactory = mRequestFactory;
        this.mArgs = mArgs;
        this.mCallFactory = mCallFactory;
        this.mResponseConverter = mResponseConverter;
        mRawCall = mCallFactory.newCall(mRequestFactory.create(mArgs));
    }

    @Override
    public Response<T> execute() throws IOException {
        synchronized (this) {
            if (mIsExecuted) {
                throw new IllegalStateException("call already executed.");
            }
            mIsExecuted = true;
        }
        return parseResponse(mRawCall.execute());
    }

    @Override
    public void enqueue(@NonNull CallBack<T> callBack) {
        synchronized (this) {
            if (mIsExecuted) throw new IllegalStateException("Already executed.");
            mIsExecuted = true;
        }
        if (mIsCanceled) {
            mRawCall.cancel();
        }
        mRawCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                callFailure(e);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response rawResponse) throws IOException {
                try {
                    Response<T> response = parseResponse(rawResponse);
                    callBack.onResponse(OkHttpCall.this, response);
                } catch (Throwable throwable) {
                    callFailure(throwable);
                }
            }

            private void callFailure(@NonNull Throwable throwable) {
                callBack.onFailure(OkHttpCall.this, throwable);
            }
        });

    }

    /**
     * 解析请求原生回执信息
     */
    private Response<T> parseResponse(@NonNull okhttp3.Response rawResponse) throws IOException {
        ResponseBody responseBody = rawResponse.body();
        // responseBody是有状态的(closeable)，后面我们单独clone一份
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(responseBody.contentType(), responseBody.contentLength()))
                .build();
        int code = rawResponse.code();
        // error
        if (code < 200 || code >= 300) {
            try {
                ResponseBody body = Utils.buffer(responseBody);
                return Response.error(responseBody, rawResponse);
            } catch (IOException e) {
                throw e;
            } finally {
                rawResponse.close();
            }
        }

        // TODO: 2022/5/2 这块没深入了解
        if (code == 204 || code == 205) {
            rawResponse.close();
            return Response.success(null, rawResponse);
        }

        ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(responseBody);
        try {
            T body = mResponseConverter.convert(catchingBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }

    }

    @Override
    public boolean isExecuted() {
        return mIsExecuted;
    }

    @Override
    public void cancel() {
        synchronized (this) {
            mIsCanceled = true;
            mRawCall.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        return mIsCanceled;
    }

    @Override
    public Request getRequest() {
        return mRawCall.request();
    }

    /**
     * 空content的ResponseBody
     */
    static final class NoContentResponseBody extends ResponseBody {
        private final MediaType mContentType;
        private final long mContentLength;

        public NoContentResponseBody(MediaType mContentType, long mContentLength) {
            this.mContentType = mContentType;
            this.mContentLength = mContentLength;
        }

        @Override
        public long contentLength() {
            return mContentLength;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return mContentType;
        }

        @NonNull
        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    // TODO: 2022/5/2 没深入
    static final class ExceptionCatchingResponseBody extends ResponseBody {
        private final ResponseBody delegate;
        private final BufferedSource delegateSource;
        @Nullable
        IOException thrownException;

        ExceptionCatchingResponseBody(ResponseBody delegate) {
            this.delegate = delegate;
            this.delegateSource =
                    Okio.buffer(
                            new ForwardingSource(delegate.source()) {
                                @Override
                                public long read(Buffer sink, long byteCount) throws IOException {
                                    try {
                                        return super.read(sink, byteCount);
                                    } catch (IOException e) {
                                        thrownException = e;
                                        throw e;
                                    }
                                }
                            });
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public BufferedSource source() {
            return delegateSource;
        }

        @Override
        public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }

}
