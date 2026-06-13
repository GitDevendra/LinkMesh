package com.linkmesh.config;

import com.linkmesh.util.SnowflakeIdGenerator;
import com.linkmesh.util.WorkerIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(WorkerIdProvider workerIdProvider) {
        return new SnowflakeIdGenerator(workerIdProvider.getWorkerId());
    }
}