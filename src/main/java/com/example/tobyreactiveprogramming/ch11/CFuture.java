package com.example.tobyreactiveprogramming.ch11;

import com.example.tobyreactiveprogramming.ch10.Chapter10Application.Completion;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CFuture {
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    /*
    //CompletableFuture<Integer> cf = CompletableFuture.completedFuture(1); //비동기 작업을 간단하게 완료하는 작업을 할 수 있다.
    CompletableFuture<Integer> cf = new CompletableFuture<>(); //값을 넘기지 않았기 때문에 무한정 대기
    //cf.complete(2);//값을 넣어준다
    cf.completeExceptionally(new RuntimeException()); //예외가 발생했다는것을 알려준다
    System.out.println(cf.get());
     */
    //내부적으로 Future, CompletionStage 인터페이스를 구현하고 있다.
    //  (CompletionStage = 하나의 작업을 비동기 작업으로 수행하고, 완료가 됫을때, 해당 작업에 의존적인 메서드를 실행시켜줄 수 있는 클래스)
    /*
    CompletableFuture
        .runAsync(() -> log.info("runAsync"))//파라미터로 Runnable을 받는다.
        .thenRun(() -> log.info("thenRun"))//비동기 작업이 완료되었으면, 해당 백그라운드에서 이 람다를 실행해라(동기적 실행)
        .thenRun(() -> log.info("thenRun"));
    log.info("exit");
     */
    //비동기 결과를 다음 비동기 작업에 전달할 수 있다
    /*
    CompletableFuture
        .supplyAsync(() ->{
          log.info("runAsync");
          return 1;
        }) //리턴값이 있어야 한다
        .thenApply(s -> {
          log.info("thenApply {} ", s);
          return s + 1;
        })//앞에 비동기 작업을 결과를 받아 사용할 수 있다.
        .thenApply(s2 -> {
          log.info("thenApply {} ", s2);
          return s2 * 3;
        })
        .thenAccept(s3 -> log.info("thenAccept {}", s3));
    log.info("exit");
    ForkJoinPool.commonPool().shutdown();
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
     */
    /*
    CompletableFuture
        .supplyAsync(() ->{
          log.info("runAsync");
          return 1;
        })
        .thenCompose(s -> { //flatMap 에 해당하는 부분으로 보면 된다.
          log.info("thenApply {} ", s);
          //비동기 작업으로 인해 타입이 CompletableFuture 타입으로 변경이된다.
          return CompletableFuture.completedFuture(s + 1);//function을 결과 값이 또 다른 Completable 결과로 나와야 할 경우가 있다.
        })
        .thenApply(s2 -> { //map에 해당
          log.info("thenApply {} ", s2);
          return s2 * 3;
        })
        .thenAccept(s3 -> log.info("thenAccept {}", s3));
    log.info("exit");
    ForkJoinPool.commonPool().shutdown();
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
     */
    /*
    CompletableFuture
        .supplyAsync(() ->{
          log.info("runAsync");
          if(true)
            throw new RuntimeException();
          return 1;
        })
        .thenCompose(s -> {
          log.info("thenApply {} ", s);
          return CompletableFuture.completedFuture(s + 1);
        })
        .thenApply(s2 -> {
          log.info("thenApply {} ", s2);
          return s2 * 3;
        })
        .exceptionally(e -> -10) //예외를 잡아 복구하는 기능을 적용할 수 있다
        .thenAccept(s3 -> log.info("thenAccept {}", s3));
    log.info("exit");
    ForkJoinPool.commonPool().shutdown();
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
     */
    /*
    CompletableFuture
        .supplyAsync(() ->{
          log.info("runAsync");
          if(true)
            throw new RuntimeException();
          return 1;
        })
        .thenCompose(s -> {
          log.info("thenApply {} ", s);
          return CompletableFuture.completedFuture(s + 1);
        })
        .thenApply(s2 -> {
          log.info("thenApply {} ", s2);
          return s2 * 3;
        })
        .exceptionally(e -> -10)
        .thenAccept(s3 -> log.info("thenAccept {}", s3));
    log.info("exit");
    ForkJoinPool.commonPool().shutdown();
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
     */
    //현재는 하나의 스레드를 통해 모든 비동기 작업을 하지만, 만약 비동기 작업을 다른 스레드에서 실행하고 싶다면 어떻게 할까?
    ExecutorService es = Executors.newFixedThreadPool(10);

    CompletableFuture
        .supplyAsync(() ->{
          log.info("runAsync");
          return 1;
        }, es)
        .thenCompose(s -> {
          log.info("thenApply {} ", s);
          return CompletableFuture.completedFuture(s + 1);
        })
        .thenApplyAsync(s2 -> { //현재 스레드 풀 정책에 따라 새로운 스레드를 할당한다.
          log.info("thenApply {} ", s2);
          return s2 * 3;
        }, es)
        .exceptionally(e -> -10)
        .thenAcceptAsync(s3 -> log.info("thenAccept {}", s3), es);
    log.info("exit");
    ForkJoinPool.commonPool().shutdown();
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
  }
}
