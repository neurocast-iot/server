package com.neurocast.framework.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 定时任务与异步任务配置。
 * 定时任务：流停推检查、文件清理等。
 * 异步任务：外部事件处理（避免阻塞 ThingsBoard 规则链回调）。
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {

    /**
     * 外部事件异步处理线程池。
     * 有界队列 + CallerRunsPolicy 实现天然限流，防止事件堆积导致 OOM。
     */
    @Bean("eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}
