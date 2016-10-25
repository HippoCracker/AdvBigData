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
    Map<String, Map> map =
          gson.fromJson(data, new TypeToken<Map<String, Map>>(){}.getType());
    flatten(result, map, "");
    return result;
  }

  private static void flatten(Map<String, Object> result,
                              Map<String, Map> map,
                              String prefix) {

    Iterator<Map.Entry<String, Map>> iterator =
          map.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Map> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();
      key = prefix + "." + key;

      if (value instanceof Map) {
        flatten(result, (Map)value, key);
      } else if (value instanceof List) {
        Set<String> keys = flattenList(result, (List)value, key);
        result.put(key, keys);
      } else {
        result.put(key, value);
      }
    }
  }

  public static Set<String> flattenList(Map<String, Object> result,
                                 List<Object> list,
                                 String prefix) {
    Set<String> keys = new HashSet<>();
    for (int i = 0; i < list.size(); i++) {
      String key = prefix + "." + i;
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

  public static String serialize(Map<String, Object> map) {
    if (map == null || map.keySet().size() == 0) {
      return "";
    }
    return gson.toJson(map);
  }
}
