package com.finance.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        // entrypoint — kept tiny on purpose, all config lives in dedicated classes
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
