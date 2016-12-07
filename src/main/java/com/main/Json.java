package com.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.main.common.Utils;
import com.main.context.JsonContext;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.main.common.Utils.*;

public class Json {

    private JsonContext jsonContext;
    private String storageKey;
    private String schemaKey;


    public static Json newJson(String json) {
        Json jsonObject = new Json(json);
        jsonObject.init();
        return jsonObject;
    }

    public static Json emptyJson(String id, String name, String type, String json) {
        return new Json(id, name, type, json);
    }

    public static Json flatJson() {
        Json json = new Json();
        json.jsonContext.parse("{}", true);
        return json;
    }

    private Json() {
        this.jsonContext = new JsonContext();
    }

    private Json(String json) {
        this(json, false);
    }

    private Json(String json, boolean isFlat) {
        jsonContext = new JsonContext().parse(json, isFlat);
        if (!isFlat) {
            init();
        }
    }

    private Json(String id, String name, String type, String json) {
        jsonContext = new JsonContext().parse(json == null || json.isEmpty() ? "{}" : json);
        jsonContext.add(ID, id);
        jsonContext.add(NAME, name);
        jsonContext.add(TYPE, type);
        init();
    }

    private void init() {
        storageKey = createStorageKey();
        schemaKey = createSchemaKey();
        jsonContext.add(VERSION, "0");
        jsonContext.add(STORAGE_KEY, storageKey);
        jsonContext.add(SCHEMA_KEY, schemaKey);
        updateETag();
    }

    public boolean isEmpty() {
        return (jsonContext.json() == null && jsonContext.flatJson() == null);
    }

    public Json parse(String json) {
        jsonContext.parse(json);
        return this;
    }

    public String jsonString() {
        return jsonContext.jsonString();
    }

    public String flatJsonString() {
        return jsonContext.flatJsonString();
    }

    public Json add(String key, Object value) {
        jsonContext.add(key, value);
        updateETag();
        return this;
    }

    public Json addToFlat(String key, Object value) {
        jsonContext.addToFlat(key, value);
        return this;
    }

    public Json remove(String key) {
        jsonContext.remove(key);
        updateETag();
        return this;
    }

    public Object get(String key) {
        return jsonContext.get(key);
    }

    public boolean has(String key) {
        return jsonContext.has(key);
    }

    public boolean hasFlat(String key) {
        return jsonContext.hasFlat(key);
    }

    public String getAsString(String key) {
        return jsonContext.getAsString(key);
    }

    public Object getFlatAttribute(String key) {
        return jsonContext.getFlatAttribute(key);
    }


    public String id() {
        return jsonContext.getAsString(ID);
    }

    public Json flat() {
        jsonContext.flat();
        return this;
    }

    public Json restore() {
        jsonContext.restore();
        return this;
    }

    public Json merge(Json json) {
        jsonContext.merge(json.jsonContext);
        return this;
    }

    public Iterator<?> jsonIterator() {
        return jsonContext.jsonIterator();
    }

    public Set<Map.Entry<String, JsonElement>> flatEntrySet() {
        return jsonContext.flatEntrySet();
    }

    public Set<Map.Entry<String, JsonElement>> jsonEntrySet() {
        return jsonContext.jsonEntrySet();
    }

    public boolean hasModified(String eTag) {
        return !eTag().equals(eTag);
    }

    public String eTag() {
        try {
            return jsonContext.getAsString(E_TAG);
        } catch (NullPointerException e) {
            return "";
        }
    }

    public String storageKey() { return jsonContext.getAsString(STORAGE_KEY); }

    public String schemaKey() { return jsonContext.getAsString(SCHEMA_KEY); }

    public Json updateETag() {
        String etag = null;
        int version = jsonContext.getAsInt(VERSION) + 1;
        try {
            etag = Utils.md5(storageKey + version);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to generate etag, storageKey: "
                    + storageKey + " version: " + version, e);
        }
        jsonContext.set(E_TAG, etag);
        jsonContext.set(VERSION, version);
        return this;
    }

    private String createStorageKey() {
        String type = jsonContext.getAsString(TYPE);
        String name = jsonContext.getAsString(NAME);

        String id;
        if (jsonContext.has(ID)) {
            id = jsonContext.getAsString(ID);
        } else {
            id = Utils.newUUID();
            jsonContext.add(ID, id);
        }
        return join(SEPARATOR, type, name, id);
    }

    private String createSchemaKey() {
        String type = jsonContext.getAsString(TYPE);
        String name = jsonContext.getAsString(NAME);

        return join(SEPARATOR, type, name, SCHEMA);
    }

    private static void mustHaveOrThrow(String name, JsonObject obj) {
        if (!obj.has(name)) {
            throw new IllegalArgumentException("Missing attribute: " + name);
        }
    }
}


