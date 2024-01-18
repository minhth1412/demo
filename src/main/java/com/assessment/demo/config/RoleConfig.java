package com.assessment.demo.config;

import com.assessment.demo.entity.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.assessment.demo.service.RoleService;

@Configuration
public class RoleConfig {

    @Autowired
    private RoleService roleService;

    @Value("${spring.role_admin.id}")
    private int role1_id;

    @Value("${spring.role_admin.name}")
    private String role1_name;

    @Value("${spring.role_user.id}")
    private int role2_id;

    @Value("${spring.role_user.name}")
    private String role2_name;

    // This will be executed after the bean has been constructed and
    //  the dependencies have been injected. This ensures that the roles
    //  are created when the application starts.
    @PostConstruct
    public void createRolesIfNotExists() {
        roleService.createRoleIfNotExists(role1_id, role1_name);
        roleService.createRoleIfNotExists(role2_id, role2_name);
    }
}
