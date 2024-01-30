package com.assessment.demo.configuration;

import com.assessment.demo.service.RoleService;
import com.assessment.demo.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataConfig {
    /** Service used in this configuration class:
     */
    private final RoleService roleService;
    private final UserService userService;

    /**
     * values use to create default roles in database
     */
    @Value("${spring.role_admin.id}")
    private int role1_id;

    @Value("${spring.role_admin.name}")
    private String role1_name;

    @Value("${spring.role_user.id}")
    private int role2_id;

    @Value("${spring.role_user.name}")
    private String role2_name;

    /**
     *  This method will be executed after the bean has been constructed and
     *       the dependencies have been injected using {@link PostConstruct}. This ensures that the roles
     *       are created when the application starts.
     */
    @PostConstruct
    public void createIfNotExists() {
        // Create 2 roles default if not existed
        roleService.createRoleIfNotExists(role1_id, role1_name);
        roleService.createRoleIfNotExists(role2_id, role2_name);

        // After that, create an admin account if not existed
        userService.createAdminAccountIfNotExists(role1_id);
    }
}
