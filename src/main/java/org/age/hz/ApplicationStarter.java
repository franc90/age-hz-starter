package org.age.hz;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationStarter implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(ApplicationStarter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Welcome to zombo.com");
    }
}
