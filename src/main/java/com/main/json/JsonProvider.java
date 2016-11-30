package com.main.json;

import com.google.gson.JsonElement;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface JsonProvider {

    static final Object UNDEFINED = new Object();

    Object parse(String json) throws InvalidJsonException;

    Object parse(InputStream jsonStream, String charset) throws InvalidJsonException;

    <T> T fromJson(String json, Type type);

    String toJson(Object obj);

    Object createArray();

    Object createMap();

    boolean isArray(Object obj);

    int length(Object obj);

    Iterable<?> toIterable(Object obj);

    Iterator<?> iterator(Object obj);

    Collection<String> getPropertyKeys(Object obj);

    Object getArrayIndex(Object obj, int idx);

    void setArrayIndex(Object obj, int idx, Object newValue);

    Object getMapValue(Object obj, String key);

    void setProperty(Object obj, Object key, Object value);

    void removeProperty(Object obj, Object key);

    boolean isMap(Object obj);

    Object unwrap(Object obj);

    Object flat(Object obj);

    Object restore(Object obj);

    void add(Object obj, String key, Object value);

    void remove(Object obj, String key);

    Object get(Object obj, String key);

    String getAsString(Object obj, String key);

    Integer getAsInt(Object obj, String key);

    Object getFlatAttribute(Object obj, String key);

    boolean has(Object obj, String key);

    void set(Object obj, String key, Object newValue);

    void merge(Object obj1, Object obj2);

    Set<Map.Entry<String, JsonElement>> entrySet(Object obj);
}
