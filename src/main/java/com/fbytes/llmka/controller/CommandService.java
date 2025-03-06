package com.fbytes.llmka.controller;

import com.fbytes.llmka.service.NewsDataCheck.INewsDataCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("command")
public class CommandService {

    @Autowired
    INewsDataCheck newsDataCheck;

    @GetMapping("/compress")
    public ResponseEntity compressStore() {
        try {
            newsDataCheck.cleanupStore();
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
