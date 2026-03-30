package com.example.ecommerce_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class EcommerceAppApplication {
	private final DataSource dataSource;

	public EcommerceAppApplication(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static void main(String[] args) {
		SpringApplication.run(EcommerceAppApplication.class, args);
	}

	@PostConstruct
	public void testDatabaseConnection() {
		try (Connection connection = dataSource.getConnection()) {
			System.out.println("Database connected successfully!");
			System.out.println("DB URL: " + connection.getMetaData().getURL());
		} catch (Exception e) {
			System.out.println("Database connection FAILED!");
			e.printStackTrace();
		}
	}

}
