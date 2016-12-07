package com.main.json;

import com.google.gson.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import com.google.gson.internal.LazilyParsedNumber;
import com.main.common.Configuration;
import com.main.common.Utils;

import static com.main.common.Utils.*;

public class GsonJsonProvider extends AbstractJsonProvider {


    private static final JsonParser PARSER = new JsonParser();
    private Gson gson;

    public GsonJsonProvider() { this(new Gson()); }

    public GsonJsonProvider(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object unwrap(final Object o) {
        if (o == null) {
            return null;
        }
        if (!(o instanceof JsonElement)) {
            return o;
        }

        JsonElement e = (JsonElement) o;
        if (e.isJsonNull()) {
            return null;
        } else if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isString()) {
                return p.getAsString();
            } else if (p.isBoolean()) {
                return p.getAsBoolean();
            } else if (p.isNumber()) {
                return unwrapNumber(p.getAsNumber());
            }
        }
        return o;
    }

    private static Number unwrapNumber(final Number n) {
        Number unwrapped;

        if (n instanceof LazilyParsedNumber) {
            LazilyParsedNumber lpn = (LazilyParsedNumber) n;
            BigDecimal bigDecimal = new BigDecimal(lpn.toString());
            if (bigDecimal.scale() <= 0) {
                if (bigDecimal.compareTo(new BigDecimal(Integer.MAX_VALUE)) <= 0) {
                    unwrapped = bigDecimal.intValue();
                } else {
                    unwrapped = bigDecimal.longValue();
                }
            } else {
                unwrapped = bigDecimal.doubleValue();
            }
        } else {
            unwrapped = n;
        }
        return unwrapped;
    }

    @Override
    public Object parse(String json) throws InvalidJsonException {
        return PARSER.parse(json);
    }

    @Override
    public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
        try {
            return PARSER.parse(new InputStreamReader(jsonStream, charset));
        } catch (UnsupportedEncodingException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    @Override
    public Iterable<?> toIterable(final Object obj) {
        JsonArray arr = toJsonArray(obj);
        List<Object> values = new ArrayList<>(arr.size());
        for (Object o : arr) {
            values.add(unwrap(o));
        }
        return values;
    }

    @Override
    public String toJson(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public Object createArray() {
        return new JsonArray();
    }

    @Override
    public Object createMap() {
        return new JsonObject();
    }

    @Override
    public boolean isArray(final Object obj) {
        return (obj instanceof JsonArray || obj instanceof List);
    }

    @Override
    public Object getArrayIndex(final Object obj, final int idx) {
        return toJsonArray(obj).get(idx);
    }

    @Override
    public void setArrayIndex(final Object array, final int index, final Object newValue) {
        if (!isArray(array)) {
            throw new UnsupportedOperationException();
        }
        JsonArray arr = toJsonArray(array);
        if (index == arr.size()) {
            arr.add(createJsonElement(newValue));
        } else {
            arr.set(index, createJsonElement(newValue));
        }
    }

    @Override
    public Object getMapValue(final Object obj, final String key) {
        JsonObject jsonObject = toJsonObject(obj);
        if (jsonObject.has(key)) {
            Object o = jsonObject.get(key);
            return unwrap(o);
        } else {
            return UNDEFINED;
        }
    }

    @Override
    public void setProperty(final Object obj, final Object key, final Object value) {
        if (isMap(obj)) {
            toJsonObject(obj).add(key.toString(), createJsonElement(value));
        } else {
            JsonArray array = toJsonArray(obj);
            int index;
            if (key != null) {
                index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            } else {
                index = array.size();
            }

            if (index == array.size()) {
                array.add(createJsonElement(value));
            } else {
                array.set(index, createJsonElement(value));
            }
        }
    }

    @Override
    public void removeProperty(final Object obj, final Object key) {
        if (isMap(obj)) {
            toJsonObject(obj).remove(key.toString());
        } else {
            JsonArray array = toJsonArray(obj);
            int index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            array.remove(index);
        }
    }

    @Override
    public boolean isMap(Object obj) {
        return (obj instanceof JsonObject);
    }

    @Override
    public List<String> getPropertyKeys(Object obj) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : toJsonObject(obj).entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    @Override
    public int length(final Object obj) {
        if (isArray(obj)) {
            return toJsonArray(obj).size();
        } else if (isMap(obj)) {
            return toJsonObject(obj).size();
        } else {
            if (obj instanceof JsonElement) {
                JsonElement element = toJsonElement(obj);
                if (element.isJsonPrimitive()) {
                    return element.toString().length();
                }
            }
        }
        throw new IllegalArgumentException("length operation cannot apply to "
                + (obj != null ? obj.getClass().getName() : "null"));
    }


    @Override
    public Object flat(Object obj) {
        if (!isMap(obj)) {
            throw new IllegalArgumentException("flat operation cannot apply to object: "
                    + obj.getClass().getName());
        }
        JsonObject jsonObject = toJsonObject(obj);
        String key = jsonObject.get(STORAGE_KEY).getAsString();
        JsonObject container = new JsonObject();
        flat(container, jsonObject, key);
        return container;
    }

    private void flat(JsonObject container, JsonObject nestedObj, String key) {

        Collection<String> memberNames = getPropertyKeys(nestedObj);
        JsonObject obj = new JsonObject();

        for (String memberName : memberNames) {
            JsonElement element = nestedObj.get(memberName);
            if (isMap(element)) {
                flat(container, element.getAsJsonObject(), join(SEPARATOR, key, memberName));
            } else if (isArray(element)) {
                int count = 0;
                for (JsonElement ele : toJsonArray(element)) {
                    flat(container, ele.getAsJsonObject(), join(SEPARATOR, key, memberName, String.valueOf(count)));
                    count += 1;
                }
            } else {
                obj.add(memberName, element);
            }
        }
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(obj);
        container.add(key, jsonArray);
    }

    @Override
    public Object restore(String key, Object value) {
        JsonObject obj = new JsonObject();
        obj.add(key, createJsonElement(value));
        return restore(obj);
    }

    @Override
    public Object restore(Object obj) {
        if (!isMap(obj)) {
            throw new IllegalArgumentException("restore operation cannot apply to object: "
                    + obj.getClass().getName());
        }
        List<String> keys = getPropertyKeys(obj);
        Collections.sort(keys);

        JsonObject jsonObject = toJsonObject(obj);
        JsonObject container = new JsonObject();
        JsonObject root = container;
        for (String key : keys) {
            JsonElement value = jsonObject.get(key);
            value = restoreValue(value);
            String[] tokens = key.split("\\.");
            if (tokens.length == 3 && !container.has(key)) {
                container.add(key, value);
                if (isMap(value)) {
                    root = value.getAsJsonObject();
                }
            }
            restore(root, tokens, 3, value);
        }
        return root;
    }

    private JsonElement restoreValue(JsonElement value) {
        JsonObject restoredObj = new JsonObject();
        if (!isArray(value)) {
            return value;
        }
        for (JsonElement obj : toJsonArray(value)) {
            for (String key : getPropertyKeys(obj)) {
                JsonElement val = obj.getAsJsonObject().get(key);
                restoredObj.add(key, val);
            }
        }
        return restoredObj;
    }

    private void restore(JsonObject container, final String[] keyTokens, int startIdx, final JsonElement value) {
        if (keyTokens == null || keyTokens.length - 1 < startIdx) return;

        JsonArray arrayContainer = null;
        int len = keyTokens.length;
        for (int i = startIdx; i < len - 1; i++) {
            String key = keyTokens[i];
            if (isNumeric(key) && arrayContainer != null) {
                int index = Integer.parseInt(key);
                if (index < arrayContainer.size()) {
                    container = arrayContainer.get(index).getAsJsonObject();
                    arrayContainer = null;
                }
                continue;
            }
            if (container.has(key)) {
                JsonElement element = container.get(key);
                if (isArray(element)) {
                    arrayContainer = element.getAsJsonArray();
                } else if (isMap(element)){
                    container = element.getAsJsonObject();
                    arrayContainer = null;
                }
            } else {
                if (i + 1 < len && isNumeric(keyTokens[i + 1])) {
                    JsonArray array = new JsonArray();
                    arrayContainer = array;
                    container.add(key, array);
                } else if (!isNumeric(key)) {
                    JsonObject obj = new JsonObject();
                    container.add(key, obj);
                    //container = obj;
                }
            }
        }
        if (arrayContainer != null) {
            arrayContainer.add(value);
        } else {
            container.add(keyTokens[keyTokens.length - 1], value);
        }
    }

    @Override
    public void add(Object obj, String key, Object value) {
        if (!isMap(obj)) {
            throw new IllegalArgumentException("Failed to add key: " + key
                    + " value: " + value.getClass().getName() + " to object: "
                    + obj.getClass().getName());
        }
        if (value instanceof String) {
            value = parse((String) value);
        }
        toJsonObject(obj).add(key, createJsonElement(value));

    }

    @Override
    public void remove(Object obj, String key) {
        if (!isMap(obj)) {
            throw new IllegalArgumentException("Failed to remove key: " + key
                    + " from object: " + obj.getClass().getName());
        }
        toJsonObject(obj).remove(key);
    }

    @Override
    public Object get(Object obj, String key) {
        if (!isMap(obj)) {
            throw new IllegalArgumentException("Failed to get key: " + key
                    + " from object: " + obj.getClass().getName());
        }
        return toJsonObject(obj).get(key);
    }

    @Override
    public String getAsString(Object obj, String key) {
        return toJsonElement(get(obj, key)).getAsString();
    }

    @Override
    public Integer getAsInt(Object obj, String key) {
        return toJsonElement(get(obj, key)).getAsInt();
    }

    @Override
    public Object getFlatAttribute(Object obj, String key) {
        return toJsonObject(obj).get(key);
    }

    @Override
    public boolean has(Object obj, String key) {
        return toJsonObject(obj).has(key);
    }

    @Override
    public void set(Object obj, String key, Object newValue) {
        JsonObject jsonObject = toJsonObject(obj);
        jsonObject.remove(key);
        jsonObject.add(key, createJsonElement(newValue));
    }

    @Override
    public void merge(Object obj1, Object obj2) {
        JsonObject thisJson = toJsonObject(obj1);
        JsonObject thatJson = toJsonObject(obj2);

        for (Map.Entry<String, JsonElement> entry : thatJson.entrySet()) {
            set(thisJson, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<Map.Entry<String, JsonElement>> entrySet(Object obj) {
        return toJsonObject(obj).entrySet();
    }

    public Iterator<?> iterator(Object obj) {
        if (isArray(obj)) {
            return toJsonArray(obj).iterator();
        } else if (isMap(obj)) {
            return toJsonObject(obj).entrySet().iterator();
        }else {
            throw new IllegalArgumentException("Object: "
                    + obj.getClass().getName() + " is not iterable");
        }
    }

    private JsonElement createJsonElement(final Object o) {
        return gson.toJsonTree(o);
    }

    private JsonArray toJsonArray(final Object o) {
        return (JsonArray) o;
    }

    private JsonObject toJsonObject(final Object o) {
        return (JsonObject) o;
    }

    private JsonElement toJsonElement(final Object o) {
        return (JsonElement) o;
    }
}
