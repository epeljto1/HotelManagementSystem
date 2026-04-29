package com.example.hotel_management_system;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class HotelManagementSystemApplicationTests {

	@Test
	void mainDelegatesToSpringApplicationRun() {
		new HotelManagementSystemApplication();
		String[] args = {"--spring.main.web-application-type=none"};

		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			HotelManagementSystemApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(HotelManagementSystemApplication.class, args));
		}
	}

}
