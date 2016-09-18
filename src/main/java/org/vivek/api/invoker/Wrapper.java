package org.vivek.api.invoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * Created by vivek on 16/09/16.
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableCircuitBreaker
@EnableHystrixDashboard
public class Wrapper {

    public static void main(String[] args) {
        SpringApplication.run(Wrapper.class, args);
    }
}
