package com.colin.bussiness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.colin.bussiness.feign"})
//@EnableTransactionManagement
public class BussinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(BussinessApplication.class, args);
    }

}
