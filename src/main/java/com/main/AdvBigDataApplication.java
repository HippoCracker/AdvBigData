package com.main;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.main.cache.Cache;
import com.main.cache.LRUCache;
import com.main.context.JsonContext;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.main.common.Utils.*;


@Controller
@SpringBootApplication
public class AdvBigDataApplication {

    private static RedisConnection conn = new RedisConnection();
    private static ElasticSearch elasticSearch = new ElasticSearch();
    private static Cache cache = new LRUCache<String, String>(100);

    @ResponseBody
    @GetMapping({"/{type}/{name}/{id}", "/{type}/{name}", "/{type}/{name}/_schema"})
    String get(HttpServletRequest req, HttpServletResponse res)
            throws UnsupportedEncodingException {

        if (!isAuthorized(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "Forbid access";
        }
        RestRequest request = new RestRequest(req, "");

        if (request.eTag().equals(cache.get(request.jsonObject().storageKey()))) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return "";
        }

        Json flatJson = conn.get(request.jsonObject().storageKey());
        if (flatJson == null || flatJson.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Data not exists: " + req.getRequestURI();
        }
        flatJson.restore();
        if (flatJson.hasModified(request.eTag())) {
            res.setHeader("ETag", flatJson.eTag());
            return flatJson.jsonString();
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return "";
        }
    }

    @ResponseBody
    @PostMapping({"/{type}/{name}/{id}", "/{type}/{name}", "/{type}/{name}/_schema"})
    String create(HttpServletRequest req,
                  HttpServletResponse res,
                  @RequestBody String content)
            throws IOException, ProcessingException {

        if (!isAuthorized(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "Forbid access";
        }
        RestRequest request = new RestRequest(req, content);

        if (request.operationTarget() == RestRequest.OperationTarget.DATA) {
            Json schema = conn.get(request.jsonObject().schemaKey());
            ValidateResult validateResult = JsonValidator.validate(schema, request.jsonObject());
            if (!validateResult.success()) {
                return "Error in schema validation: \n" + validateResult.message();
            }
        }
        Json flatJson = request.jsonObject().flat();
        conn.set(request.jsonObject().flat().flatEntrySet());

        Json jsonObject = request.jsonObject();
        index(jsonObject);

        res.setHeader("ETag", request.jsonObject().eTag());
        return "Object saved, id: " + request.jsonObject().id();
    }

    @ResponseBody
    @DeleteMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String delete(HttpServletRequest req, HttpServletResponse res) {

        if (!isAuthorized(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "Forbid access";
        }
        RestRequest request = new RestRequest(req, "");
        if (request.operationTarget() == RestRequest.OperationTarget.SCHEMA) {
            return "Failt to delete schema, may cause current data invalid";
        }

        cache.remove(request.jsonObject().storageKey());

        conn.delete(request.jsonObject().storageKey());
        return "Delete object, id: " + request.jsonObject().id();
    }

    @ResponseBody
    @PatchMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String patch(HttpServletRequest req,
                 HttpServletResponse res,
                 @RequestBody String content)
            throws IOException, ProcessingException {

        if (!isAuthorized(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        RestRequest request = new RestRequest(req, content);

        Json jsonObject = conn.get(request.jsonObject().storageKey());
        jsonObject.merge(request.jsonObject())
                .restore()
                .updateETag();

        cache.put(jsonObject.storageKey(), jsonObject.eTag());

        if (request.operationTarget() == RestRequest.OperationTarget.DATA) {
            Json schema = conn.get(request.jsonObject().schemaKey());
            ValidateResult validateResult =
                    JsonValidator.validate(schema, jsonObject);
            if (!validateResult.success()) {
                return "Error in schema validation: \n" + validateResult.message();
            }
        }
        conn.set(jsonObject.flatEntrySet());
        res.setHeader("ETag", jsonObject.eTag());

        return String.format("Merge succeed, ID = %s\n Result: %s", jsonObject.id());
    }

    @ResponseBody
    @PutMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String put(HttpServletRequest req,
               HttpServletResponse res,
               @RequestBody String content)
            throws IOException, ProcessingException {

        if (!isAuthorized(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "Forbid access";
        }

        RestRequest request = new RestRequest(req, content);

        if (request.operationTarget() == RestRequest.OperationTarget.SCHEMA) {
            return "Fail to replace entrie schema, may cause current data invalid";
        }
        Json schema = conn.get(request.jsonObject().schemaKey());
        schema.restore();
        ValidateResult validateResult = JsonValidator.validate(schema, request.jsonObject());
        if (!validateResult.success()) {
            return "Error in schema validation: \n" + validateResult.message();
        }

        Json jsonObject = request.jsonObject();
        cache.put(jsonObject.storageKey(), jsonObject.eTag());

        conn.delete(request.jsonObject().storageKey());
        conn.set(jsonObject.flat().flatEntrySet());

        res.setHeader("ETag", jsonObject.eTag());
        return "Put object id: " + jsonObject.id();
    }

    @ResponseBody
    @RequestMapping("/{type}/{name}/_search")
    String search(HttpServletRequest req, @RequestBody String body){
        RestRequest request = new RestRequest(req, "");
        JsonElement jsonObject = JsonContext.parseJson(body);
        String result = search(request.name(), request.type(), jsonObject);
        return result;
    }

    private boolean isAuthorized(HttpServletRequest req) {
        String authCode = req.getHeader("Authorization");
        return conn.hasAuthCode(authCode);
    }

    private void index(Json jsonObject) {
        elasticSearch.index(jsonObject.getAsString(NAME),
                            jsonObject.getAsString(TYPE),
                            jsonObject.getAsString(ID),
                            jsonObject.jsonString());
    }

    private String search(String index, String type, JsonElement jsonElement) {
        return elasticSearch.search(index, type, jsonElement);
    }

    @GetMapping("/")
    @ResponseBody
    String home() {
        return "Welcome to home page";
    }

    @PostMapping("/")
    @ResponseBody
    String authorize(HttpServletRequest req,
                     HttpServletResponse res,
                     @RequestBody MultiValueMap<String,String> formData) {
        String plainClientCredentials= formData.get("username") + ":" + formData.get("password");
        String base64ClientCredentials = new String(Base64.encodeBase64(plainClientCredentials.getBytes()));
        String key = "Basic " + base64ClientCredentials;
        conn.saveAuthCode(key);
        res.setHeader("Authorization", key);
        return "";
    }

    @GetMapping("/search")
    @ResponseBody
    String search(@RequestBody String query) {
        return "";
    }

    public static void main(String[] args) {
        SpringApplication.run(AdvBigDataApplication.class, args);
    }
}
