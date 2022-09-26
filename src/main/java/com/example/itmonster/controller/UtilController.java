package com.example.itmonster.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UtilController {
	@Value("${spring.server-port.profile}")
	String profile;


	//무중단 배포 및 ELB 동작상태 확인
	@GetMapping("/health")
	public String healthy() {
		return "healthy";
	}

	//서버 프로파일 확인
	@GetMapping("/profile")
	public String getProfile() {
		return profile;
	}
}
