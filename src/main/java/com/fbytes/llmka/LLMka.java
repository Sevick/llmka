package com.fbytes.llmka;

import com.fbytes.llmka.integration.DataSourceReaderIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class LLMka implements CommandLineRunner {

    @Autowired
    DataSourceReaderIntegration dataSourceReaderIntegration;

    @Override
    public void run(String... args) throws Exception {
        dataSourceReaderIntegration.readDataSourceConfig(System.in);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(LLMka.class)
                .web(WebApplicationType.NONE)
                .run(args);
        //SpringApplication.run(LLMka.class, args);
    }


}
