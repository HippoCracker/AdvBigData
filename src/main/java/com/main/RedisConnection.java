package com.main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class RedisConnection {

  private Jedis jedis;
  private JSONParser parser;

  public RedisConnection() {
    jedis = new Jedis("localhost");
    parser = new JSONParser();
  }

  public String getAll() {
    String result = jedis.get("*");
    return result;
  }

  public String get(String id) {
    String result = jedis.get("post:" + id);
    return result;
  }

  public String create(String id, String data) {
    String result = jedis.set("post:" + id, data);
    return result;
  }

  public Long delete(String id) {
    Long result = jedis.del("post:" + id);
    return result;
  }

  public String put(String id, String val) {
    String result =  jedis.set(id, val);
    return result;
  }

}
