package org.umc.travlocksserver;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableJpaAuditing
@SpringBootApplication
@ConfigurationPropertiesScan
public class TravlocksServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravlocksServerApplication.class, args);
    }

}
