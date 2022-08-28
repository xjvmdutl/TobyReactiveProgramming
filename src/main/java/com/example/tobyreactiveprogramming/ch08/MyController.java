package com.example.tobyreactiveprogramming.ch08;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Slf4j
@RestController
public class MyController {

  @Autowired
  private MyService myService;

  Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

  @GetMapping("/callable")
  /*
  public Callable<String> callable() throws InterruptedException {
    log.info("callable");
    return () -> {
      log.info("async");
      Thread.sleep(2000); //이 작업을 수행하는 동안 2초간 Blocking 이 된다
      return "Hello";
    };
  }
   */
  public String callable() throws InterruptedException {
    log.info("async");
    Thread.sleep(2000); //이 작업을 수행하는 동안 2초간 Blocking 이 된다
    return "Hello";
  }

  @GetMapping("/dr")
  public DeferredResult<String> dr() throws InterruptedException{
    log.info("dr");
    DeferredResult<String> dr = new DeferredResult<>(60000L);
    results.add(dr);
    return dr;
  }

  @GetMapping("/dr/count")
  public String drCount(){
    return String.valueOf(results.size());
  }

  @GetMapping("/dr/event")
  public String drEvent(String msg){
    for(DeferredResult<String> dr : results){
      dr.setResult("Hello " + msg);
      results.remove(dr);
    }
    return "OK";
  }
  /*
  @GetMapping("/emitter")
  public ResponseBodyEmitter emmiter(){
    //데이터를 여러번에 나눠서 보낼수 있다.
    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    Executors.newSingleThreadExecutor().submit(() -> {
        try {
          for(int i=1; i<=50; ++i) {
            emitter.send("<p>Stream " + i + "</p>");
            Thread.sleep(2000);
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
    });
    return emitter;
  }
   */

}
