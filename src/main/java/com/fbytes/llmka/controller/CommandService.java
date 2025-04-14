package com.fbytes.llmka.controller;

import com.fbytes.llmka.service.Maintenance.IMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("command")
public class CommandService {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    IMaintenanceService maintenanceService;

    @GetMapping("/compress")
    public ResponseEntity<String> compressStore(@RequestParam(name = "schema", required = true) String schema) {
        try {
            maintenanceService.compressDB(schema);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Store compressed: " + schema);
    }


    @GetMapping("/beans")
    public ResponseEntity<List<String>> beans() {
        return ResponseEntity.ok().body(Arrays.asList(applicationContext.getBeanDefinitionNames()));
    }
}
