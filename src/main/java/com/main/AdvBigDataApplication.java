package com.main;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.main.cache.Cache;
import com.main.cache.LRUCache;
import com.main.common.Utils;
import com.main.context.JsonContext;
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
    private static IndexQueue indexQueue = new IndexQueue(100, elasticSearch);


    @ResponseBody
    @GetMapping({"/{type}/{name}/{id}", "/{type}/{name}", "/{type}/{name}/_schema"})
    String get(HttpServletRequest req, HttpServletResponse res)
            throws UnsupportedEncodingException {

        if (!isAuthorized(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "Forbid access";
        }
        RestRequest request = new RestRequest(req, "");

        if (request.eTag() != null &&
                request.eTag().equals(cache.get(request.jsonObject().storageKey()))) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return "";
        }

        Json flatJson = conn.get(request.jsonObject().storageKey());
        if (flatJson == null || flatJson.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "Data not exists: " + req.getRequestURI();
        }
        flatJson.restore();
        cache.put(request.jsonObject().storageKey(), flatJson.eTag());

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
        request.jsonObject().flat();
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
                .updateETag()
                .flat();

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

        return String.format("Merge succeed, ID = %s", jsonObject.id());
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

    private void index(Json json) {
        elasticSearch.index(json);
    }

    private String search(String index, String type, JsonElement jsonElement) {
        return elasticSearch.search(index, type, jsonElement);
    }

    @ResponseBody
    @RequestMapping("/_jsonpath")
    String jsonPath(@RequestBody String body) {
        JsonObject jsonObject = JsonContext.parseJson(body).getAsJsonObject();
        Path path = new Path(jsonObject.get("jsonpath").getAsString());
        Json json = conn.get(path.getKeyPattern());
        String result = new JsonFilter(json, path.getFilterPattern()).getResult();
        return result;
    }

    @GetMapping("/")
    @ResponseBody
    String home() {
        return "Welcome to home page";
    }

    @PostMapping("/")
    @ResponseBody
    String authorize(HttpServletResponse res,
                     @RequestBody MultiValueMap<String,String> formData) {
        String data = formData.get("username") + ":" + formData.get("password");
        String key = Utils.sha1(data);
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

        Thread t = new Thread(indexQueue);
        t.start();
    }
}
