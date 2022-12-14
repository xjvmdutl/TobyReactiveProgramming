package com.example.tobyreactiveprogramming.ch09;

import com.example.tobyreactiveprogramming.TobyReactiveProgrammingApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class RemoteService {

  @RestController
  public static class MyController {

    @GetMapping("/service1")
    public String service1(String req) throws InterruptedException {
      Thread.sleep(2000);
      return req + "/service1";
    }

    @GetMapping("/service2")
    public String service2(String req) throws InterruptedException {
      Thread.sleep(2000);
      return req + "/service2";
    }

  }

  public static void main(String[] args) {
    //SpringBoot를 띄울때 Properties 값을 동적으로 설정해서 띄울수 있다.
    System.setProperty("server.port", "8081");
    System.setProperty("server.tomcat.threads.max", "1000");
    System.setProperty("spring.task.execution.pool.core-size", "1000");
    SpringApplication.run(RemoteService.class, args);
  }
}
