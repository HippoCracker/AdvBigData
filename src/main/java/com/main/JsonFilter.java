package com.main;

import com.google.gson.JsonArray;
import com.jayway.jsonpath.JsonPath;
import com.main.context.JsonContext;

import java.util.Collection;

/**
 * Created by zongzesheng on 12/6/16.
 */
public class JsonFilter {

    private String json;
    private String filterString;

    public JsonFilter(Json json, String filterString) {
        this.json = json.restore().jsonString();
        this.filterString = filterString;
    }

    public String getResult() {
        return JsonContext.toJson(JsonPath.read(json, filterString));
    }
}
