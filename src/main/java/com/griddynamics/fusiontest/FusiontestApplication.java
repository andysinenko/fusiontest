package com.griddynamics.fusiontest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(scanBasePackages = {"io.seldon.wrapper", "com.griddynamics.fusiontest.model"})
@EnableAsync
@Import({ io.seldon.wrapper.config.AppConfig.class })
public class FusiontestApplication {
	public static void main(String[] args) {
		SpringApplication.run(FusiontestApplication.class, args);
	}

}
