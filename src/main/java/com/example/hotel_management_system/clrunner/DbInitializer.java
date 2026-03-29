package com.example.hotel_management_system.clrunner;

import com.example.hotel_management_system.config.DataSeeder;
import com.example.hotel_management_system.config.DatabaseInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        try{
            DatabaseInitializer.initialize();
            DataSeeder.seedData();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
