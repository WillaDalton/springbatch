package com.willadalton.springbatch;

import com.willadalton.batchrunner.BatchRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.willadalton.batchrunner.config.BatchRunnerAutoConfiguration;

@SpringBootApplication
@Import(BatchRunnerAutoConfiguration.class)
public class SpringbatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbatchApplication.class, args);
    }
}
