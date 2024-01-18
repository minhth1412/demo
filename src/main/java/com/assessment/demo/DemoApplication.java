package com.assessment.demo;

import com.assessment.demo.service.RoleService;
import com.assessment.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {
	@Value("${spring.role_admin.id}")
	int adminRoleId;

	@Autowired
	private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		userService.createAdminAccountIfNotExists(adminRoleId);
		log.info("Initialization completed successfully.");
	}
}
