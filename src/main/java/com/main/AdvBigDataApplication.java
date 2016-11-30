package com.main;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


@Controller
@SpringBootApplication
public class AdvBigDataApplication {

    private static RedisConnection conn = new RedisConnection();

    @ResponseBody
    @GetMapping({"/{type}/{name}/{id}", "/{type}/{name}", "/{type}/{name}/_schema"})
    String get(HttpServletRequest req, HttpServletResponse res)
            throws UnsupportedEncodingException {
        RestRequest request = new RestRequest(req, "");

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

        RestRequest request = new RestRequest(req, content);

        if (request.operationTarget() == RestRequest.OperationTarget.DATA) {
            Json schema = conn.get(request.jsonObject().schemaKey());
            ValidateResult validateResult = JsonValidator.validate(schema, request.jsonObject());
            if (!validateResult.success()) {
                return "Error in schema validation: \n" + validateResult.message();
            }
        }
        Json flatJson = request.jsonObject().flat();
        String result = conn.set(request.jsonObject().flat().flatEntrySet());

        res.setHeader("ETag", request.jsonObject().eTag());
        return "Object saved, id: " + request.jsonObject().id()
                + "\nresult: " + result;
    }

    @ResponseBody
    @DeleteMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String delete(HttpServletRequest req) {
        RestRequest request = new RestRequest(req, "");
        if (request.operationTarget() == RestRequest.OperationTarget.SCHEMA) {
            return "Failt to delete schema, may cause current data invalid";
        }
        conn.delete(request.jsonObject().storageKey());
        return "Delete object, id: " + request.jsonObject().id();
    }

    @ResponseBody
    @PatchMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String patch(HttpServletRequest req,
                 HttpServletResponse res,
                 @RequestBody String content)
            throws IOException, ProcessingException {

        RestRequest request = new RestRequest(req, content);

        Json jsonObject = conn.get(request.jsonObject().storageKey());
        jsonObject.merge(request.jsonObject())
                .restore()
                .updateETag();

        if (request.operationTarget() == RestRequest.OperationTarget.DATA) {
            Json schema = conn.get(request.jsonObject().schemaKey());
            ValidateResult validateResult =
                    JsonValidator.validate(schema, jsonObject);
            if (!validateResult.success()) {
                return "Error in schema validation: \n" + validateResult.message();
            }
        }
        String result = conn.set(jsonObject.flatEntrySet());
        res.setHeader("ETag", jsonObject.eTag());

        return String.format("Merge succeed, ID = %s\n Result: %s", jsonObject.id(), result);
    }

    @ResponseBody
    @PutMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String put(HttpServletRequest req,
               HttpServletResponse res,
               @RequestBody String content)
            throws IOException, ProcessingException {

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
        conn.delete(request.jsonObject().storageKey());
        String result = conn.set(jsonObject.flat().flatEntrySet());

        res.setHeader("ETag", jsonObject.eTag());
        return "Put object id: " + jsonObject.id();
    }

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Welcome to the home page";
    }

    public static void main(String[] args) {
        SpringApplication.run(AdvBigDataApplication.class, args);
    }
}
