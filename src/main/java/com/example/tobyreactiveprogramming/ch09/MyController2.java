package com.example.tobyreactiveprogramming.ch09;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //@ResponseBody + @Controller
public class MyController2 {
    @GetMapping("/rest")
    public String rest(int idx){
      return "rest " + idx;
    }
}