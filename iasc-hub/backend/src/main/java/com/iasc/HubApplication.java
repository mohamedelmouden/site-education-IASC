package com.iasc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication @EnableAsync
public class HubApplication { public static void main(String[] a){ SpringApplication.run(HubApplication.class,a);} }
