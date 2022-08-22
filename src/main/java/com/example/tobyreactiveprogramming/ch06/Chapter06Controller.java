package com.example.tobyreactiveprogramming.ch06;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Chapter06Controller {

  @RequestMapping("/hello")
  public Publisher<String> hello(String name){ //Publisher 로 리턴이 가능
    return new Publisher<String>() {
      @Override
      public void subscribe(Subscriber<? super String> s) {
        s.onSubscribe(new Subscription() {
          @Override
          public void request(long n) {
            s.onNext("Hello " + name);
            s.onComplete();
          }

          @Override
          public void cancel() {

          }
        });
      }
    }; //Subscribe 를 만들고, 데이터를 요청하는 것들은 스프링이 할일이다.
  }
}
