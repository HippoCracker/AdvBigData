package com.main.context;

import com.google.gson.JsonElement;
import com.main.common.Configuration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.main.common.Utils.notNull;

public class JsonContext implements ParseContext, DocumentContext {

    private final Configuration configuration;
    private Object json;
    private Object flatJson;

    public JsonContext() { this.configuration = Configuration.defaultConfiguration(); }

    @Override
    public JsonContext parse(String json) {
        notNull(json, "json string cannot be null");
        return parse(json, false);
    }

    public JsonContext parse(String json, boolean isFlat) {
        notNull(json, "json string cannot be null");
        if (isFlat) {
            this.flatJson = configuration.jsonProvider().parse(json);;
        } else {
            this.json = configuration.jsonProvider().parse(json);
        }
        return this;
    }

    @Override
    public JsonContext flat() {
        notNull(json, "falt opertation cannot apply to json object: %s",
                json != null ? json.getClass().getName() : "null");
        flatJson = configuration.jsonProvider().flat(json);
        return this;
    }

    @Override
    public JsonContext restore() {
        notNull(flatJson, "restore opertation cannot apply to json object: %s",
                flatJson != null ? flatJson.getClass().getName() : "null");
        json = configuration.jsonProvider().restore(flatJson);
        return this;
    }

    public JsonContext add(String key, Object value) {
        notNull(key, "Key cannot be null");
        notNull(value, "Value cannot be null");
        configuration.jsonProvider().add(json, key, value);
        return this;
    }

    public JsonContext addToFlat(String key, Object value) {
        notNull(key, "Key cannot be null");
        notNull(value, "Value cannot be null");
        configuration.jsonProvider().add(flatJson, key, value);
        return this;
    }

    public JsonContext remove(String key) {
        notNull(key, "Key cannot be null");
        configuration.jsonProvider().remove(json, key);
        return this;
    }

    public Object get(String key) {
        notNull(key, "Key cannot be null");
        return configuration.jsonProvider().get(json, key);
    }

    public boolean has(String key) {
        notNull(key, "Key cannot be null");
        return configuration.jsonProvider().has(json, key);
    }

    public void set(String key, Object newValue) {
        notNull(key, "Key cannot be null");
        notNull(newValue, "newValue cannot be null");
        configuration.jsonProvider().set(json, key, newValue);
    }

    public void merge(JsonContext jsonContext) {
        if (jsonContext == null) return;
        configuration.jsonProvider().merge(flatJson, jsonContext.flat().flatJson());
    }

    public String getAsString(String key) {
        return configuration.jsonProvider().getAsString(json, key);
    }

    public Integer getAsInt(String key) { return configuration.jsonProvider().getAsInt(json, key); }

    public Iterator<?> jsonIterator() {
        return configuration.jsonProvider().iterator(json);
    }

    public Set<Map.Entry<String, JsonElement>> flatEntrySet() {
        return configuration.jsonProvider().entrySet(flatJson);
    }

    public Set<Map.Entry<String, JsonElement>> jsonEntrySet() {
        return configuration.jsonProvider().entrySet(json);
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public <T> T json() {
        return (T) json;
    }

    @Override
    public <T> T flatJson() { return (T) flatJson; }

    @Override
    public String jsonString() {
        return configuration.jsonProvider().toJson(json);
    }

    @Override
    public String flatJsonString() {
        return configuration.jsonProvider().toJson(flatJson);
    }
}
