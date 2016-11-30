package com.main;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import static com.main.common.Utils.*;

public class RestRequest {

    private String eTag;
    private String rawJson;
    private Json jsonObject;
    private OperationTarget target;

    public enum OperationTarget {
        SCHEMA, DATA
    }

    public RestRequest(HttpServletRequest request, String json) {
        rawJson = json;
        init(request);
    }

    private void init(HttpServletRequest req) {
        eTag = req.getHeader("If-None-Match");
        String path = req.getRequestURI();
        String last = path.substring(path.lastIndexOf("/") + 1);
        if (last.equals("_schema")) {
            target = OperationTarget.SCHEMA;
        } else {
            target = OperationTarget.DATA;
        }

        // TODO: fix use hard code fetch uri data
        if (!rawJson.contains(TYPE) ||!rawJson.contains(NAME) || !rawJson.contains(ID)) {
             String[] tokens = path.split("/");
             if (tokens.length == 4) {
                 jsonObject = Json.emptyJson(tokens[3], tokens[2], tokens[1], rawJson);
             } else if (tokens.length == 3) {
                 jsonObject = Json.emptyJson("*", tokens[2], tokens[1], rawJson);
             } else {
                 throw new IllegalArgumentException("Illegal uri format: at least /{type}/{name}/");
             }
        } else {
            jsonObject = Json.newJson(rawJson);
        }

    }

    public Json jsonObject() { return jsonObject; }

    public String rawJson() { return rawJson; }

    public String eTag() { return eTag; }

    public OperationTarget OperationTarget() {
        return target;
    }

}
