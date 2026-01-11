package org.umc.travlocksserver.domain.test.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.test.dto.TestRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TestController {

	@GetMapping
	public String test() {
		return "API documents";
	}

	@GetMapping("/header-test")
	public String test(
		@RequestHeader(name = "Custom-Header")
		String customHeader) {
		return "Header value is: " + customHeader;
	}

	@GetMapping(value = "/media-test", produces = "application/xml")
	public Map<String, String> mediaTest() {
		return Collections.singletonMap("result", "ok");
	}

	@PostMapping("/valid-test")
	public String test(
		@Valid @RequestBody
		TestRequest request) {
		return "success";
	}
}
