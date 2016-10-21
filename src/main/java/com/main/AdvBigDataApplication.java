package com.main;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@SpringBootApplication
public class AdvBigDataApplication {

	private static RedisConnection conn = new RedisConnection();

	@GetMapping("/posts")
	@ResponseBody
	String getAllPosts() {
		String posts = conn.getAll();
		return posts;
	}

	@GetMapping("/posts/{postId}")
	@ResponseBody
	String getPost(@PathVariable String postId) {
		String post = conn.get(postId);
		return post;
	}

	@PostMapping("/posts/{postId}")
	@ResponseBody
	String createPost(@PathVariable String postId,
										@RequestBody String data) {
		String result = conn.create(postId, data);
		return "Post saved. ID: " + postId;
	}

	@DeleteMapping("/posts/{postId}")
	@ResponseBody
	String deletePost(@PathVariable String postId) {
		Long result = conn.delete(postId);
		return "return value is " + result;
	}

	@PatchMapping("/posts/{postId}")
	@ResponseBody
	String patchPost(@PathVariable String postId,
									 @RequestBody String content) {
		String result = null;
		try {
			result = conn.patch(postId, content);
		} catch (ParseException e) {
			return "There's a parsing error";
		}
		return result;
	}

	@PutMapping("/posts/{postId}")
	@ResponseBody
	String putPost(@PathVariable String postId,
								 @RequestBody String content) {
		String result = conn.put(postId, content);
		return result;
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
