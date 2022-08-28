package com.example.tobyreactiveprogramming.ch13;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@SpringBootApplication
public class Chapter13Application {

  @RestController
  public static class MyController {

    /*
    @GetMapping("/")
    Mono<String> hello(){
      return Mono.just("Hello WebFlux")//new로 생성하면 안되고, static 패턴인 just로 생성해야한다.
          // Publisher -> (Publisher) -> (Publisher) -> Subscriber 이런 패턴으로 구현된 것이 Reactive 방식이다.
          .log()
          ;
    }
     */
    /*
    @GetMapping("/")
    Mono<String> hello(){
      log.info("pos1");
      Mono m = Mono.just("Hello WebFlux").doOnNext(c -> log.info(c)).log(); //이전에 동기식으로 동작한다면, 해당 코드 먼저 실행되야 하지만, log가 찍히고 해당 로그가 찍힌다.
      log.info("pos2");
      return m;
    }
     */
    /*
    @GetMapping("/")
    Mono<String> hello(){
      log.info("pos1");
      //Mono m = Mono.just("Hello WebFlux").doOnNext(c -> log.info(c)).log(); //이전에 동기식으로 동작한다면, 해당 코드 먼저 실행되야 하지만, log가 찍히고 해당 로그가 찍힌다.
      String msg = generateHello();
      Mono m = Mono.just(msg).doOnNext(c -> log.info(c)).log(); //pos1 -> method generateHello() -> pos2 실행후 비동기실행 로그가 출력된다.
      log.info("pos2");
      return m;
    }
     */
    /*
    @GetMapping("/")
    Mono<String> hello() {
      log.info("pos1");
      //fromSupplier = 파라미터는 없이 리턴값만 있다.
      Mono m = Mono.fromSupplier(() -> generateHello()).doOnNext(c -> log.info(c)).log();
      log.info("pos2");
      return m;
    }
     */
    /*
    @GetMapping("/")
    Mono<String> hello() {
      log.info("pos1");
      Mono m = Mono.fromSupplier(() -> generateHello()).doOnNext(c -> log.info(c)).log();
      m.subscribe();
      log.info("pos2");
      return m;
    }
     */

    @GetMapping("/")
    Mono<String> hello() {
      log.info("pos1");
      String msg = generateHello();
      Mono<String> m = Mono.just(msg).doOnNext(c -> log.info(c)).log();
      String msg2 = m.block(); //Mono 체인으로 동작시킬 것이 아닌 String 으로 받을 때 사용한다.
      log.info("pos2");
      //return m;
      return Mono.just(msg2);
    }

    private String generateHello() {
      log.info("method generateHello()");
      return "Hello Mono";
    }
  }

  public static void main(String[] args) {
    //Tomcat을 기본으로 사용하는 것이 아닌, netty가 실행이된다.
    //비동기 non-blocking 에 사용되는 서버인 netty가 기본적으로 실행되는 것을 확인 할 수 있다.
    SpringApplication.run(Chapter13Application.class, args);
  }
}
