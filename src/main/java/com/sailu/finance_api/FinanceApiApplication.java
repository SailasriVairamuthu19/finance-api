package com.sailu.finance_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class FinanceApiApplication {

		public static void main(String[] args) {
			SpringApplication.run(FinanceApiApplication.class, args);
		}

}
