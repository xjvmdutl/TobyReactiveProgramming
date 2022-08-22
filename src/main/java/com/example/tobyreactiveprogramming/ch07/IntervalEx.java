package com.example.tobyreactiveprogramming.ch07;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public class IntervalEx {

  public static void main(String[] args) {
    Publisher<Integer> pub = sub -> {
      sub.onSubscribe(new Subscription() {
        int no = 0;
        boolean cancelled = false;

        @Override
        public void request(long n) {
          ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
          exec.scheduleAtFixedRate(() -> {
            if(cancelled){
              exec.shutdown(); //더이상 실행시키지 않는다.
              return;
            }
            sub.onNext(no++); //프로세스가 종료되기 전까지 계속 데이터를 전송
          }, 0, 300, TimeUnit.MILLISECONDS);
        }

        @Override
        public void cancel() {
          cancelled = true;
        }
      });
    };

    Publisher<Integer> takePub = sub -> {
      pub.subscribe(new Subscriber<Integer>() {
        int count = 0;
        Subscription subsc;
        @Override
        public void onSubscribe(Subscription s) {
          subsc = s;
          sub.onSubscribe(s);
        }

        @Override
        public void onNext(Integer integer) {
          sub.onNext(integer);
          if(++count > 10){ //10개 이상이면 실행을 안시킬 것이다
            subsc.cancel();//cancel를 동작시켜 준다.
            //데이터를 더이상 필요로 하지 않기 떄문에 cancel을 시키는 것이다
          }
        }

        @Override
        public void onError(Throwable t) {
          sub.onError(t);
        }

        @Override
        public void onComplete() {
          sub.onComplete();
        }
      });
    };

    takePub.subscribe(new Subscriber<>() {
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
  }
}
