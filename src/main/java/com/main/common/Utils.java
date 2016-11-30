package com.main.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by zongzesheng on 11/27/16.
 */
public final class Utils {

    public static final char SEPARATOR = '.';
    public static final String SCHEMA ="schema";
    public static final String SCHEMA_KEY ="schemaKey";
    public static final String STORAGE_KEY ="storageKey";
    public static final String E_TAG ="etag";
    public static final String VERSION = "_version";
    public static final String ID = "_id";
    public static final String NAME = "_name";
    public static final String TYPE = "_type";


    public static <T> T notNull(T object, String message, Object... values) {
        if (object == null) {
            throw new IllegalArgumentException(String.format(message, values));
        }
        return object;
    }

    public static String join(char separator, String... values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (value.isEmpty()) {
                continue;
            }
            sb.append(value).append(separator);
        }
        if (sb.charAt(sb.length() - 1) == separator) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String join(String delimiter, String wrap, Iterable<? extends Object> objs) {
        Iterator<? extends Object> iter = objs.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(wrap).append(iter.next()).append(wrap);
        while (iter.hasNext()) {
            buffer.append(delimiter).append(wrap).append(iter.next()).append(wrap);
        }
        return buffer.toString();
    }

    public static String join(String delimiter, Iterable<? extends Object> objs) {
        return join(delimiter, "", objs);
    }

    public static String newUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String uri(String id, String name, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(id).append("/").append(name).append(":").append(type);
        return sb.toString();
    }
//
//    public static void convertReservedKeywords(Object object) {
//        if (!(object instanceof JsonObject)) {
//            throw new IllegalArgumentException("Cannot convert attributes of object: "
//                    + object.getClass().getName());
//        }
//        JsonObject obj = (JsonObject) object;
//        for (String key : Configuration.RESERVED_KEYWORDS) {
//            if (obj.has(key)) {
//                JsonElement value = obj.get(key);
//                String transformedKey = transformReservedKey(key);
//                obj.add(transformedKey, value);
//                obj.remove(key);
//            }
//        }
//    }
//
//    private static String transformReservedKey(String key) {
//        if (Configuration.RESERVED_KEYWORDS.contains(key) &&
//                !key.startsWith("_")) {
//            key = "_" + key;
//        }
//        return key;
//    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static String generateETag(String key)
            throws UnsupportedEncodingException {
        byte[] bytes = key.getBytes("UTF-8");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(bytes);
        bytes = md.digest();

        StringBuffer hexBuilder = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) hexBuilder.append('0');
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }

}
