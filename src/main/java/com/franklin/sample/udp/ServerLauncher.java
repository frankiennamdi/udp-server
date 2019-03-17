package com.franklin.sample.udp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@ComponentScan("com.franklin.sample.udp")
public class ServerLauncher {

  private ConfigurableApplicationContext applicationContext;

  public static void main(String[] args) {
    new ServerLauncher().start(args);
  }

  public void start(String[] args) {
    SpringApplication app = new SpringApplication(ServerLauncher.class);
    app.setBannerMode(Banner.Mode.OFF);
    applicationContext = app.run(args);
  }

  public void stop() {
    applicationContext.close();
  }

  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(4);
    threadPoolTaskExecutor.setMaxPoolSize(10);
    threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
    threadPoolTaskExecutor.setQueueCapacity(10000);
    threadPoolTaskExecutor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("server-worker-thread-%d").build());
    threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    threadPoolTaskExecutor.initialize();
    return threadPoolTaskExecutor;
  }
}
