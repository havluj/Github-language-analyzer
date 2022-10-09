package com.havluj.github.languageanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LanguageAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LanguageAnalyzerApplication.class, args);
	}

}
