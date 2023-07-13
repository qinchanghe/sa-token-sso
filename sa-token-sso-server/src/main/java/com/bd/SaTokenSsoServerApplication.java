package com.bd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaTokenSsoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaTokenSsoServerApplication.class, args);
        System.out.println("\n------ Sa-Token-SSO 认证中心启动成功");
    }

}
