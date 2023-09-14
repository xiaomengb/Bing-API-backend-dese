package com.cxb.backend;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author cxb
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.cxb.backend.mapper")
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class ApiBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiBackendApplication.class,args);
    }
}