package com.main;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zongzesheng on 11/1/16.
 */
public class RestUtils {

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
    for (int i=0;i < bytes.length;i++) {
      String hex=Integer.toHexString(0xff & bytes[i]);
      if(hex.length()==1) hexBuilder.append('0');
      hexBuilder.append(hex);
    }
    return hexBuilder .toString();
  }
}
