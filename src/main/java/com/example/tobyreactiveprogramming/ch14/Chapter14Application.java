package com.example.tobyreactiveprogramming.ch14;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;


@Slf4j
@SpringBootApplication
public class Chapter14Application {

  @RestController
  public static class MyController {

    /*
    @GetMapping("/event/{id}")
    public Mono<Event> event(@PathVariable long id) {
      return Mono.just(new Event(id, "event " + id));//특정 오브젝트를 컬렉션으로 다뤄 리액티브 스타일로 다루고 싶다면 어떻게 할까?
    }
     */

    @GetMapping("/event/{id}")
    public Mono<List<Event>> event(@PathVariable long id) {
      List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
      return Mono.just(list); //Flux와 차이는 무엇일까?
    }

    /*
    @GetMapping("/events")
    public Flux<Event> events() {
      //return Flux.just(new Event(1L, "event1"), new Event(2L, "event2"));
      List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
      return Flux.fromIterable(list);
    }
     */
    /*
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE) //client 요청에 Accept 헤더를 메핑이 1차 목적, 어떤 타입을 리턴할지 결정하는것이 2차 목적
    public Flux<Event> events() {
      List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
      return Flux.fromIterable(list);
    }
     */
    /*
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events() {
      //generation 한 코드를 작성해 보자
      Stream<Event> stream = Stream.generate(() -> new Event(System.currentTimeMillis(), "Value"));//.limit();
      return Flux.fromStream(stream) //Stream 된 데이터를 Flux에 넣을 수 있다.
          .delayElements(Duration.ofSeconds(1)) //next로 온 데이터에 delay를 걸 수 있다.
          .take(10); //데이터를 끊어서 가지고 올 수 있다( 10개의 데이터를 카운팅 하다 cancel 기능을 수행한다)
    }
     */
    /*
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events() {
      return Flux
          .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value"))) // sink = 데이터를 흘려 보내는 역할을 sink에서 한다.
          //sink를 넘겨주면 다음 데이터를 실행하는 코드를 작성하면 된다.
          .delayElements(Duration.ofSeconds(1))
          .take(10);
    }
     */
    /*
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events() {
      return Flux
          //.range(1, 10) //1~10까지 변하는 스트림 생성
          .<Event, Long>generate(() -> 1L, (id, sink) -> {
            sink.next(new Event(id, "value " + id));
            return id + 1;
          })//take로 뒤에서 끊고 싶을 때 사용
          .delayElements(Duration.ofSeconds(1))
          .take(10);
    }
     */
    /*
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events() {
      Flux<Event> flux = Flux.<Event, Long>generate(() -> 1L, (id, sink) -> {
            sink.next(new Event(id, "value " + id));
            return id + 1;
          });
      Flux<Long> interval = Flux.interval(Duration.ofSeconds(1)); //데이터를 일정한 주기를 가지고, 0부터 값을 전달하는 함수

      //두가지 Flux를 병합할 수 있다.
      return Flux.zip(flux, interval)//2가지 이상을 작업을 묶어서 동작시킬때 사용한다.
          // 첫번째 flux 이벤트에 interval 이벤트 하나씩 묶어서 전달한다.
          .map(tu -> tu.getT1());
    }
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events() {
      Flux<String> flux = Flux.generate((sink) -> sink.next("value"));
      Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
      return Flux.zip(flux, interval)
          .map(tu -> new Event(tu.getT2(), tu.getT1())); //이벤트 조합을 해당 부분에서 사용할 수도 있다.
    }
  }

  @Data
  @AllArgsConstructor
  public static class Event {

    long id;
    String value;
  }

  public static void main(String[] args) {
    SpringApplication.run(Chapter14Application.class, args);
  }
}
