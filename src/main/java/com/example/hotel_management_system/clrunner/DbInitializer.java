package com.example.hotel_management_system.clrunner;

import com.example.hotel_management_system.config.DataSeeder;
import com.example.hotel_management_system.config.DatabaseInitializer;
import com.example.hotel_management_system.config.DatabaseSeederReservation;
import com.example.hotel_management_system.config.DatabaseSeederRoom;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        try{
            DatabaseInitializer.initialize();
            DataSeeder.seedData();
            DatabaseSeederRoom.seedRooms();
            DatabaseSeederReservation.seedReservations();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
