package com.example.tobyreactiveprogramming.ch09;


import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SuppressWarnings("deprecation")
@RestController //@ResponseBody + @Controller
public class MyController2 {

  public static final String URL1 = "http://localhost:8081/service1?req={req}";
  public static final String URL2 = "http://localhost:8081/service2?req={req}";

  /*
  @GetMapping("/rest")
  public String rest(int idx){
    return "rest " + idx;
  }
 */

  /*
  RestTemplate rt = new RestTemplate();
  @GetMapping("/rest")
  public String rest(int idx) {
    String res = rt.getForObject("http://localhost:8081/service?req={req}", String.class,
        "hello" + idx);
    return res;
  }
   */
  /*
  AsyncRestTemplate rt = new AsyncRestTemplate();
  @GetMapping("/rest")
  public ListenableFuture<ResponseEntity<String>> rest(int idx) {
    ListenableFuture<ResponseEntity<String>> res = rt.getForEntity(
        "http://localhost:8081/service?req={req}", String.class,
        "hello" + idx);
    //스프링이 ListenableFuture<ResponseEntity<String>>을 반환하게 되면 callback을 알아서 등록하고, 비동기로 실행시켜준다
    return res;
  }
   */
  AsyncRestTemplate rt = new AsyncRestTemplate(
      new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

  /*
  @GetMapping("/rest")
  public ListenableFuture<ResponseEntity<String>> rest(int idx) {
    ListenableFuture<ResponseEntity<String>> res = rt.getForEntity(
        "http://localhost:8081/service?req={req}", String.class,
        "hello" + idx);
    return res;
  }
   */
  /*
  @GetMapping("/rest")
  public DeferredResult<String> rest(int idx) {
    DeferredResult<String> dr = new DeferredResult<>();
    ListenableFuture<ResponseEntity<String>> res = rt.getForEntity(
        "http://localhost:8081/service?req={req}", String.class,
        "hello" + idx);
    res.addCallback(s -> {
      dr.setResult(
          s.getBody()//원하는 결과값
          + "/work"
      );
    }, e->{
      //비동기 작업을 콜백으로 처리할 때는 예외를 전파하면 안된다.
      //순수하게 deferredResult를 통해 처리를 해야한다.
      dr.setErrorResult(e.getMessage()); //이걸 받는 클라이언트에서 적절히 처리하면 된다.
    });
    return dr;
  }
   */

  @Autowired
  MyService myService;

  @GetMapping("/rest")
  public DeferredResult<String> rest(int idx) {
    DeferredResult<String> dr = new DeferredResult<>();
    ListenableFuture<ResponseEntity<String>> res1 = rt.getForEntity(URL1, String.class,
        "hello" + idx);

    res1.addCallback(s -> {
      ListenableFuture<ResponseEntity<String>> res2 = rt.getForEntity(URL2, String.class,
          s.getBody());
      res2.addCallback(s2 -> {
        ListenableFuture<String> res3 = myService.work(s2.getBody());
        res3.addCallback(s3 -> {
          dr.setResult(s3);
        }, e -> {
          dr.setErrorResult(e.getMessage());
        });
      }, e -> {
        dr.setErrorResult(e.getMessage());
      });
    }, e -> {
      dr.setErrorResult(e.getMessage());
    });
    return dr;
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
}