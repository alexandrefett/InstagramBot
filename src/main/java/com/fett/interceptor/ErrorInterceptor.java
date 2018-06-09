package com.fett.interceptor;

import me.postaddict.instagram.scraper.exception.InstagramAuthException;
import me.postaddict.instagram.scraper.exception.InstagramException;
import me.postaddict.instagram.scraper.exception.InstagramNotFoundException;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class ErrorInterceptor implements Interceptor {
    public ErrorInterceptor() {
    }

    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        int code = response.code();
        if (code == 200) {
            return response;
        } else {
            String body = response.body().string();
            response.body().close();
            switch(code) {
                case 401:
                    throw new InstagramAuthException("Unauthorized");
                case 402:
                default:
                    throw new InstagramException(body);// Return string json (checkpoint_required)
                case 403:
                    throw new InstagramAuthException("Access denied");
                case 404:
                    throw new InstagramNotFoundException("Resource does not exist");
            }
        }
    }
}
