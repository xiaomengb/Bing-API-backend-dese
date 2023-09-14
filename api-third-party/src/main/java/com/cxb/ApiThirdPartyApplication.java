package com.cxb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author cxb
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
public class ApiThirdPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiThirdPartyApplication.class, args);
    }
}