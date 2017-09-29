package com.github.ittalks.invoice.common.result;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by 刘春龙 on 2017/3/9.
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class APIResult {

    private static final String defaultResult = "ok";

    private String result = defaultResult;
    private Object body;

    private APIResult(Object body) {
        this.body = body;
    }

    public static APIResult Y() {
        return new APIResult(new Object());
    }

    public static APIResult Y(Object body) {
        return new APIResult(body);
    }

    public String getResult() {
        return result;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String toString() {
        return JSON.toJSONString(this);
    }
}
