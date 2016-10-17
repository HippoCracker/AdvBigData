package com.main;

import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@SpringBootApplication
public class AdvBigDataApplication {

	@GetMapping("/posts")
	@ResponseBody
	JSONObject getAllPosts() {
		return new JSONObject();
	}

	@GetMapping("/posts/{postId}")
	@ResponseBody
	JSONObject getPost(@PathVariable String postId) {
		return null;
	}

	@PostMapping("/posts/{postId}")
	@ResponseBody
	String createPost(@PathVariable String postId) {
		return "Your post request id: " + postId;
	}

	@DeleteMapping("/posts/{postId}")
	@ResponseBody
	String deletePost(@PathVariable String postId) {
		return null;
	}

	@PatchMapping("/posts/{postId}")
	@ResponseBody
	String patchPost(@PathVariable String postId) {
		return null;
	}

	@PutMapping("/posts/{postId}")
	@ResponseBody
	String putPost(@PathVariable String postId) {
		return null;
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
