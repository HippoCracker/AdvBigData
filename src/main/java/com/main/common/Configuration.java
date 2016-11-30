package com.main.common;

import com.main.json.GsonJsonProvider;
import com.main.json.JsonProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zongzesheng on 11/27/16.
 */
public class Configuration {

    public static final Set<String> RESERVED_KEYWORDS;

    static {
        Set<String> keys = new HashSet<>();
        keys.add("type");
        keys.add("id");
        keys.add("name");
        keys.add("comment");
        keys.add("uri");
        keys.add("ref");
        RESERVED_KEYWORDS = keys;
    }



    private final JsonProvider jsonProvider;

    private Configuration(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    public JsonProvider jsonProvider() {
        return jsonProvider;
    }

    public static Configuration defaultConfiguration() {
        return builder().jsonProvider(new GsonJsonProvider()).build();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private JsonProvider jsonProvider;

        public Builder jsonProvider(JsonProvider jsonProvider) {
            this.jsonProvider = jsonProvider;
            return this;
        }

        public Configuration build() {
            return new Configuration(jsonProvider);
        }
    }
}
