package com.nazarkoldun.motionstudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MotionStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotionStudioApplication.class, args);
    }
}
