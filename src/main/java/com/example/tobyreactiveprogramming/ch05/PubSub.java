package com.example.tobyreactiveprogramming.ch05;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Observer 패턴을 문제를 해결한 클래스
 */
public class PubSub {

  public static void main(String[] args) throws InterruptedException {
    //Publisher <- Observable
    //Subscriber <- Observer
    Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
    ExecutorService es = Executors.newSingleThreadExecutor();

    Publisher p = new Publisher() { //Subscribe가 구독하는 방식
      @Override
      public void subscribe(Subscriber subscriber) {

        Iterator<Integer> it = itr.iterator();

        subscriber.onSubscribe(new Subscription() {
          @Override
          public void request(long n) { //파라미터 n = 최대 받을수 있는 갯수
            es.execute(() -> {
              int i = 0;
              try {
                //while (true) {
                while (i++ < n) { //n으로 제약을 준다.
                  if (it.hasNext()) {
                    subscriber.onNext(it.next()); //요청값을 보내주면 된다.
                  } else {
                    subscriber.onComplete();
                    break;
                  }
                }
              } catch (RuntimeException e) {
                subscriber.onError(e); //onError 라는 메서드를 호출해서 우아하게 처리가능
              }
            });
          }

          @Override
          public void cancel() {

          }
        });
      }
    };

    Subscriber<Integer> s = new Subscriber<Integer>() {

      Subscription subscription;

      @Override
      public void onSubscribe(Subscription subscription) { //반드시 호출이 되어야한다.
        System.out.println(Thread.currentThread().getName() + " onSubscribe");
        //최초 request는 onSubscribe 에서 해야한다.
        this.subscription = subscription;
        subscription.request(1);
      }

      @Override
      public void onNext(Integer item) { //0~무제한
        //Publisher가 데이터를 주면 해당 메서드에서 받는다.
        System.out.println(Thread.currentThread().getName() + " onNext = " + item);
        this.subscription.request(1);
      }

      @Override
      public void onError(Throwable throwable) { //onComplete,onError 둘 중하나만 사용, 아예 안써도 됨
        //Exception 발생시키지 말고, 해당 메서드를 통해서 오류를 전해준다.
        System.out.println("onError = " + throwable.getMessage());
      }

      @Override
      public void onComplete() {
        //Publisher가 더 이상 줄 데이터가 없을 때
        System.out.println("onComplete");
      }
    };

    p.subscribe(s);
    //es.shutdown(); //쓰레드가 종료가 되어서 안된다.
    es.awaitTermination(10, TimeUnit.HOURS);
    es.shutdown();
  }

}
