package com.codingtext.codebankservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodebankserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodebankserviceApplication.class, args);
	}

}
