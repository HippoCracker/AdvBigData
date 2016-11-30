package com.main.json;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractJsonProvider implements JsonProvider {

    public boolean isArray(Object obj) {
        return (obj instanceof List);
    }

    @Override
    public int length(Object obj) {
        if (isArray(obj)) {
            return ((List) obj).size();
        } else if (isMap(obj)) {
            return getPropertyKeys(obj).size();
        } else if (obj instanceof String) {
            return ((String) obj).length();
        }
        throw new IllegalArgumentException("length operation cannot apply to "
                + obj != null ? obj.getClass().getName() : "null");
    }

    @Override
    public Iterable<?> toIterable(Object obj) {
        if (isArray(obj)) {
            return ((Iterable) obj);
        } else {
            throw new IllegalArgumentException("Cannot iterate over "
                    + obj != null ? obj.getClass().getName() : "null");
        }
    }

    @Override
    public Collection<String> getPropertyKeys(Object obj) {
        if (isMap(obj)) {
            return ((Map) obj).keySet();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object getArrayIndex(Object obj, int idx) {
        return ((List) obj).get(idx);
    }

    @Override
    public void setArrayIndex(Object obj, int idx, Object newValue) {
        if (!isArray(obj)) {
            throw new UnsupportedOperationException();
        }
        List l = (List) obj;
        if (idx == l.size()) {
            l.add(newValue);
        } else {
            l.set(idx, newValue);
        }
    }

    @Override
    public Object getMapValue(Object obj, String key) {
        Map map = (Map) obj;
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return JsonProvider.UNDEFINED;
        }
    }

    @Override
    public void setProperty(Object obj, Object key, Object value) {
        if (isMap(obj)) {
            ((Map) obj).put(key, value);
        } else {
            throw new IllegalArgumentException("setProperty operation cannot be used with "
                    + obj != null ? obj.getClass().getName() : "null");
        }
    }

    @Override
    public void removeProperty(Object obj, Object key) {
        if (isMap(obj)) {
            ((Map) obj).remove(key.toString());
        } else {
            List list = (List) obj;
            int index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            list.remove(index);
        }
    }



    @Override
    public boolean isMap(Object obj) {
        return (obj instanceof Map);
    }

    @Override
    public Object unwrap(Object obj) {
        return obj;
    }
}
