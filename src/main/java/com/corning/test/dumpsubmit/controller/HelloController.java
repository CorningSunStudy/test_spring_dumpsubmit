package com.corning.test.dumpsubmit.controller;

import com.corning.test.dumpsubmit.core.DuplicateSubmitToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/hello")
@RestController
public class HelloController {

    @GetMapping
    public String hello() {
        return "hello";
    }

}

