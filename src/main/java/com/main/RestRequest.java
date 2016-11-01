package com.main;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class RestRequest {

  private String eTag;
  private String rawPath;
  private String rawContent;
  private String[] rawPathTokens;
  private OP_TARGET target;

  private Map<String, Object> params;
  private Map<String, Object> content;
  private Map<String, Object> flattenContent;

  public static final String WILDCARD = "*";

  public enum OP_TARGET {
    SCHEMA, DATA
  }

  public RestRequest(HttpServletRequest request, String content) {
    params = new HashMap<>();
    rawContent = content;
    eTag = request.getHeader("If-None-Match");
    parseContent();
    fetchComponents(request);
  }

  private void fetchComponents(HttpServletRequest request) {
    rawPath = request.getRequestURI();
    rawPathTokens = rawPath.split("/");

    if (rawPath.contains("_schema"))
      target = OP_TARGET.SCHEMA;
    else
      target = OP_TARGET.DATA;

    params.put("_uuid", getUUID());

    String id;
    if (target == OP_TARGET.SCHEMA) {
      id = "_schema";
    } else if (rawPathTokens.length == 4) {
      id = rawPathTokens[3];
    } else {
      id = (String) params.get("_uuid");
    }

    params.put("_id", id);
    params.put("_index", rawPathTokens[1]);
    params.put("_type", rawPathTokens[2]);
    params.put("_version", "0");
    params.put("_uri", rawPath);
    params.put("properties", flattenContent());
  }

  private String getUUID() {
    return  UUID.randomUUID().toString().replaceAll("-", "");
  }

  public String schemaKey() {
    StringBuilder sb = new StringBuilder();
    sb.append(param("_index")).append(".");
    sb.append(param("_type")).append(".");
    sb.append("_schema");
    return sb.toString();
  }

  public String key() {
    StringBuilder sb = new StringBuilder();
    sb.append(param("_index"));
    sb.append(".").append(param("_type"));
    sb.append(".").append(param("_id"));
    return sb.toString();
  }

  public String value() {
    return Json.serialize(params);
  }

  public String eTag() { return eTag; }

  public String uri() {
    return rawPath;
  }

  public Object param(String key) {
    return params.get(key);
  }

  public String rawContent() { return rawContent; }

  public Map<String, Object> content() {
    return content;
  }

  public OP_TARGET target() { return target; }

  public Map<String, Object>  flattenContent() {
    if (content == null) {
      parseContent();
    }
    if (flattenContent == null) {
      flattenContent = Json.flatten(content);
    }
    return flattenContent;
  }

  private void parseContent() {
    if (content == null) {
      content = Json.deserialize(rawContent);
    }
  }

}
