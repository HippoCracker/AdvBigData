package com.main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class RedisConnection {

  private Jedis jedis;
  private JSONParser parser;

  public RedisConnection() {
    jedis = new Jedis("localhost");
    parser = new JSONParser();
  }

  public String getAllAndExclude(String pattern, String excludePattern) {
    StringBuilder valueBuilder = new StringBuilder();
    Set<String> keys = jedis.keys(pattern);
    for (String key : keys) {
      if (key.contains(excludePattern)) {
        continue;
      }
      String result = get(key);
      valueBuilder.append(result).append("\n");
    }
    return valueBuilder.toString();
  }

  public String get(String id) {
    String restoredResult = Json.restoreThenSerialize(getMap(id));
    return restoredResult;
  }

  public Map<String, Object> getMap(String id) {
    String result = jedis.get(id);
    return Json.deserialize(result);
  }

  public String create(String id, String data) {
    String result = jedis.set(id, data);
    return result;
  }

  public Long delete(String id) {
    Long result = jedis.del(id);
    return result;
  }

  public String put(String id, String val) {
    String result =  jedis.set(id, val);
    return result;
  }

}
