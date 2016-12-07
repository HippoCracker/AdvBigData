package com.main;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.main.context.JsonContext;

import java.util.Collection;
import java.util.Map;

/**
 * Created by zongzesheng on 12/6/16.
 */
public class JsonFilter {

    private JsonArray container;
    private String json;
    private String filterString;

    public JsonFilter(Json json, String filterString) {
        container = new JsonArray();
        JsonObject obj = json.getFlatJson();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getKey().contains("schema")) {
                continue;
            }
            JsonElement o = JsonContext.restore(entry.getKey(), entry.getValue());
            container.add(o);
        }
        this.json = JsonContext.toJson(container);
        this.filterString = filterString;
    }

    public String getResult() {
        return JsonContext.toJson(JsonPath.read(json, filterString));
    }
}
