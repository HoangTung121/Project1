package com.example.myreadbookapplication.network;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myreadbookapplication.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static ApiService getApiService() {
        if (retrofit == null) {
            // Tạo OkHttpClient với logging
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Thêm logging interceptor cho debug
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(logging);
            }

            // Interceptor thêm Authorization nếu có token
            httpClient.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                // Không có context toàn cục, nên giữ nguyên - caller có thể thêm Header thủ công khi gọi
                return chain.proceed(original);
            });

            // Tạo Gson với cấu hình
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
