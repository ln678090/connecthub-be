package org.ln678090.connecthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConnectHubApplication {

     static void main(String[] args) {
        SpringApplication.run(ConnectHubApplication.class, args);
    }

}
