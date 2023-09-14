package com.cxb.gateway.config;

import com.cxb.gateway.filter.AccessControllerFilter;
import com.cxb.gateway.filter.AuthenticationFilter;
import com.cxb.gateway.filter.RequestLogFilter;
import com.cxb.gateway.filter.ResponseLogFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           AccessControllerFilter accessControllerFilter,
                                           AuthenticationFilter authenticationFilter,
                                           RequestLogFilter requestLogFilter,
                                           ResponseLogFilter responseLogFilter
                                           ) {
        //调用接口经过InterfaceGatewayFilter执行
        return builder.routes()
                .route(r ->
                        r.path("/api/interface/**")
                        .filters(f -> f.filters(
                                accessControllerFilter,
                                authenticationFilter,
                                requestLogFilter,
                                responseLogFilter))
                                .uri("lb://api-interface")
                )
                .build();
    }


}
