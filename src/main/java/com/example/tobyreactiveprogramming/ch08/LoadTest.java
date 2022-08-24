package com.example.tobyreactiveprogramming.ch08;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class LoadTest {

  private static AtomicInteger counter = new AtomicInteger(0); //다른 스레드 동시에 접근X

  public static void main(String[] args) throws InterruptedException {
    ExecutorService es = Executors.newFixedThreadPool(100); //스레드 100개 할당

    RestTemplate rt = new RestTemplate();
    String url = "http://localhost:8080/dr";

    StopWatch main = new StopWatch(); //매인 스레드가 실행되는 시간을 찍어보자
    main.start();
    for (int i = 0; i < 100; ++i) {
      es.execute(() -> {
        int idx = counter.addAndGet(1);
        log.info("Thread {}", idx );

        StopWatch sw = new StopWatch();
        sw.start();

        rt.getForObject(url, String.class);

        sw.stop();
        log.info("Elapsed {} {}", idx, sw.getTotalTimeMillis());
      });
    }

    es.shutdown();
    es.awaitTermination(100, TimeUnit.SECONDS);//100초 안에 안끝나면 강제 종료

    main.stop();
    log.info("Total: {}", main.getTotalTimeSeconds());
  }
}
