package com.example.tobyreactiveprogramming.ch08;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureEx {

  interface SuccessCallback { //성공 콜백

    void onSuccess(String result);
  }

  interface ExceptionCallback { //비동기 작업을 하다 예외가 발생시 해당 인터페이스를 받게끔 한다.

    void onError(Throwable t);
  }

  public static class CallbackFutureTask extends FutureTask<String> {

    SuccessCallback sc; //생성자로, 성공시 실행될 람다를 받는다

    ExceptionCallback ec;

    public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
      super(callable);
      //sc는 널이면 안된다.
      this.sc = Objects.requireNonNull(sc); //sc가 null이 아니면 그 값을 리턴한다.
      this.ec = Objects.requireNonNull(ec);
    }

    @Override
    protected void done() {//hook 을 재정의한다.
      try {
        sc.onSuccess(get()); //get()이 반환하는 리턴값을 넣어준다.

      } catch (InterruptedException e) { //작업을 진행하지 말고, 종료하라는 메시지를 준다.
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) { //비동기 작업시 발생하는 예외, 현재 es에 처리해야될 예외
        ec.onError(e.getCause()); //e는 예외가 포장되어 있기 때문에 까서 전달
      }
    }
  }

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    ExecutorService es = Executors.newCachedThreadPool();

    /*
    Thread.sleep(2000);
    System.out.println("Hello"); //2초 후 해당 결과가 실행되고 Exit 실행
    System.out.println("Exit");
     */
    /*
    es.execute(() ->{ //Runable은 리턴을 받을 수 없다.
      //독자적으로 작업을 실행후 종료한다.
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) { }
      log.info("Async");

    });

    log.info("Exit"); // 해당 코드가 먼저 실행된다.
     */
    /*
    Future<String> future = es.submit(() -> { //callable를 받기 때문에 return과 예외를 받을 수 있다.
      Thread.sleep(2000);
      log.info("Async");
      return "Hello";
    });    //Future를 통해 결과를 받을 수 있다.
    System.out.println(future.get()); //결과가 나올때까지 코드가 blocking 된다(Blocking)
    //만약 결과를 반환하지 않을경우(Non-Blocking)
    log.info("Exit");//비동기 작업이 끝나고, Exit이 호출되었다.
     */
    /*
    Future<String> future = es.submit(() -> {
      Thread.sleep(2000);
      log.info("Async");
      return "Hello";
    });

    System.out.println(future.isDone()); //비동기 작업이 완료 됬는가를 return 해준다
    //다른 작업과 비동기 적으로 실행할 때, 사용하기 좋다.
    log.info("Exit"); //해당 코드를 실행 후, blocking 상태로 된다.
    Thread.sleep(2100);
    System.out.println(future.isDone());
    System.out.println(future.get());
     */
    /*
    FutureTask<String> futureTask = new FutureTask<>(() -> { //Callback 을 처리할 수 있는 메커니즘이 다 존재한다.
      Thread.sleep(2000);
      log.info("Async");
      return "Hello";
    }){ //익명 클래스로 만든다
      @Override
      protected void done() { //비동기 작업이 완료되면 실행되는 hook
        try {
          System.out.println(get()); //get() 을 여기서 실행
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
    es.execute(futureTask);
    es.shutdown(); //executor 서비스가 끝나게 되면 반드시 종료하자
     */

    CallbackFutureTask callbackFutureTask = new CallbackFutureTask(() -> {
      Thread.sleep(2000);
      if(true)
        throw new RuntimeException("Async ERROR!!!");
      log.info("Async");
      return "Hello";
      //}, res -> System.out.println(res)); //Success 도 람다로 받을 수 있다.
    }, System.out::println,
        e -> System.out.println("Error: " + e.getMessage()));

    es.execute(callbackFutureTask);
    es.shutdown();
  }
}
