package br.dev.matheushenrique.floccus;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FloccusApplication {
	public static void main(String[] args) {
		SpringApplication.run(FloccusApplication.class, args);
	}
	@PostConstruct
	public void teste() {

	}
}
