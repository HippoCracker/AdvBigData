package com.main;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static com.main.common.Utils.*;

public class RestRequest {

    private String eTag;
    private Json jsonObject;
    private OperationTarget target;
    private String rawJson;
    private String type;
    private String name;
    private String id;


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
        if (last.equals(SCHEMA)) {
            target = OperationTarget.SCHEMA;
        } else {
            target = OperationTarget.DATA;
        }

        // TODO: fix use hard code fetch uri data
        if (!rawJson.contains(TYPE) || !rawJson.contains(NAME) || !rawJson.contains(ID)) {
            String[] tokens = path.split("/");
            if (tokens.length < 3) {
                throw new IllegalArgumentException("Illegal uri format: at least /{type}/{name}/");
            }
            type = tokens[1];
            name = tokens[2];
            id = tokens.length == 3 ? "*" : tokens[3];
            jsonObject = Json.emptyJson(id, name, type, rawJson);
        } else {
            jsonObject = Json.newJson(rawJson);
        }
    }

    public Json jsonObject() {
        return jsonObject;
    }

    public String rawJson() {
        return rawJson;
    }

    public String eTag() {
        return eTag;
    }

    public String type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String id() {
        return id;
    }

    public OperationTarget operationTarget() {
        return target;
    }

}
