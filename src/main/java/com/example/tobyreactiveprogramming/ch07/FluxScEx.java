package com.example.tobyreactiveprogramming.ch07;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class FluxScEx {

  public static void main(String[] args) throws InterruptedException {
    /*
    Flux.range(1, 10)//1~10
        .publishOn(Schedulers.newSingle("pub")) //소비하는 쪽 속도가 느릴 경우 사용
        .log() //위쪽의 publisher 로 부터 받아오는 데이터를 볼 수 있다
        .subscribeOn(Schedulers.newSingle("sub"))
        .subscribe(System.out::println); //출력

    System.out.println("exit");
     */

    //Subscribe를 걸지 않았지만 자동으로 걸어주는 메서드가 있다.
    //대표적으로 interval
    Flux.interval(Duration.ofMillis(500))
        .take(10) //원하는 갯수만큼 데이터를 자르고 싶을 경우 해당 메서드를 사용하면 된다.
        .subscribe(s -> log.debug("onNext:{}", s)); //아무것도 실행이 되지 않는다.

    log.debug("exit");
    TimeUnit.SECONDS.sleep(10);

    /*
    //User가 만든 스레드는 메인스레드가 종료가 되어도 종료되지 않는다
    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        TimeUnit.SECONDS.sleep(2);
      }catch (InterruptedException e){

      }
      System.out.println("Hello");
    });
    System.out.println("exit");
     */
  }
}
