package com.main;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.main.context.JsonContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static com.main.common.Utils.SCHEMA;
import static com.main.common.Utils.STORAGE_KEY;


// TODO: Integer save to redis as *number* which will cause validation fail
// TODO: validate whether schema is valid schema
// TODO: Auto-generate schema base on user data, if there's not schema at all.
// TODO: Authorization, JWT: validate token
// TODO: Json.restoreMap methods not work, not restore flat list back.

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

        if (flatJson == null) {
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

    // TODO: If key exists not create a new one, ask user use other methods
    // or delete this one first.
    @ResponseBody
    @PostMapping({"/{type}/{name}/{id}", "/{type}/{name}", "/{type}/{name}/_schema"})
    String create(HttpServletRequest req,
                  HttpServletResponse res,
                  @RequestBody String content)
            throws IOException, ProcessingException {

        RestRequest request = new RestRequest(req, content);
//
//        if (request.target() == RestRequest.OP_TARGET.DATA) {
//            Map<String, Object> schemaMap = conn.getMap(request.schemaKey());
//            ValidateResult validateResult =
//                    JsonValidator.validate(
//                            Json.restoreThenSerialize((Map) schemaMap.get("properties")),
//                            request.rawContent());
//
//            if (!validateResult.success()) {
//                msgBuilder.append("Error in validation: \n")
//                        .append(validateResult.message());
//                return msgBuilder.toString();
//            }
//        }

        String result = conn.set(request.jsonObject().flat().flatEntrySet());

        res.setHeader("ETag", request.jsonObject().eTag());
        return "Object saved, id: " + request.jsonObject().id();
    }

    @ResponseBody
    @DeleteMapping({"/{type}/{name}/{id}", "/{type}/{name}/_schema"})
    String delete(HttpServletRequest req) {
        RestRequest request = new RestRequest(req, "");
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

//        if (request.target() == RestRequest.OP_TARGET.DATA) {
//            Map<String, Object> schemaMap = conn.getMap(request.schemaKey());
//            ValidateResult validateResult = JsonValidator.validate(
//                    Json.restoreThenSerialize((Map) schemaMap.get("properties")),
//                    mergeJson);
//            if (!validateResult.success()) {
//                msgBuilder.append("Error in schema validation: \n")
//                        .append(validateResult.message());
//            }
//        }

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
        Json jsonObject = request.jsonObject();

        Json shema = conn.get(request.jsonObject().schemaKey());

//        ValidateResult validateResult = JsonValidator.validate(
//                Json.restoreThenSerialize((Map) schemaMap.get("properties")),
//                request.rawContent());
//        if (!validateResult.success()) {
//            msgBuilder.append("Error in schema validation: \n")
//                    .append(validateResult.message());
//        }
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
