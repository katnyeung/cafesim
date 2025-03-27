package com.cafesim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Main {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
    }
}