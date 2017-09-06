package com.github.ittalks.invoice.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class LeShuiServiceProxy {

    private LeShuiServiceProxy() {
    }

    private static LeShuiService mInstance;

    private static final OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(60, TimeUnit.SECONDS).
            readTimeout(60, TimeUnit.SECONDS).
            writeTimeout(60, TimeUnit.SECONDS).build();

    public static synchronized LeShuiService getInstance() {
        if (mInstance == null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();//使用 gson coverter，统一日期请求格式
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.leshui365.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
            mInstance = retrofit.create(LeShuiService.class);
        }
        return mInstance;
    }
}
