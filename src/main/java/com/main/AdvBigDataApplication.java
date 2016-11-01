package com.main;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


// TODO: Integer save to redis as *number* which will cause validation fail
// TODO: validate whether schema is valid schema
// TODO: Auto-generate schema base on user data, if there's not schema at all.
// TODO: E-Tag
// TODO: Authorization, JWT: validate token

@Controller
@SpringBootApplication
public class AdvBigDataApplication {

	private static RedisConnection conn = new RedisConnection();

	@ResponseBody
	@GetMapping({"/{index}/{type}/{id}", "/{index}/{type}", "/{index}/{type}/_schema"})
	String get(HttpServletRequest r) {
		RestRequest request = new RestRequest(r, "");
		String path = r.getRequestURI();
		String last = path.substring(path.lastIndexOf("/") + 1);
		if (last.equals(request.param("_type"))) {
			String pattern = request.param("_index") + "." +
											 request.param("_type") + ".*";
			return conn.getAllAndExclude(pattern, "_schema");
		} else {
			return conn.get(request.key());
		}
	}

	// TODO: If key exists not create a new one, ask user use other methods
	// or delete this one first.
	@ResponseBody
	@PostMapping({"/{index}/{type}/{id}", "/{index}/{type}", "/{index}/{type}/_schema"})
	String create(HttpServletRequest r, @RequestBody String content)
			throws IOException, ProcessingException {
		StringBuilder msgBuilder = new StringBuilder();

		RestRequest request = new RestRequest(r, content);
		if (request.target() == RestRequest.OP_TARGET.DATA) {
			String schema = conn.get(request.schemaKey());
			ValidateResult validateResult =
					JsonValidator.validate(schema, request.rawContent());

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

		return msgBuilder.toString();
	}

	@ResponseBody
	@DeleteMapping({"/{index}/{type}/{id}", "/{index}/{type}/_schema"})
	String delete(HttpServletRequest r) {
		RestRequest request = new RestRequest(r, "");
		Long result = conn.delete(request.key());
		return "Delete opertaion result: " + result;
	}

	@ResponseBody
	@PatchMapping({"/{index}/{type}/{id}", "/{index}/{type}/_schema"})
	String patch(HttpServletRequest r, @RequestBody String content)
			throws IOException, ProcessingException {

		RestRequest request = new RestRequest(r, content);
		Map<String, Object> newAttrMap = request.flattenContent();

		String data = conn.get(request.key());
		Map<String, Object> dataMap = Json.deserialize(data);

		Map<String, Object> mergeResult = new HashMap<>();
		mergeResult.putAll((Map)dataMap.get("properties"));
		mergeResult.putAll(newAttrMap);
		String mergeJson = Json.serialize(mergeResult);

		StringBuilder msgBuilder = new StringBuilder();
		if (request.target() == RestRequest.OP_TARGET.DATA) {
			String schema = conn.get(request.schemaKey());
			ValidateResult validateResult =
					JsonValidator.validate(schema, mergeJson);
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
		return msgBuilder.toString();
	}

	@ResponseBody
	@PutMapping({"/{index}/{type}/{id}", "/{index}/{type}/_schema"})
	String put(HttpServletRequest r, @RequestBody String content)
			throws IOException, ProcessingException {
		RestRequest request = new RestRequest(r, content);
		StringBuilder msgBuilder = new StringBuilder();
		String schema = conn.get(request.schemaKey());
		ValidateResult validateResult =
				JsonValidator.validate(schema, request.rawContent());
		if (!validateResult.success()) {
			msgBuilder.append("Error in schema validation: \n")
								.append(validateResult.message());
		}

		String data = conn.get(request.key());
		Map<String, Object> dataMap = Json.deserialize(data);
		dataMap.put("properties", request.content());
		incrementVersion(dataMap);
		String result = conn.put(request.key(), Json.serialize(dataMap));
		msgBuilder.append("Data PUT: ID = ").append(request.param("_id"))
				.append("\nResult:\n").append(result);
		return msgBuilder.toString();
	}

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Welcome to the home page";
	}

	public static void main(String[] args) {
		SpringApplication.run(AdvBigDataApplication.class, args);
	}

	private void incrementVersion(Map<String, Object> map) {
		Object v = map.get("_version");
		double version;
		if (v instanceof String) {
			version = Double.parseDouble((String) v);
		} else {
			version = (double) v;
		}
		map.put("_version", version + 1);
	}
}
