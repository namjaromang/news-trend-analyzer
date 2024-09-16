package co.news.insight.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // 동시에 실행할 스레드 수
    executor.setMaxPoolSize(20); // 최대 스레드 수
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("CategoryThread-");
    executor.initialize();
    return executor;
  }
}

