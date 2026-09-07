package com.neurocast.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * neurocast-server 启动类
 */
@SpringBootApplication(scanBasePackages = "com.neurocast")
@MapperScan("com.neurocast.**.mapper")
public class NeurocastApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeurocastApplication.class, args);
    }
}
