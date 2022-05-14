package net.funkenburg.gc.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class Config {
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    ExecutorService executorService() {
        BlockingQueue<Runnable> threadQueue = new ArrayBlockingQueue<Runnable>(1);
        return new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, threadQueue);
    }
}
