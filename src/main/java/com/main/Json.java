package com.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class Json {

  private static Gson gson = new Gson();

  private Json() {}

  public static String serialize(Map<String, Object> source) {
    return gson.toJson(source);
  }

  public static Map<String, Object> deserialize(String source) {
    return gson.fromJson(source, new TypeToken<Map<String, Object>>(){}.getType());
  }

  public static Map<String, Object> flatten(Map<String, Object> source) {
    Map<String, Object> result = new HashMap<>();
    if (source == null) {
      return result;
    }
    flatten(result, source, "");
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
      String key = concatKey("list_item" + String.valueOf(i), prefix);
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

  public static String restoreThenSerialize(Map<String, Object> source) {
    Map<String, Object> restored = restore(source);
    return Json.serialize(restored);
  }

  public static Map<String, Object> restore(Map<String, Object> source) {
    if (source == null || source.keySet().size() == 0) {
      return new HashMap<String, Object>();
    }

    Map<String, Object>  result = new HashMap<>();

    Iterator<Map.Entry<String, Object>> iterator =
          source.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();

      if (key.contains("list_item")) {
        continue;
      }
      innerRestore(result, source, key, value);
    }
    return result;
  }

  private static void innerRestore(Map<String, Object> result,
                                   Map<String, Object> source,
                                   String key,
                                   Object val) {
    if (key.contains("list_item")) {
      key = key.substring(0, key.indexOf("list_item"));
    }
    String[] keywords = key.split("\\.");

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

    if (val instanceof List) {
      val = restoreList(source, (List)val);
    }
    if (nestedMap != null) {
      nestedMap.put(keywords[keywords.length - 1], val);
    } else {
      result.put(keywords[keywords.length - 1], val);
    }
  }

  private static List<Object> restoreList(Map<String, Object> source,
                                          List<Object> val) {
    List<Object> values = new ArrayList<>();
    for (Object k : (List)val) {
      String keyword = String.valueOf(k);
      values.add(source.get(keyword));
    }
    return values;
  }
}


