package com.example.tobyreactiveprogramming.ch05;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.objenesis.ObjenesisSerializer;

@SuppressWarnings("deprecation")
public class Ob {

  // FRP(Functional Reactive Programming)
  // Reactive : 외부에 이벤트가 발생하면, 거기에 대응하는 방식으로 코드를 작성하는 것
  // Duality : 상대성
  // Observer Pattern : Listener - Event
  // Reactive Streams - 표준(Reactive 에 관한 표준 스펙)

  public static void main(String[] args) {
    /*
    //List<Integer> list = Arrays.asList(1, 2, 3, 4, 5); //List로 받을 수 있다.
    Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5); //문제 없다. List는 Iterable 인터페이스를 상속받기 때문
    //for-each //Iterable 이기떄문에 for-each를 사용할 수 있는것이다.
    //꼭 Collection 이 아니야도 Iterable 인터페이스를 구현한다면 for-each 구문을 사용할 수 있다.
     */
    /*
    Iterable<Integer> iter = new Iterable<>() { //인터페이스의 메서드가 하나이므로 람다로 만들자
      @Override
      public Iterator<Integer> iterator() {
        return null;
      }
    };
     */
    /*
    Iterable<Integer> iter = () ->
        new Iterator<>() {
          int i = 0;
          final static int MAX = 10;

          @Override
          public boolean hasNext() {
            return i < MAX;
          }

          @Override
          public Integer next() {
            return ++i;
          }
        };

    for (Integer i : iter) {
      System.out.println("i = " + i);
    }

    //JAVA5 이전 방식
    for(Iterator<Integer> it = iter.iterator(); it.hasNext();){ //증가값 쓸 필요 없다
      System.out.println(it.next());
    }
     */
    //Observable //Source -> Event/Data 를 던진다
    //Observer에게 던진다(target)
    //이렇게 하기 위해서는 Observable에 Observer를 등록해야한다
    /*
    Observer ob = new Observer() { //받는쪽
      @Override
      public void update(Observable o, Object arg) {
        System.out.println(arg);
      }
    };

    IntObservable io = new IntObservable();
    io.addObserver(ob); //등록

    io.run();
     */
    Observer ob = new Observer() {
      @Override
      public void update(Observable o, Object arg) {
        System.out.println(Thread.currentThread().getName() + " " + arg);
      }
    };
    IntObservable io = new IntObservable();
    io.addObserver(ob);

    ExecutorService es = Executors.newSingleThreadExecutor();
    es.execute(io);

    System.out.println(Thread.currentThread().getName() + "    EXIT");
    es.shutdown();
  }

  static class IntObservable extends Observable implements Runnable { //비동기 적으로 돌리기 위해 Runnable 추가

    @Override
    public void run() {
      for (int i = 1; i <= 10; ++i) { //source 쪽
        setChanged(); //변화가 생겼다는 것을 해당 메서드로 알려줌
        //int i = it.next()                     //PULL
        notifyObservers(i); //변경되는 것을 알려준다 //PUSH
        //i가 메서드 안에 있으므로 Push 라고 하는것이다.
      }
    }
  }
}
