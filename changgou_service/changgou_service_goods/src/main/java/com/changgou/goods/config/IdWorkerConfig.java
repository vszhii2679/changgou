package com.changgou.goods.config;

import com.changgou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdWorkerConfig {

    @Value("${idWorker.workerId}")
    private int workerID;
    @Value("${idWorker.datacenterId}")
    private int datacenterId;

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(workerID, datacenterId);
    }
}
