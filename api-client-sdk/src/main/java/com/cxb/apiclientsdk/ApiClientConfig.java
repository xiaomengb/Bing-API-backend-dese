package com.cxb.apiclientsdk;

import com.cxb.apiclientsdk.client.BingApiClient;
import com.cxb.apiclientsdk.client.DefaultAbstractApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * api 配置类
 *
 * @author cxb
 */
@Data
@Configuration
@ConfigurationProperties("bing-api.client")
@ComponentScan
public class ApiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public BingApiClient apiClient(){
        return new BingApiClient(accessKey,secretKey);
    }
}
