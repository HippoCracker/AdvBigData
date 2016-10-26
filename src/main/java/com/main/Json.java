package com.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class Json {

  private static Gson gson = new Gson();

  private Json() {}

  public static Map<String, Object> deserialize(String data) {
    Map<String, Object> result = new HashMap<>();
    if (data == null || data.length() == 0) {
      return result;
    }
    Map<String, Object> map =
          gson.fromJson(data, new TypeToken<Map<String, Object>>(){}.getType());
    flatten(result, map, "");
    return result;
  }

  private static void flatten(Map<String, Object> result,
                              Map<String, Object> map,
                              String prefix) {

    Iterator<Map.Entry<String, Object>> iterator =
          map.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();
      key = concatKey(key, prefix);

      if (value instanceof Map) {
        flatten(result, (Map)value, key);
      } else if (value instanceof List) {
        List<String> keys = flattenList(result, (List)value, key);
        result.put(key, keys);
      } else {
        result.put(key, value);
      }
    }
  }

  private static List<String> flattenList(Map<String, Object> result,
                                 List<Object> list,
                                 String prefix) {
    List<String> keys = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      String key = concatKey("_index" + String.valueOf(i), prefix);
      Object value = list.get(i);

      if (value instanceof Map) {
        flatten(result, (Map)value, key);
      } else {
        result.put(key, value);
      }
      keys.add(key);
    }
    return keys;
  }

  private static String concatKey(String key, String prefix) {
    return prefix.length() == 0 ? key : prefix + "." + key;
  }

  public static String serialize(Map<String, Object> source) {
    if (source == null || source.keySet().size() == 0) {
      return "";
    }

    Map<String, Object>  result = new HashMap<>();

    Iterator<Map.Entry<String, Object>> iterator =
          source.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();

      if (key.contains("_index")) {
        continue;
      }
      restore(result, source, key, value);
    }
    return gson.toJson(result);
  }

  private static void restore(Map<String, Object> result,
                              Map<String, Object> source,
                              String key,
                              Object val) {
    String[] keywords = key.split("\\.");

    if (val instanceof List) {
      restoreList(result, source, key, (List)val);
      return;
    }

    Map<String, Object> nestedMap = null;
    for (int i = 0; i < keywords.length - 1; i++) {
      String keyword = keywords[i];
      if (result.containsKey(keyword) &&
          result.get(keyword) instanceof Map) {
        nestedMap = (Map)result.get(keyword);
      } else {
        nestedMap = new HashMap<>();
        result.put(keyword, nestedMap);
      }
    }
    if (nestedMap != null) {
      nestedMap.put(keywords[keywords.length - 1], val);
    } else {
      result.put(keywords[keywords.length - 1], val);
    }
  }

  private static void restoreList(Map<String, Object> result,
                                  Map<String, Object> source,
                                  String key,
                                  List<Object> val) {
    List<Object> values = new ArrayList<>();
    for (Object k : (List)val) {
      String keyword = String.valueOf(k);
      values.add(retoreMap(source, keyword));
    }
    result.put(key, values);
  }

  private static Map<String, Object> retoreMap(Map<String, Object> source,
                                               String prefix) {
    Map<String, Object> map = new HashMap<>();
    for (String key : source.keySet()) {
      if (key.startsWith(prefix)) {
        Object value = source.get(key);
        key = key.replace(prefix + ".", "");
        map.put(key, value);
      }
    }
    return map;
  }
}


