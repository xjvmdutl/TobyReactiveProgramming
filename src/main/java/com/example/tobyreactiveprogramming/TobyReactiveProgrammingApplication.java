package com.example.tobyreactiveprogramming;

import com.example.tobyreactiveprogramming.ch08.MyService;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@EnableAsync //해당 어노테이션을 선언해야된다
@SpringBootApplication
public class TobyReactiveProgrammingApplication {

  public static void main(String[] args) {
    /*
    try (ConfigurableApplicationContext c = SpringApplication.run(
        TobyReactiveProgrammingApplication.class, args)) {
    }
     */
    SpringApplication.run(TobyReactiveProgrammingApplication.class, args);
  }

  //@Autowired MyService myService;

  /*
  @Bean
  public ApplicationRunner run() {
    //SpringBoot 가 뜨면서 바로 실행할 코드를 작성한다
    return args -> {
      log.info("run()");
      Future<String> future = myService.hello();
      log.info("exit: {}", future.isDone());
      log.info("result: {}", future.get());
    };
  }
   */
  /*
  @Bean
  public ApplicationRunner run() {
    //SpringBoot 가 뜨면서 바로 실행할 코드를 작성한다
    return args -> {
      log.info("run()");
      ListenableFuture<String> listenableFuture = myService.hello();
      listenableFuture.addCallback(s -> System.out.println(s),
          e -> System.out.println(e.getMessage()));
      log.info("exit");
    };
  }

  @Bean
  public ThreadPoolExecutor threadPoolExecutor() {
    //maxPoolSize는 Queue가 꽉 찾을때 동작한다
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100, 100,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2));
    return threadPoolExecutor;
  }
   */

}
