package com.cxb.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDubbo
@EnableTransactionManagement
@EnableDiscoveryClient
public class ApiOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiOrderApplication.class, args);
    }

}
