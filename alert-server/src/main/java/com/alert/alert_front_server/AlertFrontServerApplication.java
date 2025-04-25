package com.alert.alert_front_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AlertFrontServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertFrontServerApplication.class, args);
	}

}
