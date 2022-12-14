package com.example.tobyreactiveprogramming.ch09;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class LoadTest {

  private static AtomicInteger counter = new AtomicInteger(0);

  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
    ExecutorService es = Executors.newFixedThreadPool(100);
    RestTemplate rt = new RestTemplate();
    String url = "http://localhost:8080/rest?idx={idx}";//100개의 요청을 모아서 100개 생성후 한번에 돌리고 싶으면 어떻게 할까? 동기화
    CyclicBarrier barrier = new CyclicBarrier(101);
    for (int i = 0; i < 100; ++i) {
      //es.execute(() -> {
      es.submit(() -> {
        int idx = counter.addAndGet(
            1);//exception을 처리하는 코드가 람다식 안(Runnable)에 있다면, 메서드 밖으로 던질 수 없고 try-catch로 잡아야한다.
        barrier.await(); //await를 만난 숫자가 CyclicBarrier에 파라미터로 들어온 숫자로 되게 되면, 아래 코드가 실행이 된다.
        log.info("Thread {}", idx);
        StopWatch sw = new StopWatch();
        sw.start();
        String res = rt.getForObject(url, String.class, idx);
        sw.stop();
        log.info("Elapsed {} {} {}", idx, sw.getTotalTimeMillis(), res);
        return null;
      });
    }
    barrier.await();
    StopWatch main = new StopWatch();
    main.start();
    es.shutdown();
    es.awaitTermination(100, TimeUnit.SECONDS);
    main.stop();

    log.info("Total: {}", main.getTotalTimeSeconds());
  }
}

