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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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
	@GetMapping({"/{index}/{type}/{id}", "/{index}/{type}", "/{index}/{type}/_schema"})
	String get(HttpServletRequest req, HttpServletResponse res)
			throws UnsupportedEncodingException {
		RestRequest request = new RestRequest(req, "");

		String path = req.getRequestURI();
		String last = path.substring(path.lastIndexOf("/") + 1);

		Map<String, Object> data;
		if (last.equals(request.param("_type")) ||
				last.equals("/")) {
			String pattern = request.param("_index") + "." +
											 request.param("_type") + ".*";
			return conn.getAllAndExclude(pattern, "_schema");
		} else {
			data = conn.getMap(request.key());
		}

		if (data == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return "Data not exists: " + req.getRequestURI();
		}

		String key = (String)data.get("_uri") + data.get("_version");
		String eTag = RestUtils.generateETag(key);
		res.setHeader("ETag", eTag);
		if (eTag.equals(request.eTag())) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return "";
		} else {
			data.put("properties", Json.restore((Map)data.get("properties")));
			return Json.serialize(data);
		}
	}

	// TODO: If key exists not create a new one, ask user use other methods
	// or delete this one first.
	@ResponseBody
	@PostMapping({"/{index}/{type}/{id}", "/{index}/{type}", "/{index}/{type}/_schema"})
	String create(HttpServletRequest req,
								HttpServletResponse res,
								@RequestBody String content)
			throws IOException, ProcessingException {

		StringBuilder msgBuilder = new StringBuilder();
		RestRequest request = new RestRequest(req, content);

		if (request.target() == RestRequest.OP_TARGET.DATA) {
			Map<String, Object> schemaMap = conn.getMap(request.schemaKey());
			ValidateResult validateResult =
					JsonValidator.validate(
							Json.restoreThenSerialize((Map)schemaMap.get("properties")),
							request.rawContent());

			if (!validateResult.success()) {
				msgBuilder.append("Error in validation: \n")
									.append(validateResult.message());
				return msgBuilder.toString();
			}
		}

		String result = conn.create(request.key(), request.value());
		msgBuilder.append("Data saved: ID = ")
				.append(request.param("_id"))
				.append("\n").append(result);

		String eTag = RestUtils.generateETag(request.key() + request.eTag());
		res.setHeader("ETag", eTag);
		return msgBuilder.toString();
	}

	@ResponseBody
	@DeleteMapping({"/{index}/{type}/{id}", "/{index}/{type}/_schema"})
	String delete(HttpServletRequest req) {
		RestRequest request = new RestRequest(req, "");
		Long result = conn.delete(request.key());
		return "Delete opertaion result: " + result;
	}

	@ResponseBody
	@PatchMapping({"/{index}/{type}/{id}", "/{index}/{type}/_schema"})
	String patch(HttpServletRequest req,
							 HttpServletResponse res,
							 @RequestBody String content)
			throws IOException, ProcessingException {

		RestRequest request = new RestRequest(req, content);
		Map<String, Object> newAttrMap = request.flattenContent();

		String data = conn.get(request.key());
		Map<String, Object> dataMap = Json.deserialize(data);

		Map<String, Object> mergeResult = new HashMap<>();
		mergeResult.putAll((Map)dataMap.get("properties"));
		mergeResult.putAll(newAttrMap);
		String mergeJson = Json.serialize(mergeResult);

		StringBuilder msgBuilder = new StringBuilder();
		if (request.target() == RestRequest.OP_TARGET.DATA) {
			Map<String, Object> schemaMap = conn.getMap(request.schemaKey());
			ValidateResult validateResult = JsonValidator.validate(
					Json.restoreThenSerialize((Map)schemaMap.get("properties")),
					mergeJson);
			if (!validateResult.success()) {
				msgBuilder.append("Error in schema validation: \n")
									.append(validateResult.message());
			}
		}
		dataMap.put("properties", mergeResult);
		incrementVersion(dataMap);

		String result = conn.put(request.key(), Json.serialize(dataMap));
		msgBuilder.append("Data MERGE: ID = ").append(request.param("_id"))
							.append("\nResult:\n").append(result);

		String eTag = RestUtils.generateETag(request.key() + request.eTag());
		res.setHeader("ETag", eTag);

		return msgBuilder.toString();
	}

	@ResponseBody
	@PutMapping({"/{index}/{type}/{id}", "/{index}/{type}/_schema"})
	String put(HttpServletRequest req,
						 HttpServletResponse res,
						 @RequestBody String content)
			throws IOException, ProcessingException {

		RestRequest request = new RestRequest(req, content);
		StringBuilder msgBuilder = new StringBuilder();
		Map<String, Object> schemaMap = conn.getMap(request.schemaKey());

		ValidateResult validateResult = JsonValidator.validate(
				Json.restoreThenSerialize((Map)schemaMap.get("properties")),
				request.rawContent());
		if (!validateResult.success()) {
			msgBuilder.append("Error in schema validation: \n")
								.append(validateResult.message());
		}

		String data = conn.get(request.key());
		Map<String, Object> dataMap = Json.deserialize(data);
		dataMap.put("properties", request.content());
		incrementVersion(dataMap);
		String result = conn.put(request.key(), Json.serialize(dataMap));

		String eTag = RestUtils.generateETag(request.key() + request.eTag());
		res.setHeader("ETag", eTag);
		msgBuilder.append("Data PUT: ID = ").append(request.param("_id"))
				.append("\nResult:\n").append(result);
		return msgBuilder.toString();
	}

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Welcome to the home page";
	}

	private void incrementVersion(Map<String, Object> map) {
		Object v = map.get("_version");
		Objects.requireNonNull(v);
		double version;
		if (v instanceof String) {
			version = Double.parseDouble((String) v);
		} else {
			version = (double) v;
		}
		map.put("_version", version + 1);
	}

	public static void main(String[] args) {
		SpringApplication.run(AdvBigDataApplication.class, args);
	}
}
