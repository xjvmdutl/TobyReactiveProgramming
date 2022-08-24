package com.example.tobyreactiveprogramming.ch08;

import java.util.List;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Component
public class MyService {
  /*
  @Async //해당 어노테이션만 붙이는 순간 비동기 작업이 수행된다.
  public Future<String> hello() throws InterruptedException{ //비동기 작업은 Future로 결과를 받아야한다
    Thread.sleep(2000);
    log.info("hello()");
    return new AsyncResult<>( "Hello"); //비동기 결과값을 담아주는 클래스
  }
   */
  @Async(value = "threadPoolExecutor")
  public ListenableFuture<String> hello() throws InterruptedException{//스프링에서 만들어 놓은 ListenableFuture를 사용해야한다
    log.info("hello()");
    Thread.sleep(2000);
    return new AsyncResult<>( "Hello");
  }
}
