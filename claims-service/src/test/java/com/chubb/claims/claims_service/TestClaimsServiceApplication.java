package com.chubb.claims.claims_service;

import org.springframework.boot.SpringApplication;

public class TestClaimsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ClaimsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
