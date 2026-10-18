package br.com.fiap.marketbuddies.worker;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class MarketbuddiesWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketbuddiesWorkerApplication.class, args);
	}

}
