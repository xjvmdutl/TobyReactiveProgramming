package com.example.tobyreactiveprogramming.ch11;

import com.example.tobyreactiveprogramming.ch10.Chapter10Application;
import com.example.tobyreactiveprogramming.ch10.Chapter10Application.AcceptCompletion;
import com.example.tobyreactiveprogramming.ch10.Chapter10Application.ApplyCompletion;
import com.example.tobyreactiveprogramming.ch10.Chapter10Application.Completion;
import com.example.tobyreactiveprogramming.ch10.Chapter10Application.ErrorCompletion;
import com.example.tobyreactiveprogramming.ch10.Chapter10Application.MyService;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootApplication
public class Chapter11Application {

  @RestController
  public static class MyController {

    public static final String URL1 = "http://localhost:8081/service1?req={req}";
    public static final String URL2 = "http://localhost:8081/service2?req={req}";

    AsyncRestTemplate rt = new AsyncRestTemplate(
        new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    @Autowired
    MyService myService;

    @GetMapping("/rest")
    public DeferredResult<String> rest(int idx) {
      DeferredResult<String> dr = new DeferredResult<>();
      //해당 값이 반환하는 것이 반환하는것이 CompletableFuture 타입이 아니다.
      toCompletableFuture(rt.getForEntity(URL1, String.class, "h" + idx))
          .thenCompose(s -> {
           /* if(true)
              throw new RuntimeException();*/
            return toCompletableFuture(rt.getForEntity(URL2, String.class, s.getBody()));
          }) //2번쨰 비동기 결과
          .thenApplyAsync(s2 -> myService.work(s2.getBody())) //myService가 더이상 비동기가 아니므로 thenApply를 사용하면 된다
          //스레드를 비동기로 하는 Async 키워드를 붙이자
          .thenAccept(s3 -> dr.setResult(s3))
          .exceptionally(e -> {dr.setErrorResult(e.getMessage()); return (Void)null;} )
      ;
      return dr;
    }

    private <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
      CompletableFuture<T> completableFuture = new CompletableFuture<>();
      listenableFuture.addCallback(s -> {
        completableFuture.complete(s);
      }, e -> {
        completableFuture.completeExceptionally(e);
      });
      return completableFuture;
    }
  }

  @Service
  public static class MyService {
    //@Async //CompletableFuture를 사용하면 더이상 스프링을 비동기 어노테이션이 필요 없다
    public String work(String req) {
      return req + "/asyncwork";
    }
  }

  @Bean
  public ThreadPoolExecutor myThreadPool() {
    ThreadPoolExecutor te = new ThreadPoolExecutor(10, 100, 100,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2));
    return te;
  }

  public static void main(String[] args) {
    SpringApplication.run(Chapter11Application.class, args);
  }

}
