package com.example.tobyreactiveprogramming.ch07;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import reactor.core.publisher.Flux;

@Slf4j
public class SchedulerEx {

  public static void main(String[] args) {
    //모두 main 쓰레드에서 동작한다
    Publisher<Integer> pub = sub -> {
      sub.onSubscribe(new Subscription() {
        @Override
        public void request(long n) {
          log.debug("request()");
          sub.onNext(1);
          sub.onNext(2);
          sub.onNext(3);
          sub.onNext(4);
          sub.onNext(5);
          sub.onComplete();
        }

        @Override
        public void cancel() {

        }
      });
    };
    //pub

    /*
    Publisher<Integer> subOnPub = sub -> {
      ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
        //스레드의 이름을 바꿔주기 위해 스프링에서 제공하는 CustomizableThreadFactory를 사용한다
        @Override
        public String getThreadNamePrefix() {
          return "subOn-";
        }
      }); //동시에 하나 이상의 스레드를 주지 않는다.
      es.execute(() -> pub.subscribe(sub)); //메인 스레드가 아닌 다른 스레드에서 실행시켜준다.
    };
     */

    Publisher<Integer> pubOnPub = sub -> {
      pub.subscribe(new Subscriber<Integer>() {
        ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
          @Override
          public String getThreadNamePrefix() {
            return "pubOn-";
          }
        });// 요청이 한번에 들어오면 모두 실행되는 것이 아니다.
        //하나의 Publisher가 여러 쓰레드를 생성해서 실행시킬때에도 순서가 지켜진다.(동시성이 깨지지 않는다.)

        @Override
        public void onSubscribe(Subscription s) {
          sub.onSubscribe(s);
        }

        @Override
        public void onNext(Integer integer) {
          es.execute(() -> sub.onNext(integer));
        }

        @Override
        public void onError(Throwable t) {
          es.execute(() -> sub.onError(t));
          es.shutdown(); //더이상 구독이 유용하지 않으니 종료
        }

        @Override
        public void onComplete() {
          es.execute(() -> sub.onComplete());
          es.shutdown(); //더이상 구독이 유용하지 않으니 종료
        }
      });
    };

    //sub
    pubOnPub.subscribe(new Subscriber<Integer>() {
      @Override
      public void onSubscribe(Subscription s) {
        log.debug("onSubscribe");
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Integer integer) {
        log.debug("onNext:{}", integer);
      }

      @Override
      public void onError(Throwable t) {
        log.debug("onError:{}", t);
      }

      @Override
      public void onComplete() {
        log.debug("onComplete");
      }
    });
    System.out.println("exit");
  }

}