package com.imt.api_invocations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ApiInvocationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiInvocationsApplication.class, args);
    }

}
