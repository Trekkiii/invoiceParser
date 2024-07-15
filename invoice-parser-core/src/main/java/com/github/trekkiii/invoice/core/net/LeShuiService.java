package com.github.trekkiii.invoice.core.net;

import com.alibaba.fastjson.JSONObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public interface LeShuiService {

    @POST("/fapiao/ajax")
    Call<JSONObject> getInfo(@Body Object info);
}
