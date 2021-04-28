package com.as.light;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class CoreApplication extends SpringBootServletInitializer
{
    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }
}