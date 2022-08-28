package com.example.tobyreactiveprogramming.ch10;

import com.example.tobyreactiveprogramming.ch09.RemoteService;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootApplication
public class Chapter10Application {

  @RestController
  public static class MyController {

    public static final String URL1 = "http://localhost:8081/service1?req={req}";
    public static final String URL2 = "http://localhost:8081/service2?req={req}";

    AsyncRestTemplate rt = new AsyncRestTemplate(
        new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    @Autowired
    MyService myService;

    @GetMapping("/rest")
    public DeferredResult<String> rest(int idx) {
      DeferredResult<String> dr = new DeferredResult<>();
      Completion
          .from(rt.getForEntity(URL1, String.class, "h" + idx))//비동기 결과를 가지고 있는 오브젝트
          .andApply(s -> rt.getForEntity(URL2, String.class,
              s.getBody())) // 해당 함수는 람다를 실행한 결과를 반환해 주어야한다.
          .andApply(s -> myService.work(
              s.getBody())) //반환타입이 다르기 때문에 오류가 발생한다(구조는 같다) -> 오버로딩을 작성해도 되지만 별로 않좋다(제네릭으로)
          .andError(e -> dr.setErrorResult(
              e.toString())) //.andError 가 호출이 되면, 더이상 뒤에 코드가 동작하지 않고 종료하고 싶다.
          .andAccept(s -> dr.setResult(s)) //앞에서 전달받은 결과를 람다식안으로 넘겨주는 역할을 하고 싶다 //리턴값을 이용하지 않기때문에 실행만 하면된다
      ;
      return dr;
    }
  }

  /*
  public static class Completion{

    protected Completion next;

    //비동기 작업을 결과를 담는 클래스
    public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
      //생성하는 static factory 메서드
      Completion completion = new Completion();
      lf.addCallback(s -> {
        //성공
        completion.complete(s);
      }, e -> {
        completion.error(e);
      });
      return completion;
    }

    //성공을 처리하는 메서드
    protected void complete(ResponseEntity<String> s) {
      if (next != null) {
        next.run(s);
      }
    }

    protected void run(ResponseEntity<String> value) {
      //if(con != null){
      //  con.accept(value);
      //} else if (fn != null) { //fn이 존재 할 경우 다음 단계로 넘어가야한다.
      //  ListenableFuture<ResponseEntity<String>> listenableFuture = fn.apply(value); //앞에 작업 종료
      //  listenableFuture.addCallback(
      //      s -> complete(s), e -> error(e)
      //  ); //콜백을 가지고 연결을 해야한다.
      //}
    }

    //에러를 처리하는 메서드
    protected void error(Throwable e) {
      if (next != null) {
        next.error(e);
      }
    }

    public void andAccept(Consumer<ResponseEntity<String>> con) {
      Completion completion = new AcceptCompletion(con);
      this.next = completion;
    }

    public Completion andApply(
        Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
      Completion completion = new ApplyCompletion(fn);
      this.next = completion;
      return completion;
    }

    public Completion andError(Consumer<Throwable> econ) {
      Completion completion = new ErrorCompletion(econ);
      this.next = completion;
      return completion;
    }
  }

  public static class AcceptCompletion extends Completion {

    private Consumer<ResponseEntity<String>> con;

    public AcceptCompletion(Consumer<ResponseEntity<String>> con) {
      this.con = con;
    }

    @Override
    protected void run(ResponseEntity<String> value) {
      con.accept(value);
    }
  }

  public static class ApplyCompletion extends Completion {

    private Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn;

    public ApplyCompletion(
        Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
      this.fn = fn;
    }

    @Override
    protected void run(ResponseEntity<String> value) {
      ListenableFuture<ResponseEntity<String>> listenableFuture = fn.apply(value); //앞에 작업 종료
      listenableFuture.addCallback(
          s -> complete(s), e -> error(e)
      );
    }
  }

  public static class ErrorCompletion extends Completion {

    private Consumer<Throwable> econ;

    public ErrorCompletion(Consumer<Throwable> econ) {
      this.econ = econ;
    }

    @Override
    protected void run(ResponseEntity<String> value) {
      if (next != null) {
        next.run(value);
      }
    }

    @Override
    protected void error(Throwable e) {
      econ.accept(e); //다음으로 패싱한다.
    }
  }
   */
  public static class Completion<S, T> {//andApply를 적용하면 타입이 2가지를 받아야한다

    protected Completion next;

    //비동기 작업을 결과를 담는 클래스
    public static <S, T> Completion<S, T> from(ListenableFuture<T> lf) {
      Completion<S, T> completion = new Completion<>();
      lf.addCallback(s -> {
        //성공
        completion.complete(s);
      }, e -> {
        completion.error(e);
      });
      return completion;
    }

    //성공을 처리하는 메서드
    protected void complete(T s) {
      if (next != null) {
        next.run(s);
      }
    }

    protected void run(S value) {

    }

    //에러를 처리하는 메서드
    protected void error(Throwable e) {
      if (next != null) {
        next.error(e);
      }
    }

    public void andAccept(Consumer<T> con) {
      Completion<T, Void> completion = new AcceptCompletion<>(con);
      this.next = completion;
    }

    public <V> Completion<T, V> andApply(
        Function<T, ListenableFuture<V>> fn) { //현재 생성이 되는 결과 값이라 T타입
      Completion<T, V> completion = new ApplyCompletion<>(fn);
      this.next = completion;
      return completion;
    }

    public Completion<T, T> andError(Consumer<Throwable> econ) {
      Completion<T, T> completion = new ErrorCompletion<>(econ);
      this.next = completion;
      return completion;
    }
  }

  public static class AcceptCompletion<S> extends Completion<S, Void> {

    private Consumer<S> con;

    public AcceptCompletion(Consumer<S> con) {
      this.con = con;
    }

    @Override
    protected void run(S value) {
      con.accept(value);
    }
  }

  public static class ApplyCompletion<S, T> extends Completion<S, T> {

    private Function<S, ListenableFuture<T>> fn;

    public ApplyCompletion(
        Function<S, ListenableFuture<T>> fn) {
      this.fn = fn;
    }

    @Override
    protected void run(S value) {
      ListenableFuture<T> listenableFuture = fn.apply(value); //앞에 작업 종료
      listenableFuture.addCallback(
          s -> complete(s), e -> error(e)
      );
    }
  }

  public static class ErrorCompletion<T> extends Completion<T, T> {

    private Consumer<Throwable> econ;

    public ErrorCompletion(Consumer<Throwable> econ) {
      this.econ = econ;
    }

    @Override
    protected void run(T value) {
      if (next != null) {
        next.run(value);
      }
    }

    @Override
    protected void error(Throwable e) {
      econ.accept(e); //다음으로 패싱한다.
    }
  }

  @Service
  public static class MyService {

    @Async
    public ListenableFuture<String> work(String req) {
      return new AsyncResult<>(req + "/asyncwork");
    }
  }

  @Bean
  public ThreadPoolExecutor myThreadPool() {
    ThreadPoolExecutor te = new ThreadPoolExecutor(10, 100, 100,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2));
    return te;
  }

  public static void main(String[] args) {
    SpringApplication.run(Chapter10Application.class, args);
  }

}