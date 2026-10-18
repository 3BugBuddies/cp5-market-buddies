package br.com.fiap.marketbuddies.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MarketbuddiesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketbuddiesApiApplication.class, args);
	}

}
