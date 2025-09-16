
package com.tiora.mob;


import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@SpringBootApplication
@EntityScan(basePackages = "com.tiora.mob.entity")
@EnableJpaAuditing
	@EnableScheduling
public class TioraMobApiApplication {
	private static final Logger logger = LoggerFactory.getLogger(TioraMobApiApplication.class);

	public static void main(String[] args) {
		logger.info("Starting TioraMobApiApplication");
		SpringApplication.run(TioraMobApiApplication.class, args);
		logger.info("TioraMobApiApplication started successfully");
	}

}
