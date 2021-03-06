package com.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;

import java.util.*;


public class RedisConnection {

    private Jedis jedis;

    public RedisConnection() {
        jedis = new Jedis("localhost");
    }

    public Json get(String pattern, String excludePattern) {
        Json jsonObject = Json.flatJson();
        Set<String> keys = keys(pattern);
        for (String key : keys) {
            if (excludePattern != null && key.contains(excludePattern)) {
                continue;
            }
            String value = jedis.get(key);
            jsonObject.addToFlat(key, value);
        }
        return jsonObject;
    }

    public Json get(String pattern) {
        return get(pattern, null);
    }

    public boolean hasAuthCode(String key) {
        if (key == null || key.isEmpty()) return false;
        return jedis.exists(key);
    }

    public void saveAuthCode(String key, String encodedKey) {
        jedis.set(encodedKey, "");
        jedis.expire(encodedKey, 1800);
    }

    public String set(Set<Map.Entry<String, JsonElement>> entrySet) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String result = jedis.set(entry.getKey(), entry.getValue().toString());
            builder.append(result).append("\n");
        }
        return builder.toString();
    }

    public void delete(String pattern) {
        Set<String> keys = keys(pattern);
        for (String key : keys) {
            jedis.del(key);
        }
    }

    public Set<String> keys(String pattern) {
        return jedis.keys(pattern + "*");
    }
}
