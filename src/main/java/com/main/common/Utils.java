package com.main.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.UUID;

public final class Utils {

    public static final char SEPARATOR = '.';
    public static final String SCHEMA ="schema";
    public static final String SCHEMA_KEY ="schemaKey";
    public static final String STORAGE_KEY ="storageKey";
    public static final String E_TAG ="etag";
    public static final String VERSION = "__version";
    public static final String ID = "__id";
    public static final String NAME = "__name";
    public static final String TYPE = "__type";


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

    public static String md5(String key)
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

        return byteToHex(bytes);
    }

    public static String sha1(String str) {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        return sha1;
    }

    private static String byteToHex(byte[] bytes) {
        StringBuffer hexBuilder = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) hexBuilder.append('0');
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }
}
