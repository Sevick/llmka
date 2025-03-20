package com.fbytes.llmka.controller;

import com.fbytes.llmka.service.NewsDataCheck.INewsDataCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@Profile("dev")
@RequestMapping("command")
public class CommandService {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    INewsDataCheck newsDataCheck;

    @GetMapping("/compress")
    public ResponseEntity compressStore(@RequestParam(name = "schema", required = true) String schema) {
        try {
            newsDataCheck.cleanupStore(schema);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }


    @GetMapping("/beans")
    public ResponseEntity beans() {
        return ResponseEntity.ok().body(Arrays.asList(applicationContext.getBeanDefinitionNames()));
    }
}
