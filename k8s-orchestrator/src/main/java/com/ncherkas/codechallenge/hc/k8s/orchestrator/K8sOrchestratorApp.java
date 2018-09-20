package com.ncherkas.codechallenge.hc.k8s.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@SpringBootApplication
public class K8sOrchestratorApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(K8sOrchestratorApp.class);

	public static void main(String[] args) {
		LOGGER.info("Starting K8S Orchestrator Application...");
		SpringApplication.run(K8sOrchestratorApp.class, args);
	}
}
