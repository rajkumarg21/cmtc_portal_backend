package com.cmtc.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CmtcPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmtcPortalApplication.class, args);
	}
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Add this bean for RestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
