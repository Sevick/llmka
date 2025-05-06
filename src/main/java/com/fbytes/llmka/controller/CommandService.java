package com.fbytes.llmka.controller;

import com.fbytes.llmka.service.Maintenance.IMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
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


    @PostMapping("/compress")
    public ResponseEntity<String> compressStore(@RequestParam(name = "schema", required = true) String schema) {
        try {
            maintenanceService.compressDB(schema);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body(MessageFormat.format("[{0}] store compressed", schema));
    }

    @PostMapping("/compressmeta")
    public ResponseEntity<String> compressMeta(@RequestParam(name = "schema", required = true) String schema,
                                               @RequestParam(name = "size", required = false) Integer size) {
        try {
            maintenanceService.compressMeta(schema, size);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body(MessageFormat.format("[{0}] meta compressed", schema));
    }


    @GetMapping("/beans")
    public ResponseEntity<List<String>> beans() {
        return ResponseEntity.ok().body(Arrays.asList(applicationContext.getBeanDefinitionNames()));
    }
}
