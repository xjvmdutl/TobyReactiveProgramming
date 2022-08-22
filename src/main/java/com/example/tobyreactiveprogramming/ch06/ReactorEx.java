package com.example.tobyreactiveprogramming.ch06;

import reactor.core.publisher.Flux;

public class ReactorEx {

  public static void main(String[] args) {
    Flux.<Integer>create(e -> { //타입을 지정하지 않으면 Object로 받는다.
          e.next(1);
          e.next(2);
          e.next(3);
          e.complete();
        })//Flux = Publisher
        .log() //log를 통해 데이터가 어떤식으로 전달되는지 볼 수 있다.
        .map(s -> s*10)
        .reduce(0, (a,b) -> a + b)
        .log()
        .subscribe(System.out::println); //publisher 이니 Subscribe 가능
  }
}
