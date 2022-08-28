package com.example.tobyreactiveprogramming.ch12;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.ClientResponseWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Slf4j
@EnableAsync
@SpringBootApplication
public class Chapter12Application {

  @RestController
  public static class MyController {

    public static final String URL1 = "http://localhost:8081/service1?req={req}";
    public static final String URL2 = "http://localhost:8081/service2?req={req}";

    @Autowired
    private MyService myService;

    WebClient client = WebClient.create(); //AsyncRestTemplate 와 비슷하다고 생각하면 된다.

    @GetMapping("/rest")
    public Mono<String> rest(int idx) {
      //Mono를 사용하면, 어떤 오브젝트든 받아서 반환할 수 있다.
      //Mono를 컨테이너(컬렉션)이라고, 생각하면 된다 -> 컨테이너 = Optional, List 등과 같이 컨테이너로 데이터를 감싸면 여러 기능을 쓸 수 있다.
      //Mono<String> m = Mono.just("Hello");//일반 String 메서드와 달리 Mono로 감싸게 되면, 많은 기능을 쓸 수 있는것을 볼 수 있다
      //return Mono.just("Hello");

      /*
      Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange(); //URL1에 해당하는 API를 호출한 코드
      //ClientResponse에 header,status,body 정보가 있다
      //정의하는 것만으로는 API가 호출되지 않는다.
      //res.subscribe(); //이 동작을 해야지만, 실제 API를 호출한다. //이 동작은 스프링이 해준다(리턴 타입이 Mono일 경우)
      //Mono 안에 ClientResponse 안에 감싸 있기 때문에 이를 바꿔서 변경해 주어야한다.
      Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
      //map은 원소를 변환후, 담겨져 있던 곳에 다시 담고, flatMap 같은 경우 다시 담아 주지 않는다.
      return body;
      return client.get().uri(URL1, idx).exchange().flatMap(c -> c.bodyToMono(String.class));
       */
      /*
      return client.get().uri(URL1, idx).exchange() //Mono<ClientResponse>
          .flatMap(c -> c.bodyToMono(String.class)) //Mono<String>
          .flatMap(res1 -> client.get().uri(URL2, res1).exchange()) //Mono<ClientResponse>
          .flatMap(c -> c.bodyToMono(String.class)) //Mono<String>
        ;
       */
      /*
      return client.get().uri(URL1, idx).exchange()
          .flatMap(c -> c.bodyToMono(String.class))
          .flatMap(res1 -> client.get().uri(URL2, res1).exchange())
          .flatMap(c -> c.bodyToMono(String.class))
          //.map( res2 -> myService.work(res2)) //퍄랴미터도 Mono<String> 반환도 Mono<String>이라 map을 써도 된다.
          //만약 myService.work 가 오래걸리는 서비스이다? -> 동기적으로 실행시키게 되므로, 스레드가 blocking 된다.
          .flatMap( res2 -> Mono.fromCompletionStage(myService.work(res2))) //CompletableFuture<String> -> Mono<String>
          ;
       */
      return client.get().uri(URL1, idx).exchange()
          .flatMap(c -> c.bodyToMono(String.class))
          .doOnNext(c -> log.info("log1 : {}",c)) //Publisher 에서 넘어올때, doOnNext로 로그를 찍을 수 있다.
          .flatMap(res1 -> client.get().uri(URL2, res1).exchange())
          .flatMap(c -> c.bodyToMono(String.class))
          .doOnNext(c -> log.info("log2 : {}",c))
          .flatMap( res2 -> Mono.fromCompletionStage(myService.work(res2)))
          .doOnNext(c -> log.info("log3 : {}",c))
          ;
    }
  }

  @Service
  public static class MyService {
    @Async
    public CompletableFuture<String> work(String req) {
      return CompletableFuture.completedFuture(req + "/asyncwork");
    }
  }

  @Bean
  public ThreadPoolExecutor myThreadPool() {
    ThreadPoolExecutor te = new ThreadPoolExecutor(10, 100, 100,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2));
    return te;
  }

  public static void main(String[] args) {
    System.setProperty("reactor.ipc.netty.workerCount", "1");
    System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
    SpringApplication.run(Chapter12Application.class, args);
  }

}
