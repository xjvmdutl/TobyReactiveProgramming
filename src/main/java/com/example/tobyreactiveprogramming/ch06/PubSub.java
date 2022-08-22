package com.example.tobyreactiveprogramming.ch06;


import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Reactive Streams - Operators Publisher -> [Data1] -> Operator -> [Data2] -> Subscriber Operator
 * 연산을 통하면서 데이터가 가공되서 Subscriber에 제공이 된다.
 */

/**
 * Publisher -> [DATA1] -> mapPub -> [DATA2] -> LogSub <- subscribe(logSub) -> onSubscribe(s) ->
 * onNext -> onNext -> onComplete mapPub을 걸쳐서 동작해야되기 떄문에 Publisher에서 제공하는 기능을 모두 제공해야한다.
 */
@Slf4j
public class PubSub {

  public static void main(String[] args) {
    Publisher<Integer> pub = iterPub(
        Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
    //pub을 mapPub에 연결시켜주었다.
    //Funtion 인터페이스 = 파라미터 타입(Integer) , 반환타입(Integer)라 Function<Integer, Integer> 라고 썻다
    //Publisher<Integer> mapPub = mapPub(pub, (Function<Integer, Integer>)s -> s * 10);
    //Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
    //Publisher<Integer> map2Pub = mapPub(mapPub, s -> -s);
    //Publisher<Integer> sumPub = sumPub(pub); //계산만 하고 있다가 합계가 모두 계산되면 전달해야한다.

    //초기 데이터를 시작으로, 함수의 연산을 통해 연산을 계속 진행해 최종 결과를 반환
    //Publisher<Integer> reducePub = reducePub(pub, 0, (BiFunction<Integer, Integer, Integer>)(a, b ) -> a + b); //BiFunction 을 통해 인자가 2개인 람다를 전달
    //ex) 1,2,3,4,5
    // 0 -> (0, 1) -> 0 + 1 = 1
    // 1 -> (1, 2) -> 1 + 2 = 3
    // 3 -> (3, 3) -> 3 + 3 = 6
    // 6 -> (6, 4) -> 6 + 4 = 10
    // 10 -> (10, 5) -> 10 + 5 = 15

    //Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]");
    //Publisher<String> reducePub = reducePub(pub, "", (a, b) -> a + "-" + b);
    Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(),
        (a, b) -> a.append(b + ","));
    reducePub.subscribe(logSub()); //구독 진행
  }


  private static Publisher<Integer> iterPub(final List<Integer> iter) {
    return new Publisher<Integer>() {
      /*
      Iterable<Integer> iter = Stream.iterate(1, a -> a + 1).limit(10)//Seed값에 대한 것을 a로 받아서 쓸 수 있다
          .collect(Collectors.toList());
       */

      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        sub.onSubscribe(new Subscription() {
          @Override
          public void request(long n) {
            try {
              iter.forEach(s -> sub.onNext(s)); //iterable이 가진 데이터를 모두 넘긴다.
              sub.onComplete();
            } catch (Throwable t) {
              sub.onError(t);
            }
          }

          @Override
          public void cancel() {
            //Subscriber 가 어떠한 이유로 더이상 처리하고 싶지 않을때, cancel 을 호출한다.
            //결론적으로 Publisher 에게 더이상 데이터를 받고 싶지 않을 때 호출
          }
        });
      }
    };
  }

  /*
  //리펙토링 전
  private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
    return new Publisher<Integer>() {
      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        //전에 Publisher 만들던 것과 같다.
        //logSubscriber 가 해당 메서드를 호출할 것이다
        pub.subscribe(new Subscriber<Integer>() {//logSub가 mapPub을 호출하기 때문에 mapPub에서는 pub를 호출한다고 생각하면 된다
          @Override
          public void onSubscribe(Subscription s) {
            sub.onSubscribe(s); //아무일 없이 중계만 한다
          }

          @Override
          public void onNext(Integer i) {
            sub.onNext(f.apply(i)); //Fuction을 적용한 값을 Sub에 넘기고 싶다.

          }

          @Override
          public void onError(Throwable t) {
            sub.onError(t);//아무일 없이 중계만 한다
          }

          @Override
          public void onComplete() {
            sub.onComplete();//아무일 없이 중계만 한다
          }
        });
      }
    };
  }
   */

  //리펙토링 후
  /*
  private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
    return new Publisher<Integer>() {
      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        pub.subscribe(new DelegateSub(sub){
          @Override
          public void onNext(Integer i) { //오버라이드 해서 적용한다.
            sub.onNext(f.apply(i));
          }
        }); //delegateSub 에 subscriber를 전달하자
      }
    };
  }
   */
  //제네릭 적용
  /*
  private static <T> Publisher<T> mapPub(Publisher<T> pub, Function<T, T> f) {
    return new Publisher<T>() {
      @Override
      public void subscribe(Subscriber<? super T> sub) {
        pub.subscribe(new DelegateSub<T>(sub){
          @Override
          public void onNext(T i) {
            sub.onNext(f.apply(i));
          }
        });
      }
    };
  }
   */
  //적용 -> 반환 타입이 다르게 적용
  private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
    return new Publisher<R>() {
      @Override
      public void subscribe(Subscriber<? super R> sub) {
        pub.subscribe(new DelegateSub<T, R>(sub) {
          @Override
          public void onNext(T i) {
            sub.onNext(f.apply(i));
          }
        });
      }
    };
  }

  /*
  private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
    return new Publisher<Integer>() {
      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        pub.subscribe(new DelegateSub(sub){
          int sum = 0;

          @Override
          public void onNext(Integer i) {
            sum += i; //Sum을 결과를 어디서 전달해야할까 -> onComplete 에서 전달하면 된다.
          }

          @Override
          public void onComplete() {
            sub.onNext(sum); //complete 이라고, 다른 Subscriber 에 complete 를 넘길 필요 없다.
            sub.onComplete();//onNext 를 한번 호출 후(결과) onComplete 를 호출해준다
          }
        }); //중계할 Sub를 넘긴다.
      }
    };
  }

  private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init,
      BiFunction<Integer, Integer, Integer> bf) {
    return new Publisher<Integer>() {
      @Override
      public void subscribe(Subscriber<? super Integer> sub) {
        pub.subscribe(new DelegateSub(sub){
          int result = init;
          @Override
          public void onNext(Integer i) {
            result = bf.apply(result, i);
          }

          @Override
          public void onComplete() {
            sub.onNext(result);
            sub.onComplete();
          }
        });
      }
    };
  }
   */
  //reducePub에 제네릭 적용
  private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init,
      BiFunction<R, T, R> bf) {
    return new Publisher<R>() {
      @Override
      public void subscribe(Subscriber<? super R> sub) {
        pub.subscribe(new DelegateSub<T, R>(sub) {
          R result = init;

          @Override
          public void onNext(T i) {
            result = bf.apply(result, i);
          }

          @Override
          public void onComplete() {
            sub.onNext(result);
            sub.onComplete();
          }
        });
      }
    };
  }

  /*
  //제네릭 적용 전
  private static Subscriber<Integer> logSub() {
    return new Subscriber<Integer>() {
      @Override

      public void onSubscribe(Subscription s) {
        log.debug("onSubscribe:");
        s.request(Long.MAX_VALUE); //너가 가지고 있는 데이터 모두 줘(무제한으로 생각하면 된다)
      }

      @Override
      public void onNext(Integer i) {
        log.debug("onNext: {}", i);
      }

      @Override
      public void onError(Throwable t) {
        log.debug("onError:{}", t);
      }

      @Override
      public void onComplete() {
        log.debug("onComplete");
      }
    };
  }
   */
  private static <T> Subscriber<T> logSub() {
    return new Subscriber<T>() {
      @Override
      public void onSubscribe(Subscription s) {
        log.debug("onSubscribe:");
        s.request(Long.MAX_VALUE); //너가 가지고 있는 데이터 모두 줘(무제한으로 생각하면 된다)
      }

      @Override
      public void onNext(T i) {
        log.debug("onNext: {}", i);
      }

      @Override
      public void onError(Throwable t) {
        log.debug("onError:{}", t);
      }

      @Override
      public void onComplete() {
        log.debug("onComplete");
      }
    };
  }
}
